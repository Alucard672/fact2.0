package com.garment.auth.service;

import com.garment.auth.dto.StorageUsage;
import com.garment.auth.dto.TenantLimits;
import com.garment.auth.entity.Tenant;
import com.garment.auth.entity.TenantUser;
import com.garment.auth.mapper.TenantMapper;
import com.garment.auth.mapper.TenantUserMapper;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 租户限制服务
 *
 * @author garment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantLimitService {

    private final TenantMapper tenantMapper;
    private final TenantUserMapper tenantUserMapper;

    /**
     * 获取租户限制信息
     */
    public TenantLimits getTenantLimits() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw BusinessException.badRequest("租户上下文未设置");
        }

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw BusinessException.notFound("租户不存在");
        }

        TenantLimits limits = new TenantLimits();
        
        // 设置基础限制
        limits.setMaxUsers(tenant.getMaxUsers());
        limits.setMaxStorage(tenant.getMaxStorage());
        
        // 获取当前使用情况
        limits.setCurrentUserCount(getCurrentUserCount(tenantId));
        limits.setCurrentStorageUsage(getCurrentStorageUsage(tenantId));
        
        // 设置功能限制
        limits.setFeatureLimits(getFeatureLimits(tenant));
        
        // 设置订阅信息
        limits.setSubscriptionInfo(getSubscriptionInfo(tenant));
        
        return limits;
    }

    /**
     * 检查用户数量限制
     */
    public boolean checkUserLimit() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return false;
        }

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            return false;
        }

        int currentUserCount = getCurrentUserCount(tenantId);
        return currentUserCount < tenant.getMaxUsers();
    }

    /**
     * 检查存储空间限制
     */
    public boolean checkStorageLimit(long fileSize) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return false;
        }

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            return false;
        }

        long currentUsage = getCurrentStorageUsage(tenantId);
        return (currentUsage + fileSize) <= tenant.getMaxStorage();
    }

    /**
     * 获取存储使用情况
     */
    public StorageUsage getStorageUsage() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw BusinessException.badRequest("租户上下文未设置");
        }

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw BusinessException.notFound("租户不存在");
        }

        StorageUsage usage = new StorageUsage();
        usage.setTotalStorage(tenant.getMaxStorage());
        
        long currentUsage = getCurrentStorageUsage(tenantId);
        usage.setUsedStorage(currentUsage);
        usage.setAvailableStorage(tenant.getMaxStorage() - currentUsage);
        usage.setUsagePercentage((double) currentUsage / tenant.getMaxStorage() * 100);
        
        // TODO: 实现存储类型分布统计
        StorageUsage.StorageTypeDistribution distribution = new StorageUsage.StorageTypeDistribution();
        distribution.setImages(0L);
        distribution.setDocuments(0L);
        distribution.setLogs(0L);
        distribution.setOthers(currentUsage);
        usage.setDistribution(distribution);
        
        return usage;
    }

    /**
     * 检查特定功能是否可用
     */
    public boolean isFeatureEnabled(String featureCode) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return false;
        }

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            return false;
        }

        // 根据套餐类型判断功能是否启用
        return isFeatureAvailableForPlan(tenant.getSubscriptionPlan(), featureCode);
    }

    /**
     * 验证用户数量限制
     */
    public void validateUserLimit() {
        if (!checkUserLimit()) {
            throw BusinessException.badRequest("已达到最大用户数限制");
        }
    }

    /**
     * 验证存储空间限制
     */
    public void validateStorageLimit(long fileSize) {
        if (!checkStorageLimit(fileSize)) {
            throw BusinessException.badRequest("已达到最大存储空间限制");
        }
    }

    /**
     * 验证功能权限
     */
    public void validateFeatureAccess(String featureCode) {
        if (!isFeatureEnabled(featureCode)) {
            throw BusinessException.forbidden("当前套餐不支持此功能");
        }
    }

    // 私有辅助方法

    private int getCurrentUserCount(Long tenantId) {
        QueryWrapper<TenantUser> qw = new QueryWrapper<>();
        qw.eq("tenant_id", tenantId).eq("deleted", 0);
        return tenantUserMapper.selectCount(qw).intValue();
    }

    private long getCurrentStorageUsage(Long tenantId) {
        // TODO: 实现实际的存储使用量统计
        // 这里返回模拟数据
        return 100L * 1024 * 1024; // 100MB
    }

    private List<TenantLimits.FeatureLimit> getFeatureLimits(Tenant tenant) {
        String plan = tenant.getSubscriptionPlan();
        
        return Arrays.asList(
            createFeatureLimit("basic_operations", "基础操作", true, null, "创建、编辑、删除基础数据"),
            createFeatureLimit("advanced_reports", "高级报表", 
                isFeatureAvailableForPlan(plan, "advanced_reports"), null, "生成和导出高级统计报表"),
            createFeatureLimit("api_access", "API访问", 
                isFeatureAvailableForPlan(plan, "api_access"), null, "通过API访问系统数据"),
            createFeatureLimit("custom_fields", "自定义字段", 
                isFeatureAvailableForPlan(plan, "custom_fields"), null, "自定义数据字段"),
            createFeatureLimit("data_backup", "数据备份", 
                isFeatureAvailableForPlan(plan, "data_backup"), null, "自动数据备份和恢复"),
            createFeatureLimit("multi_language", "多语言支持", 
                isFeatureAvailableForPlan(plan, "multi_language"), null, "界面多语言切换"),
            createFeatureLimit("mobile_app", "移动应用", 
                isFeatureAvailableForPlan(plan, "mobile_app"), null, "移动端应用访问"),
            createFeatureLimit("workflow", "工作流", 
                isFeatureAvailableForPlan(plan, "workflow"), null, "自定义业务流程")
        );
    }

    private TenantLimits.FeatureLimit createFeatureLimit(String code, String name, boolean enabled, 
                                                        Integer limitValue, String description) {
        TenantLimits.FeatureLimit limit = new TenantLimits.FeatureLimit();
        limit.setFeatureCode(code);
        limit.setFeatureName(name);
        limit.setEnabled(enabled);
        limit.setLimitValue(limitValue);
        limit.setDescription(description);
        return limit;
    }

    private TenantLimits.SubscriptionInfo getSubscriptionInfo(Tenant tenant) {
        TenantLimits.SubscriptionInfo info = new TenantLimits.SubscriptionInfo();
        info.setPlanType(tenant.getSubscriptionPlan());
        info.setPlanName(getPlanName(tenant.getSubscriptionPlan()));
        info.setStartDate(tenant.getSubscriptionStart() != null ? tenant.getSubscriptionStart().toString() : null);
        info.setEndDate(tenant.getSubscriptionEnd() != null ? tenant.getSubscriptionEnd().toString() : null);
        info.setStatus(tenant.getStatus());
        return info;
    }

    private String getPlanName(String planType) {
        switch (planType) {
            case "trial": return "试用版";
            case "basic": return "基础版";
            case "standard": return "标准版";
            case "premium": return "高级版";
            default: return "未知套餐";
        }
    }

    private boolean isFeatureAvailableForPlan(String plan, String featureCode) {
        switch (featureCode) {
            case "basic_operations":
                return true; // 所有套餐都支持基础操作
                
            case "advanced_reports":
                return "standard".equals(plan) || "premium".equals(plan);
                
            case "api_access":
                return "standard".equals(plan) || "premium".equals(plan);
                
            case "custom_fields":
                return "standard".equals(plan) || "premium".equals(plan);
                
            case "data_backup":
                return "standard".equals(plan) || "premium".equals(plan);
                
            case "multi_language":
                return "premium".equals(plan);
                
            case "mobile_app":
                return "premium".equals(plan);
                
            case "workflow":
                return "premium".equals(plan);
                
            default:
                return false;
        }
    }

    /**
     * 检查订阅是否即将过期（7天内）
     */
    public boolean isSubscriptionExpiringSoon() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return false;
        }

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null || tenant.getSubscriptionEnd() == null) {
            return false;
        }

        LocalDate endDate = tenant.getSubscriptionEnd();
        LocalDate now = LocalDate.now();
        LocalDate warningDate = now.plusDays(7);
        
        return endDate.isAfter(now) && endDate.isBefore(warningDate);
    }

    /**
     * 检查订阅是否已过期
     */
    public boolean isSubscriptionExpired() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return false;
        }

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null || tenant.getSubscriptionEnd() == null) {
            return false;
        }

        return tenant.getSubscriptionEnd().isBefore(LocalDate.now());
    }
}