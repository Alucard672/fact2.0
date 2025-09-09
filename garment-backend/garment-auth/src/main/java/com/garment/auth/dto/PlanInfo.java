package com.garment.auth.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐信息DTO
 *
 * @author garment
 */
@Data
public class PlanInfo {

    private String planType;

    private String planName;

    private String description;

    private BigDecimal monthlyPrice;

    private BigDecimal yearlyPrice;

    private Integer maxUsers;

    private Long maxStorage;

    private String storageUnit;

    private String supportLevel;

    private List<String> features;

    private List<String> limitations;

    private Boolean isPopular;

    private Boolean isRecommended;

    /**
     * 试用期（天）
     */
    private Integer trialDays;
}