package com.garment.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.garment.admin.entity.SubscriptionPlan;
import com.garment.admin.service.SubscriptionPlanService;
import com.garment.admin.dto.subscription.CreatePlanRequest;
import com.garment.admin.dto.subscription.UpdatePlanRequest;
import com.garment.admin.dto.subscription.PlanQueryRequest;
import com.garment.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 订阅套餐控制器
 */
@Tag(name = "订阅套餐管理")
@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @Operation(summary = "分页查询套餐")
    @PostMapping("/query")
    public Result<IPage<SubscriptionPlan>> queryPlans(@RequestBody PlanQueryRequest request) {
        IPage<SubscriptionPlan> result = subscriptionPlanService.queryPlans(request);
        return Result.success(result);
    }

    @Operation(summary = "获取所有有效套餐")
    @GetMapping("/active")
    public Result<List<SubscriptionPlan>> getActivePlans() {
        List<SubscriptionPlan> plans = subscriptionPlanService.getActivePlans();
        return Result.success(plans);
    }

    @Operation(summary = "获取套餐详情")
    @GetMapping("/{id}")
    public Result<SubscriptionPlan> getPlan(@PathVariable Long id) {
        SubscriptionPlan plan = subscriptionPlanService.getById(id);
        return Result.success(plan);
    }

    @Operation(summary = "根据套餐代码获取套餐")
    @GetMapping("/code/{planCode}")
    public Result<SubscriptionPlan> getPlanByCode(@PathVariable String planCode) {
        SubscriptionPlan plan = subscriptionPlanService.getPlanByCode(planCode);
        return Result.success(plan);
    }

    @Operation(summary = "创建套餐")
    @PostMapping
    public Result<SubscriptionPlan> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanService.createPlan(request);
        return Result.success(plan);
    }

    @Operation(summary = "更新套餐")
    @PutMapping("/{id}")
    public Result<SubscriptionPlan> updatePlan(@PathVariable Long id,
                                              @Valid @RequestBody UpdatePlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanService.updatePlan(id, request);
        return Result.success(plan);
    }

    @Operation(summary = "启用/停用套餐")
    @PostMapping("/{id}/toggle-status")
    public Result<Void> togglePlanStatus(@PathVariable Long id) {
        subscriptionPlanService.togglePlanStatus(id);
        return Result.success();
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        subscriptionPlanService.deletePlan(id);
        return Result.success();
    }
}




