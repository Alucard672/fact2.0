package com.garment.auth.controller;

import com.garment.auth.dto.LoginResult;
import com.garment.auth.dto.SwitchTenantRequest;
import com.garment.auth.dto.UserTenantInfo;
import com.garment.auth.service.TenantSwitchService;
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
 * 租户切换控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant-switch")
@Api(tags = "租户切换")
@RequiredArgsConstructor
public class TenantSwitchController {

    private final TenantSwitchService tenantSwitchService;

    /**
     * 获取用户的所有租户
     */
    @GetMapping("/tenants")
    @ApiOperation("获取用户的所有租户")
    public Result<List<UserTenantInfo>> getUserTenants() {
        try {
            List<UserTenantInfo> tenants = tenantSwitchService.getUserTenants();
            return Result.success(tenants);
        } catch (Exception e) {
            log.error("获取用户租户列表失败: {}", e.getMessage());
            return Result.error("获取租户列表失败: " + e.getMessage());
        }
    }

    /**
     * 切换租户
     */
    @PostMapping("/switch")
    @ApiOperation("切换租户")
    public Result<LoginResult> switchTenant(@RequestBody @Validated SwitchTenantRequest request) {
        try {
            LoginResult result = tenantSwitchService.switchTenant(request);
            return Result.success("租户切换成功", result);
        } catch (Exception e) {
            log.error("切换租户失败: {}", e.getMessage());
            return Result.error("切换租户失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前租户信息
     */
    @GetMapping("/current")
    @ApiOperation("获取当前租户信息")
    public Result<UserTenantInfo> getCurrentTenant() {
        try {
            UserTenantInfo tenantInfo = tenantSwitchService.getCurrentTenantInfo();
            return Result.success(tenantInfo);
        } catch (Exception e) {
            log.error("获取当前租户信息失败: {}", e.getMessage());
            return Result.error("获取当前租户信息失败: " + e.getMessage());
        }
    }

    /**
     * 离开租户
     */
    @PostMapping("/leave/{tenantId}")
    @ApiOperation("离开租户")
    public Result<Void> leaveTenant(@ApiParam("租户ID") @PathVariable Long tenantId) {
        try {
            tenantSwitchService.leaveTenant(tenantId);
            return Result.success("成功离开租户");
        } catch (Exception e) {
            log.error("离开租户失败: {}", e.getMessage());
            return Result.error("离开租户失败: " + e.getMessage());
        }
    }

    /**
     * 检查租户权限
     */
    @GetMapping("/check-access/{tenantId}")
    @ApiOperation("检查租户访问权限")
    public Result<Boolean> checkTenantAccess(@ApiParam("租户ID") @PathVariable Long tenantId) {
        try {
            // 从上下文获取当前用户ID
            Long userId = com.garment.common.context.TenantContext.getCurrentUserId();
            boolean hasAccess = tenantSwitchService.hasAccessToTenant(userId, tenantId);
            return Result.success(hasAccess);
        } catch (Exception e) {
            log.error("检查租户权限失败: {}", e.getMessage());
            return Result.error("检查租户权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户在指定租户中的角色
     */
    @GetMapping("/role/{tenantId}")
    @ApiOperation("获取用户在指定租户中的角色")
    public Result<String> getUserRoleInTenant(@ApiParam("租户ID") @PathVariable Long tenantId) {
        try {
            Long userId = com.garment.common.context.TenantContext.getCurrentUserId();
            String role = tenantSwitchService.getUserRoleInTenant(userId, tenantId);
            return Result.success(role);
        } catch (Exception e) {
            log.error("获取用户角色失败: {}", e.getMessage());
            return Result.error("获取用户角色失败: " + e.getMessage());
        }
    }
}