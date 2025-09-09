package com.garment.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 *
 * @author garment
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatProperties {

    /**
     * 小程序AppID
     */
    private String appId;
    
    /**
     * 小程序AppSecret
     */
    private String appSecret;
    
    /**
     * 微信授权URL
     */
    private String authUrl = "https://api.weixin.qq.com/sns/jscode2session";
    
    /**
     * 获取用户信息URL
     */
    private String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo";
    
    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 5000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout = 5000;

    // Getter methods
    public String getAppId() { return appId; }
    public String getAppSecret() { return appSecret; }
    public String getAuthUrl() { return authUrl; }
}