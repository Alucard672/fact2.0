package com.garment.auth.dto;

import lombok.Data;

import java.util.List;

/**
 * 租户功能限制DTO
 *
 * @author garment
 */
@Data
public class TenantLimits {

    /**
     * 用户数量限制
     */
    private Integer maxUsers;

    /**
     * 存储空间限制（字节）
     */
    private Long maxStorage;

    /**
     * 当前用户数量
     */
    private Integer currentUserCount;

    /**
     * 当前存储使用量（字节）
     */
    private Long currentStorageUsage;

    /**
     * 功能限制列表
     */
    private List<FeatureLimit> featureLimits;

    /**
     * 订阅套餐信息
     */
    private SubscriptionInfo subscriptionInfo;

    @Data
    public static class FeatureLimit {
        /**
         * 功能名称
         */
        private String featureName;

        /**
         * 功能代码
         */
        private String featureCode;

        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 限制值（如最大数量等）
         */
        private Integer limitValue;

        /**
         * 描述
         */
        private String description;
    }

    @Data
    public static class SubscriptionInfo {
        /**
         * 套餐类型
         */
        private String planType;

        /**
         * 套餐名称
         */
        private String planName;

        /**
         * 开始日期
         */
        private String startDate;

        /**
         * 结束日期
         */
        private String endDate;

        /**
         * 套餐状态
         */
        private String status;
    }
}