package com.garment.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.admin.entity.SubscriptionPlan;
import com.garment.admin.mapper.SubscriptionPlanMapper;
import com.garment.admin.service.SubscriptionPlanService;
import com.garment.admin.dto.subscription.CreatePlanRequest;
import com.garment.admin.dto.subscription.UpdatePlanRequest;
import com.garment.admin.dto.subscription.PlanQueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 订阅套餐服务实现
 */
@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl extends ServiceImpl<SubscriptionPlanMapper, SubscriptionPlan> implements SubscriptionPlanService {

    @Override
    public IPage<SubscriptionPlan> queryPlans(PlanQueryRequest request) {
        Page<SubscriptionPlan> page = new Page<>(request.getPage(), request.getSize());
        
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getPlanCode()), SubscriptionPlan::getPlanCode, request.getPlanCode())
               .like(StringUtils.hasText(request.getPlanName()), SubscriptionPlan::getPlanName, request.getPlanName())
               .eq(StringUtils.hasText(request.getPlanType()), SubscriptionPlan::getPlanType, request.getPlanType())
               .eq(StringUtils.hasText(request.getStatus()), SubscriptionPlan::getStatus, request.getStatus())
               .eq(request.getIsRecommended() != null, SubscriptionPlan::getIsRecommended, request.getIsRecommended())
               .orderByAsc(SubscriptionPlan::getSortOrder)
               .orderByDesc(SubscriptionPlan::getCreatedAt);
        
        return page(page, wrapper);
    }

    @Override
    public List<SubscriptionPlan> getActivePlans() {
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubscriptionPlan::getStatus, "active")
               .orderByAsc(SubscriptionPlan::getSortOrder)
               .orderByDesc(SubscriptionPlan::getIsRecommended);
        
        return list(wrapper);
    }

    @Override
    @Transactional
    public SubscriptionPlan createPlan(CreatePlanRequest request) {
        // 检查套餐代码是否已存在
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubscriptionPlan::getPlanCode, request.getPlanCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("套餐代码已存在");
        }

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanCode(request.getPlanCode());
        plan.setPlanName(request.getPlanName());
        plan.setDescription(request.getDescription());
        plan.setPlanType(request.getPlanType());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setTrialDays(request.getTrialDays());
        plan.setUserLimit(request.getUserLimit());
        plan.setWorkshopLimit(request.getWorkshopLimit());
        plan.setStorageLimit(request.getStorageLimit());
        plan.setFeatures(request.getFeatures() != null ? String.join(",", request.getFeatures()) : null);
        plan.setIsRecommended(request.getIsRecommended());
        plan.setSortOrder(request.getSortOrder());
        plan.setStatus("active");

        save(plan);
        return plan;
    }

    @Override
    @Transactional
    public SubscriptionPlan updatePlan(Long id, UpdatePlanRequest request) {
        SubscriptionPlan plan = getById(id);
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        if (StringUtils.hasText(request.getPlanName())) {
            plan.setPlanName(request.getPlanName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            plan.setDescription(request.getDescription());
        }
        if (request.getMonthlyPrice() != null) {
            plan.setMonthlyPrice(request.getMonthlyPrice());
        }
        if (request.getYearlyPrice() != null) {
            plan.setYearlyPrice(request.getYearlyPrice());
        }
        if (request.getTrialDays() != null) {
            plan.setTrialDays(request.getTrialDays());
        }
        if (request.getUserLimit() != null) {
            plan.setUserLimit(request.getUserLimit());
        }
        if (request.getWorkshopLimit() != null) {
            plan.setWorkshopLimit(request.getWorkshopLimit());
        }
        if (request.getStorageLimit() != null) {
            plan.setStorageLimit(request.getStorageLimit());
        }
        if (request.getFeatures() != null) {
            plan.setFeatures(String.join(",", request.getFeatures()));
        }
        if (request.getIsRecommended() != null) {
            plan.setIsRecommended(request.getIsRecommended());
        }
        if (request.getSortOrder() != null) {
            plan.setSortOrder(request.getSortOrder());
        }
        if (StringUtils.hasText(request.getStatus())) {
            plan.setStatus(request.getStatus());
        }

        updateById(plan);
        return plan;
    }

    @Override
    @Transactional
    public void togglePlanStatus(Long id) {
        SubscriptionPlan plan = getById(id);
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        String newStatus = "active".equals(plan.getStatus()) ? "inactive" : "active";
        plan.setStatus(newStatus);
        updateById(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        SubscriptionPlan plan = getById(id);
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        // 检查是否有关联的订阅
        // TODO: 添加订阅检查逻辑

        removeById(id);
    }

    @Override
    public SubscriptionPlan getPlanByCode(String planCode) {
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubscriptionPlan::getPlanCode, planCode);
        return getOne(wrapper);
    }
}




