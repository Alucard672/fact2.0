package com.garment.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.admin.entity.SubscriptionPlan;
import com.garment.admin.dto.subscription.CreatePlanRequest;
import com.garment.admin.dto.subscription.UpdatePlanRequest;
import com.garment.admin.dto.subscription.PlanQueryRequest;

import java.util.List;

/**
 * 订阅套餐服务接口
 */
public interface SubscriptionPlanService extends IService<SubscriptionPlan> {

    /**
     * 分页查询套餐
     */
    IPage<SubscriptionPlan> queryPlans(PlanQueryRequest request);

    /**
     * 获取所有有效套餐
     */
    List<SubscriptionPlan> getActivePlans();

    /**
     * 创建套餐
     */
    SubscriptionPlan createPlan(CreatePlanRequest request);

    /**
     * 更新套餐
     */
    SubscriptionPlan updatePlan(Long id, UpdatePlanRequest request);

    /**
     * 启用/停用套餐
     */
    void togglePlanStatus(Long id);

    /**
     * 删除套餐
     */
    void deletePlan(Long id);

    /**
     * 根据套餐代码获取套餐
     */
    SubscriptionPlan getPlanByCode(String planCode);
}




