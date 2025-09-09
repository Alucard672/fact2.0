package com.garment.production.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import com.garment.production.dto.ScanSubmitRequest;
import com.garment.production.dto.ScanTakeRequest;
import com.garment.production.entity.Bundle;
import com.garment.production.entity.ProductionFlow;
import com.garment.production.mapper.ProductionFlowMapper;
import com.garment.production.service.BundleService;
import com.garment.production.service.ProductionFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产流转服务实现
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionFlowServiceImpl extends ServiceImpl<ProductionFlowMapper, ProductionFlow> 
        implements ProductionFlowService {

    private final BundleService bundleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductionFlow scanTake(ScanTakeRequest request) {
        // 解析二维码获取包信息
        Bundle bundle = bundleService.getBundleByQrCode(request.getQrCode());
        if (bundle == null) {
            throw new BusinessException("无效的二维码");
        }
        
        // 验证包状态
        if (!"pending".equals(bundle.getStatus())) {
            throw new BusinessException("该包不在待领工状态");
        }
        
        // 检查是否已被其他工人领工
        if (bundle.getCurrentWorkerId() != null) {
            throw new BusinessException("该包已被其他工人领工");
        }
        
        // 获取工人ID（从JWT或请求中获取）
        Long workerId = getCurrentWorkerId(request.getWorkerId());
        
        // 创建生产流转记录
        ProductionFlow flow = new ProductionFlow();
        flow.setTenantId(TenantContext.getCurrentTenantId());
        flow.setBundleId(bundle.getId());
        flow.setBundleNo(bundle.getBundleNo());
        flow.setProcessId(request.getProcessId());
        // TODO: 从基础资料服务获取工序信息
        flow.setProcessName("缝合"); // 临时数据
        flow.setWorkerId(workerId);
        // TODO: 从用户服务获取工人信息
        flow.setWorkerName("张三"); // 临时数据
        // TODO: 从基础资料服务获取车间信息
        flow.setWorkshopId(1L); // 临时数据
        flow.setWorkshopName("一车间"); // 临时数据
        flow.setActionType("take");
        flow.setQuantity(bundle.getQuantity());
        flow.setQuantityOk(0);
        flow.setQuantityNg(0);
        // TODO: 从工价服务获取单价
        flow.setUnitPrice(BigDecimal.valueOf(1.20)); // 临时数据
        flow.setTotalAmount(BigDecimal.ZERO);
        flow.setRemark(request.getRemark());
        flow.setOperatedAt(LocalDateTime.now());
        flow.setCreatedBy(workerId);
        
        // 保存流转记录
        save(flow);
        
        // 更新包状态
        bundle.setStatus("in_work");
        bundle.setCurrentProcessId(request.getProcessId());
        bundle.setCurrentWorkerId(workerId);
        bundle.setStartedAt(LocalDateTime.now());
        bundle.setUpdatedBy(workerId);
        bundle.setUpdatedAt(LocalDateTime.now());
        bundleService.updateById(bundle);
        
        log.info("领工成功，包号：{}，工人ID：{}", bundle.getBundleNo(), workerId);
        return flow;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductionFlow scanSubmit(ScanSubmitRequest request) {
        // 解析二维码获取包信息
        Bundle bundle = bundleService.getBundleByQrCode(request.getQrCode());
        if (bundle == null) {
            throw new BusinessException("无效的二维码");
        }
        
        // 验证包状态
        if (!"in_work".equals(bundle.getStatus())) {
            throw new BusinessException("该包不在生产中状态");
        }
        
        // 获取工人ID并验证是否为当前领工人
        Long workerId = getCurrentWorkerId(request.getWorkerId());
        if (!workerId.equals(bundle.getCurrentWorkerId())) {
            throw new BusinessException("只有领工人才能交工");
        }
        
        // 验证数量
        int totalSubmit = request.getQuantityOk() + request.getQuantityNg();
        if (totalSubmit > bundle.getQuantity()) {
            throw new BusinessException("交工数量不能超过包总数量");
        }
        
        // 计算工资
        BigDecimal unitPrice = BigDecimal.valueOf(1.20); // TODO: 从工价服务获取
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(request.getQuantityOk()));
        
        // 创建生产流转记录
        ProductionFlow flow = new ProductionFlow();
        flow.setTenantId(TenantContext.getCurrentTenantId());
        flow.setBundleId(bundle.getId());
        flow.setBundleNo(bundle.getBundleNo());
        flow.setProcessId(bundle.getCurrentProcessId());
        // TODO: 从基础资料服务获取工序信息
        flow.setProcessName("缝合"); // 临时数据
        flow.setWorkerId(workerId);
        // TODO: 从用户服务获取工人信息
        flow.setWorkerName("张三"); // 临时数据
        // TODO: 从基础资料服务获取车间信息
        flow.setWorkshopId(1L); // 临时数据
        flow.setWorkshopName("一车间"); // 临时数据
        flow.setActionType("submit");
        flow.setQuantity(totalSubmit);
        flow.setQuantityOk(request.getQuantityOk());
        flow.setQuantityNg(request.getQuantityNg());
        flow.setUnitPrice(unitPrice);
        flow.setTotalAmount(totalAmount);
        flow.setQualityScore(request.getQualityScore());
        flow.setNeedRepair(request.getNeedRepair());
        flow.setRemark(request.getRemark());
        flow.setOperatedAt(LocalDateTime.now());
        flow.setCreatedBy(workerId);
        
        // 保存流转记录
        save(flow);
        
        // 更新包状态
        if (request.getNeedRepair() && request.getQuantityNg() > 0) {
            // 需要返修
            bundle.setStatus("returned");
        } else {
            // 完成
            bundle.setStatus("completed");
            bundle.setCompletedAt(LocalDateTime.now());
        }
        
        bundle.setCurrentProcessId(null);
        bundle.setCurrentWorkerId(null);
        bundle.setUpdatedBy(workerId);
        bundle.setUpdatedAt(LocalDateTime.now());
        bundleService.updateById(bundle);
        
        log.info("交工成功，包号：{}，工人ID：{}，合格数：{}", 
            bundle.getBundleNo(), workerId, request.getQuantityOk());
        return flow;
    }

    @Override
    public List<ProductionFlow> getFlowsByBundleId(Long bundleId) {
        LambdaQueryWrapper<ProductionFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductionFlow::getBundleId, bundleId)
               .orderByDesc(ProductionFlow::getOperatedAt);
        return list(wrapper);
    }

    @Override
    public List<ProductionFlow> getCurrentWorkByWorkerId(Long workerId) {
        // 查询工人当前在制的包（最新的领工记录且未交工）
        LambdaQueryWrapper<ProductionFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductionFlow::getWorkerId, workerId)
               .eq(ProductionFlow::getActionType, "take")
               .notExists("SELECT 1 FROM production_flows pf2 " +
                         "WHERE pf2.bundle_id = production_flows.bundle_id " +
                         "AND pf2.worker_id = production_flows.worker_id " +
                         "AND pf2.action_type = 'submit' " +
                         "AND pf2.operated_at > production_flows.operated_at")
               .orderByDesc(ProductionFlow::getOperatedAt);
        
        return list(wrapper);
    }

    /**
     * 获取当前工人ID
     */
    private Long getCurrentWorkerId(Long workerId) {
        if (workerId != null) {
            return workerId;
        }
        
        // TODO: 从JWT中获取当前用户ID
        return 1L; // 临时返回
    }
}




