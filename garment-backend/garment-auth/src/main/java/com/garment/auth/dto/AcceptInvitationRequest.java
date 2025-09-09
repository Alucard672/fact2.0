package com.garment.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 接受邀请请求DTO
 *
 * @author garment
 */
@Data
public class AcceptInvitationRequest {

    @NotBlank(message = "邀请码不能为空")
    private String invitationCode;

    /**
     * 用户信息（如果是新用户）
     */
    private String username;
    
    private String password;
    
    private String name;
    
    /**
     * 微信信息（如果通过微信注册）
     */
    private String wechatCode;
}