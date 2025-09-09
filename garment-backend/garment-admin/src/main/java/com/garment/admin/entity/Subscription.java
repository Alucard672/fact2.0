package com.garment.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户订阅实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("subscriptions")
public class Subscription {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 套餐ID
     */
    @TableField("plan_id")
    private Long planId;

    /**
     * 套餐名称
     */
    @TableField("plan_name")
    private String planName;

    /**
     * 套餐价格
     */
    @TableField("plan_price")
    private BigDecimal planPrice;

    /**
     * 订阅状态：active-有效，expired-过期，cancelled-已取消，suspended-暂停
     */
    @TableField("status")
    private String status;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 自动续费：0-否，1-是
     */
    @TableField("auto_renew")
    private Boolean autoRenew;

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
     * 备注
     */
    @TableField("remark")
    private String remark;

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




