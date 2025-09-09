package com.garment.auth.dto;

import lombok.Data;

/**
 * 用户租户信息DTO
 *
 * @author garment
 */
@Data
public class UserTenantInfo {

    private Long tenantId;

    private String tenantCode;

    private String companyName;

    private String companyType;

    private String role;

    private String employeeNo;

    private String department;

    private String position;

    private String status;

    private String subscriptionPlan;

    private String tenantStatus;

    /**
     * 是否为当前租户
     */
    private Boolean isCurrent;
}