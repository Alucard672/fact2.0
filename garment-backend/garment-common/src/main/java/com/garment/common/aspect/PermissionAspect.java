package com.garment.common.aspect;

import com.garment.common.annotation.RequirePermission;
import com.garment.common.annotation.RequireRole;
import com.garment.common.exception.BusinessException;
import com.garment.common.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 权限检查切面
 *
 * @author garment
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    
    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    private final PermissionService permissionService;

    // 移除显式构造器，使用 @RequiredArgsConstructor 生成的构造器
    // public PermissionAspect(PermissionService permissionService) {
    //     this.permissionService = permissionService;
    // }

    /**
     * 权限检查
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        String[] permissions = requirePermission.value();
        RequirePermission.LogicalOperator logical = requirePermission.logical();
        
        log.debug("检查权限: {}, 逻辑: {}", Arrays.toString(permissions), logical);
        
        boolean hasPermission;
        if (logical == RequirePermission.LogicalOperator.AND) {
            hasPermission = permissionService.hasAllPermissions(permissions);
        } else {
            hasPermission = permissionService.hasAnyPermission(permissions);
        }
        
        if (!hasPermission) {
            log.warn("权限检查失败: 需要权限 {}, 逻辑 {}", Arrays.toString(permissions), logical);
            throw BusinessException.forbidden("权限不足");
        }
        
        log.debug("权限检查通过");
    }

    /**
     * 角色检查
     */
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        String[] roles = requireRole.value();
        RequireRole.LogicalOperator logical = requireRole.logical();
        
        log.debug("检查角色: {}, 逻辑: {}", Arrays.toString(roles), logical);
        
        boolean hasRole;
        if (logical == RequireRole.LogicalOperator.AND) {
            // 检查是否拥有所有角色（通常不太可能，因为一个用户通常只有一个角色）
            hasRole = Arrays.stream(roles).allMatch(permissionService::hasRole);
        } else {
            hasRole = permissionService.hasAnyRole(roles);
        }
        
        if (!hasRole) {
            log.warn("角色检查失败: 需要角色 {}, 逻辑 {}", Arrays.toString(roles), logical);
            throw BusinessException.forbidden("角色权限不足");
        }
        
        log.debug("角色检查通过");
    }
}