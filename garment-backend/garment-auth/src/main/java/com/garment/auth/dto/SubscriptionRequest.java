package com.garment.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 订阅套餐请求DTO
 *
 * @author garment
 */
@Data
public class SubscriptionRequest {

    @NotBlank(message = "套餐类型不能为空")
    private String planType; // trial, basic, standard, premium

    @NotNull(message = "订阅时长不能为空")
    @Min(value = 1, message = "订阅时长必须大于0")
    private Integer duration; // 订阅时长（月）

    /**
     * 支付方式
     */
    private String paymentMethod; // alipay, wechat, bank

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 优惠券代码
     */
    private String couponCode;

    /**
     * 备注
     */
    private String remark;
}