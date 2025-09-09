package com.garment.payroll.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import com.garment.payroll.entity.PieceworkRecord;
import com.garment.payroll.entity.ProcessPrice;
import com.garment.payroll.mapper.PieceworkRecordMapper;
import com.garment.payroll.service.PieceworkRecordService;
import com.garment.payroll.service.ProcessPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计件记录服务实现
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PieceworkRecordServiceImpl extends ServiceImpl<PieceworkRecordMapper, PieceworkRecord> 
        implements PieceworkRecordService {

    private final ProcessPriceService processPriceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PieceworkRecord generateFromProductionFlow(Long productionFlowId) {
        // TODO: 从生产管理模块获取生产流转记录
        // 这里模拟生产流转记录数据
        Map<String, Object> flowData = getProductionFlowData(productionFlowId);
        
        if (flowData == null) {
            throw new BusinessException("生产流转记录不存在");
        }
        
        // 创建计件记录
        PieceworkRecord record = new PieceworkRecord();
        record.setTenantId(TenantContext.getCurrentTenantId());
        record.setWorkerId((Long) flowData.get("workerId"));
        record.setWorkerName((String) flowData.get("workerName"));
        record.setWorkshopId((Long) flowData.get("workshopId"));
        record.setWorkshopName((String) flowData.get("workshopName"));
        record.setBundleId((Long) flowData.get("bundleId"));
        record.setBundleNo((String) flowData.get("bundleNo"));
        record.setProcessId((Long) flowData.get("processId"));
        record.setProcessName((String) flowData.get("processName"));
        record.setStyleId((Long) flowData.get("styleId"));
        record.setStyleCode((String) flowData.get("styleCode"));
        record.setStyleName((String) flowData.get("styleName"));
        record.setColor((String) flowData.get("color"));
        record.setSize((String) flowData.get("size"));
        record.setQuantity((Integer) flowData.get("quantity"));
        record.setQualifiedQuantity((Integer) flowData.get("qualifiedQuantity"));
        record.setDefectQuantity((Integer) flowData.get("defectQuantity"));
        
        // 获取工序单价
        ProcessPrice processPrice = processPriceService.getActivePrice(
            record.getStyleId(), record.getProcessId());
        
        if (processPrice != null) {
            record.setUnitPrice(processPrice.getFinalPrice());
        } else {
            // 使用默认单价
            record.setUnitPrice(BigDecimal.valueOf(1.00));
        }
        
        // 计算质量系数
        BigDecimal qualityFactor = calculateQualityFactor(
            record.getQualifiedQuantity(), record.getDefectQuantity());
        record.setQualityFactor(qualityFactor);
        
        // 计算难度系数和紧急系数
        record.setDifficultyFactor(BigDecimal.ONE); // 默认值
        record.setUrgencyFactor(BigDecimal.ONE); // 默认值
        
        // 计算金额
        BigDecimal amount = record.getUnitPrice()
            .multiply(BigDecimal.valueOf(record.getQualifiedQuantity()));
        record.setAmount(amount);
        
        BigDecimal actualAmount = amount
            .multiply(record.getQualityFactor())
            .multiply(record.getDifficultyFactor())
            .multiply(record.getUrgencyFactor())
            .setScale(2, RoundingMode.HALF_UP);
        record.setActualAmount(actualAmount);
        
        // 设置时间信息
        LocalDateTime now = LocalDateTime.now();
        record.setWorkDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        record.setStartTime((LocalDateTime) flowData.get("startTime"));
        record.setEndTime((LocalDateTime) flowData.get("endTime"));
        
        // 计算用时（分钟）
        if (record.getStartTime() != null && record.getEndTime() != null) {
            long duration = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMinutes();
            record.setDuration((int) duration);
        }
        
        record.setStatus("pending");
        record.setCreatedBy(1L); // TODO: 从当前用户上下文获取
        
        // 保存记录
        save(record);
        
        log.info("生成计件记录成功，工人：{}，包号：{}，金额：{}", 
            record.getWorkerName(), record.getBundleNo(), record.getActualAmount());
        
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSettle(List<Long> recordIds, Long payrollPeriodId) {
        if (recordIds == null || recordIds.isEmpty()) {
            return 0;
        }
        
        LambdaQueryWrapper<PieceworkRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PieceworkRecord::getId, recordIds)
               .eq(PieceworkRecord::getStatus, "pending")
               .eq(PieceworkRecord::getTenantId, TenantContext.getCurrentTenantId());
        
        List<PieceworkRecord> records = list(wrapper);
        
        if (records.isEmpty()) {
            return 0;
        }
        
        // 更新记录状态
        LocalDateTime now = LocalDateTime.now();
        records.forEach(record -> {
            record.setStatus("settled");
            record.setPayrollPeriodId(payrollPeriodId);
            record.setSettledAt(now);
            record.setUpdatedBy(1L); // TODO: 从当前用户上下文获取
            record.setUpdatedAt(now);
        });
        
        updateBatchById(records);
        
        log.info("批量结算计件记录成功，数量：{}，工资周期：{}", records.size(), payrollPeriodId);
        
        return records.size();
    }

    @Override
    public Page<PieceworkRecord> pageRecords(Page<PieceworkRecord> page, Long workerId, Long workshopId, 
                                           LocalDate startDate, LocalDate endDate, String status) {
        LambdaQueryWrapper<PieceworkRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PieceworkRecord::getTenantId, TenantContext.getCurrentTenantId())
               .eq(workerId != null, PieceworkRecord::getWorkerId, workerId)
               .eq(workshopId != null, PieceworkRecord::getWorkshopId, workshopId)
               .ge(startDate != null, PieceworkRecord::getWorkDate, startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
               .le(endDate != null, PieceworkRecord::getWorkDate, endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
               .eq(StrUtil.isNotBlank(status), PieceworkRecord::getStatus, status)
               .orderByDesc(PieceworkRecord::getCreatedAt);
        
        return page(page, wrapper);
    }

    @Override
    public Map<String, Object> calculateWorkerDailySummary(Long workerId, String workDate) {
        LambdaQueryWrapper<PieceworkRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PieceworkRecord::getTenantId, TenantContext.getCurrentTenantId())
               .eq(PieceworkRecord::getWorkerId, workerId)
               .eq(PieceworkRecord::getWorkDate, workDate);
        
        List<PieceworkRecord> records = list(wrapper);
        
        Map<String, Object> summary = new HashMap<>();
        
        if (records.isEmpty()) {
            summary.put("totalRecords", 0);
            summary.put("totalPieces", 0);
            summary.put("totalAmount", BigDecimal.ZERO);
            summary.put("avgQualityRate", BigDecimal.ZERO);
            summary.put("totalDuration", 0);
            return summary;
        }
        
        int totalPieces = records.stream().mapToInt(PieceworkRecord::getQuantity).sum();
        int totalQualified = records.stream().mapToInt(PieceworkRecord::getQualifiedQuantity).sum();
        BigDecimal totalAmount = records.stream()
            .map(PieceworkRecord::getActualAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalDuration = records.stream()
            .mapToInt(record -> record.getDuration() != null ? record.getDuration() : 0)
            .sum();
        
        BigDecimal avgQualityRate = totalPieces > 0 
            ? BigDecimal.valueOf(totalQualified * 100.0 / totalPieces).setScale(1, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        summary.put("totalRecords", records.size());
        summary.put("totalPieces", totalPieces);
        summary.put("totalAmount", totalAmount);
        summary.put("avgQualityRate", avgQualityRate);
        summary.put("totalDuration", totalDuration);
        summary.put("records", records);
        
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustAmount(Long recordId, BigDecimal adjustAmount, String reason) {
        PieceworkRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("计件记录不存在");
        }
        
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("只有待结算的记录才能调整金额");
        }
        
        // 调整金额
        BigDecimal newAmount = record.getActualAmount().add(adjustAmount);
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("调整后金额不能为负数");
        }
        
        record.setActualAmount(newAmount);
        record.setRemark(record.getRemark() + "\n调整原因：" + reason + "，调整金额：" + adjustAmount);
        record.setUpdatedBy(1L); // TODO: 从当前用户上下文获取
        record.setUpdatedAt(LocalDateTime.now());
        
        boolean result = updateById(record);
        
        if (result) {
            log.info("调整计件记录金额成功，记录ID：{}，调整金额：{}，原因：{}", 
                recordId, adjustAmount, reason);
        }
        
        return result;
    }

    @Override
    public byte[] exportRecords(Long workerId, LocalDate startDate, LocalDate endDate) {
        // TODO: 使用POI生成Excel文件
        log.info("导出计件记录，工人ID：{}，时间范围：{} - {}", workerId, startDate, endDate);
        
        // 这里应该生成实际的Excel文件
        return "计件记录导出内容".getBytes();
    }

    /**
     * 模拟获取生产流转记录数据
     */
    private Map<String, Object> getProductionFlowData(Long productionFlowId) {
        // 这里应该调用生产管理模块的API获取真实数据
        Map<String, Object> data = new HashMap<>();
        data.put("workerId", 1L);
        data.put("workerName", "张三");
        data.put("workshopId", 1L);
        data.put("workshopName", "一车间");
        data.put("bundleId", 1L);
        data.put("bundleNo", "BDL20240120001");
        data.put("processId", 1L);
        data.put("processName", "缝合");
        data.put("styleId", 1L);
        data.put("styleCode", "ST001");
        data.put("styleName", "经典T恤");
        data.put("color", "白色");
        data.put("size", "M");
        data.put("quantity", 100);
        data.put("qualifiedQuantity", 98);
        data.put("defectQuantity", 2);
        data.put("startTime", LocalDateTime.now().minusHours(2));
        data.put("endTime", LocalDateTime.now());
        
        return data;
    }

    /**
     * 计算质量系数
     */
    private BigDecimal calculateQualityFactor(Integer qualifiedQuantity, Integer defectQuantity) {
        int total = qualifiedQuantity + defectQuantity;
        if (total == 0) {
            return BigDecimal.ONE;
        }
        
        double qualityRate = (double) qualifiedQuantity / total;
        
        // 根据质量率计算系数
        if (qualityRate >= 0.98) {
            return BigDecimal.valueOf(1.05); // 优秀奖励5%
        } else if (qualityRate >= 0.95) {
            return BigDecimal.ONE; // 正常
        } else if (qualityRate >= 0.90) {
            return BigDecimal.valueOf(0.95); // 扣除5%
        } else {
            return BigDecimal.valueOf(0.85); // 扣除15%
        }
    }
}




