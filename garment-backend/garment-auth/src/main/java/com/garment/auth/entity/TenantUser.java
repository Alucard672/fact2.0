package com.garment.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户用户关联实体
 *
 * @author garment
 */
@Data
@TableName("tenant_users")
public class TenantUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 租户内角色
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
     * 权限配置
     */
    private String permissions;

    /**
     * 状态
     */
    private String status;

    /**
     * 邀请人ID
     */
    private Long invitedBy;

    /**
     * 邀请时间
     */
    private LocalDateTime invitedAt;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;

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