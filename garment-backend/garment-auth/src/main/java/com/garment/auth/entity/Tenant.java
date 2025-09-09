package com.garment.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户实体
 *
 * @author garment
 */
@Data
@TableName("tenants")
public class Tenant {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 企业类型
     */
    private String companyType;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 企业地址
     */
    private String address;

    /**
     * 营业执照号
     */
    private String businessLicense;

    /**
     * 税号
     */
    private String taxNumber;

    /**
     * 企业Logo
     */
    private String logoUrl;

    /**
     * 订阅套餐
     */
    private String subscriptionPlan;

    /**
     * 订阅开始日期
     */
    private LocalDate subscriptionStart;

    /**
     * 订阅结束日期
     */
    private LocalDate subscriptionEnd;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大存储空间(字节)
     */
    private Long maxStorage;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建人ID
     */
    private Long createdBy;

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