package com.garment.stats.service;

import com.garment.stats.dto.ProductionStatsRequest;
import com.garment.stats.dto.ProductionStatsResponse;
import com.garment.stats.dto.WorkshopRankingResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 生产统计服务接口
 *
 * @author system
 */
public interface ProductionStatsService {

    /**
     * 获取生产统计数据
     *
     * @param request 统计请求
     * @return 统计响应
     */
    ProductionStatsResponse getProductionStats(ProductionStatsRequest request);

    /**
     * 获取车间排名
     *
     * @param period    统计周期：today-今日，yesterday-昨日，week-本周，month-本月
     * @param startDate 开始日期（自定义周期使用）
     * @param endDate   结束日期（自定义周期使用）
     * @param sortBy    排序方式：overall-综合，production-产量，efficiency-效率，quality-质量
     * @return 车间排名
     */
    WorkshopRankingResponse getWorkshopRanking(String period, LocalDate startDate, LocalDate endDate, String sortBy);

    /**
     * 获取今日统计（用于小程序首页）
     *
     * @param workerId 工人ID（可选）
     * @return 今日统计数据
     */
    Map<String, Object> getTodayStats(Long workerId);

    /**
     * 获取实时生产数据
     *
     * @return 实时数据
     */
    Map<String, Object> getRealTimeStats();

    /**
     * 导出生产报表
     *
     * @param request 统计请求
     * @return Excel文件字节数组
     */
    byte[] exportProductionReport(ProductionStatsRequest request);

    /**
     * 刷新统计缓存
     */
    void refreshStatsCache();
}




