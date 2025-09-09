package com.garment.admin.dto.subscription;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建套餐请求DTO
 */
@Data
public class CreatePlanRequest {

    /**
     * 套餐代码
     */
    @NotBlank(message = "套餐代码不能为空")
    private String planCode;

    /**
     * 套餐名称
     */
    @NotBlank(message = "套餐名称不能为空")
    private String planName;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 套餐类型
     */
    @NotBlank(message = "套餐类型不能为空")
    private String planType;

    /**
     * 月付价格
     */
    @NotNull(message = "月付价格不能为空")
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
    private Integer trialDays = 0;

    /**
     * 用户数量限制
     */
    @NotNull(message = "用户数量限制不能为空")
    @Min(value = 1, message = "用户数量限制至少为1")
    private Integer userLimit;

    /**
     * 车间数量限制
     */
    @NotNull(message = "车间数量限制不能为空")
    @Min(value = 1, message = "车间数量限制至少为1")
    private Integer workshopLimit;

    /**
     * 存储空间限制（MB）
     */
    @NotNull(message = "存储空间限制不能为空")
    @Min(value = 100, message = "存储空间限制至少为100MB")
    private Long storageLimit;

    /**
     * 功能权限列表
     */
    private List<String> features;

    /**
     * 是否推荐
     */
    private Boolean isRecommended = false;

    /**
     * 排序权重
     */
    private Integer sortOrder = 0;
}




