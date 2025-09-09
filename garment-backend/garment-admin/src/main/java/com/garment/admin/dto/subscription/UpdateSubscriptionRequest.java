package com.garment.admin.dto.subscription;

import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 更新订阅请求DTO
 */
@Data
public class UpdateSubscriptionRequest {

    /**
     * 是否自动续费
     */
    private Boolean autoRenew;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：active-有效，expired-过期，cancelled-已取消，suspended-暂停
     */
    private String status;
}




