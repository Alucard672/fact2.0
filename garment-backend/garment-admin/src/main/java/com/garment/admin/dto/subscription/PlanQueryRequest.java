package com.garment.admin.dto.subscription;

import lombok.Data;

/**
 * 套餐查询请求DTO
 */
@Data
public class PlanQueryRequest {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;

    /**
     * 套餐代码
     */
    private String planCode;

    /**
     * 套餐名称
     */
    private String planName;

    /**
     * 套餐类型
     */
    private String planType;

    /**
     * 状态
     */
    private String status;

    /**
     * 是否推荐
     */
    private Boolean isRecommended;
}




