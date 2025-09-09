package com.garment.auth.dto;

import lombok.Data;

/**
 * 微信用户信息DTO
 *
 * @author garment
 */
@Data
public class WechatUserInfo {

    /**
     * 微信OpenID
     */
    private String openid;
    
    /**
     * 微信UnionID
     */
    private String unionid;
    
    /**
     * 会话密钥
     */
    private String sessionKey;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 用户性别 0-未知 1-男 2-女
     */
    private Integer gender;
    
    /**
     * 用户所在国家
     */
    private String country;
    
    /**
     * 用户所在省份
     */
    private String province;
    
    /**
     * 用户所在城市
     */
    private String city;
    
    /**
     * 用户语言
     */
    private String language;
}