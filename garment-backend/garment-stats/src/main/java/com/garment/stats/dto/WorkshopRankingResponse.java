package com.garment.stats.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 车间排名响应
 *
 * @author system
 */
@Data
public class WorkshopRankingResponse {

    /**
     * 排名列表
     */
    private List<WorkshopRanking> rankings;

    /**
     * 车间排名
     */
    @Data
    public static class WorkshopRanking {
        /**
         * 车间ID
         */
        private Long workshopId;

        /**
         * 车间名称
         */
        private String workshopName;

        /**
         * 车间主管
         */
        private String managerName;

        /**
         * 综合得分
         */
        private BigDecimal overallScore;

        /**
         * 综合排名
         */
        private Integer overallRank;

        /**
         * 产量
         */
        private Integer production;

        /**
         * 产量排名
         */
        private Integer productionRank;

        /**
         * 效率
         */
        private BigDecimal efficiency;

        /**
         * 效率排名
         */
        private Integer efficiencyRank;

        /**
         * 质量合格率
         */
        private BigDecimal qualityRate;

        /**
         * 质量排名
         */
        private Integer qualityRank;

        /**
         * 准时交货率
         */
        private BigDecimal onTimeRate;

        /**
         * 准时率排名
         */
        private Integer onTimeRank;

        /**
         * 趋势：up-上升，down-下降，stable-稳定
         */
        private String trend;

        /**
         * 趋势值
         */
        private BigDecimal trendValue;

        /**
         * 工人数量
         */
        private Integer workerCount;

        /**
         * 工位数量
         */
        private Integer workstationCount;
    }
}




