package com.garment.admin.dto.subscription;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订阅查询请求DTO
 */
@Data
public class SubscriptionQueryRequest {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 套餐ID
     */
    private Long planId;

    /**
     * 订阅状态
     */
    private String status;

    /**
     * 开始时间（查询范围起始）
     */
    private LocalDateTime startTimeFrom;

    /**
     * 开始时间（查询范围结束）
     */
    private LocalDateTime startTimeTo;

    /**
     * 结束时间（查询范围起始）
     */
    private LocalDateTime endTimeFrom;

    /**
     * 结束时间（查询范围结束）
     */
    private LocalDateTime endTimeTo;

    /**
     * 是否自动续费
     */
    private Boolean autoRenew;
}




