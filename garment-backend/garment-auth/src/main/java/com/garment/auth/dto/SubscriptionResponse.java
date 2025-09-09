package com.garment.auth.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅记录响应DTO
 *
 * @author garment
 */
@Data
public class SubscriptionResponse {

    private Long id;

    private Long tenantId;

    private String tenantName;

    private String planType;

    private String planName;

    private Integer duration;

    private BigDecimal originalAmount;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    private String paymentMethod;

    private String paymentStatus;

    private String paymentOrderNo;

    private String couponCode;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    /**
     * 套餐功能限制
     */
    private PlanLimits planLimits;

    @Data
    public static class PlanLimits {
        private Integer maxUsers;
        private Long maxStorage;
        private String features;
        private String supportLevel;
    }
}