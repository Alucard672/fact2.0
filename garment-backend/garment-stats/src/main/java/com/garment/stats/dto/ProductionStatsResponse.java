package com.garment.stats.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 生产统计响应
 *
 * @author system
 */
@Data
public class ProductionStatsResponse {

    /**
     * 核心指标
     */
    private CoreMetrics metrics;

    /**
     * 趋势数据
     */
    private List<TrendData> trendData;

    /**
     * 车间分布
     */
    private List<WorkshopDistribution> workshopDistribution;

    /**
     * 款式排行
     */
    private List<StyleRanking> styleRanking;

    /**
     * 工序完成情况
     */
    private List<ProcessCompletion> processCompletion;

    /**
     * 核心指标
     */
    @Data
    public static class CoreMetrics {
        /**
         * 总产量
         */
        private Integer totalProduction;

        /**
         * 产量环比增长率
         */
        private BigDecimal productionGrowthRate;

        /**
         * 平均合格率
         */
        private BigDecimal avgQualityRate;

        /**
         * 质量环比增长率
         */
        private BigDecimal qualityGrowthRate;

        /**
         * 平均效率
         */
        private BigDecimal avgEfficiency;

        /**
         * 效率环比增长率
         */
        private BigDecimal efficiencyGrowthRate;

        /**
         * 返修率
         */
        private BigDecimal repairRate;

        /**
         * 返修率环比变化
         */
        private BigDecimal repairRateChange;
    }

    /**
     * 趋势数据
     */
    @Data
    public static class TrendData {
        /**
         * 日期
         */
        private String date;

        /**
         * 产量
         */
        private Integer production;

        /**
         * 合格数量
         */
        private Integer qualifiedQuantity;

        /**
         * 次品数量
         */
        private Integer defectQuantity;

        /**
         * 合格率
         */
        private BigDecimal qualityRate;

        /**
         * 效率
         */
        private BigDecimal efficiency;
    }

    /**
     * 车间分布
     */
    @Data
    public static class WorkshopDistribution {
        /**
         * 车间ID
         */
        private Long workshopId;

        /**
         * 车间名称
         */
        private String workshopName;

        /**
         * 产量
         */
        private Integer production;

        /**
         * 占比
         */
        private BigDecimal percentage;

        /**
         * 排名
         */
        private Integer rank;
    }

    /**
     * 款式排行
     */
    @Data
    public static class StyleRanking {
        /**
         * 款式ID
         */
        private Long styleId;

        /**
         * 款式编码
         */
        private String styleCode;

        /**
         * 款式名称
         */
        private String styleName;

        /**
         * 产量
         */
        private Integer production;

        /**
         * 占比
         */
        private BigDecimal percentage;

        /**
         * 排名
         */
        private Integer rank;
    }

    /**
     * 工序完成情况
     */
    @Data
    public static class ProcessCompletion {
        /**
         * 工序ID
         */
        private Long processId;

        /**
         * 工序名称
         */
        private String processName;

        /**
         * 完成数量
         */
        private Integer completedQuantity;

        /**
         * 在制数量
         */
        private Integer inProgressQuantity;

        /**
         * 完成率
         */
        private BigDecimal completionRate;

        /**
         * 平均用时（分钟）
         */
        private BigDecimal avgDuration;
    }
}




