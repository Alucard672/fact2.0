package com.garment.common.service;

import com.garment.common.context.TenantContext;
import com.garment.common.enums.Permission;
import com.garment.common.enums.TenantRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 权限服务
 *
 * @author garment
 */
@Slf4j
@Service
public class PermissionService {

    /**
     * 检查当前用户是否有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        String userRole = TenantContext.getCurrentUserRole();
        if (userRole == null) {
            return false;
        }
        
        TenantRole role = TenantRole.fromCode(userRole);
        if (role == null) {
            return false;
        }
        
        return role.hasPermission(permissionCode);
    }

    /**
     * 检查当前用户是否有指定权限
     */
    public boolean hasPermission(Permission permission) {
        return hasPermission(permission.getCode());
    }

    /**
     * 检查当前用户是否有所有指定权限
     */
    public boolean hasAllPermissions(String... permissionCodes) {
        return Arrays.stream(permissionCodes).allMatch(this::hasPermission);
    }

    /**
     * 检查当前用户是否有任一指定权限
     */
    public boolean hasAnyPermission(String... permissionCodes) {
        return Arrays.stream(permissionCodes).anyMatch(this::hasPermission);
    }

    /**
     * 检查当前用户是否有指定角色
     */
    public boolean hasRole(String roleCode) {
        String userRole = TenantContext.getCurrentUserRole();
        return roleCode.equals(userRole);
    }

    /**
     * 检查当前用户是否有任一指定角色
     */
    public boolean hasAnyRole(String... roleCodes) {
        String userRole = TenantContext.getCurrentUserRole();
        return Arrays.stream(roleCodes).anyMatch(role -> role.equals(userRole));
    }

    /**
     * 获取当前用户的所有权限
     */
    public List<Permission> getCurrentUserPermissions() {
        String userRole = TenantContext.getCurrentUserRole();
        if (userRole == null) {
            return Arrays.asList();
        }
        
        TenantRole role = TenantRole.fromCode(userRole);
        if (role == null) {
            return Arrays.asList();
        }
        
        return role.getPermissions();
    }

    /**
     * 是否是系统管理员
     */
    public boolean isSystemAdmin() {
        Boolean isSystemAdmin = TenantContext.getCurrentUserSystemAdmin();
        return Boolean.TRUE.equals(isSystemAdmin);
    }

    /**
     * 是否是租户拥有者
     */
    public boolean isTenantOwner() {
        return hasRole(TenantRole.OWNER.getCode());
    }

    /**
     * 是否是管理员（包括租户拥有者和管理员）
     */
    public boolean isAdmin() {
        return hasAnyRole(TenantRole.OWNER.getCode(), TenantRole.ADMIN.getCode());
    }

    /**
     * 检查用户权限并抛出异常
     */
    public void checkPermission(String permissionCode) {
        if (!hasPermission(permissionCode)) {
            throw new SecurityException("权限不足: " + permissionCode);
        }
    }

    /**
     * 检查用户角色并抛出异常
     */
    public void checkRole(String roleCode) {
        if (!hasRole(roleCode)) {
            throw new SecurityException("角色权限不足: " + roleCode);
        }
    }
}