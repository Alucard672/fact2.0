package com.garment.auth.service;

import com.garment.auth.entity.User;
import com.garment.auth.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

/**
 * 用户详情服务实现
 *
 * @author garment
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户详情: {}", username);

        // 根据用户名或手机号查询用户
        User user = null;
        if (username.matches("^1[3-9]\\d{9}$")) {
            // 手机号登录
            user = userMapper.findByPhone(username);
        } else {
            // 用户名登录
            user = userMapper.findByUsername(username);
        }

        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (!"active".equals(user.getStatus())) {
            log.warn("用户已被禁用: {}", username);
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 构建UserDetails对象
        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getUsername());
        
        // 设置密码（如果没有密码则设置为空，用于手机号验证码登录）
        builder.password(user.getPassword() != null ? user.getPassword() : "");
        
        // 设置账号状态
        builder.disabled(!"active".equals(user.getStatus()));
        builder.accountExpired(false);
        builder.credentialsExpired(false);
        builder.accountLocked("locked".equals(user.getStatus()));

        // 设置权限（这里先设置基础权限，具体权限在租户上下文中处理）
        if (user.getIsSystemAdmin() != null && user.getIsSystemAdmin()) {
            builder.authorities("ROLE_SYSTEM_ADMIN");
        } else {
            builder.authorities("ROLE_USER");
        }

        UserDetails userDetails = builder.build();
        log.debug("用户详情加载完成: {}", username);
        
        return userDetails;
    }
}