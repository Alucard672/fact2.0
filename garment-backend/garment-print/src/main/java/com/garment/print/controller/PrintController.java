package com.garment.print.controller;

import com.garment.common.result.Result;
import com.garment.print.dto.CreatePrintTaskRequest;
import com.garment.print.entity.PrintTask;
import com.garment.print.service.PrintService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打印控制器
 *
 * @author system
 */
@RestController
@RequestMapping("/api/print")
@RequiredArgsConstructor
public class PrintController {

    private final PrintService printService;

    /**
     * 创建打印任务
     */
    @PostMapping("/tasks")
    public Result<PrintTask> createPrintTask(@RequestBody @Validated CreatePrintTaskRequest request) {
        PrintTask printTask = printService.createPrintTask(request);
        return Result.success(printTask);
    }

    /**
     * 执行打印任务
     */
    @PostMapping("/tasks/{taskId}/execute")
    public Result<Void> executePrintTask(@PathVariable Long taskId) {
        boolean success = printService.executePrintTask(taskId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("打印任务执行失败");
        }
    }

    /**
     * 批量打印包菲票
     */
    @PostMapping("/bundle-tickets/batch")
    public Result<PrintTask> batchPrintBundleTickets(
            @RequestParam Long cutOrderId,
            @RequestParam(required = false) List<Long> bundleIds,
            @RequestParam(required = false) String printerIp) {
        
        PrintTask printTask = printService.batchPrintBundleTickets(cutOrderId, bundleIds, printerIp);
        return Result.success(printTask);
    }

    /**
     * 打印单个包菲票
     */
    @PostMapping("/bundle-tickets/{bundleId}")
    public Result<PrintTask> printSingleBundleTicket(
            @PathVariable Long bundleId,
            @RequestParam(required = false) String printerIp) {
        
        PrintTask printTask = printService.printSingleBundleTicket(bundleId, printerIp);
        return Result.success(printTask);
    }

    /**
     * 检查打印机状态
     */
    @GetMapping("/printers/{printerIp}/status")
    public Result<Boolean> checkPrinterStatus(@PathVariable String printerIp) {
        boolean online = printService.checkPrinterStatus(printerIp);
        return Result.success(online);
    }
}



