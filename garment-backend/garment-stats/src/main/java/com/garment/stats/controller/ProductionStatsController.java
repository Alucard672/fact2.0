package com.garment.stats.controller;

import com.garment.common.result.Result;
import com.garment.stats.dto.ProductionStatsRequest;
import com.garment.stats.dto.ProductionStatsResponse;
import com.garment.stats.dto.WorkshopRankingResponse;
import com.garment.stats.service.ProductionStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 生产统计控制器
 *
 * @author system
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class ProductionStatsController {

    private final ProductionStatsService productionStatsService;

    /**
     * 获取生产统计数据
     */
    @PostMapping("/production")
    public Result<ProductionStatsResponse> getProductionStats(@RequestBody @Validated ProductionStatsRequest request) {
        ProductionStatsResponse response = productionStatsService.getProductionStats(request);
        return Result.success(response);
    }

    /**
     * 获取车间排名
     */
    @GetMapping("/workshop-ranking")
    public Result<WorkshopRankingResponse> getWorkshopRanking(
            @RequestParam(defaultValue = "today") String period,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "overall") String sortBy) {
        
        WorkshopRankingResponse response = productionStatsService.getWorkshopRanking(period, startDate, endDate, sortBy);
        return Result.success(response);
    }

    /**
     * 获取今日统计（小程序用）
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayStats(@RequestParam(required = false) Long workerId) {
        Map<String, Object> stats = productionStatsService.getTodayStats(workerId);
        return Result.success(stats);
    }

    /**
     * 获取实时生产数据
     */
    @GetMapping("/realtime")
    public Result<Map<String, Object>> getRealTimeStats() {
        Map<String, Object> stats = productionStatsService.getRealTimeStats();
        return Result.success(stats);
    }

    /**
     * 导出生产报表
     */
    @PostMapping("/production/export")
    public ResponseEntity<byte[]> exportProductionReport(@RequestBody @Validated ProductionStatsRequest request) {
        byte[] excelData = productionStatsService.exportProductionReport(request);
        
        String fileName = String.format("生产统计报表_%s_%s.xlsx", 
            request.getStartDate(), request.getEndDate());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    /**
     * 刷新统计缓存
     */
    @PostMapping("/cache/refresh")
    public Result<Void> refreshStatsCache() {
        productionStatsService.refreshStatsCache();
        return Result.success();
    }
}



