package com.garment.auth.service;

import com.alibaba.fastjson.JSON;
import com.garment.auth.config.WechatProperties;
import com.garment.auth.dto.WechatAuthResponse;
import com.garment.auth.dto.WechatUserInfo;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 微信服务
 *
 * @author garment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {
    
    private static final Logger log = LoggerFactory.getLogger(WechatService.class);

    private final WechatProperties wechatProperties;
    private final RestTemplate restTemplate;

    /**
     * 通过code获取用户信息
     */
    public WechatUserInfo getUserInfoByCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("微信授权码不能为空");
        }

        try {
            // 1. 通过code获取openid和session_key
            WechatAuthResponse authResponse = getAuthResponse(code);
            
            if (authResponse.getErrcode() != null && authResponse.getErrcode() != 0) {
                log.error("微信授权失败: code={}, errcode={}, errmsg={}", 
                    code, authResponse.getErrcode(), authResponse.getErrmsg());
                throw new BusinessException("微信授权失败: " + authResponse.getErrmsg());
            }

            // 2. 构建用户信息
            WechatUserInfo userInfo = new WechatUserInfo();
            userInfo.setOpenid(authResponse.getOpenid());
            userInfo.setUnionid(authResponse.getUnionid());
            userInfo.setSessionKey(authResponse.getSession_key());

            log.info("微信授权成功: openid={}, unionid={}", 
                authResponse.getOpenid(), authResponse.getUnionid());
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("获取微信用户信息失败: code={}", code, e);
            throw new BusinessException("获取微信用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 解密微信用户信息
     */
    public WechatUserInfo decryptUserInfo(String encryptedData, String iv, String sessionKey) {
        try {
            // Base64解码
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);

            // AES解密
            SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKeyBytes, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);
            
            log.debug("解密后的用户信息: {}", decryptedData);
            
            // 解析JSON
            return JSON.parseObject(decryptedData, WechatUserInfo.class);
            
        } catch (Exception e) {
            log.error("解密微信用户信息失败", e);
            throw new BusinessException("解密用户信息失败");
        }
    }

    /**
     * 解密微信手机号，返回纯手机号字符串
     */
    public String decryptPhoneNumber(String encryptedData, String iv, String sessionKey) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);

            SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKeyBytes, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String json = new String(decryptedBytes, StandardCharsets.UTF_8);
            log.debug("解密后的手机号JSON: {}", json);
            // 微信返回形如 {"phoneNumber":"13800138000","purePhoneNumber":"13800138000",...}
            com.alibaba.fastjson.JSONObject obj = JSON.parseObject(json);
            String phone = obj.getString("purePhoneNumber");
            if (!StringUtils.hasText(phone)) phone = obj.getString("phoneNumber");
            if (!StringUtils.hasText(phone)) throw new BusinessException("解析手机号失败");
            return phone;
        } catch (Exception e) {
            log.error("解密微信手机号失败", e);
            throw new BusinessException("解密手机号失败");
        }
    }

    /**
     * 验证数据完整性
     */
    public boolean validateSignature(String rawData, String signature, String sessionKey) {
        try {
            // 计算签名
            String dataWithKey = rawData + sessionKey;
            // 这里需要使用SHA1算法计算摘要，简化处理
            return signature.equals(calculateSha1(dataWithKey));
        } catch (Exception e) {
            log.error("验证数据签名失败", e);
            return false;
        }
    }

    /**
     * 通过code调用微信接口获取授权信息
     */
    private WechatAuthResponse getAuthResponse(String code) {
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatProperties.getAuthUrl(),
                wechatProperties.getAppId(),
                wechatProperties.getAppSecret(),
                code);

        log.debug("请求微信授权接口: {}", url);
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String responseBody = response.getBody();
        
        log.debug("微信授权接口响应: {}", responseBody);
        
        return JSON.parseObject(responseBody, WechatAuthResponse.class);
    }

    /**
     * 计算SHA1摘要（简化实现）
     */
    private String calculateSha1(String data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算SHA1失败", e);
        }
    }
}