package com.garment.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请记录实体
 *
 * @author garment
 */
@Data
@TableName("invitations")
public class Invitation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人手机号
     */
    private String phone;

    /**
     * 被邀请人邮箱
     */
    private String email;

    /**
     * 被邀请人姓名
     */
    private String name;

    /**
     * 邀请角色
     */
    private String role;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

    /**
     * 车间ID
     */
    private Long workshopId;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 邀请消息
     */
    private String message;

    /**
     * 状态：pending-待处理, accepted-已接受, rejected-已拒绝, expired-已过期
     */
    private String status;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 接受时间
     */
    private LocalDateTime acceptedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;
}