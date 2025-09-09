package com.garment.common.utils;

import com.garment.common.constants.CommonConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author garment
 */
@Slf4j
@Component
public class JwtTokenUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    /**
     * 令牌秘钥
     */
    @Value("${jwt.secret:garment-production-jwt-secret-key-2024}")
    private String secret;

    /**
     * 令牌有效期（默认30分钟）
     */
    @Value("${jwt.expiration:1800}")
    private int expiration;

    /**
     * 刷新令牌有效期（默认7天）
     */
    @Value("${jwt.refresh-expiration:604800}")
    private int refreshExpiration;

    /**
     * 获取密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取签发时间
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.get(CommonConstants.JWT_USERID).toString());
    }

    /**
     * 从令牌中获取租户ID
     */
    public Long getTenantIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object tenantId = claims.get(CommonConstants.JWT_TENANT_ID);
        return tenantId != null ? Long.parseLong(tenantId.toString()) : null;
    }

    /**
     * 从令牌中获取用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("role");
    }

    /**
     * 从令牌中获取指定声明
     */
    public <T> T getClaimFromToken(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中获取所有声明
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析JWT令牌失败: {}", e.getMessage());
            throw new RuntimeException("无效的令牌");
        }
    }

    /**
     * 检查令牌是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username, Long tenantId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstants.JWT_USERID, userId);
        claims.put(CommonConstants.JWT_TENANT_ID, tenantId);
        claims.put("role", role);
        
        return generateToken(claims, username, expiration);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstants.JWT_USERID, userId);
        claims.put("type", "refresh");
        
        return generateToken(claims, username, refreshExpiration);
    }

    /**
     * 生成令牌
     */
    private String generateToken(Map<String, Object> claims, String subject, int expiration) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expiration * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证令牌
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String usernameFromToken = getUsernameFromToken(token);
            return (usernameFromToken.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("验证JWT令牌失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新令牌
     */
    public String refreshToken(String token) {
        try {
            final Claims claims = getClaimsFromToken(token);
            final String username = claims.getSubject();
            final Long userId = Long.parseLong(claims.get(CommonConstants.JWT_USERID).toString());
            final Long tenantId = claims.get(CommonConstants.JWT_TENANT_ID) != null ? 
                    Long.parseLong(claims.get(CommonConstants.JWT_TENANT_ID).toString()) : null;
            final String role = (String) claims.get("role");

            return generateAccessToken(userId, username, tenantId, role);
        } catch (Exception e) {
            log.error("刷新JWT令牌失败: {}", e.getMessage());
            throw new RuntimeException("刷新令牌失败");
        }
    }

    /**
     * 解析令牌头部
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 检查是否为刷新令牌
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
}