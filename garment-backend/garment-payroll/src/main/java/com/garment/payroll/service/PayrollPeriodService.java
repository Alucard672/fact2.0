package com.garment.payroll.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.payroll.entity.PayrollPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 工资周期服务接口
 *
 * @author system
 */
public interface PayrollPeriodService extends IService<PayrollPeriod> {

    /**
     * 创建工资周期
     *
     * @param periodType 周期类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工资周期
     */
    PayrollPeriod createPeriod(String periodType, LocalDate startDate, LocalDate endDate);

    /**
     * 自动生成工资周期
     *
     * @param periodType 周期类型
     * @param count 生成数量
     * @return 生成的周期列表
     */
    List<PayrollPeriod> autoGeneratePeriods(String periodType, int count);

    /**
     * 计算工资周期
     *
     * @param periodId 周期ID
     * @return 是否成功
     */
    boolean calculatePeriod(Long periodId);

    /**
     * 锁定工资周期
     *
     * @param periodId 周期ID
     * @return 是否成功
     */
    boolean lockPeriod(Long periodId);

    /**
     * 发放工资
     *
     * @param periodId 周期ID
     * @return 是否成功
     */
    boolean payPeriod(Long periodId);

    /**
     * 获取工资周期统计
     *
     * @param periodId 周期ID
     * @return 统计数据
     */
    Map<String, Object> getPeriodStatistics(Long periodId);

    /**
     * 获取工人工资明细
     *
     * @param periodId 周期ID
     * @param workerId 工人ID
     * @return 工资明细
     */
    Map<String, Object> getWorkerPayrollDetail(Long periodId, Long workerId);

    /**
     * 导出工资表
     *
     * @param periodId 周期ID
     * @return Excel文件字节数组
     */
    byte[] exportPayroll(Long periodId);
}




