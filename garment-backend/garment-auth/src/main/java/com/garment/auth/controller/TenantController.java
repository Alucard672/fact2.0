package com.garment.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.auth.dto.CreateTenantRequest;
import com.garment.auth.dto.TenantResponse;
import com.garment.auth.dto.UpdateTenantRequest;
import com.garment.auth.service.TenantService;
import com.garment.common.annotation.RequireRole;
import com.garment.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 租户管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/tenants")
@Api(tags = "租户管理")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * 创建租户
     */
    @PostMapping
    @ApiOperation("创建租户")
    public Result<TenantResponse> createTenant(@RequestBody @Validated CreateTenantRequest request) {
        try {
            TenantResponse response = tenantService.createTenant(request);
            return Result.success("租户创建成功", response);
        } catch (Exception e) {
            log.error("创建租户失败: {}", e.getMessage());
            return Result.error("创建租户失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前租户信息
     */
    @GetMapping("/current")
    @ApiOperation("获取当前租户信息")
    public Result<TenantResponse> getCurrentTenant() {
        try {
            TenantResponse response = tenantService.getCurrentTenantInfo();
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取当前租户信息失败: {}", e.getMessage());
            return Result.error("获取租户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取租户信息
     */
    @GetMapping("/{tenantId}")
    @ApiOperation("获取租户信息")
    @RequireRole({"owner", "admin"})
    public Result<TenantResponse> getTenant(
            @ApiParam("租户ID") @PathVariable Long tenantId) {
        try {
            TenantResponse response = tenantService.getTenantInfo(tenantId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取租户信息失败: {}", e.getMessage());
            return Result.error("获取租户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新租户信息
     */
    @PutMapping("/{tenantId}")
    @ApiOperation("更新租户信息")
    @RequireRole({"owner", "admin"})
    public Result<TenantResponse> updateTenant(
            @ApiParam("租户ID") @PathVariable Long tenantId,
            @RequestBody @Validated UpdateTenantRequest request) {
        try {
            TenantResponse response = tenantService.updateTenant(tenantId, request);
            return Result.success("租户信息更新成功", response);
        } catch (Exception e) {
            log.error("更新租户信息失败: {}", e.getMessage());
            return Result.error("更新租户信息失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询租户列表（系统管理员功能）
     */
    @GetMapping
    @ApiOperation("分页查询租户列表")
    public Result<Page<TenantResponse>> getTenantList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size,
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        try {
            Page<TenantResponse> response = tenantService.getTenantList(page, size, keyword, status);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询租户列表失败: {}", e.getMessage());
            return Result.error("查询租户列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除租户
     */
    @DeleteMapping("/{tenantId}")
    @ApiOperation("删除租户")
    public Result<Void> deleteTenant(
            @ApiParam("租户ID") @PathVariable Long tenantId) {
        try {
            tenantService.deleteTenant(tenantId);
            return Result.success("租户删除成功");
        } catch (Exception e) {
            log.error("删除租户失败: {}", e.getMessage());
            return Result.error("删除租户失败: " + e.getMessage());
        }
    }

    /**
     * 租户注册（公开接口）
     */
    @PostMapping("/register")
    @ApiOperation("租户注册")
    public Result<TenantResponse> registerTenant(@RequestBody @Validated CreateTenantRequest request) {
        try {
            // 设置默认为试用版
            request.setSubscriptionPlan("trial");
            
            TenantResponse response = tenantService.createTenant(request);
            return Result.success("租户注册成功", response);
        } catch (Exception e) {
            log.error("租户注册失败: {}", e.getMessage());
            return Result.error("租户注册失败: " + e.getMessage());
        }
    }

    /**
     * 检查企业名称是否可用
     */
    @GetMapping("/check-name")
    @ApiOperation("检查企业名称是否可用")
    public Result<Boolean> checkCompanyName(@RequestParam String companyName) {
        try {
            // TODO: 实现企业名称检查逻辑
            return Result.success(true);
        } catch (Exception e) {
            log.error("检查企业名称失败: {}", e.getMessage());
            return Result.error("检查企业名称失败: " + e.getMessage());
        }
    }

    /**
     * 检查租户编码是否可用
     */
    @GetMapping("/check-code")
    @ApiOperation("检查租户编码是否可用")
    public Result<Boolean> checkTenantCode(@RequestParam String tenantCode) {
        try {
            // TODO: 实现租户编码检查逻辑
            return Result.success(true);
        } catch (Exception e) {
            log.error("检查租户编码失败: {}", e.getMessage());
            return Result.error("检查租户编码失败: " + e.getMessage());
        }
    }
}