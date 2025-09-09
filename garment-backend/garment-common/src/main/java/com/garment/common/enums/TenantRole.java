package com.garment.common.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 租户内角色枚举
 *
 * @author garment
 */
public enum TenantRole {
    
    OWNER("owner", "租户拥有者", Arrays.asList(
        // 拥有所有权限
        Permission.TENANT_VIEW, Permission.TENANT_EDIT,
        Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_EDIT, Permission.USER_DELETE, Permission.USER_INVITE,
        Permission.WORKSHOP_VIEW, Permission.WORKSHOP_CREATE, Permission.WORKSHOP_EDIT, Permission.WORKSHOP_DELETE,
        Permission.STYLE_VIEW, Permission.STYLE_CREATE, Permission.STYLE_EDIT, Permission.STYLE_DELETE,
        Permission.PROCESS_VIEW, Permission.PROCESS_CREATE, Permission.PROCESS_EDIT, Permission.PROCESS_DELETE,
        Permission.PRICE_VIEW, Permission.PRICE_CREATE, Permission.PRICE_EDIT, Permission.PRICE_DELETE,
        Permission.CUT_ORDER_VIEW, Permission.CUT_ORDER_CREATE, Permission.CUT_ORDER_EDIT, Permission.CUT_ORDER_DELETE, Permission.CUT_ORDER_CONFIRM,
        Permission.BUNDLE_VIEW, Permission.BUNDLE_EDIT, Permission.BUNDLE_PRINT,
        Permission.PRODUCTION_SCAN, Permission.PRODUCTION_TAKE, Permission.PRODUCTION_SUBMIT, Permission.PRODUCTION_RETURN, Permission.PRODUCTION_REPAIR,
        Permission.PIECEWORK_VIEW, Permission.PIECEWORK_EDIT, Permission.PIECEWORK_CALCULATE, Permission.PIECEWORK_SETTLEMENT, Permission.PIECEWORK_LOCK,
        Permission.REPORT_VIEW, Permission.REPORT_EXPORT, Permission.REPORT_ADVANCED,
        Permission.SYSTEM_CONFIG, Permission.SYSTEM_LOG, Permission.SYSTEM_BACKUP,
        Permission.QUALITY_VIEW, Permission.QUALITY_GRADE, Permission.QUALITY_DEFECT
    )),
    
    ADMIN("admin", "管理员", Arrays.asList(
        Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_EDIT, Permission.USER_INVITE,
        Permission.WORKSHOP_VIEW, Permission.WORKSHOP_CREATE, Permission.WORKSHOP_EDIT,
        Permission.STYLE_VIEW, Permission.STYLE_CREATE, Permission.STYLE_EDIT,
        Permission.PROCESS_VIEW, Permission.PROCESS_CREATE, Permission.PROCESS_EDIT,
        Permission.PRICE_VIEW, Permission.PRICE_CREATE, Permission.PRICE_EDIT,
        Permission.CUT_ORDER_VIEW, Permission.CUT_ORDER_CREATE, Permission.CUT_ORDER_EDIT, Permission.CUT_ORDER_CONFIRM,
        Permission.BUNDLE_VIEW, Permission.BUNDLE_EDIT, Permission.BUNDLE_PRINT,
        Permission.PRODUCTION_SCAN, Permission.PRODUCTION_TAKE, Permission.PRODUCTION_SUBMIT, Permission.PRODUCTION_RETURN, Permission.PRODUCTION_REPAIR,
        Permission.PIECEWORK_VIEW, Permission.PIECEWORK_EDIT, Permission.PIECEWORK_CALCULATE, Permission.PIECEWORK_SETTLEMENT,
        Permission.REPORT_VIEW, Permission.REPORT_EXPORT, Permission.REPORT_ADVANCED,
        Permission.QUALITY_VIEW, Permission.QUALITY_GRADE, Permission.QUALITY_DEFECT
    )),
    
    MANAGER("manager", "经理", Arrays.asList(
        Permission.USER_VIEW, Permission.USER_INVITE,
        Permission.WORKSHOP_VIEW,
        Permission.STYLE_VIEW, Permission.STYLE_CREATE, Permission.STYLE_EDIT,
        Permission.PROCESS_VIEW,
        Permission.PRICE_VIEW, Permission.PRICE_CREATE, Permission.PRICE_EDIT,
        Permission.CUT_ORDER_VIEW, Permission.CUT_ORDER_CREATE, Permission.CUT_ORDER_EDIT, Permission.CUT_ORDER_CONFIRM,
        Permission.BUNDLE_VIEW, Permission.BUNDLE_EDIT, Permission.BUNDLE_PRINT,
        Permission.PRODUCTION_SCAN, Permission.PRODUCTION_TAKE, Permission.PRODUCTION_SUBMIT, Permission.PRODUCTION_RETURN, Permission.PRODUCTION_REPAIR,
        Permission.PIECEWORK_VIEW, Permission.PIECEWORK_CALCULATE,
        Permission.REPORT_VIEW, Permission.REPORT_EXPORT,
        Permission.QUALITY_VIEW, Permission.QUALITY_GRADE, Permission.QUALITY_DEFECT
    )),
    
    SUPERVISOR("supervisor", "主管", Arrays.asList(
        Permission.STYLE_VIEW,
        Permission.PROCESS_VIEW,
        Permission.PRICE_VIEW,
        Permission.CUT_ORDER_VIEW,
        Permission.BUNDLE_VIEW, Permission.BUNDLE_PRINT,
        Permission.PRODUCTION_SCAN, Permission.PRODUCTION_TAKE, Permission.PRODUCTION_SUBMIT, Permission.PRODUCTION_RETURN, Permission.PRODUCTION_REPAIR,
        Permission.PIECEWORK_VIEW,
        Permission.REPORT_VIEW,
        Permission.QUALITY_VIEW, Permission.QUALITY_GRADE, Permission.QUALITY_DEFECT
    )),
    
    WORKER("worker", "工人", Arrays.asList(
        Permission.STYLE_VIEW,
        Permission.PROCESS_VIEW,
        Permission.PRODUCTION_SCAN, Permission.PRODUCTION_TAKE, Permission.PRODUCTION_SUBMIT, Permission.PRODUCTION_RETURN,
        Permission.PIECEWORK_VIEW
    ));
    
    private final String code;
    private final String name;
    private final List<Permission> permissions;
    
    TenantRole(String code, String name, List<Permission> permissions) {
        this.code = code;
        this.name = name;
        this.permissions = permissions;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Permission> getPermissions() {
        return permissions;
    }
    
    /**
     * 根据角色代码查找角色
     */
    public static TenantRole fromCode(String code) {
        for (TenantRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
    
    /**
     * 检查是否有指定权限（通过权限代码）
     */
    public boolean hasPermission(String permissionCode) {
        Permission permission = Permission.fromCode(permissionCode);
        return permission != null && hasPermission(permission);
    }
}