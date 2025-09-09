package com.garment.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录结果
 *
 * @author garment
 */
@Data
@ApiModel("登录结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResult {

    @ApiModelProperty("访问令牌")
    private String accessToken;

    @ApiModelProperty("刷新令牌")
    private String refreshToken;

    @ApiModelProperty("令牌类型")
    private String tokenType = "Bearer";

    @ApiModelProperty("过期时间(秒)")
    private Long expiresIn;

    @ApiModelProperty("用户信息")
    private UserInfo user;

    @ApiModelProperty("租户信息")
    private TenantInfo tenant;

    /**
     * 用户信息
     */
    @Data
    @ApiModel("用户信息")
    public static class UserInfo {
        @ApiModelProperty("用户ID")
        private Long id;

        @ApiModelProperty("用户名")
        private String username;

        @ApiModelProperty("手机号")
        private String phone;

        @ApiModelProperty("姓名")
        private String name;

        @ApiModelProperty("头像")
        private String avatar;

        @ApiModelProperty("当前租户ID")
        private Long currentTenantId;

        @ApiModelProperty("角色")
        private String role;

        @ApiModelProperty("权限列表")
        private List<String> permissions;

        @ApiModelProperty("最后登录时间")
        private LocalDateTime lastLoginTime;
    }

    /**
     * 租户信息
     */
    @Data
    @ApiModel("租户信息")
    public static class TenantInfo {
        @ApiModelProperty("租户ID")
        private Long id;

        @ApiModelProperty("租户编码")
        private String tenantCode;

        @ApiModelProperty("企业名称")
        private String companyName;

        @ApiModelProperty("企业类型")
        private String companyType;

        @ApiModelProperty("订阅套餐")
        private String subscriptionPlan;

        @ApiModelProperty("状态")
        private String status;
    }
}