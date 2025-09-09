package com.garment.admin.service;

import java.util.Map;

/**
 * 集成服务接口
 *
 * @author system
 */
public interface IntegrationService {

    /**
     * 完整的生产流程：从裁床订单到计件记录
     *
     * @param cutOrderId 裁床订单ID
     * @return 流程结果
     */
    Map<String, Object> executeCompleteProductionFlow(Long cutOrderId);

    /**
     * 扫码领工集成流程
     *
     * @param qrCode 二维码
     * @param workerId 工人ID
     * @param processId 工序ID
     * @return 流程结果
     */
    Map<String, Object> executeScanTakeFlow(String qrCode, Long workerId, Long processId);

    /**
     * 扫码交工集成流程
     *
     * @param qrCode 二维码
     * @param workerId 工人ID
     * @param qualifiedQuantity 合格数量
     * @param defectQuantity 次品数量
     * @return 流程结果
     */
    Map<String, Object> executeScanSubmitFlow(String qrCode, Long workerId, 
                                             Integer qualifiedQuantity, Integer defectQuantity);

    /**
     * 批量打印包菲票
     *
     * @param cutOrderId 裁床订单ID
     * @param printerIp 打印机IP
     * @return 打印结果
     */
    Map<String, Object> batchPrintBundleTickets(Long cutOrderId, String printerIp);

    /**
     * 自动结算工资
     *
     * @param payrollPeriodId 工资周期ID
     * @return 结算结果
     */
    Map<String, Object> autoCalculatePayroll(Long payrollPeriodId);
}




