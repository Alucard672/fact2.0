package com.garment.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.auth.dto.PlanInfo;
import com.garment.auth.dto.SubscriptionRequest;
import com.garment.auth.dto.SubscriptionResponse;
import com.garment.auth.entity.Subscription;
import com.garment.auth.entity.Tenant;
import com.garment.auth.mapper.SubscriptionMapper;
import com.garment.auth.mapper.TenantMapper;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订阅服务
 *
 * @author garment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;
    private final TenantMapper tenantMapper;

    /**
     * 获取所有套餐信息
     */
    public List<PlanInfo> getAllPlans() {
        return Arrays.asList(
            createPlanInfo("trial", "试用版", "免费试用30天", 
                BigDecimal.ZERO, BigDecimal.ZERO, 5, 1024L * 1024 * 1024, "基础",
                Arrays.asList("基础功能", "5个用户", "1GB存储", "邮件支持"),
                Arrays.asList("功能受限", "数据保留30天"), false, false, 30),
                
            createPlanInfo("basic", "基础版", "适合小型工厂", 
                new BigDecimal("299"), new BigDecimal("2990"), 20, 10L * 1024 * 1024 * 1024, "邮件",
                Arrays.asList("完整功能", "20个用户", "10GB存储", "邮件支持", "数据备份"),
                Arrays.asList("用户数限制", "存储空间限制"), false, true, 7),
                
            createPlanInfo("standard", "标准版", "适合中型工厂", 
                new BigDecimal("899"), new BigDecimal("8990"), 50, 50L * 1024 * 1024 * 1024, "电话+邮件",
                Arrays.asList("高级功能", "50个用户", "50GB存储", "电话+邮件支持", "高级报表", "API接口"),
                Arrays.asList("存储空间限制"), true, false, 7),
                
            createPlanInfo("premium", "高级版", "适合大型工厂", 
                new BigDecimal("1999"), new BigDecimal("19990"), 999, 200L * 1024 * 1024 * 1024, "专属客服",
                Arrays.asList("全部功能", "无限用户", "200GB存储", "专属客服", "定制开发", "私有部署"),
                Arrays.asList(), false, false, 7)
        );
    }

    /**
     * 获取当前租户的订阅信息
     */
    public SubscriptionResponse getCurrentSubscription() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw BusinessException.badRequest("租户上下文未设置");
        }

        Subscription subscription = subscriptionMapper.findCurrentSubscription(tenantId);
        if (subscription == null) {
            // 如果没有订阅记录，返回试用版信息
            return createTrialSubscriptionResponse(tenantId);
        }

        return convertToResponse(subscription);
    }

    /**
     * 创建订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw BusinessException.badRequest("租户上下文未设置");
        }

        log.info("创建订阅: tenantId={}, planType={}, duration={}", tenantId, request.getPlanType(), request.getDuration());

        // 1. 验证套餐类型
        if (!isValidPlanType(request.getPlanType())) {
            throw BusinessException.badRequest("无效的套餐类型");
        }

        // 2. 计算价格
        BigDecimal originalAmount = calculateOriginalAmount(request.getPlanType(), request.getDuration());
        BigDecimal discountAmount = calculateDiscountAmount(originalAmount, request.getCouponCode());
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);

        // 3. 创建订阅记录
        Subscription subscription = new Subscription();
        subscription.setTenantId(tenantId);
        subscription.setPlanType(request.getPlanType());
        subscription.setDuration(request.getDuration());
        subscription.setOriginalAmount(originalAmount);
        subscription.setDiscountAmount(discountAmount);
        subscription.setFinalAmount(finalAmount);
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setPaymentStatus("pending");
        subscription.setPaymentOrderNo(generateOrderNo());
        subscription.setCouponCode(request.getCouponCode());
        subscription.setStatus("pending");
        subscription.setRemark(request.getRemark());

        subscriptionMapper.insert(subscription);

        log.info("订阅创建成功: id={}, orderNo={}", subscription.getId(), subscription.getPaymentOrderNo());

        return convertToResponse(subscription);
    }

    /**
     * 确认支付
     */
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionResponse confirmPayment(String orderNo) {
        log.info("确认支付: orderNo={}", orderNo);

        Subscription subscription = subscriptionMapper.findByPaymentOrderNo(orderNo);
        if (subscription == null) {
            throw BusinessException.notFound("订阅记录不存在");
        }

        if (!"pending".equals(subscription.getPaymentStatus())) {
            throw BusinessException.badRequest("订阅状态不正确");
        }

        // 1. 更新订阅状态
        subscription.setPaymentStatus("paid");
        subscription.setStatus("active");
        subscription.setPaidAt(LocalDateTime.now());

        // 2. 设置订阅有效期
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(subscription.getDuration());
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);

        subscriptionMapper.updateById(subscription);

        // 3. 更新租户套餐信息
        updateTenantPlan(subscription.getTenantId(), subscription.getPlanType(), endDate);

        log.info("支付确认成功: subscriptionId={}", subscription.getId());

        return convertToResponse(subscription);
    }

    /**
     * 取消订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscription(Long subscriptionId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        
        Subscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null || !subscription.getTenantId().equals(tenantId)) {
            throw BusinessException.notFound("订阅记录不存在");
        }

        if ("cancelled".equals(subscription.getStatus())) {
            throw BusinessException.badRequest("订阅已取消");
        }

        subscription.setStatus("cancelled");
        subscriptionMapper.updateById(subscription);

        log.info("订阅已取消: subscriptionId={}", subscriptionId);
    }

    /**
     * 获取订阅历史
     */
    public Page<SubscriptionResponse> getSubscriptionHistory(int page, int size) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw BusinessException.badRequest("租户上下文未设置");
        }

        Page<Subscription> pageRequest = new Page<>(page, size);
        QueryWrapper<Subscription> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tenant_id", tenantId)
                   .eq("deleted", 0)
                   .orderByDesc("created_at");

        Page<Subscription> subscriptionPage = subscriptionMapper.selectPage(pageRequest, queryWrapper);

        // 转换为响应DTO
        Page<SubscriptionResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(subscriptionPage, responsePage);

        List<SubscriptionResponse> responseList = subscriptionPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    /**
     * 续费
     */
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionResponse renewSubscription(SubscriptionRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();
        
        // 获取当前订阅
        Subscription currentSubscription = subscriptionMapper.findCurrentSubscription(tenantId);
        
        // 创建新的订阅记录
        Subscription newSubscription = new Subscription();
        newSubscription.setTenantId(tenantId);
        newSubscription.setPlanType(request.getPlanType());
        newSubscription.setDuration(request.getDuration());
        
        // 计算价格
        BigDecimal originalAmount = calculateOriginalAmount(request.getPlanType(), request.getDuration());
        BigDecimal discountAmount = calculateDiscountAmount(originalAmount, request.getCouponCode());
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);
        
        newSubscription.setOriginalAmount(originalAmount);
        newSubscription.setDiscountAmount(discountAmount);
        newSubscription.setFinalAmount(finalAmount);
        newSubscription.setPaymentMethod(request.getPaymentMethod());
        newSubscription.setPaymentStatus("pending");
        newSubscription.setPaymentOrderNo(generateOrderNo());
        newSubscription.setCouponCode(request.getCouponCode());
        newSubscription.setStatus("pending");
        newSubscription.setRemark("续费订阅");

        // 设置开始时间为当前订阅结束时间（如果有的话）
        if (currentSubscription != null && currentSubscription.getEndDate() != null) {
            newSubscription.setStartDate(currentSubscription.getEndDate());
            newSubscription.setEndDate(currentSubscription.getEndDate().plusMonths(request.getDuration()));
        } else {
            newSubscription.setStartDate(LocalDateTime.now());
            newSubscription.setEndDate(LocalDateTime.now().plusMonths(request.getDuration()));
        }

        subscriptionMapper.insert(newSubscription);

        log.info("续费订阅创建成功: id={}, orderNo={}", newSubscription.getId(), newSubscription.getPaymentOrderNo());

        return convertToResponse(newSubscription);
    }

    /**
     * 处理过期订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionMapper.findExpiredSubscriptions();
        
        for (Subscription subscription : expiredSubscriptions) {
            try {
                // 更新订阅状态为过期
                subscription.setStatus("expired");
                subscriptionMapper.updateById(subscription);
                
                // 将租户降级为试用版
                updateTenantPlan(subscription.getTenantId(), "trial", LocalDateTime.now().plusDays(30));
                
                log.info("处理过期订阅: subscriptionId={}, tenantId={}", subscription.getId(), subscription.getTenantId());
            } catch (Exception e) {
                log.error("处理过期订阅失败: subscriptionId={}, error={}", subscription.getId(), e.getMessage());
            }
        }
    }

    // 私有辅助方法

    private PlanInfo createPlanInfo(String planType, String planName, String description,
                                   BigDecimal monthlyPrice, BigDecimal yearlyPrice, 
                                   Integer maxUsers, Long maxStorage, String supportLevel,
                                   List<String> features, List<String> limitations,
                                   Boolean isPopular, Boolean isRecommended, Integer trialDays) {
        PlanInfo plan = new PlanInfo();
        plan.setPlanType(planType);
        plan.setPlanName(planName);
        plan.setDescription(description);
        plan.setMonthlyPrice(monthlyPrice);
        plan.setYearlyPrice(yearlyPrice);
        plan.setMaxUsers(maxUsers);
        plan.setMaxStorage(maxStorage);
        plan.setStorageUnit("GB");
        plan.setSupportLevel(supportLevel);
        plan.setFeatures(features);
        plan.setLimitations(limitations);
        plan.setIsPopular(isPopular);
        plan.setIsRecommended(isRecommended);
        plan.setTrialDays(trialDays);
        return plan;
    }

    private SubscriptionResponse createTrialSubscriptionResponse(Long tenantId) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setTenantId(tenantId);
        response.setPlanType("trial");
        response.setPlanName("试用版");
        response.setStatus("active");
        response.setStartDate(LocalDateTime.now());
        response.setEndDate(LocalDateTime.now().plusDays(30));
        
        SubscriptionResponse.PlanLimits limits = new SubscriptionResponse.PlanLimits();
        limits.setMaxUsers(5);
        limits.setMaxStorage(1024L * 1024 * 1024);
        limits.setFeatures("基础功能");
        limits.setSupportLevel("基础");
        response.setPlanLimits(limits);
        
        return response;
    }

    private boolean isValidPlanType(String planType) {
        return Arrays.asList("trial", "basic", "standard", "premium").contains(planType);
    }

    private BigDecimal calculateOriginalAmount(String planType, Integer duration) {
        BigDecimal monthlyPrice;
        switch (planType) {
            case "trial":
                monthlyPrice = BigDecimal.ZERO;
                break;
            case "basic":
                monthlyPrice = new BigDecimal("299");
                break;
            case "standard":
                monthlyPrice = new BigDecimal("899");
                break;
            case "premium":
                monthlyPrice = new BigDecimal("1999");
                break;
            default:
                throw BusinessException.badRequest("无效的套餐类型");
        }
        
        BigDecimal amount = monthlyPrice.multiply(new BigDecimal(duration));
        
        // 年付优惠（12个月按10个月计算）
        if (duration >= 12) {
            amount = monthlyPrice.multiply(new BigDecimal(10)).multiply(new BigDecimal(duration / 12));
            if (duration % 12 != 0) {
                amount = amount.add(monthlyPrice.multiply(new BigDecimal(duration % 12)));
            }
        }
        
        return amount;
    }

    private BigDecimal calculateDiscountAmount(BigDecimal originalAmount, String couponCode) {
        // TODO: 实现优惠券逻辑
        return BigDecimal.ZERO;
    }

    private String generateOrderNo() {
        return "SUB" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private void updateTenantPlan(Long tenantId, String planType, LocalDateTime endDate) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant != null) {
            tenant.setSubscriptionPlan(planType);
            tenant.setSubscriptionEnd(endDate.toLocalDate());
            
            // 更新套餐限制
            switch (planType) {
                case "trial":
                    tenant.setMaxUsers(5);
                    tenant.setMaxStorage(1024L * 1024 * 1024);
                    break;
                case "basic":
                    tenant.setMaxUsers(20);
                    tenant.setMaxStorage(10L * 1024 * 1024 * 1024);
                    break;
                case "standard":
                    tenant.setMaxUsers(50);
                    tenant.setMaxStorage(50L * 1024 * 1024 * 1024);
                    break;
                case "premium":
                    tenant.setMaxUsers(999);
                    tenant.setMaxStorage(200L * 1024 * 1024 * 1024);
                    break;
            }
            
            tenantMapper.updateById(tenant);
        }
    }

    private SubscriptionResponse convertToResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        BeanUtils.copyProperties(subscription, response);

        // 获取租户名称
        Tenant tenant = tenantMapper.selectById(subscription.getTenantId());
        if (tenant != null) {
            response.setTenantName(tenant.getCompanyName());
        }

        // 设置套餐名称
        PlanInfo planInfo = getAllPlans().stream()
                .filter(plan -> plan.getPlanType().equals(subscription.getPlanType()))
                .findFirst()
                .orElse(null);
        if (planInfo != null) {
            response.setPlanName(planInfo.getPlanName());
            
            SubscriptionResponse.PlanLimits limits = new SubscriptionResponse.PlanLimits();
            limits.setMaxUsers(planInfo.getMaxUsers());
            limits.setMaxStorage(planInfo.getMaxStorage());
            limits.setFeatures(String.join(", ", planInfo.getFeatures()));
            limits.setSupportLevel(planInfo.getSupportLevel());
            response.setPlanLimits(limits);
        }

        return response;
    }
}