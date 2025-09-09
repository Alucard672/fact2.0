package com.garment.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求DTO
 *
 * @author garment
 */
@Data
public class WechatLoginRequest {

    @NotBlank(message = "微信授权码不能为空")
    private String code;

    /**
     * 用户信息（可选，用于首次注册）
     */
    private String nickname;
    
    private String avatar;
    
    private String gender;
    
    /**
     * 手机号（可选，用于绑定）
     */
    private String phone;
    
    /**
     * 手机验证码（绑定手机号时需要）
     */
    private String smsCode;
}