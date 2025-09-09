package com.garment.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.garment.admin.entity.Subscription;
import com.garment.admin.service.SubscriptionService;
import com.garment.admin.dto.subscription.CreateSubscriptionRequest;
import com.garment.admin.dto.subscription.UpdateSubscriptionRequest;
import com.garment.admin.dto.subscription.SubscriptionQueryRequest;
import com.garment.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 租户订阅控制器
 */
@Tag(name = "租户订阅管理")
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "分页查询订阅")
    @PostMapping("/query")
    public Result<IPage<Subscription>> querySubscriptions(@RequestBody SubscriptionQueryRequest request) {
        IPage<Subscription> result = subscriptionService.querySubscriptions(request);
        return Result.success(result);
    }

    @Operation(summary = "获取订阅详情")
    @GetMapping("/{id}")
    public Result<Subscription> getSubscription(@PathVariable Long id) {
        Subscription subscription = subscriptionService.getById(id);
        return Result.success(subscription);
    }

    @Operation(summary = "创建订阅")
    @PostMapping
    public Result<Subscription> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionService.createSubscription(request);
        return Result.success(subscription);
    }

    @Operation(summary = "更新订阅")
    @PutMapping("/{id}")
    public Result<Subscription> updateSubscription(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateSubscriptionRequest request) {
        Subscription subscription = subscriptionService.updateSubscription(id, request);
        return Result.success(subscription);
    }

    @Operation(summary = "续费订阅")
    @PostMapping("/{id}/renew")
    public Result<Subscription> renewSubscription(@PathVariable Long id,
                                                  @RequestParam Integer months) {
        Subscription subscription = subscriptionService.renewSubscription(id, months);
        return Result.success(subscription);
    }

    @Operation(summary = "取消订阅")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelSubscription(@PathVariable Long id,
                                          @RequestParam(required = false) String reason) {
        subscriptionService.cancelSubscription(id, reason);
        return Result.success();
    }

    @Operation(summary = "暂停订阅")
    @PostMapping("/{id}/suspend")
    public Result<Void> suspendSubscription(@PathVariable Long id,
                                           @RequestParam(required = false) String reason) {
        subscriptionService.suspendSubscription(id, reason);
        return Result.success();
    }

    @Operation(summary = "恢复订阅")
    @PostMapping("/{id}/resume")
    public Result<Void> resumeSubscription(@PathVariable Long id) {
        subscriptionService.resumeSubscription(id);
        return Result.success();
    }

    @Operation(summary = "获取租户当前订阅")
    @GetMapping("/tenant/{tenantId}/current")
    public Result<Subscription> getCurrentSubscription(@PathVariable Long tenantId) {
        Subscription subscription = subscriptionService.getCurrentSubscription(tenantId);
        return Result.success(subscription);
    }

    @Operation(summary = "检查租户订阅状态")
    @GetMapping("/tenant/{tenantId}/check")
    public Result<Boolean> checkTenantSubscription(@PathVariable Long tenantId) {
        boolean isValid = subscriptionService.checkTenantSubscription(tenantId);
        return Result.success(isValid);
    }

    @Operation(summary = "检查功能权限")
    @GetMapping("/tenant/{tenantId}/feature/{feature}")
    public Result<Boolean> checkFeaturePermission(@PathVariable Long tenantId,
                                                  @PathVariable String feature) {
        boolean hasPermission = subscriptionService.checkFeaturePermission(tenantId, feature);
        return Result.success(hasPermission);
    }

    @Operation(summary = "检查用户数量限制")
    @GetMapping("/tenant/{tenantId}/user-limit")
    public Result<Boolean> checkUserLimit(@PathVariable Long tenantId,
                                         @RequestParam int currentUserCount) {
        boolean withinLimit = subscriptionService.checkUserLimit(tenantId, currentUserCount);
        return Result.success(withinLimit);
    }

    @Operation(summary = "检查车间数量限制")
    @GetMapping("/tenant/{tenantId}/workshop-limit")
    public Result<Boolean> checkWorkshopLimit(@PathVariable Long tenantId,
                                             @RequestParam int currentWorkshopCount) {
        boolean withinLimit = subscriptionService.checkWorkshopLimit(tenantId, currentWorkshopCount);
        return Result.success(withinLimit);
    }

    @Operation(summary = "检查存储空间限制")
    @GetMapping("/tenant/{tenantId}/storage-limit")
    public Result<Boolean> checkStorageLimit(@PathVariable Long tenantId,
                                            @RequestParam long currentStorageUsage) {
        boolean withinLimit = subscriptionService.checkStorageLimit(tenantId, currentStorageUsage);
        return Result.success(withinLimit);
    }
}




