package com.garment.auth.filter;

import com.garment.common.context.TenantContext;
import com.garment.common.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 *
 * @author garment
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 获取请求头中的Authorization
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenUtil.resolveToken(authHeader);
        
        try {
            // 验证token
            if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 黑名单校验：若 token 在黑名单中，直接跳过认证，由后续处理为未授权
                try {
                    String blacklistKey = "jwt:blacklist:" + token;
                    if (stringRedisTemplate != null && Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey))) {
                        log.info("检测到黑名单令牌，拒绝认证");
                        TenantContext.clear();
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }
                } catch (Exception ex) {
                    // 黑名单检查失败不应影响正常认证流程，仅记录日志
                    log.warn("黑名单校验异常: {}", ex.getMessage());
                }
                
                // 从token中获取用户信息
                String username = jwtTokenUtil.getUsernameFromToken(token);
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                Long tenantId = jwtTokenUtil.getTenantIdFromToken(token);
                String role = jwtTokenUtil.getRoleFromToken(token);
                
                // 验证token是否有效
                if (StringUtils.hasText(username) && !jwtTokenUtil.isTokenExpired(token)) {
                    
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 验证token
                    if (jwtTokenUtil.validateToken(token, userDetails.getUsername())) {
                        
                        // 设置租户上下文
                        TenantContext.setCurrentUserId(userId);
                        TenantContext.setCurrentUsername(username);
                        if (tenantId != null) {
                            TenantContext.setCurrentTenantId(tenantId);
                        }
                        if (StringUtils.hasText(role)) {
                            TenantContext.setCurrentUserRole(role);
                        }
                        
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authToken = 
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, 
                                        null, 
                                        userDetails.getAuthorities()
                                );
                        
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置认证信息到Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        log.debug("用户 {} 通过JWT认证成功，租户ID: {}", username, tenantId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            // 清理上下文
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理租户上下文
            TenantContext.clear();
        }
    }

    /**
     * 跳过JWT认证的路径
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 跳过认证的路径
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/public/") ||
               path.equals("/api/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/swagger-resources/") ||
               path.startsWith("/v2/api-docs") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/druid/");
    }
}