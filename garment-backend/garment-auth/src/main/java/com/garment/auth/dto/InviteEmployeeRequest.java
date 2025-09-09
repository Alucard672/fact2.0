package com.garment.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 邀请员工请求DTO
 *
 * @author garment
 */
@Data
public class InviteEmployeeRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String email;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "角色不能为空")
    private String role; // admin, manager, supervisor, worker

    private String employeeNo;

    private String department;

    private String position;

    private Long workshopId;

    private String message;
}