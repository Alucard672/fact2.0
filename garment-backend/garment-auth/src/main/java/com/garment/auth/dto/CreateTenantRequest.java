package com.garment.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 创建租户请求DTO
 *
 * @author garment
 */
@Data
public class CreateTenantRequest {

    @NotBlank(message = "企业名称不能为空")
    private String companyName;

    private String companyType = "factory"; // factory, workshop, trading

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    private String contactEmail;

    private String address;

    private String businessLicense;

    private String taxNumber;

    private String logoUrl;

    /**
     * 订阅套餐
     */
    private String subscriptionPlan = "trial"; // trial, basic, standard, premium

    /**
     * 初始员工信息
     */
    private String adminName;
    
    private String adminPhone;
    
    private String adminUsername;
    
    private String adminPassword;
}