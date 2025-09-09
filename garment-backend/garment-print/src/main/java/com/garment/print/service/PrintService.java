package com.garment.print.service;

import com.garment.print.dto.BundleTicketData;
import com.garment.print.dto.CreatePrintTaskRequest;
import com.garment.print.entity.PrintTask;

import java.util.List;

/**
 * 打印服务接口
 *
 * @author system
 */
public interface PrintService {

    /**
     * 创建打印任务
     *
     * @param request 打印请求
     * @return 打印任务
     */
    PrintTask createPrintTask(CreatePrintTaskRequest request);

    /**
     * 执行打印任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean executePrintTask(Long taskId);

    /**
     * 批量打印包菲票
     *
     * @param cutOrderId 裁床订单ID
     * @param bundleIds  包ID列表（可选）
     * @param printerIp  打印机IP
     * @return 打印任务
     */
    PrintTask batchPrintBundleTickets(Long cutOrderId, List<Long> bundleIds, String printerIp);

    /**
     * 打印单个包菲票
     *
     * @param bundleId  包ID
     * @param printerIp 打印机IP
     * @return 打印任务
     */
    PrintTask printSingleBundleTicket(Long bundleId, String printerIp);

    /**
     * 生成包菲票数据
     *
     * @param bundleId 包ID
     * @return 包菲票数据
     */
    BundleTicketData generateBundleTicketData(Long bundleId);

    /**
     * 检查打印机状态
     *
     * @param printerIp 打印机IP
     * @return 是否在线
     */
    boolean checkPrinterStatus(String printerIp);
}




