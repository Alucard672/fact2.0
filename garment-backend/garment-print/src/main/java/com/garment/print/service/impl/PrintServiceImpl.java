package com.garment.print.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import com.garment.print.dto.BundleTicketData;
import com.garment.print.dto.CreatePrintTaskRequest;
import com.garment.print.entity.PrintTask;
import com.garment.print.entity.PrintTemplate;
import com.garment.print.service.PrintService;
import com.garment.print.service.PrintTaskService;
import com.garment.print.service.PrintTemplateService;
import com.garment.print.util.PrintUtil;
import com.garment.print.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印服务实现
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrintServiceImpl implements PrintService {

    private final PrintTaskService printTaskService;
    private final PrintTemplateService printTemplateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrintTask createPrintTask(CreatePrintTaskRequest request) {
        // 验证裁床订单存在
        // TODO: 调用生产管理服务验证订单
        
        // 获取打印模板
        PrintTemplate template;
        if (request.getTemplateId() != null) {
            template = printTemplateService.getById(request.getTemplateId());
            if (template == null) {
                throw new BusinessException("打印模板不存在");
            }
        } else {
            template = printTemplateService.getDefaultTemplate(request.getPrintType());
            if (template == null) {
                template = printTemplateService.createDefaultTemplate(request.getPrintType());
            }
        }

        // 获取包列表
        List<Long> bundleIds = request.getBundleIds();
        if (bundleIds == null || bundleIds.isEmpty()) {
            // TODO: 调用生产管理服务获取订单下的所有包
            bundleIds = List.of(1L, 2L, 3L); // 临时数据
        }

        // 生成打印数据
        Map<String, Object> printData = new HashMap<>();
        printData.put("cutOrderId", request.getCutOrderId());
        printData.put("bundleIds", bundleIds);
        printData.put("printType", request.getPrintType());
        printData.put("templateId", template.getId());
        printData.put("printParams", request.getPrintParams());

        // 创建打印任务
        PrintTask printTask = new PrintTask();
        printTask.setTenantId(TenantContext.getCurrentTenantId());
        printTask.setTaskNo(generateTaskNo());
        printTask.setCutOrderId(request.getCutOrderId());
        // TODO: 从生产管理服务获取订单号
        printTask.setOrderNo("CUT20240120001"); // 临时数据
        printTask.setPrintType(request.getPrintType());
        printTask.setTemplateId(template.getId());
        printTask.setTemplateName(template.getTemplateName());
        printTask.setPrintCount(bundleIds.size());
        printTask.setPrintData(JSON.toJSONString(printData));
        printTask.setPrinterIp(request.getPrinterIp());
        printTask.setPrinterName(request.getPrinterName());
        printTask.setStatus("pending");
        printTask.setCreatedBy(1L); // TODO: 从当前用户上下文获取

        // 保存打印任务
        printTaskService.save(printTask);

        log.info("创建打印任务成功，任务号：{}", printTask.getTaskNo());
        return printTask;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executePrintTask(Long taskId) {
        PrintTask printTask = printTaskService.getById(taskId);
        if (printTask == null) {
            throw new BusinessException("打印任务不存在");
        }

        if (!"pending".equals(printTask.getStatus())) {
            throw new BusinessException("任务状态不正确，无法执行打印");
        }

        try {
            // 更新任务状态为打印中
            printTask.setStatus("printing");
            printTask.setPrintStartTime(LocalDateTime.now());
            printTaskService.updateById(printTask);

            // 解析打印数据
            Map<String, Object> printData = JSON.parseObject(printTask.getPrintData(), Map.class);
            List<Long> bundleIds = (List<Long>) printData.get("bundleIds");

            // 获取打印模板
            PrintTemplate template = printTemplateService.getById(printTask.getTemplateId());
            if (template == null) {
                throw new BusinessException("打印模板不存在");
            }

            // 检查打印机状态
            if (StrUtil.isNotBlank(printTask.getPrinterIp()) && 
                !checkPrinterStatus(printTask.getPrinterIp())) {
                throw new BusinessException("打印机离线或无法连接");
            }

            // 执行具体的打印逻辑
            boolean success = false;
            if ("bundle".equals(printTask.getPrintType())) {
                success = executeBundleTicketPrint(bundleIds, template, printTask.getPrinterIp());
            } else if ("qr".equals(printTask.getPrintType())) {
                success = executeQrCodePrint(bundleIds, template, printTask.getPrinterIp());
            } else if ("label".equals(printTask.getPrintType())) {
                success = executeLabelPrint(bundleIds, template, printTask.getPrinterIp());
            }

            // 更新任务状态
            if (success) {
                printTask.setStatus("completed");
                printTask.setPrintEndTime(LocalDateTime.now());
                log.info("打印任务执行成功，任务号：{}", printTask.getTaskNo());
            } else {
                printTask.setStatus("failed");
                printTask.setErrorMessage("打印执行失败");
                log.error("打印任务执行失败，任务号：{}", printTask.getTaskNo());
            }

            printTaskService.updateById(printTask);
            return success;

        } catch (Exception e) {
            // 更新任务状态为失败
            printTask.setStatus("failed");
            printTask.setErrorMessage(e.getMessage());
            printTask.setPrintEndTime(LocalDateTime.now());
            printTaskService.updateById(printTask);
            
            log.error("打印任务执行异常，任务号：{}，错误：{}", printTask.getTaskNo(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public PrintTask batchPrintBundleTickets(Long cutOrderId, List<Long> bundleIds, String printerIp) {
        CreatePrintTaskRequest request = new CreatePrintTaskRequest();
        request.setCutOrderId(cutOrderId);
        request.setPrintType("bundle");
        request.setBundleIds(bundleIds);
        request.setPrinterIp(printerIp);
        
        PrintTask printTask = createPrintTask(request);
        
        // 异步执行打印任务
        // TODO: 使用异步任务队列执行
        executePrintTask(printTask.getId());
        
        return printTask;
    }

    @Override
    public PrintTask printSingleBundleTicket(Long bundleId, String printerIp) {
        // TODO: 从包信息获取裁床订单ID
        Long cutOrderId = 1L; // 临时数据
        
        return batchPrintBundleTickets(cutOrderId, List.of(bundleId), printerIp);
    }

    @Override
    public BundleTicketData generateBundleTicketData(Long bundleId) {
        // TODO: 从生产管理服务获取包信息
        BundleTicketData data = new BundleTicketData();
        data.setBundleNo("BDL20240120001");
        data.setOrderNo("CUT20240120001");
        data.setStyleCode("ST001");
        data.setStyleName("经典T恤");
        data.setColor("白色");
        data.setSize("M");
        data.setQuantity(100);
        data.setBedNo("B001");
        data.setQrCode("BDL-1-1-" + bundleId + "-check-A1B2");
        data.setQrCodeImage(QrCodeUtil.generateQrCodeBase64(data.getQrCode()));
        data.setPrintTime(PrintUtil.formatPrintTime());
        data.setCompanyName("优衣服装厂");
        
        return data;
    }

    @Override
    public boolean checkPrinterStatus(String printerIp) {
        return PrintUtil.isPrinterOnline(printerIp);
    }

    /**
     * 执行包菲票打印
     */
    private boolean executeBundleTicketPrint(List<Long> bundleIds, PrintTemplate template, String printerIp) {
        try {
            for (Long bundleId : bundleIds) {
                // 生成包菲票数据
                BundleTicketData ticketData = generateBundleTicketData(bundleId);
                
                // 准备模板数据
                Map<String, Object> templateData = new HashMap<>();
                templateData.put("bundleNo", ticketData.getBundleNo());
                templateData.put("orderNo", ticketData.getOrderNo());
                templateData.put("styleCode", ticketData.getStyleCode());
                templateData.put("styleName", ticketData.getStyleName());
                templateData.put("color", ticketData.getColor());
                templateData.put("size", ticketData.getSize());
                templateData.put("quantity", ticketData.getQuantity());
                templateData.put("bedNo", ticketData.getBedNo());
                templateData.put("segmentTag", ticketData.getSegmentTag());
                templateData.put("layerFrom", ticketData.getLayerFrom());
                templateData.put("layerTo", ticketData.getLayerTo());
                templateData.put("qrCode", ticketData.getQrCode());
                templateData.put("qrCodeImage", ticketData.getQrCodeImage());
                templateData.put("printTime", ticketData.getPrintTime());
                templateData.put("companyName", ticketData.getCompanyName());

                // 渲染模板
                String htmlContent = PrintUtil.renderTemplate(template.getTemplateContent(), templateData);
                
                // 发送到打印机（这里需要根据实际打印机类型实现）
                if (StrUtil.isNotBlank(printerIp)) {
                    // 转换为打印机指令
                    byte[] printData = htmlContent.getBytes("UTF-8");
                    boolean sent = PrintUtil.sendToPrinter(printerIp, printData);
                    if (!sent) {
                        log.error("发送打印数据到打印机失败：{}", printerIp);
                        return false;
                    }
                } else {
                    // 本地打印或其他方式
                    log.info("本地打印包菲票：{}", ticketData.getBundleNo());
                }
            }
            return true;
        } catch (Exception e) {
            log.error("执行包菲票打印失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行二维码打印
     */
    private boolean executeQrCodePrint(List<Long> bundleIds, PrintTemplate template, String printerIp) {
        // TODO: 实现二维码打印逻辑
        log.info("执行二维码打印，包数量：{}", bundleIds.size());
        return true;
    }

    /**
     * 执行标签打印
     */
    private boolean executeLabelPrint(List<Long> bundleIds, PrintTemplate template, String printerIp) {
        // TODO: 实现标签打印逻辑
        log.info("执行标签打印，包数量：{}", bundleIds.size());
        return true;
    }

    /**
     * 生成任务号
     */
    private String generateTaskNo() {
        String dateStr = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
        String prefix = "PT" + dateStr;
        String suffix = IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
        return prefix + suffix;
    }
}




