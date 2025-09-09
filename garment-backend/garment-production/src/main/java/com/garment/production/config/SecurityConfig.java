package com.garment.production.config;

import com.alibaba.fastjson.JSON;
import com.garment.common.context.TenantContext;
import com.garment.common.result.Result;
import com.garment.common.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Production 模块安全配置：接入基于 JWT 的无状态认证
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            log.warn("未认证访问: {} - {}", request.getRequestURI(), authException.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(JSON.toJSONString(Result.unauthorized("请先登录")));
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("无权限访问: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(JSON.toJSONString(Result.forbidden("权限不足")));
        };
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtTokenUtil);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // 公开接口白名单
                .antMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                ).permitAll()
                // GET 只读生产查询接口可匿名（便于小程序健康探测与演示）
                .antMatchers(org.springframework.http.HttpMethod.GET, "/api/production/**").permitAll()
                // 其他均需认证
                .anyRequest().authenticated()
            .and()
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            .and()
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 基于 JWT 的 OncePerRequestFilter
     */
    @Slf4j
    public static class JwtAuthFilter extends OncePerRequestFilter {
        private final JwtTokenUtil jwtTokenUtil;

        public JwtAuthFilter(JwtTokenUtil jwtTokenUtil) {
            this.jwtTokenUtil = jwtTokenUtil;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");
            String token = jwtTokenUtil.resolveToken(authHeader);

            try {
                if (StringUtils.hasText(token)) {
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    Long userId = jwtTokenUtil.getUserIdFromToken(token);
                    Long tenantId = jwtTokenUtil.getTenantIdFromToken(token);
                    String role = jwtTokenUtil.getRoleFromToken(token);

                    if (StringUtils.hasText(username) && !jwtTokenUtil.isTokenExpired(token)
                            && jwtTokenUtil.validateToken(token, username)) {
                        // 注入租户上下文
                        TenantContext.setCurrentUserId(userId);
                        TenantContext.setCurrentUsername(username);
                        if (tenantId != null) {
                            TenantContext.setCurrentTenantId(tenantId);
                        }
                        if (StringUtils.hasText(role)) {
                            TenantContext.setCurrentUserRole(role);
                        }

                        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        StringUtils.hasText(role)
                                                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                                : Collections.emptyList()
                                );
                        authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                log.error("JWT校验失败: {}", e.getMessage());
                TenantContext.clear();
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
            }

            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
        }
    }
}