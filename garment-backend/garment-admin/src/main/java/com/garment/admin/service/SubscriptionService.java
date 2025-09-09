package com.garment.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.admin.entity.Subscription;
import com.garment.admin.dto.subscription.CreateSubscriptionRequest;
import com.garment.admin.dto.subscription.UpdateSubscriptionRequest;
import com.garment.admin.dto.subscription.SubscriptionQueryRequest;

/**
 * 租户订阅服务接口
 */
public interface SubscriptionService extends IService<Subscription> {

    /**
     * 分页查询订阅信息
     */
    IPage<Subscription> querySubscriptions(SubscriptionQueryRequest request);

    /**
     * 创建订阅
     */
    Subscription createSubscription(CreateSubscriptionRequest request);

    /**
     * 更新订阅
     */
    Subscription updateSubscription(Long id, UpdateSubscriptionRequest request);

    /**
     * 续费订阅
     */
    Subscription renewSubscription(Long id, Integer months);

    /**
     * 取消订阅
     */
    void cancelSubscription(Long id, String reason);

    /**
     * 暂停订阅
     */
    void suspendSubscription(Long id, String reason);

    /**
     * 恢复订阅
     */
    void resumeSubscription(Long id);

    /**
     * 检查租户订阅状态
     */
    boolean checkTenantSubscription(Long tenantId);

    /**
     * 获取租户当前有效订阅
     */
    Subscription getCurrentSubscription(Long tenantId);

    /**
     * 检查功能权限
     */
    boolean checkFeaturePermission(Long tenantId, String feature);

    /**
     * 检查用户数量限制
     */
    boolean checkUserLimit(Long tenantId, int currentUserCount);

    /**
     * 检查车间数量限制
     */
    boolean checkWorkshopLimit(Long tenantId, int currentWorkshopCount);

    /**
     * 检查存储空间限制
     */
    boolean checkStorageLimit(Long tenantId, long currentStorageUsage);

    /**
     * 处理过期订阅
     */
    void handleExpiredSubscriptions();
}




