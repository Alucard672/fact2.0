package com.garment.common.enums;

/**
 * 权限枚举
 *
 * @author garment
 */
public enum Permission {
    
    // 租户管理权限
    TENANT_VIEW("tenant:view", "查看租户信息"),
    TENANT_EDIT("tenant:edit", "编辑租户信息"),
    TENANT_DELETE("tenant:delete", "删除租户"),
    
    // 用户管理权限
    USER_VIEW("user:view", "查看用户列表"),
    USER_CREATE("user:create", "创建用户"),
    USER_EDIT("user:edit", "编辑用户"),
    USER_DELETE("user:delete", "删除用户"),
    USER_INVITE("user:invite", "邀请用户"),
    
    // 车间管理权限
    WORKSHOP_VIEW("workshop:view", "查看车间信息"),
    WORKSHOP_CREATE("workshop:create", "创建车间"),
    WORKSHOP_EDIT("workshop:edit", "编辑车间"),
    WORKSHOP_DELETE("workshop:delete", "删除车间"),
    
    // 款式管理权限
    STYLE_VIEW("style:view", "查看款式"),
    STYLE_CREATE("style:create", "创建款式"),
    STYLE_EDIT("style:edit", "编辑款式"),
    STYLE_DELETE("style:delete", "删除款式"),
    
    // 工序管理权限
    PROCESS_VIEW("process:view", "查看工序"),
    PROCESS_CREATE("process:create", "创建工序"),
    PROCESS_EDIT("process:edit", "编辑工序"),
    PROCESS_DELETE("process:delete", "删除工序"),
    
    // 工价管理权限
    PRICE_VIEW("price:view", "查看工价"),
    PRICE_CREATE("price:create", "创建工价"),
    PRICE_EDIT("price:edit", "编辑工价"),
    PRICE_DELETE("price:delete", "删除工价"),
    
    // 裁床管理权限
    CUT_ORDER_VIEW("cut_order:view", "查看裁床单"),
    CUT_ORDER_CREATE("cut_order:create", "创建裁床单"),
    CUT_ORDER_EDIT("cut_order:edit", "编辑裁床单"),
    CUT_ORDER_DELETE("cut_order:delete", "删除裁床单"),
    CUT_ORDER_CONFIRM("cut_order:confirm", "确认裁床单"),
    
    // 包管理权限
    BUNDLE_VIEW("bundle:view", "查看包信息"),
    BUNDLE_EDIT("bundle:edit", "编辑包信息"),
    BUNDLE_PRINT("bundle:print", "打印菲票"),
    
    // 生产操作权限
    PRODUCTION_SCAN("production:scan", "扫码操作"),
    PRODUCTION_TAKE("production:take", "领取包"),
    PRODUCTION_SUBMIT("production:submit", "提交包"),
    PRODUCTION_RETURN("production:return", "退包"),
    PRODUCTION_REPAIR("production:repair", "返修操作"),
    
    // 计件工资权限
    PIECEWORK_VIEW("piecework:view", "查看计件记录"),
    PIECEWORK_EDIT("piecework:edit", "编辑计件记录"),
    PIECEWORK_CALCULATE("piecework:calculate", "计算工资"),
    PIECEWORK_SETTLEMENT("piecework:settlement", "工资结算"),
    PIECEWORK_LOCK("piecework:lock", "锁定工资"),
    
    // 统计报表权限
    REPORT_VIEW("report:view", "查看报表"),
    REPORT_EXPORT("report:export", "导出报表"),
    REPORT_ADVANCED("report:advanced", "高级报表"),
    
    // 系统管理权限
    SYSTEM_CONFIG("system:config", "系统配置"),
    SYSTEM_LOG("system:log", "查看日志"),
    SYSTEM_BACKUP("system:backup", "数据备份"),
    
    // 质量管理权限
    QUALITY_VIEW("quality:view", "查看质量记录"),
    QUALITY_GRADE("quality:grade", "质量评级"),
    QUALITY_DEFECT("quality:defect", "记录疵点");
    
    private final String code;
    private final String description;
    
    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据权限代码查找权限
     */
    public static Permission fromCode(String code) {
        for (Permission permission : values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        return null;
    }
}