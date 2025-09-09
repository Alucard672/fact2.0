package com.garment.auth.service;

import com.garment.auth.dto.LoginResult;
import com.garment.auth.dto.SwitchTenantRequest;
import com.garment.auth.dto.UserTenantInfo;
import com.garment.auth.entity.Tenant;
import com.garment.auth.entity.TenantUser;
import com.garment.auth.entity.User;
import com.garment.auth.mapper.TenantMapper;
import com.garment.auth.mapper.TenantUserMapper;
import com.garment.auth.mapper.UserMapper;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户切换服务
 *
 * @author garment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSwitchService {

    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final TenantUserMapper tenantUserMapper;
    private final AuthService authService;

    /**
     * 获取用户的所有租户
     */
    public List<UserTenantInfo> getUserTenants() {
        Long userId = TenantContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 获取用户的所有租户关联
        List<TenantUser> tenantUsers = tenantUserMapper.findTenantsByUserId(userId);
        
        return tenantUsers.stream()
                .filter(tenantUser -> "active".equals(tenantUser.getStatus()))
                .map(tenantUser -> {
                    UserTenantInfo info = new UserTenantInfo();
                    
                    // 获取租户信息
                    Tenant tenant = tenantMapper.selectById(tenantUser.getTenantId());
                    if (tenant != null && !"deleted".equals(tenant.getStatus())) {
                        BeanUtils.copyProperties(tenant, info);
                        info.setTenantId(tenant.getId());
                        info.setTenantCode(tenant.getTenantCode());
                        info.setCompanyName(tenant.getCompanyName());
                        info.setCompanyType(tenant.getCompanyType());
                        info.setSubscriptionPlan(tenant.getSubscriptionPlan());
                        info.setTenantStatus(tenant.getStatus());
                    }
                    
                    // 设置用户在该租户中的角色信息
                    info.setRole(tenantUser.getRole());
                    info.setEmployeeNo(tenantUser.getEmployeeNo());
                    info.setDepartment(tenantUser.getDepartment());
                    info.setPosition(tenantUser.getPosition());
                    info.setStatus(tenantUser.getStatus());
                    
                    // 判断是否为当前租户
                    info.setIsCurrent(tenantUser.getTenantId().equals(user.getCurrentTenantId()));
                    
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * 切换租户
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResult switchTenant(SwitchTenantRequest request) {
        Long userId = TenantContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("用户未登录");
        }

        log.info("用户切换租户: userId={}, targetTenantId={}", userId, request.getTenantId());

        // 1. 验证目标租户
        Tenant tenant = tenantMapper.selectById(request.getTenantId());
        if (tenant == null || tenant.getDeleted()) {
            throw BusinessException.notFound("目标租户不存在");
        }

        if (!"active".equals(tenant.getStatus())) {
            throw BusinessException.badRequest("目标租户未激活或已被禁用");
        }

        // 2. 验证用户是否有权限访问该租户
        TenantUser tenantUser = tenantUserMapper.findByUserIdAndTenantId(userId, request.getTenantId());
        if (tenantUser == null || !"active".equals(tenantUser.getStatus())) {
            throw BusinessException.forbidden("您没有访问该租户的权限");
        }

        // 3. 更新用户的当前租户
        userMapper.updateCurrentTenant(userId, request.getTenantId());

        // 4. 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 5. 生成新的登录结果（包含新的租户信息）
        LoginResult result = authService.generateLoginResultForUser(user, request.getTenantId(), tenantUser.getRole());

        log.info("租户切换成功: userId={}, newTenantId={}, role={}", userId, request.getTenantId(), tenantUser.getRole());
        
        return result;
    }

    /**
     * 获取当前租户信息
     */
    public UserTenantInfo getCurrentTenantInfo() {
        Long userId = TenantContext.getCurrentUserId();
        Long tenantId = TenantContext.getCurrentTenantId();
        
        if (userId == null || tenantId == null) {
            throw BusinessException.badRequest("用户或租户上下文未设置");
        }

        // 获取租户用户信息
        TenantUser tenantUser = tenantUserMapper.findByUserIdAndTenantId(userId, tenantId);
        if (tenantUser == null) {
            throw BusinessException.notFound("租户用户关联不存在");
        }

        // 获取租户信息
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw BusinessException.notFound("租户不存在");
        }

        UserTenantInfo info = new UserTenantInfo();
        
        // 设置租户信息
        info.setTenantId(tenant.getId());
        info.setTenantCode(tenant.getTenantCode());
        info.setCompanyName(tenant.getCompanyName());
        info.setCompanyType(tenant.getCompanyType());
        info.setSubscriptionPlan(tenant.getSubscriptionPlan());
        info.setTenantStatus(tenant.getStatus());
        
        // 设置用户在该租户中的角色信息
        info.setRole(tenantUser.getRole());
        info.setEmployeeNo(tenantUser.getEmployeeNo());
        info.setDepartment(tenantUser.getDepartment());
        info.setPosition(tenantUser.getPosition());
        info.setStatus(tenantUser.getStatus());
        info.setIsCurrent(true);
        
        return info;
    }

    /**
     * 验证租户权限
     */
    public boolean hasAccessToTenant(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return false;
        }

        TenantUser tenantUser = tenantUserMapper.findByUserIdAndTenantId(userId, tenantId);
        return tenantUser != null && "active".equals(tenantUser.getStatus());
    }

    /**
     * 获取用户在指定租户中的角色
     */
    public String getUserRoleInTenant(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return null;
        }

        TenantUser tenantUser = tenantUserMapper.findByUserIdAndTenantId(userId, tenantId);
        return tenantUser != null ? tenantUser.getRole() : null;
    }

    /**
     * 离开租户
     */
    @Transactional(rollbackFor = Exception.class)
    public void leaveTenant(Long tenantId) {
        Long userId = TenantContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("用户未登录");
        }

        log.info("用户离开租户: userId={}, tenantId={}", userId, tenantId);

        // 1. 验证租户用户关联
        TenantUser tenantUser = tenantUserMapper.findByUserIdAndTenantId(userId, tenantId);
        if (tenantUser == null) {
            throw BusinessException.notFound("您不是该租户的成员");
        }

        // 2. 检查是否是租户拥有者
        if ("owner".equals(tenantUser.getRole())) {
            throw BusinessException.badRequest("租户拥有者不能离开租户");
        }

        // 3. 软删除租户用户关联
        tenantUser.setDeleted(true);
        tenantUserMapper.updateById(tenantUser);

        // 4. 如果离开的是当前租户，需要切换到其他租户
        User user = userMapper.selectById(userId);
        if (user != null && tenantId.equals(user.getCurrentTenantId())) {
            List<TenantUser> remainingTenants = tenantUserMapper.findTenantsByUserId(userId);
            
            if (!remainingTenants.isEmpty()) {
                // 切换到第一个可用的租户
                Long newTenantId = remainingTenants.get(0).getTenantId();
                userMapper.updateCurrentTenant(userId, newTenantId);
            } else {
                // 没有其他租户，清空当前租户
                userMapper.updateCurrentTenant(userId, null);
            }
        }

        log.info("用户成功离开租户: userId={}, tenantId={}", userId, tenantId);
    }
}