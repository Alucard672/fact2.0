package com.garment.admin.dto.subscription;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

/**
 * 更新套餐请求DTO
 */
@Data
public class UpdatePlanRequest {

    /**
     * 套餐名称
     */
    private String planName;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 月付价格
     */
    @DecimalMin(value = "0", message = "月付价格不能小于0")
    private BigDecimal monthlyPrice;

    /**
     * 年付价格
     */
    @DecimalMin(value = "0", message = "年付价格不能小于0")
    private BigDecimal yearlyPrice;

    /**
     * 试用天数
     */
    @Min(value = 0, message = "试用天数不能小于0")
    private Integer trialDays;

    /**
     * 用户数量限制
     */
    @Min(value = 1, message = "用户数量限制至少为1")
    private Integer userLimit;

    /**
     * 车间数量限制
     */
    @Min(value = 1, message = "车间数量限制至少为1")
    private Integer workshopLimit;

    /**
     * 存储空间限制（MB）
     */
    @Min(value = 100, message = "存储空间限制至少为100MB")
    private Long storageLimit;

    /**
     * 功能权限列表
     */
    private List<String> features;

    /**
     * 是否推荐
     */
    private Boolean isRecommended;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;
}




