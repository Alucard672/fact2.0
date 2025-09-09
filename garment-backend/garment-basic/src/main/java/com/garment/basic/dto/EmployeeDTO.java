package com.garment.basic.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 员工DTO
 *
 * @author garment
 */
@Data
public class EmployeeDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 角色
     */
    @NotBlank(message = "角色不能为空")
    private String role;

    /**
     * 员工工号
     */
    @NotBlank(message = "员工工号不能为空")
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
     * 权限配置 (JSON格式)
     */
    private String permissions;

    /**
     * 状态
     */
    private String status = "active";

    /**
     * 邀请人ID
     */
    private Long invitedBy;

    /**
     * 加入时间
     */
    private String joinedAt;
}