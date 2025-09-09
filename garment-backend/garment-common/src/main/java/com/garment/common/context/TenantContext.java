package com.garment.common.context;

/**
 * 租户上下文
 * 用于在请求处理过程中传递租户信息
 *
 * @author garment
 */
public class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();
    
    private static final ThreadLocal<Boolean> IS_SYSTEM_ADMIN = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     */
    public static void setCurrentTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前租户ID
     */
    public static Long getCurrentTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return USER_ID.get();
    }

    /**
     * 设置当前用户名
     */
    public static void setCurrentUsername(String username) {
        USERNAME.set(username);
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        return USERNAME.get();
    }

    /**
     * 设置当前用户角色
     */
    public static void setCurrentUserRole(String role) {
        USER_ROLE.set(role);
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        return USER_ROLE.get();
    }

    /**
     * 设置当前用户是否系统管理员
     */
    public static void setCurrentUserSystemAdmin(Boolean isSystemAdmin) {
        IS_SYSTEM_ADMIN.set(isSystemAdmin);
    }

    /**
     * 获取当前用户是否系统管理员
     */
    public static Boolean getCurrentUserSystemAdmin() {
        return IS_SYSTEM_ADMIN.get();
    }

    /**
     * 清理当前线程的上下文信息
     */
    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        USERNAME.remove();
        USER_ROLE.remove();
        IS_SYSTEM_ADMIN.remove();
    }

    /**
     * 检查是否设置了租户上下文
     */
    public static boolean hasTenantContext() {
        return TENANT_ID.get() != null;
    }

    /**
     * 检查是否设置了用户上下文
     */
    public static boolean hasUserContext() {
        return USER_ID.get() != null;
    }
}