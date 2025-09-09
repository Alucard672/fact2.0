package com.garment.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.auth.dto.PlanInfo;
import com.garment.auth.dto.SubscriptionRequest;
import com.garment.auth.dto.SubscriptionResponse;
import com.garment.auth.service.SubscriptionService;
import com.garment.common.annotation.RequireRole;
import com.garment.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订阅管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@Api(tags = "订阅管理")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 获取所有套餐信息（公开接口）
     */
    @GetMapping("/plans")
    @ApiOperation("获取所有套餐信息")
    public Result<List<PlanInfo>> getAllPlans() {
        try {
            List<PlanInfo> plans = subscriptionService.getAllPlans();
            return Result.success(plans);
        } catch (Exception e) {
            log.error("获取套餐信息失败: {}", e.getMessage());
            return Result.error("获取套餐信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前订阅
     */
    @GetMapping("/current")
    @ApiOperation("获取当前订阅")
    public Result<SubscriptionResponse> getCurrentSubscription() {
        try {
            SubscriptionResponse subscription = subscriptionService.getCurrentSubscription();
            return Result.success(subscription);
        } catch (Exception e) {
            log.error("获取当前订阅失败: {}", e.getMessage());
            return Result.error("获取当前订阅失败: " + e.getMessage());
        }
    }

    /**
     * 创建订阅
     */
    @PostMapping
    @ApiOperation("创建订阅")
    @RequireRole({"owner", "admin"})
    public Result<SubscriptionResponse> createSubscription(@RequestBody @Validated SubscriptionRequest request) {
        try {
            SubscriptionResponse response = subscriptionService.createSubscription(request);
            return Result.success("订阅创建成功", response);
        } catch (Exception e) {
            log.error("创建订阅失败: {}", e.getMessage());
            return Result.error("创建订阅失败: " + e.getMessage());
        }
    }

    /**
     * 确认支付
     */
    @PostMapping("/confirm-payment")
    @ApiOperation("确认支付")
    public Result<SubscriptionResponse> confirmPayment(@RequestParam String orderNo) {
        try {
            SubscriptionResponse response = subscriptionService.confirmPayment(orderNo);
            return Result.success("支付确认成功", response);
        } catch (Exception e) {
            log.error("确认支付失败: {}", e.getMessage());
            return Result.error("确认支付失败: " + e.getMessage());
        }
    }

    /**
     * 取消订阅
     */
    @PostMapping("/{subscriptionId}/cancel")
    @ApiOperation("取消订阅")
    @RequireRole({"owner", "admin"})
    public Result<Void> cancelSubscription(@ApiParam("订阅ID") @PathVariable Long subscriptionId) {
        try {
            subscriptionService.cancelSubscription(subscriptionId);
            return Result.success("订阅已取消");
        } catch (Exception e) {
            log.error("取消订阅失败: {}", e.getMessage());
            return Result.error("取消订阅失败: " + e.getMessage());
        }
    }

    /**
     * 获取订阅历史
     */
    @GetMapping("/history")
    @ApiOperation("获取订阅历史")
    @RequireRole({"owner", "admin"})
    public Result<Page<SubscriptionResponse>> getSubscriptionHistory(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<SubscriptionResponse> response = subscriptionService.getSubscriptionHistory(page, size);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取订阅历史失败: {}", e.getMessage());
            return Result.error("获取订阅历史失败: " + e.getMessage());
        }
    }

    /**
     * 续费
     */
    @PostMapping("/renew")
    @ApiOperation("续费订阅")
    @RequireRole({"owner", "admin"})
    public Result<SubscriptionResponse> renewSubscription(@RequestBody @Validated SubscriptionRequest request) {
        try {
            SubscriptionResponse response = subscriptionService.renewSubscription(request);
            return Result.success("续费成功", response);
        } catch (Exception e) {
            log.error("续费失败: {}", e.getMessage());
            return Result.error("续费失败: " + e.getMessage());
        }
    }

    /**
     * 升级套餐
     */
    @PostMapping("/upgrade")
    @ApiOperation("升级套餐")
    @RequireRole({"owner", "admin"})
    public Result<SubscriptionResponse> upgradeSubscription(@RequestBody @Validated SubscriptionRequest request) {
        try {
            // 升级逻辑与创建订阅类似，但需要处理差价
            SubscriptionResponse response = subscriptionService.createSubscription(request);
            return Result.success("套餐升级成功", response);
        } catch (Exception e) {
            log.error("升级套餐失败: {}", e.getMessage());
            return Result.error("升级套餐失败: " + e.getMessage());
        }
    }

    /**
     * 处理过期订阅（定时任务接口）
     */
    @PostMapping("/process-expired")
    @ApiOperation("处理过期订阅")
    public Result<Void> processExpiredSubscriptions() {
        try {
            subscriptionService.processExpiredSubscriptions();
            return Result.success("过期订阅处理完成");
        } catch (Exception e) {
            log.error("处理过期订阅失败: {}", e.getMessage());
            return Result.error("处理过期订阅失败: " + e.getMessage());
        }
    }

    /**
     * 获取套餐价格计算
     */
    @GetMapping("/calculate-price")
    @ApiOperation("计算套餐价格")
    public Result<Object> calculatePrice(
            @RequestParam String planType,
            @RequestParam Integer duration,
            @RequestParam(required = false) String couponCode) {
        try {
            // TODO: 实现价格计算逻辑
            return Result.success("价格计算功能待实现");
        } catch (Exception e) {
            log.error("计算价格失败: {}", e.getMessage());
            return Result.error("计算价格失败: " + e.getMessage());
        }
    }
}