package com.garment.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信手机号授权登录请求
 */
@Data
@ApiModel("微信手机号授权登录请求")
public class WechatPhoneLoginRequest {

    @ApiModelProperty(value = "wx.login 获取的 code", required = true)
    @NotBlank(message = "微信code不能为空")
    private String code;

    @ApiModelProperty(value = "getPhoneNumber 返回的加密数据，可选")
    private String encryptedData;

    @ApiModelProperty(value = "getPhoneNumber 返回的iv，可选")
    private String iv;

    @ApiModelProperty(value = "getPhoneNumber 返回的code(新版)，可选")
    private String phoneCode;
}