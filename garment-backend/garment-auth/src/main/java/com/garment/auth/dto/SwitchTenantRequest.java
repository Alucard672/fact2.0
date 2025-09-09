package com.garment.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 切换租户请求DTO
 *
 * @author garment
 */
@Data
public class SwitchTenantRequest {

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    /**
     * 租户编码（可选，用于验证）
     */
    private String tenantCode;
}