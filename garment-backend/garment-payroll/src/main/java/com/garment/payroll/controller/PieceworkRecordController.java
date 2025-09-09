package com.garment.payroll.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.common.result.Result;
import com.garment.payroll.entity.PieceworkRecord;
import com.garment.payroll.service.PieceworkRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 计件记录控制器
 *
 * @author system
 */
@RestController
@RequestMapping("/api/payroll/piecework-records")
@RequiredArgsConstructor
public class PieceworkRecordController {

    private final PieceworkRecordService pieceworkRecordService;

    /**
     * 分页查询计件记录
     */
    @GetMapping
    public Result<Page<PieceworkRecord>> pageRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) Long workshopId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String status) {
        
        Page<PieceworkRecord> page = new Page<>(current, size);
        Page<PieceworkRecord> result = pieceworkRecordService.pageRecords(
            page, workerId, workshopId, startDate, endDate, status);
        return Result.success(result);
    }

    /**
     * 获取计件记录详情
     */
    @GetMapping("/{id}")
    public Result<PieceworkRecord> getRecord(@PathVariable Long id) {
        PieceworkRecord record = pieceworkRecordService.getById(id);
        return Result.success(record);
    }

    /**
     * 从生产流转记录生成计件记录
     */
    @PostMapping("/generate/{productionFlowId}")
    public Result<PieceworkRecord> generateFromProductionFlow(@PathVariable Long productionFlowId) {
        PieceworkRecord record = pieceworkRecordService.generateFromProductionFlow(productionFlowId);
        return Result.success(record);
    }

    /**
     * 批量结算计件记录
     */
    @PostMapping("/batch-settle")
    public Result<Integer> batchSettle(
            @RequestParam List<Long> recordIds,
            @RequestParam Long payrollPeriodId) {
        
        int count = pieceworkRecordService.batchSettle(recordIds, payrollPeriodId);
        return Result.success(count);
    }

    /**
     * 计算工人日汇总
     */
    @GetMapping("/worker/{workerId}/daily-summary")
    public Result<Map<String, Object>> getWorkerDailySummary(
            @PathVariable Long workerId,
            @RequestParam String workDate) {
        
        Map<String, Object> summary = pieceworkRecordService.calculateWorkerDailySummary(workerId, workDate);
        return Result.success(summary);
    }

    /**
     * 调整计件记录金额
     */
    @PostMapping("/{id}/adjust")
    public Result<Void> adjustAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal adjustAmount,
            @RequestParam String reason) {
        
        boolean success = pieceworkRecordService.adjustAmount(id, adjustAmount, reason);
        if (success) {
            return Result.success();
        } else {
            return Result.error("调整失败");
        }
    }

    /**
     * 导出计件记录
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRecords(
            @RequestParam(required = false) Long workerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        byte[] excelData = pieceworkRecordService.exportRecords(workerId, startDate, endDate);
        
        String fileName = String.format("计件记录_%s_%s.xlsx", startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}



