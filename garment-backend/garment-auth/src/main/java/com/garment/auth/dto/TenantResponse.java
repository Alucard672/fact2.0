package com.garment.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户信息响应DTO
 *
 * @author garment
 */
@Data
public class TenantResponse {

    private Long id;

    private String tenantCode;

    private String companyName;

    private String companyType;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String businessLicense;

    private String taxNumber;

    private String logoUrl;

    private String subscriptionPlan;

    private String subscriptionStart;

    private String subscriptionEnd;

    private Integer maxUsers;

    private Long maxStorage;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 租户统计信息
     */
    private TenantStats stats;

    @Data
    public static class TenantStats {
        private Integer userCount;      // 用户数量
        private Integer activeUserCount; // 活跃用户数量
        private Long storageUsed;       // 已使用存储空间
        private Integer workshopCount;  // 车间数量
        private Integer styleCount;     // 款式数量
        private Integer processCount;   // 工序数量
    }
}