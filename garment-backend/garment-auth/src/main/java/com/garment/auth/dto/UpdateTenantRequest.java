package com.garment.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新租户请求DTO
 *
 * @author garment
 */
@Data
public class UpdateTenantRequest {

    @NotBlank(message = "企业名称不能为空")
    private String companyName;

    private String companyType;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String contactEmail;

    private String address;

    private String businessLicense;

    private String taxNumber;

    private String logoUrl;
}