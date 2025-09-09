package com.garment.payroll.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.payroll.entity.PieceworkRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 计件记录服务接口
 *
 * @author system
 */
public interface PieceworkRecordService extends IService<PieceworkRecord> {

    /**
     * 根据生产流转记录自动生成计件记录
     *
     * @param productionFlowId 生产流转记录ID
     * @return 计件记录
     */
    PieceworkRecord generateFromProductionFlow(Long productionFlowId);

    /**
     * 批量结算计件记录
     *
     * @param recordIds 记录ID列表
     * @param payrollPeriodId 工资周期ID
     * @return 结算数量
     */
    int batchSettle(List<Long> recordIds, Long payrollPeriodId);

    /**
     * 分页查询计件记录
     *
     * @param page 分页参数
     * @param workerId 工人ID
     * @param workshopId 车间ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param status 状态
     * @return 分页结果
     */
    Page<PieceworkRecord> pageRecords(Page<PieceworkRecord> page, Long workerId, Long workshopId, 
                                     LocalDate startDate, LocalDate endDate, String status);

    /**
     * 计算工人指定日期的计件汇总
     *
     * @param workerId 工人ID
     * @param workDate 工作日期
     * @return 汇总数据
     */
    Map<String, Object> calculateWorkerDailySummary(Long workerId, String workDate);

    /**
     * 调整计件记录金额
     *
     * @param recordId 记录ID
     * @param adjustAmount 调整金额
     * @param reason 调整原因
     * @return 是否成功
     */
    boolean adjustAmount(Long recordId, BigDecimal adjustAmount, String reason);

    /**
     * 导出计件记录
     *
     * @param workerId 工人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel文件字节数组
     */
    byte[] exportRecords(Long workerId, LocalDate startDate, LocalDate endDate);
}




