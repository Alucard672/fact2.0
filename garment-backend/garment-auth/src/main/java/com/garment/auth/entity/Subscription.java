package com.garment.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅记录实体
 *
 * @author garment
 */
@Data
@TableName("subscriptions")
public class Subscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 套餐类型
     */
    private String planType;

    /**
     * 订阅时长（月）
     */
    private Integer duration;

    /**
     * 原价
     */
    private BigDecimal originalAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 最终金额
     */
    private BigDecimal finalAmount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付状态：pending-待支付, paid-已支付, cancelled-已取消, refunded-已退款
     */
    private String paymentStatus;

    /**
     * 支付订单号
     */
    private String paymentOrderNo;

    /**
     * 优惠券代码
     */
    private String couponCode;

    /**
     * 订阅状态：active-生效中, expired-已过期, cancelled-已取消
     */
    private String status;

    /**
     * 开始日期
     */
    private LocalDateTime startDate;

    /**
     * 结束日期
     */
    private LocalDateTime endDate;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;
}