package com.garment.common.utils;

import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 租户数据隔离工具类
 *
 * @author garment
 */
@Slf4j
public class TenantUtils {
    
    private static final Logger log = LoggerFactory.getLogger(TenantUtils.class);

    /**
     * 检查租户上下文
     */
    public static void checkTenantContext() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw BusinessException.badRequest("租户上下文未设置");
        }
    }

    /**
     * 检查资源是否属于当前租户
     */
    public static void checkTenantResource(Long resourceTenantId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        
        if (currentTenantId == null) {
            throw BusinessException.badRequest("租户上下文未设置");
        }
        
        if (resourceTenantId == null) {
            throw BusinessException.badRequest("资源租户ID为空");
        }
        
        if (!currentTenantId.equals(resourceTenantId)) {
            throw BusinessException.forbidden("无权访问其他租户的资源");
        }
    }

    /**
     * 检查资源是否属于当前租户（允许系统管理员跳过检查）
     */
    public static void checkTenantResourceWithAdmin(Long resourceTenantId) {
        // 系统管理员可以跳过租户隔离检查
        Boolean isSystemAdmin = TenantContext.getCurrentUserSystemAdmin();
        if (Boolean.TRUE.equals(isSystemAdmin)) {
            return;
        }
        
        checkTenantResource(resourceTenantId);
    }

    /**
     * 执行忽略租户隔离的操作
     */
    public static <T> T executeWithoutTenant(java.util.function.Supplier<T> supplier) {
        Long originalTenantId = TenantContext.getCurrentTenantId();
        try {
            // 临时清除租户上下文
            TenantContext.setCurrentTenantId(null);
            return supplier.get();
        } finally {
            // 恢复原始租户上下文
            TenantContext.setCurrentTenantId(originalTenantId);
        }
    }

    /**
     * 执行忽略租户隔离的操作（无返回值）
     */
    public static void executeWithoutTenant(Runnable runnable) {
        Long originalTenantId = TenantContext.getCurrentTenantId();
        try {
            // 临时清除租户上下文
            TenantContext.setCurrentTenantId(null);
            runnable.run();
        } finally {
            // 恢复原始租户上下文
            TenantContext.setCurrentTenantId(originalTenantId);
        }
    }

    /**
     * 以指定租户身份执行操作
     */
    public static <T> T executeAsTenant(Long tenantId, java.util.function.Supplier<T> supplier) {
        Long originalTenantId = TenantContext.getCurrentTenantId();
        try {
            TenantContext.setCurrentTenantId(tenantId);
            return supplier.get();
        } finally {
            TenantContext.setCurrentTenantId(originalTenantId);
        }
    }

    /**
     * 以指定租户身份执行操作（无返回值）
     */
    public static void executeAsTenant(Long tenantId, Runnable runnable) {
        Long originalTenantId = TenantContext.getCurrentTenantId();
        try {
            TenantContext.setCurrentTenantId(tenantId);
            runnable.run();
        } finally {
            TenantContext.setCurrentTenantId(originalTenantId);
        }
    }

    /**
     * 获取当前租户ID，如果没有则抛出异常
     */
    public static Long requireCurrentTenantId() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw BusinessException.badRequest("租户上下文未设置");
        }
        return tenantId;
    }

    /**
     * 为实体设置租户ID
     */
    public static void setTenantId(Object entity) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null && entity != null) {
            try {
                java.lang.reflect.Method setTenantId = entity.getClass().getMethod("setTenantId", Long.class);
                setTenantId.invoke(entity, tenantId);
            } catch (Exception e) {
                log.debug("无法为实体设置租户ID: {}", e.getMessage());
            }
        }
    }

    /**
     * 验证实体的租户ID
     */
    public static void validateEntityTenantId(Object entity) {
        if (entity == null) {
            return;
        }
        
        try {
            java.lang.reflect.Method getTenantId = entity.getClass().getMethod("getTenantId");
            Long entityTenantId = (Long) getTenantId.invoke(entity);
            
            if (entityTenantId != null) {
                checkTenantResource(entityTenantId);
            }
        } catch (Exception e) {
            log.debug("无法验证实体租户ID: {}", e.getMessage());
        }
    }
}