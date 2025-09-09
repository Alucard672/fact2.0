package com.garment.auth.controller;

import com.garment.common.annotation.RequirePermission;
import com.garment.common.annotation.RequireRole;
import com.garment.common.enums.Permission;
import com.garment.common.enums.TenantRole;
import com.garment.common.service.PermissionService;
import com.garment.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
@Api(tags = "权限管理")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取所有权限列表
     */
    @GetMapping("/list")
    @ApiOperation("获取所有权限列表")
    @RequireRole({"owner", "admin"})
    public Result<List<PermissionInfo>> getPermissionList() {
        List<PermissionInfo> permissions = Arrays.stream(Permission.values())
                .map(permission -> new PermissionInfo(
                        permission.getCode(),
                        permission.getDescription()
                ))
                .collect(Collectors.toList());
        
        return Result.success(permissions);
    }

    /**
     * 获取所有角色列表
     */
    @GetMapping("/roles")
    @ApiOperation("获取所有角色列表")
    @RequireRole({"owner", "admin"})
    public Result<List<RoleInfo>> getRoleList() {
        List<RoleInfo> roles = Arrays.stream(TenantRole.values())
                .map(role -> new RoleInfo(
                        role.getCode(),
                        role.getName(),
                        role.getPermissions().stream()
                                .map(Permission::getCode)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        
        return Result.success(roles);
    }

    /**
     * 获取当前用户权限
     */
    @GetMapping("/current")
    @ApiOperation("获取当前用户权限")
    public Result<UserPermissionInfo> getCurrentUserPermissions() {
        List<Permission> permissions = permissionService.getCurrentUserPermissions();
        
        UserPermissionInfo userPermission = new UserPermissionInfo();
        userPermission.setPermissions(permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList()));
        userPermission.setIsSystemAdmin(permissionService.isSystemAdmin());
        userPermission.setIsTenantOwner(permissionService.isTenantOwner());
        userPermission.setIsAdmin(permissionService.isAdmin());
        
        return Result.success(userPermission);
    }

    /**
     * 检查权限
     */
    @PostMapping("/check")
    @ApiOperation("检查权限")
    public Result<Boolean> checkPermission(@RequestParam String permissionCode) {
        boolean hasPermission = permissionService.hasPermission(permissionCode);
        return Result.success(hasPermission);
    }

    /**
     * 测试权限注解（仅用于演示）
     */
    @GetMapping("/test/user-view")
    @ApiOperation("测试用户查看权限")
    @RequirePermission("user:view")
    public Result<String> testUserViewPermission() {
        return Result.success("您有用户查看权限");
    }

    /**
     * 测试角色注解（仅用于演示）
     */
    @GetMapping("/test/admin")
    @ApiOperation("测试管理员角色")
    @RequireRole({"owner", "admin"})
    public Result<String> testAdminRole() {
        return Result.success("您是管理员");
    }

    /**
     * 权限信息DTO
     */
    public static class PermissionInfo {
        private String code;
        private String description;

        public PermissionInfo(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * 角色信息DTO
     */
    public static class RoleInfo {
        private String code;
        private String name;
        private List<String> permissions;

        public RoleInfo(String code, String name, List<String> permissions) {
            this.code = code;
            this.name = name;
            this.permissions = permissions;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }
    }

    /**
     * 用户权限信息DTO
     */
    public static class UserPermissionInfo {
        private List<String> permissions;
        private Boolean isSystemAdmin;
        private Boolean isTenantOwner;
        private Boolean isAdmin;

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public Boolean getIsSystemAdmin() {
            return isSystemAdmin;
        }

        public void setIsSystemAdmin(Boolean isSystemAdmin) {
            this.isSystemAdmin = isSystemAdmin;
        }

        public Boolean getIsTenantOwner() {
            return isTenantOwner;
        }

        public void setIsTenantOwner(Boolean isTenantOwner) {
            this.isTenantOwner = isTenantOwner;
        }

        public Boolean getIsAdmin() {
            return isAdmin;
        }

        public void setIsAdmin(Boolean isAdmin) {
            this.isAdmin = isAdmin;
        }
    }
}