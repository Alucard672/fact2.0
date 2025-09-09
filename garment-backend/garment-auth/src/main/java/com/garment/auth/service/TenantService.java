package com.garment.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.auth.dto.CreateTenantRequest;
import com.garment.auth.dto.TenantResponse;
import com.garment.auth.dto.UpdateTenantRequest;
import com.garment.auth.entity.Tenant;
import com.garment.auth.entity.TenantUser;
import com.garment.auth.entity.User;
import com.garment.auth.mapper.TenantMapper;
import com.garment.auth.mapper.TenantUserMapper;
import com.garment.auth.mapper.UserMapper;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 租户服务
 *
 * @author garment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final TenantUserMapper tenantUserMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建租户
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("创建租户: {}", request.getCompanyName());

        // 1. 检查企业名称是否已存在
        QueryWrapper<Tenant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("company_name", request.getCompanyName())
                   .eq("deleted", 0);
        if (tenantMapper.selectCount(queryWrapper) > 0) {
            throw BusinessException.badRequest("企业名称已存在");
        }

        // 2. 检查联系电话是否已被使用
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("contact_phone", request.getContactPhone())
                   .eq("deleted", 0);
        if (tenantMapper.selectCount(queryWrapper) > 0) {
            throw BusinessException.badRequest("联系电话已被使用");
        }

        // 3. 创建管理员用户（如果指定了用户信息）
        User adminUser = null;
        if (StringUtils.hasText(request.getAdminPhone()) || StringUtils.hasText(request.getAdminUsername())) {
            adminUser = createAdminUser(request);
        }

        // 4. 创建租户
        Tenant tenant = new Tenant();
        BeanUtils.copyProperties(request, tenant);
        tenant.setTenantCode(generateTenantCode());
        tenant.setStatus("pending");
        tenant.setMaxUsers(getMaxUsersByPlan(request.getSubscriptionPlan()));
        tenant.setMaxStorage(getMaxStorageByPlan(request.getSubscriptionPlan()));
        tenant.setCreatedBy(adminUser != null ? adminUser.getId() : 0L);

        // 设置订阅信息
        if ("trial".equals(request.getSubscriptionPlan())) {
            tenant.setSubscriptionStart(LocalDate.now());
            tenant.setSubscriptionEnd(LocalDate.now().plusDays(30)); // 试用30天
        }

        tenantMapper.insert(tenant);
        log.info("租户创建成功: tenantCode={}, id={}", tenant.getTenantCode(), tenant.getId());

        // 5. 创建租户用户关联
        if (adminUser != null) {
            TenantUser tenantUser = new TenantUser();
            tenantUser.setTenantId(tenant.getId());
            tenantUser.setUserId(adminUser.getId());
            tenantUser.setRole("owner");
            tenantUser.setEmployeeNo("ADMIN001");
            tenantUser.setStatus("active");
            tenantUser.setJoinedAt(LocalDateTime.now());

            tenantUserMapper.insert(tenantUser);

            // 更新用户的当前租户
            userMapper.updateCurrentTenant(adminUser.getId(), tenant.getId());
        }

        // 6. 激活租户（试用版直接激活）
        if ("trial".equals(request.getSubscriptionPlan())) {
            tenant.setStatus("active");
            tenantMapper.updateById(tenant);
        }

        return convertToResponse(tenant);
    }

    /**
     * 获取租户信息
     */
    public TenantResponse getTenantInfo(Long tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null || tenant.getDeleted()) {
            throw BusinessException.notFound("租户不存在");
        }

        TenantResponse response = convertToResponse(tenant);
        
        // 添加统计信息
        response.setStats(getTenantStats(tenantId));
        
        return response;
    }

    /**
     * 获取当前租户信息
     */
    public TenantResponse getCurrentTenantInfo() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw BusinessException.badRequest("未设置租户上下文");
        }
        return getTenantInfo(tenantId);
    }

    /**
     * 更新租户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantResponse updateTenant(Long tenantId, UpdateTenantRequest request) {
        log.info("更新租户信息: tenantId={}", tenantId);

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null || tenant.getDeleted()) {
            throw BusinessException.notFound("租户不存在");
        }

        // 检查权限（只有租户拥有者或管理员可以修改）
        checkTenantPermission(tenantId);

        // 检查企业名称是否与其他租户重复
        if (!request.getCompanyName().equals(tenant.getCompanyName())) {
            QueryWrapper<Tenant> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("company_name", request.getCompanyName())
                       .ne("id", tenantId)
                       .eq("deleted", 0);
            if (tenantMapper.selectCount(queryWrapper) > 0) {
                throw BusinessException.badRequest("企业名称已存在");
            }
        }

        // 更新租户信息
        BeanUtils.copyProperties(request, tenant);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.updateById(tenant);

        log.info("租户信息更新成功: tenantId={}", tenantId);
        return convertToResponse(tenant);
    }

    /**
     * 分页查询租户列表（系统管理员功能）
     */
    public Page<TenantResponse> getTenantList(int page, int size, String keyword, String status) {
        Page<Tenant> pageRequest = new Page<>(page, size);
        
        QueryWrapper<Tenant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like("company_name", keyword)
                .or().like("tenant_code", keyword)
                .or().like("contact_person", keyword)
                .or().like("contact_phone", keyword)
            );
        }
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<Tenant> tenantPage = tenantMapper.selectPage(pageRequest, queryWrapper);
        
        // 转换为响应DTO
        Page<TenantResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(tenantPage, responsePage);
        
        List<TenantResponse> responseList = tenantPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);
        
        return responsePage;
    }

    /**
     * 删除租户（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenant(Long tenantId) {
        log.info("删除租户: tenantId={}", tenantId);

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null || tenant.getDeleted()) {
            throw BusinessException.notFound("租户不存在");
        }

        // 检查权限（只有系统管理员可以删除租户）
        if (!TenantContext.getCurrentUserSystemAdmin()) {
            throw BusinessException.forbidden("权限不足");
        }

        // 软删除租户
        tenant.setDeleted(true);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.updateById(tenant);

        log.info("租户删除成功: tenantId={}", tenantId);
    }

    /**
     * 创建管理员用户
     */
    private User createAdminUser(CreateTenantRequest request) {
        User user = new User();
        
        if (StringUtils.hasText(request.getAdminPhone())) {
            // 检查手机号是否已存在
            User existUser = userMapper.findByPhone(request.getAdminPhone());
            if (existUser != null) {
                throw BusinessException.badRequest("管理员手机号已被使用");
            }
            user.setPhone(request.getAdminPhone());
        }
        
        if (StringUtils.hasText(request.getAdminUsername())) {
            // 检查用户名是否已存在
            User existUser = userMapper.findByUsername(request.getAdminUsername());
            if (existUser != null) {
                throw BusinessException.badRequest("管理员用户名已被使用");
            }
            user.setUsername(request.getAdminUsername());
        }
        
        user.setName(StringUtils.hasText(request.getAdminName()) ? request.getAdminName() : request.getContactPerson());
        
        if (StringUtils.hasText(request.getAdminPassword())) {
            user.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        }
        
        user.setStatus("active");
        userMapper.insert(user);
        
        return user;
    }

    /**
     * 生成租户编码
     */
    private String generateTenantCode() {
        String prefix = "T" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + suffix;
    }

    /**
     * 根据套餐获取最大用户数
     */
    private Integer getMaxUsersByPlan(String plan) {
        switch (plan) {
            case "trial": return 5;
            case "basic": return 20;
            case "standard": return 50;
            case "premium": return 999;
            default: return 5;
        }
    }

    /**
     * 根据套餐获取最大存储空间
     */
    private Long getMaxStorageByPlan(String plan) {
        switch (plan) {
            case "trial": return 1024L * 1024 * 1024; // 1GB
            case "basic": return 10L * 1024 * 1024 * 1024; // 10GB
            case "standard": return 50L * 1024 * 1024 * 1024; // 50GB
            case "premium": return 200L * 1024 * 1024 * 1024; // 200GB
            default: return 1024L * 1024 * 1024; // 1GB
        }
    }

    /**
     * 检查租户权限
     */
    private void checkTenantPermission(Long tenantId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (!tenantId.equals(currentTenantId)) {
            throw BusinessException.forbidden("无权访问该租户");
        }
        
        // 检查用户角色
        String role = TenantContext.getCurrentUserRole();
        if (!"owner".equals(role) && !"admin".equals(role)) {
            throw BusinessException.forbidden("权限不足");
        }
    }

    /**
     * 获取租户统计信息
     */
    private TenantResponse.TenantStats getTenantStats(Long tenantId) {
        TenantResponse.TenantStats stats = new TenantResponse.TenantStats();
        
        // 用户数量统计
        QueryWrapper<TenantUser> tenantUserQuery = new QueryWrapper<>();
        tenantUserQuery.eq("tenant_id", tenantId).eq("deleted", 0);
        stats.setUserCount(tenantUserMapper.selectCount(tenantUserQuery).intValue());
        
        tenantUserQuery.eq("status", "active");
        stats.setActiveUserCount(tenantUserMapper.selectCount(tenantUserQuery).intValue());
        
        // TODO: 其他统计信息（车间、款式、工序等）需要在相应模块完成后添加
        stats.setStorageUsed(0L);
        stats.setWorkshopCount(0);
        stats.setStyleCount(0);
        stats.setProcessCount(0);
        
        return stats;
    }

    /**
     * 转换为响应DTO
     */
    private TenantResponse convertToResponse(Tenant tenant) {
        TenantResponse response = new TenantResponse();
        BeanUtils.copyProperties(tenant, response);
        
        if (tenant.getSubscriptionStart() != null) {
            response.setSubscriptionStart(tenant.getSubscriptionStart().toString());
        }
        if (tenant.getSubscriptionEnd() != null) {
            response.setSubscriptionEnd(tenant.getSubscriptionEnd().toString());
        }
        
        return response;
    }
}