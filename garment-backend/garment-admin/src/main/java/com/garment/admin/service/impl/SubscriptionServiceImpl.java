package com.garment.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.admin.entity.Subscription;
import com.garment.admin.entity.SubscriptionPlan;
import com.garment.admin.mapper.SubscriptionMapper;
import com.garment.admin.service.SubscriptionService;
import com.garment.admin.service.SubscriptionPlanService;
import com.garment.admin.dto.subscription.CreateSubscriptionRequest;
import com.garment.admin.dto.subscription.UpdateSubscriptionRequest;
import com.garment.admin.dto.subscription.SubscriptionQueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户订阅服务实现
 */
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements SubscriptionService {

    private final SubscriptionPlanService subscriptionPlanService;

    @Override
    public IPage<Subscription> querySubscriptions(SubscriptionQueryRequest request) {
        Page<Subscription> page = new Page<>(request.getPage(), request.getSize());
        
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getTenantId() != null, Subscription::getTenantId, request.getTenantId())
               .eq(request.getPlanId() != null, Subscription::getPlanId, request.getPlanId())
               .eq(StringUtils.hasText(request.getStatus()), Subscription::getStatus, request.getStatus())
               .eq(request.getAutoRenew() != null, Subscription::getAutoRenew, request.getAutoRenew())
               .ge(request.getStartTimeFrom() != null, Subscription::getStartTime, request.getStartTimeFrom())
               .le(request.getStartTimeTo() != null, Subscription::getStartTime, request.getStartTimeTo())
               .ge(request.getEndTimeFrom() != null, Subscription::getEndTime, request.getEndTimeFrom())
               .le(request.getEndTimeTo() != null, Subscription::getEndTime, request.getEndTimeTo())
               .orderByDesc(Subscription::getCreatedAt);
        
        return page(page, wrapper);
    }

    @Override
    @Transactional
    public Subscription createSubscription(CreateSubscriptionRequest request) {
        // 获取套餐信息
        SubscriptionPlan plan = subscriptionPlanService.getById(request.getPlanId());
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        // 检查是否已有有效订阅
        Subscription existingSubscription = getCurrentSubscription(request.getTenantId());
        if (existingSubscription != null && "active".equals(existingSubscription.getStatus())) {
            throw new RuntimeException("租户已有有效订阅");
        }

        // 创建订阅
        Subscription subscription = new Subscription();
        subscription.setTenantId(request.getTenantId());
        subscription.setPlanId(request.getPlanId());
        subscription.setPlanName(plan.getPlanName());
        subscription.setPlanPrice(plan.getMonthlyPrice().multiply(java.math.BigDecimal.valueOf(request.getMonths())));
        subscription.setStatus("active");
        
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now();
        subscription.setStartTime(startTime);
        subscription.setEndTime(startTime.plusMonths(request.getMonths()));
        
        subscription.setAutoRenew(request.getAutoRenew());
        subscription.setUserLimit(plan.getUserLimit());
        subscription.setWorkshopLimit(plan.getWorkshopLimit());
        subscription.setStorageLimit(plan.getStorageLimit());
        subscription.setFeatures(plan.getFeatures());
        subscription.setRemark(request.getRemark());

        save(subscription);
        return subscription;
    }

    @Override
    @Transactional
    public Subscription updateSubscription(Long id, UpdateSubscriptionRequest request) {
        Subscription subscription = getById(id);
        if (subscription == null) {
            throw new RuntimeException("订阅不存在");
        }

        if (request.getAutoRenew() != null) {
            subscription.setAutoRenew(request.getAutoRenew());
        }
        if (request.getEndTime() != null) {
            subscription.setEndTime(request.getEndTime());
        }
        if (StringUtils.hasText(request.getStatus())) {
            subscription.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getRemark())) {
            subscription.setRemark(request.getRemark());
        }

        updateById(subscription);
        return subscription;
    }

    @Override
    @Transactional
    public Subscription renewSubscription(Long id, Integer months) {
        Subscription subscription = getById(id);
        if (subscription == null) {
            throw new RuntimeException("订阅不存在");
        }

        // 延长订阅时间
        LocalDateTime newEndTime = subscription.getEndTime().plusMonths(months);
        subscription.setEndTime(newEndTime);
        subscription.setStatus("active");

        updateById(subscription);
        return subscription;
    }

    @Override
    @Transactional
    public void cancelSubscription(Long id, String reason) {
        Subscription subscription = getById(id);
        if (subscription == null) {
            throw new RuntimeException("订阅不存在");
        }

        subscription.setStatus("cancelled");
        subscription.setRemark(reason);
        updateById(subscription);
    }

    @Override
    @Transactional
    public void suspendSubscription(Long id, String reason) {
        Subscription subscription = getById(id);
        if (subscription == null) {
            throw new RuntimeException("订阅不存在");
        }

        subscription.setStatus("suspended");
        subscription.setRemark(reason);
        updateById(subscription);
    }

    @Override
    @Transactional
    public void resumeSubscription(Long id) {
        Subscription subscription = getById(id);
        if (subscription == null) {
            throw new RuntimeException("订阅不存在");
        }

        subscription.setStatus("active");
        updateById(subscription);
    }

    @Override
    public boolean checkTenantSubscription(Long tenantId) {
        Subscription subscription = getCurrentSubscription(tenantId);
        return subscription != null && "active".equals(subscription.getStatus()) 
               && subscription.getEndTime().isAfter(LocalDateTime.now());
    }

    @Override
    public Subscription getCurrentSubscription(Long tenantId) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getTenantId, tenantId)
               .in(Subscription::getStatus, "active", "suspended")
               .orderByDesc(Subscription::getCreatedAt)
               .last("LIMIT 1");
        
        return getOne(wrapper);
    }

    @Override
    public boolean checkFeaturePermission(Long tenantId, String feature) {
        Subscription subscription = getCurrentSubscription(tenantId);
        if (subscription == null || !"active".equals(subscription.getStatus())) {
            return false;
        }

        String features = subscription.getFeatures();
        return features != null && features.contains(feature);
    }

    @Override
    public boolean checkUserLimit(Long tenantId, int currentUserCount) {
        Subscription subscription = getCurrentSubscription(tenantId);
        if (subscription == null || !"active".equals(subscription.getStatus())) {
            return false;
        }

        return currentUserCount <= subscription.getUserLimit();
    }

    @Override
    public boolean checkWorkshopLimit(Long tenantId, int currentWorkshopCount) {
        Subscription subscription = getCurrentSubscription(tenantId);
        if (subscription == null || !"active".equals(subscription.getStatus())) {
            return false;
        }

        return currentWorkshopCount <= subscription.getWorkshopLimit();
    }

    @Override
    public boolean checkStorageLimit(Long tenantId, long currentStorageUsage) {
        Subscription subscription = getCurrentSubscription(tenantId);
        if (subscription == null || !"active".equals(subscription.getStatus())) {
            return false;
        }

        return currentStorageUsage <= subscription.getStorageLimit() * 1024 * 1024; // 转换为字节
    }

    @Override
    @Transactional
    public void handleExpiredSubscriptions() {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getStatus, "active")
               .lt(Subscription::getEndTime, LocalDateTime.now());
        
        List<Subscription> expiredSubscriptions = list(wrapper);
        
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus("expired");
            updateById(subscription);
        }
    }
}




