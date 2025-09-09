package com.garment.admin.dto.subscription;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 创建订阅请求DTO
 */
@Data
public class CreateSubscriptionRequest {

    /**
     * 租户ID
     */
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    /**
     * 套餐ID
     */
    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    /**
     * 订阅月数
     */
    @NotNull(message = "订阅月数不能为空")
    @Min(value = 1, message = "订阅月数至少为1个月")
    private Integer months;

    /**
     * 是否自动续费
     */
    private Boolean autoRenew = false;

    /**
     * 开始时间（可选，默认为当前时间）
     */
    private LocalDateTime startTime;

    /**
     * 备注
     */
    private String remark;
}




