package com.garment.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅套餐实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("subscription_plans")
public class SubscriptionPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 套餐代码
     */
    @TableField("plan_code")
    private String planCode;

    /**
     * 套餐名称
     */
    @TableField("plan_name")
    private String planName;

    /**
     * 套餐描述
     */
    @TableField("description")
    private String description;

    /**
     * 套餐类型：trial-试用版，basic-基础版，standard-标准版，premium-高级版，enterprise-企业版
     */
    @TableField("plan_type")
    private String planType;

    /**
     * 月付价格
     */
    @TableField("monthly_price")
    private BigDecimal monthlyPrice;

    /**
     * 年付价格
     */
    @TableField("yearly_price")
    private BigDecimal yearlyPrice;

    /**
     * 试用天数
     */
    @TableField("trial_days")
    private Integer trialDays;

    /**
     * 用户数量限制
     */
    @TableField("user_limit")
    private Integer userLimit;

    /**
     * 车间数量限制
     */
    @TableField("workshop_limit")
    private Integer workshopLimit;

    /**
     * 存储空间限制（MB）
     */
    @TableField("storage_limit")
    private Long storageLimit;

    /**
     * 功能权限（JSON格式存储）
     */
    @TableField("features")
    private String features;

    /**
     * 是否推荐：0-否，1-是
     */
    @TableField("is_recommended")
    private Boolean isRecommended;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：active-启用，inactive-停用
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;
}




