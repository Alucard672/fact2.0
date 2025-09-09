package com.garment.auth.dto;

import lombok.Data;

/**
 * 微信授权响应DTO
 *
 * @author garment
 */
@Data
public class WechatAuthResponse {

    /**
     * 用户的唯一标识
     */
    private String openid;
    
    /**
     * 会话密钥
     */
    private String session_key;
    
    /**
     * 用户在开放平台的唯一标识符
     */
    private String unionid;
    
    /**
     * 错误码
     */
    private Integer errcode;
    
    /**
     * 错误信息
     */
    private String errmsg;

    // Getter methods
    public String getOpenid() { return openid; }
    public String getSession_key() { return session_key; }
    public String getUnionid() { return unionid; }
    public Integer getErrcode() { return errcode; }
    public String getErrmsg() { return errmsg; }
}