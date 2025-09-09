package com.garment.auth.service;

import com.garment.auth.dto.*;
import com.garment.auth.entity.Tenant;
import com.garment.auth.entity.TenantUser;
import com.garment.auth.entity.User;
import com.garment.auth.mapper.TenantMapper;
import com.garment.auth.mapper.TenantUserMapper;
import com.garment.auth.mapper.UserMapper;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import com.garment.common.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 *
 * @author garment
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private TenantUserMapper tenantUserMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SmsService smsService;

    @Autowired
    private WechatService wechatService;

    /**
     * 手机号登录
     */
    public LoginResult phoneLogin(PhoneLoginRequest request) {
        log.info("手机号登录: {}", request.getPhone());

        // 验证验证码
        if (!smsService.verifyCode(request.getPhone(), request.getVerifyCode())) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }

        // 查询用户
        User user = userMapper.findByPhone(request.getPhone());
        if (user == null) {
            throw BusinessException.notFound("用户不存在，请先注册");
        }

        if (!"active".equals(user.getStatus())) {
            throw BusinessException.forbidden("用户已被禁用");
        }

        // 处理租户切换
        Long currentTenantId = handleTenantSwitch(user, request.getTenantCode());

        // 更新最后登录信息
        userMapper.updateLastLoginInfo(user.getId(), getClientIp(), currentTenantId);

        // 生成令牌
        return generateLoginResult(user, currentTenantId);
    }

    /**
     * 用户名密码登录
     */
    public LoginResult login(LoginRequest request) {
        log.info("用户名登录: {}", request.getUsername());

        // 查询用户
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        if (!"active".equals(user.getStatus())) {
            throw BusinessException.forbidden("用户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        // 处理租户切换
        Long currentTenantId = handleTenantSwitch(user, request.getTenantCode());

        // 更新最后登录信息
        userMapper.updateLastLoginInfo(user.getId(), getClientIp(), currentTenantId);

        // 生成令牌
        return generateLoginResult(user, currentTenantId);
    }

    /**
     * 微信登录
     */
    public LoginResult wechatLogin(WechatLoginRequest request) {
        log.info("微信登录: code={}", request.getCode());

        try {
            // 1. 通过code获取微信用户信息
            WechatUserInfo wechatUserInfo = wechatService.getUserInfoByCode(request.getCode());
            
            if (!StringUtils.hasText(wechatUserInfo.getOpenid())) {
                throw BusinessException.badRequest("获取微信用户信息失败");
            }

            // 2. 查询系统中是否存在该用户
            User user = userMapper.findByWechatOpenid(wechatUserInfo.getOpenid());
            
            if (user == null) {
                // 首次登录，创建用户
                user = createUserFromWechat(wechatUserInfo, request);
            } else {
                // 更新用户微信信息
                updateUserWechatInfo(user, wechatUserInfo);
            }

            if (!"active".equals(user.getStatus())) {
                throw BusinessException.forbidden("用户已被禁用");
            }

            // 3. 处理租户切换
            Long currentTenantId = handleTenantSwitch(user, null);

            // 4. 更新最后登录信息
            userMapper.updateLastLoginInfo(user.getId(), getClientIp(), currentTenantId);

            // 5. 生成令牌
            return generateLoginResult(user, currentTenantId);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            throw BusinessException.error("微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 绑定手机号（微信用户）
     */
    public void bindPhone(Long userId, String phone, String smsCode) {
        log.info("绑定手机号: userId={}, phone={}", userId, phone);

        // 验证短信验证码
        if (!smsService.verifyCode(phone, smsCode)) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }

        // 检查手机号是否已被其他用户绑定
        User existUser = userMapper.findByPhone(phone);
        if (existUser != null && !existUser.getId().equals(userId)) {
            throw BusinessException.badRequest("该手机号已被其他用户绑定");
        }

        // 更新用户手机号
        userMapper.updatePhone(userId, phone);
        
        log.info("手机号绑定成功: userId={}, phone={}", userId, phone);
    }

    /**
     * 刷新令牌
     */
    public TokenInfo refreshToken(String refreshToken) {
        log.info("刷新令牌");

        try {
            // 验证刷新令牌
            if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
                throw BusinessException.unauthorized("无效的刷新令牌");
            }

            if (jwtTokenUtil.isTokenExpired(refreshToken)) {
                throw BusinessException.unauthorized("刷新令牌已过期");
            }

            // 生成新的访问令牌
            String newAccessToken = jwtTokenUtil.refreshToken(refreshToken);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(
                    jwtTokenUtil.getUserIdFromToken(refreshToken),
                    jwtTokenUtil.getUsernameFromToken(refreshToken)
            );

            return new TokenInfo(newAccessToken, newRefreshToken, 1800L);

        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw BusinessException.unauthorized("刷新令牌失败");
        }
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        try {
            if (StringUtils.hasText(token)) {
                String jwt = jwtTokenUtil.resolveToken(token);
                if (jwt != null) {
                    // 将令牌加入黑名单（Redis）
                    String key = "jwt:blacklist:" + jwt;
                    redisTemplate.opsForValue().set(key, "1", 2, TimeUnit.HOURS);
                    log.info("用户登出成功");
                }
            }
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    public Object getCurrentUserInfo() {
        Long userId = TenantContext.getCurrentUserId();
        Long tenantId = TenantContext.getCurrentTenantId();

        if (userId == null) {
            throw BusinessException.unauthorized("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 构建用户信息
        LoginResult.UserInfo userInfo = new LoginResult.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setPhone(user.getPhone());
        userInfo.setName(user.getName());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setCurrentTenantId(user.getCurrentTenantId());
        userInfo.setLastLoginTime(user.getLastLoginTime());

        // 获取用户在当前租户的角色和权限
        if (tenantId != null) {
            TenantUser tenantUser = tenantUserMapper.findByUserIdAndTenantId(userId, tenantId);
            if (tenantUser != null) {
                userInfo.setRole(tenantUser.getRole());
                // TODO: 解析权限列表
            }
        }

        return userInfo;
    }

    /**
     * 发送短信验证码
     */
    public void sendSmsCode(String phone) {
        log.info("发送验证码: {}", phone);

        // 检查发送频率限制
        String limitKey = "sms:limit:" + phone;
        if (redisTemplate.hasKey(limitKey)) {
            throw BusinessException.badRequest("发送过于频繁，请稍后再试");
        }

        // 生成验证码
        String code = generateVerifyCode();

        // 发送短信
        boolean success = smsService.sendVerifyCode(phone, code);
        if (!success) {
            throw BusinessException.error("验证码发送失败");
        }

        // 保存验证码到Redis（5分钟有效期）
        String codeKey = "sms:code:" + phone;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);

        // 设置发送频率限制（60秒）
        redisTemplate.opsForValue().set(limitKey, "1", 60, TimeUnit.SECONDS);

        log.info("验证码发送成功: {}", phone);
    }

    /**
     * 处理租户切换
     */
    private Long handleTenantSwitch(User user, String tenantCode) {
        Long currentTenantId = user.getCurrentTenantId();

        // 如果指定了租户编码，尝试切换
        if (StringUtils.hasText(tenantCode)) {
            Tenant tenant = tenantMapper.findByTenantCode(tenantCode);
            if (tenant == null) {
                throw BusinessException.notFound("租户不存在");
            }

            // 检查用户是否属于该租户
            TenantUser tenantUser = tenantUserMapper.findByUserIdAndTenantId(user.getId(), tenant.getId());
            if (tenantUser == null || !"active".equals(tenantUser.getStatus())) {
                throw BusinessException.forbidden("您没有访问该租户的权限");
            }

            currentTenantId = tenant.getId();
        }

        // 如果用户没有当前租户，获取用户的第一个活跃租户
        if (currentTenantId == null) {
            List<TenantUser> tenantUsers = tenantUserMapper.findTenantsByUserId(user.getId());
            if (!tenantUsers.isEmpty()) {
                currentTenantId = tenantUsers.get(0).getTenantId();
            }
        }

        return currentTenantId;
    }

    /**
     * 为用户生成登录结果（供其他服务调用）
     */
    public LoginResult generateLoginResultForUser(User user, Long tenantId, String role) {
        // 生成令牌
        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername(), tenantId, role);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 构建登录结果
        LoginResult result = new LoginResult();
        result.setAccessToken(accessToken);
        result.setRefreshToken(refreshToken);
        result.setExpiresIn(1800L); // 30分钟

        // 用户信息
        LoginResult.UserInfo userInfo = new LoginResult.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setPhone(user.getPhone());
        userInfo.setName(user.getName());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setCurrentTenantId(tenantId);
        userInfo.setRole(role);
        userInfo.setLastLoginTime(LocalDateTime.now());
        // TODO: 设置权限列表
        result.setUser(userInfo);

        // 租户信息
        if (tenantId != null) {
            Tenant tenant = tenantMapper.selectById(tenantId);
            if (tenant != null) {
                LoginResult.TenantInfo tenantInfo = new LoginResult.TenantInfo();
                tenantInfo.setId(tenant.getId());
                tenantInfo.setTenantCode(tenant.getTenantCode());
                tenantInfo.setCompanyName(tenant.getCompanyName());
                tenantInfo.setCompanyType(tenant.getCompanyType());
                tenantInfo.setSubscriptionPlan(tenant.getSubscriptionPlan());
                tenantInfo.setStatus(tenant.getStatus());
                result.setTenant(tenantInfo);
            }
        }

        return result;
    }

    /**
     * 生成登录结果
     */
    public LoginResult generateLoginResult(User user, Long tenantId) {
        // 获取用户角色
        String role = "user";
        if (tenantId != null) {
            TenantUser tenantUser = tenantUserMapper.findByUserIdAndTenantId(user.getId(), tenantId);
            if (tenantUser != null) {
                role = tenantUser.getRole();
            }
        }

        // 生成令牌
        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername(), tenantId, role);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 构建登录结果
        LoginResult result = new LoginResult();
        result.setAccessToken(accessToken);
        result.setRefreshToken(refreshToken);
        result.setExpiresIn(1800L); // 30分钟

        // 用户信息
        LoginResult.UserInfo userInfo = new LoginResult.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setPhone(user.getPhone());
        userInfo.setName(user.getName());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setCurrentTenantId(tenantId);
        userInfo.setRole(role);
        userInfo.setLastLoginTime(LocalDateTime.now());
        // TODO: 设置权限列表
        result.setUser(userInfo);

        // 租户信息
        if (tenantId != null) {
            Tenant tenant = tenantMapper.selectById(tenantId);
            if (tenant != null) {
                LoginResult.TenantInfo tenantInfo = new LoginResult.TenantInfo();
                tenantInfo.setId(tenant.getId());
                tenantInfo.setTenantCode(tenant.getTenantCode());
                tenantInfo.setCompanyName(tenant.getCompanyName());
                tenantInfo.setCompanyType(tenant.getCompanyType());
                tenantInfo.setSubscriptionPlan(tenant.getSubscriptionPlan());
                tenantInfo.setStatus(tenant.getStatus());
                result.setTenant(tenantInfo);
            }
        }

        return result;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerifyCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

    /**
     * 获取客户端IP（简化实现）
     */
    private String getClientIp() {
        // TODO: 从HttpServletRequest中获取真实IP
        return "127.0.0.1";
    }

    /**
     * 从微信信息创建用户
     */
    private User createUserFromWechat(WechatUserInfo wechatUserInfo, WechatLoginRequest request) {
        User user = new User();
        user.setWechatOpenid(wechatUserInfo.getOpenid());
        user.setWechatUnionid(wechatUserInfo.getUnionid());
        user.setName(StringUtils.hasText(request.getNickname()) ? request.getNickname() : "微信用户");
        user.setAvatar(request.getAvatar());
        user.setStatus("active");
        
        // 如果提供了手机号，先验证再设置
        if (StringUtils.hasText(request.getPhone())) {
            if (!StringUtils.hasText(request.getSmsCode())) {
                throw BusinessException.badRequest("绑定手机号需要验证码");
            }
            
            if (!smsService.verifyCode(request.getPhone(), request.getSmsCode())) {
                throw BusinessException.badRequest("验证码错误或已过期");
            }
            
            // 检查手机号是否已被其他用户使用
            User existUser = userMapper.findByPhone(request.getPhone());
            if (existUser != null) {
                throw BusinessException.badRequest("该手机号已被其他用户使用");
            }
            
            user.setPhone(request.getPhone());
        }
        
        // 设置性别
        if (StringUtils.hasText(request.getGender())) {
            user.setGender(request.getGender());
        }

        userMapper.insert(user);
        log.info("微信用户创建成功: openid={}, userId={}", wechatUserInfo.getOpenid(), user.getId());
        
        return user;
    }

    /**
     * 更新用户微信信息
     */
    private void updateUserWechatInfo(User user, WechatUserInfo wechatUserInfo) {
        boolean needUpdate = false;
        
        // 更新UnionID（如果有的话）
        if (StringUtils.hasText(wechatUserInfo.getUnionid()) && 
            !wechatUserInfo.getUnionid().equals(user.getWechatUnionid())) {
            user.setWechatUnionid(wechatUserInfo.getUnionid());
            needUpdate = true;
        }
        
        if (needUpdate) {
            userMapper.updateById(user);
            log.info("用户微信信息更新成功: userId={}, openid={}", user.getId(), wechatUserInfo.getOpenid());
        }
    }

    /**
     * 微信手机号授权登录
     */
    public LoginResult wechatPhoneLogin(WechatPhoneLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw BusinessException.badRequest("微信code不能为空");
        }
        // 1. 获取 openid 与 sessionKey
        WechatUserInfo wechat = wechatService.getUserInfoByCode(request.getCode());
        if (wechat == null || !StringUtils.hasText(wechat.getOpenid()) || !StringUtils.hasText(wechat.getSessionKey())) {
            throw BusinessException.error("微信授权失败，缺少openid或sessionKey");
        }

        // 2. 解密手机号（必须有手机号授权）
        if (!StringUtils.hasText(request.getEncryptedData()) || !StringUtils.hasText(request.getIv())) {
            throw BusinessException.badRequest("未获取到手机号授权，请在小程序端先获取手机号");
        }
        String phone = wechatService.decryptPhoneNumber(request.getEncryptedData(), request.getIv(), wechat.getSessionKey());
        if (!StringUtils.hasText(phone)) {
            throw BusinessException.error("解析手机号失败");
        }

        // 3. 优先通过手机号匹配用户；否则通过 openid 匹配；都没有则创建新用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            user = userMapper.findByWechatOpenid(wechat.getOpenid());
        }
        if (user == null) {
            // 创建新用户，默认激活，绑定手机号与openid
            user = new User();
            user.setName("微信用户");
            user.setPhone(phone);
            user.setWechatOpenid(wechat.getOpenid());
            user.setWechatUnionid(wechat.getUnionid());
            user.setStatus("active");
            userMapper.insert(user);
            log.info("创建新用户并绑定手机号: userId={}, phone={}", user.getId(), phone);
        } else {
            // 补充绑定信息
            boolean changed = false;
            if (!StringUtils.hasText(user.getPhone())) { user.setPhone(phone); changed = true; }
            if (!StringUtils.hasText(user.getWechatOpenid())) { user.setWechatOpenid(wechat.getOpenid()); changed = true; }
            if (changed) { userMapper.updateById(user); }
        }

        // 4. 角色处理：如果该用户在当前租户没有角色，且是首次创建/无绑定，默认赋予 worker 角色（后续可通过邀请/管理变更）
        Long currentTenantId = handleTenantSwitch(user, null);
        String role = null;
        if (currentTenantId != null) {
            TenantUser tu = tenantUserMapper.findByUserIdAndTenantId(user.getId(), currentTenantId);
            if (tu == null) {
                // 默认赋予worker角色
                TenantUser newTu = new TenantUser();
                newTu.setTenantId(currentTenantId);
                newTu.setUserId(user.getId());
                newTu.setRole("worker");
                tenantUserMapper.insert(newTu);
                role = "worker";
            } else {
                role = tu.getRole();
            }
        }
        
        // 5. 生成登录结果（若本次默认赋予了角色，带上该角色）
        if (StringUtils.hasText(role)) {
            return generateLoginResultForUser(user, currentTenantId, role);
        }
        return generateLoginResult(user, currentTenantId);
    }
}