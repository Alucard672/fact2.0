package com.garment.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 短信服务
 *
 * @author garment
 */
@Slf4j
@Service
public class SmsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 发送验证码
     * 这里是模拟实现，实际项目中需要对接真实的短信服务商
     */
    public boolean sendVerifyCode(String phone, String code) {
        try {
            // TODO: 对接真实的短信服务商API
            // 比如阿里云短信服务、腾讯云短信等
            
            log.info("模拟发送短信验证码 - 手机号: {}, 验证码: {}", phone, code);
            
            // 模拟发送成功
            return true;
        } catch (Exception e) {
            log.error("发送短信验证码失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送邀请短信
     */
    public boolean sendInvitationSms(String phone, String message) {
        try {
            // TODO: 对接真实的短信服务商API
            
            log.info("模拟发送邀请短信 - 手机号: {}, 内容: {}", phone, message);
            
            // 模拟发送成功
            return true;
        } catch (Exception e) {
            log.error("发送邀请短信失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String inputCode) {
        try {
            String cacheKey = "sms:code:" + phone;
            String cachedCode = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedCode == null) {
                log.warn("验证码已过期或不存在: {}", phone);
                return false;
            }
            
            boolean valid = cachedCode.equals(inputCode);
            
            if (valid) {
                // 验证成功后删除验证码
                redisTemplate.delete(cacheKey);
                log.info("验证码验证成功: {}", phone);
            } else {
                log.warn("验证码错误: {} - 输入: {}, 期望: {}", phone, inputCode, cachedCode);
            }
            
            return valid;
        } catch (Exception e) {
            log.error("验证验证码失败: {}", e.getMessage());
            return false;
        }
    }
}