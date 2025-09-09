package com.garment.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请信息响应DTO
 *
 * @author garment
 */
@Data
public class InvitationResponse {

    private Long id;

    private Long tenantId;

    private String tenantName;

    private Long inviterId;

    private String inviterName;

    private String phone;

    private String email;

    private String name;

    private String role;

    private String employeeNo;

    private String department;

    private String position;

    private Long workshopId;

    private String workshopName;

    private String invitationCode;

    private String message;

    private String status;

    private LocalDateTime expiresAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime createdAt;

    /**
     * 邀请链接
     */
    private String invitationUrl;
}