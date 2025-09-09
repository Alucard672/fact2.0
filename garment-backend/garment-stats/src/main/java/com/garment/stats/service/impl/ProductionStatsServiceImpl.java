package com.garment.stats.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.garment.common.context.TenantContext;
import com.garment.stats.dto.ProductionStatsRequest;
import com.garment.stats.dto.ProductionStatsResponse;
import com.garment.stats.dto.WorkshopRankingResponse;
import com.garment.stats.service.ProductionStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生产统计服务实现
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionStatsServiceImpl implements ProductionStatsService {

    @Override
    @Cacheable(value = "productionStats", key = "#request.dimension + '_' + #request.startDate + '_' + #request.endDate")
    public ProductionStatsResponse getProductionStats(ProductionStatsRequest request) {
        log.info("获取生产统计数据，维度：{}，时间范围：{} - {}", 
            request.getDimension(), request.getStartDate(), request.getEndDate());

        ProductionStatsResponse response = new ProductionStatsResponse();
        
        // 获取核心指标
        response.setMetrics(calculateCoreMetrics(request));
        
        // 获取趋势数据
        response.setTrendData(calculateTrendData(request));
        
        // 获取车间分布
        response.setWorkshopDistribution(calculateWorkshopDistribution(request));
        
        // 获取款式排行
        response.setStyleRanking(calculateStyleRanking(request));
        
        // 获取工序完成情况
        response.setProcessCompletion(calculateProcessCompletion(request));
        
        return response;
    }

    @Override
    @Cacheable(value = "workshopRanking", key = "#period + '_' + #sortBy")
    public WorkshopRankingResponse getWorkshopRanking(String period, LocalDate startDate, LocalDate endDate, String sortBy) {
        log.info("获取车间排名，周期：{}，排序：{}", period, sortBy);

        // 确定统计时间范围
        LocalDate[] dateRange = calculateDateRange(period, startDate, endDate);
        LocalDate start = dateRange[0];
        LocalDate end = dateRange[1];

        WorkshopRankingResponse response = new WorkshopRankingResponse();
        
        // 模拟数据（实际应从数据库查询）
        List<WorkshopRankingResponse.WorkshopRanking> rankings = new ArrayList<>();
        
        // 车间1
        WorkshopRankingResponse.WorkshopRanking ranking1 = new WorkshopRankingResponse.WorkshopRanking();
        ranking1.setWorkshopId(1L);
        ranking1.setWorkshopName("一车间");
        ranking1.setManagerName("张主管");
        ranking1.setOverallScore(BigDecimal.valueOf(88.2));
        ranking1.setProduction(2150);
        ranking1.setEfficiency(BigDecimal.valueOf(85.6));
        ranking1.setQualityRate(BigDecimal.valueOf(97.2));
        ranking1.setOnTimeRate(BigDecimal.valueOf(94.5));
        ranking1.setTrend("stable");
        ranking1.setTrendValue(BigDecimal.ZERO);
        ranking1.setWorkerCount(35);
        ranking1.setWorkstationCount(40);
        rankings.add(ranking1);

        // 车间2
        WorkshopRankingResponse.WorkshopRanking ranking2 = new WorkshopRankingResponse.WorkshopRanking();
        ranking2.setWorkshopId(2L);
        ranking2.setWorkshopName("二车间");
        ranking2.setManagerName("李主管");
        ranking2.setOverallScore(BigDecimal.valueOf(86.8));
        ranking2.setProduction(1980);
        ranking2.setEfficiency(BigDecimal.valueOf(88.2));
        ranking2.setQualityRate(BigDecimal.valueOf(99.1));
        ranking2.setOnTimeRate(BigDecimal.valueOf(92.3));
        ranking2.setTrend("down");
        ranking2.setTrendValue(BigDecimal.valueOf(-1));
        ranking2.setWorkerCount(28);
        ranking2.setWorkstationCount(35);
        rankings.add(ranking2);

        // 车间3
        WorkshopRankingResponse.WorkshopRanking ranking3 = new WorkshopRankingResponse.WorkshopRanking();
        ranking3.setWorkshopId(3L);
        ranking3.setWorkshopName("三车间");
        ranking3.setManagerName("王主管");
        ranking3.setOverallScore(BigDecimal.valueOf(95.5));
        ranking3.setProduction(2580);
        ranking3.setEfficiency(BigDecimal.valueOf(92.3));
        ranking3.setQualityRate(BigDecimal.valueOf(98.5));
        ranking3.setOnTimeRate(BigDecimal.valueOf(96.8));
        ranking3.setTrend("up");
        ranking3.setTrendValue(BigDecimal.valueOf(2));
        ranking3.setWorkerCount(42);
        ranking3.setWorkstationCount(50);
        rankings.add(ranking3);

        // 根据排序方式排序
        rankings = sortWorkshopRankings(rankings, sortBy);
        
        // 设置排名
        setRankings(rankings);
        
        response.setRankings(rankings);
        return response;
    }

    @Override
    @Cacheable(value = "todayStats", key = "#workerId")
    public Map<String, Object> getTodayStats(Long workerId) {
        Map<String, Object> stats = new HashMap<>();
        
        if (workerId != null) {
            // 工人个人统计
            stats.put("totalPieces", 285);
            stats.put("totalBundles", 3);
            stats.put("totalAmount", "342.00");
            stats.put("qualityRate", 98.2);
        } else {
            // 全厂统计
            stats.put("totalProduction", 15680);
            stats.put("activeWorkers", 156);
            stats.put("completedOrders", 23);
            stats.put("avgEfficiency", 87.5);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getRealTimeStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 实时生产数据
        stats.put("currentProduction", 1250);
        stats.put("todayTarget", 1500);
        stats.put("completionRate", 83.3);
        stats.put("activeWorkstations", 45);
        stats.put("totalWorkstations", 52);
        stats.put("avgEfficiency", 87.5);
        stats.put("qualityRate", 96.8);
        stats.put("timestamp", DateUtil.now());
        
        return stats;
    }

    @Override
    public byte[] exportProductionReport(ProductionStatsRequest request) {
        // TODO: 使用POI生成Excel报表
        log.info("导出生产报表，维度：{}，时间范围：{} - {}", 
            request.getDimension(), request.getStartDate(), request.getEndDate());
        
        // 这里应该生成实际的Excel文件
        return "生产统计报表内容".getBytes();
    }

    @Override
    @CacheEvict(value = {"productionStats", "workshopRanking", "todayStats"}, allEntries = true)
    public void refreshStatsCache() {
        log.info("刷新统计缓存");
    }

    /**
     * 计算核心指标
     */
    private ProductionStatsResponse.CoreMetrics calculateCoreMetrics(ProductionStatsRequest request) {
        ProductionStatsResponse.CoreMetrics metrics = new ProductionStatsResponse.CoreMetrics();
        
        // 模拟数据计算
        metrics.setTotalProduction(156800);
        metrics.setProductionGrowthRate(BigDecimal.valueOf(12.5));
        metrics.setAvgQualityRate(BigDecimal.valueOf(96.8));
        metrics.setQualityGrowthRate(BigDecimal.valueOf(2.3));
        metrics.setAvgEfficiency(BigDecimal.valueOf(82.5));
        metrics.setEfficiencyGrowthRate(BigDecimal.valueOf(5.2));
        metrics.setRepairRate(BigDecimal.valueOf(3.2));
        metrics.setRepairRateChange(BigDecimal.valueOf(-1.5));
        
        return metrics;
    }

    /**
     * 计算趋势数据
     */
    private List<ProductionStatsResponse.TrendData> calculateTrendData(ProductionStatsRequest request) {
        List<ProductionStatsResponse.TrendData> trendData = new ArrayList<>();
        
        // 根据维度生成不同的趋势数据
        LocalDate current = request.getStartDate();
        while (!current.isAfter(request.getEndDate())) {
            ProductionStatsResponse.TrendData data = new ProductionStatsResponse.TrendData();
            data.setDate(current.toString());
            
            // 模拟波动数据
            Random random = new Random(current.toEpochDay());
            int baseProduction = 500;
            int production = baseProduction + random.nextInt(200);
            int defect = random.nextInt(20);
            
            data.setProduction(production);
            data.setQualifiedQuantity(production - defect);
            data.setDefectQuantity(defect);
            data.setQualityRate(BigDecimal.valueOf((double)(production - defect) / production * 100)
                .setScale(1, RoundingMode.HALF_UP));
            data.setEfficiency(BigDecimal.valueOf(80 + random.nextInt(20))
                .setScale(1, RoundingMode.HALF_UP));
            
            trendData.add(data);
            
            // 根据维度增加日期
            current = incrementDate(current, request.getDimension());
        }
        
        return trendData;
    }

    /**
     * 计算车间分布
     */
    private List<ProductionStatsResponse.WorkshopDistribution> calculateWorkshopDistribution(ProductionStatsRequest request) {
        List<ProductionStatsResponse.WorkshopDistribution> distributions = new ArrayList<>();
        
        // 模拟车间数据
        String[] workshopNames = {"一车间", "二车间", "三车间", "四车间"};
        int[] productions = {45200, 38600, 52800, 32400};
        int totalProduction = Arrays.stream(productions).sum();
        
        for (int i = 0; i < workshopNames.length; i++) {
            ProductionStatsResponse.WorkshopDistribution distribution = 
                new ProductionStatsResponse.WorkshopDistribution();
            distribution.setWorkshopId((long)(i + 1));
            distribution.setWorkshopName(workshopNames[i]);
            distribution.setProduction(productions[i]);
            distribution.setPercentage(BigDecimal.valueOf((double)productions[i] / totalProduction * 100)
                .setScale(1, RoundingMode.HALF_UP));
            distribution.setRank(i + 1);
            distributions.add(distribution);
        }
        
        // 按产量排序
        distributions.sort((a, b) -> b.getProduction().compareTo(a.getProduction()));
        
        // 重新设置排名
        for (int i = 0; i < distributions.size(); i++) {
            distributions.get(i).setRank(i + 1);
        }
        
        return distributions;
    }

    /**
     * 计算款式排行
     */
    private List<ProductionStatsResponse.StyleRanking> calculateStyleRanking(ProductionStatsRequest request) {
        List<ProductionStatsResponse.StyleRanking> rankings = new ArrayList<>();
        
        // 模拟款式数据
        String[] styleCodes = {"ST001", "ST002", "ST003", "ST004", "ST005"};
        String[] styleNames = {"经典T恤", "休闲裤", "连衣裙", "衬衫", "外套"};
        int[] productions = {15800, 12600, 10200, 8900, 6500};
        int totalProduction = Arrays.stream(productions).sum();
        
        for (int i = 0; i < styleCodes.length; i++) {
            ProductionStatsResponse.StyleRanking ranking = new ProductionStatsResponse.StyleRanking();
            ranking.setStyleId((long)(i + 1));
            ranking.setStyleCode(styleCodes[i]);
            ranking.setStyleName(styleNames[i]);
            ranking.setProduction(productions[i]);
            ranking.setPercentage(BigDecimal.valueOf((double)productions[i] / totalProduction * 100)
                .setScale(1, RoundingMode.HALF_UP));
            ranking.setRank(i + 1);
            rankings.add(ranking);
        }
        
        return rankings;
    }

    /**
     * 计算工序完成情况
     */
    private List<ProductionStatsResponse.ProcessCompletion> calculateProcessCompletion(ProductionStatsRequest request) {
        List<ProductionStatsResponse.ProcessCompletion> completions = new ArrayList<>();
        
        // 模拟工序数据
        String[] processNames = {"裁剪", "缝合", "锁边", "熨烫", "包装"};
        int[] completed = {15600, 14800, 14200, 13500, 12800};
        int[] inProgress = {400, 800, 1400, 2100, 2800};
        
        for (int i = 0; i < processNames.length; i++) {
            ProductionStatsResponse.ProcessCompletion completion = 
                new ProductionStatsResponse.ProcessCompletion();
            completion.setProcessId((long)(i + 1));
            completion.setProcessName(processNames[i]);
            completion.setCompletedQuantity(completed[i]);
            completion.setInProgressQuantity(inProgress[i]);
            
            int total = completed[i] + inProgress[i];
            completion.setCompletionRate(BigDecimal.valueOf((double)completed[i] / total * 100)
                .setScale(1, RoundingMode.HALF_UP));
            
            // 模拟平均用时
            completion.setAvgDuration(BigDecimal.valueOf(15 + i * 5));
            
            completions.add(completion);
        }
        
        return completions;
    }

    /**
     * 计算日期范围
     */
    private LocalDate[] calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        LocalDate start, end;
        LocalDate today = LocalDate.now();
        
        switch (period) {
            case "today":
                start = end = today;
                break;
            case "yesterday":
                start = end = today.minusDays(1);
                break;
            case "week":
                start = today.minusDays(today.getDayOfWeek().getValue() - 1);
                end = today;
                break;
            case "month":
                start = today.withDayOfMonth(1);
                end = today;
                break;
            case "custom":
                start = startDate != null ? startDate : today.minusDays(7);
                end = endDate != null ? endDate : today;
                break;
            default:
                start = today;
                end = today;
        }
        
        return new LocalDate[]{start, end};
    }

    /**
     * 排序车间排名
     */
    private List<WorkshopRankingResponse.WorkshopRanking> sortWorkshopRankings(
            List<WorkshopRankingResponse.WorkshopRanking> rankings, String sortBy) {
        
        switch (sortBy) {
            case "production":
                rankings.sort((a, b) -> b.getProduction().compareTo(a.getProduction()));
                break;
            case "efficiency":
                rankings.sort((a, b) -> b.getEfficiency().compareTo(a.getEfficiency()));
                break;
            case "quality":
                rankings.sort((a, b) -> b.getQualityRate().compareTo(a.getQualityRate()));
                break;
            case "overall":
            default:
                rankings.sort((a, b) -> b.getOverallScore().compareTo(a.getOverallScore()));
                break;
        }
        
        return rankings;
    }

    /**
     * 设置排名
     */
    private void setRankings(List<WorkshopRankingResponse.WorkshopRanking> rankings) {
        // 综合排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setOverallRank(i + 1);
        }
        
        // 产量排名
        List<WorkshopRankingResponse.WorkshopRanking> productionRanking = rankings.stream()
            .sorted((a, b) -> b.getProduction().compareTo(a.getProduction()))
            .collect(Collectors.toList());
        for (int i = 0; i < productionRanking.size(); i++) {
            productionRanking.get(i).setProductionRank(i + 1);
        }
        
        // 效率排名
        List<WorkshopRankingResponse.WorkshopRanking> efficiencyRanking = rankings.stream()
            .sorted((a, b) -> b.getEfficiency().compareTo(a.getEfficiency()))
            .collect(Collectors.toList());
        for (int i = 0; i < efficiencyRanking.size(); i++) {
            efficiencyRanking.get(i).setEfficiencyRank(i + 1);
        }
        
        // 质量排名
        List<WorkshopRankingResponse.WorkshopRanking> qualityRanking = rankings.stream()
            .sorted((a, b) -> b.getQualityRate().compareTo(a.getQualityRate()))
            .collect(Collectors.toList());
        for (int i = 0; i < qualityRanking.size(); i++) {
            qualityRanking.get(i).setQualityRank(i + 1);
        }
        
        // 准时率排名
        List<WorkshopRankingResponse.WorkshopRanking> onTimeRanking = rankings.stream()
            .sorted((a, b) -> b.getOnTimeRate().compareTo(a.getOnTimeRate()))
            .collect(Collectors.toList());
        for (int i = 0; i < onTimeRanking.size(); i++) {
            onTimeRanking.get(i).setOnTimeRank(i + 1);
        }
    }

    /**
     * 根据维度增加日期
     */
    private LocalDate incrementDate(LocalDate date, String dimension) {
        switch (dimension) {
            case "day":
                return date.plusDays(1);
            case "week":
                return date.plusWeeks(1);
            case "month":
                return date.plusMonths(1);
            case "quarter":
                return date.plusMonths(3);
            case "year":
                return date.plusYears(1);
            default:
                return date.plusDays(1);
        }
    }
}




