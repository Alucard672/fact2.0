package com.garment.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.auth.dto.AcceptInvitationRequest;
import com.garment.auth.dto.InvitationResponse;
import com.garment.auth.dto.InviteEmployeeRequest;
import com.garment.auth.dto.LoginResult;
import com.garment.auth.service.InvitationService;
import com.garment.common.annotation.RequireRole;
import com.garment.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 邀请管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/invitations")
@Api(tags = "邀请管理")
@RequiredArgsConstructor
public class InvitationController {
    
    private static final Logger log = LoggerFactory.getLogger(InvitationController.class);

    private final InvitationService invitationService;

    /**
     * 发送邀请
     */
    @PostMapping
    @ApiOperation("发送员工邀请")
    @RequireRole({"owner", "admin", "manager"})
    public Result<InvitationResponse> sendInvitation(@RequestBody @Validated InviteEmployeeRequest request) {
        try {
            InvitationResponse response = invitationService.sendInvitation(request);
            return Result.success("邀请发送成功", response);
        } catch (Exception e) {
            log.error("发送邀请失败: {}", e.getMessage());
            return Result.error("发送邀请失败: " + e.getMessage());
        }
    }

    /**
     * 获取邀请详情（公开接口）
     */
    @GetMapping("/{invitationCode}")
    @ApiOperation("获取邀请详情")
    public Result<InvitationResponse> getInvitation(
            @ApiParam("邀请码") @PathVariable String invitationCode) {
        try {
            InvitationResponse response = invitationService.getInvitationByCode(invitationCode);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取邀请详情失败: {}", e.getMessage());
            return Result.error("获取邀请详情失败: " + e.getMessage());
        }
    }

    /**
     * 接受邀请（公开接口）
     */
    @PostMapping("/accept")
    @ApiOperation("接受邀请")
    public Result<LoginResult> acceptInvitation(@RequestBody @Validated AcceptInvitationRequest request) {
        try {
            LoginResult result = invitationService.acceptInvitation(request);
            return Result.success("邀请接受成功", result);
        } catch (Exception e) {
            log.error("接受邀请失败: {}", e.getMessage());
            return Result.error("接受邀请失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝邀请（公开接口）
     */
    @PostMapping("/{invitationCode}/reject")
    @ApiOperation("拒绝邀请")
    public Result<Void> rejectInvitation(
            @ApiParam("邀请码") @PathVariable String invitationCode) {
        try {
            invitationService.rejectInvitation(invitationCode);
            return Result.success();
        } catch (Exception e) {
            log.error("拒绝邀请失败: {}", e.getMessage());
            return Result.error("拒绝邀请失败: " + e.getMessage());
        }
    }

    /**
     * 获取邀请列表
     */
    @GetMapping
    @ApiOperation("获取邀请列表")
    @RequireRole({"owner", "admin", "manager"})
    public Result<Page<InvitationResponse>> getInvitationList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        try {
            Page<InvitationResponse> response = invitationService.getInvitationList(page, size, status);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取邀请列表失败: {}", e.getMessage());
            return Result.error("获取邀请列表失败: " + e.getMessage());
        }
    }

    /**
     * 取消邀请
     */
    @PostMapping("/{invitationId}/cancel")
    @ApiOperation("取消邀请")
    @RequireRole({"owner", "admin", "manager"})
    public Result<Void> cancelInvitation(
            @ApiParam("邀请ID") @PathVariable Long invitationId) {
        try {
            invitationService.cancelInvitation(invitationId);
            return Result.success();
        } catch (Exception e) {
            log.error("取消邀请失败: {}", e.getMessage());
            return Result.error("取消邀请失败: " + e.getMessage());
        }
    }

    /**
     * 重发邀请
     */
    @PostMapping("/{invitationId}/resend")
    @ApiOperation("重发邀请")
    @RequireRole({"owner", "admin", "manager"})
    public Result<InvitationResponse> resendInvitation(
            @ApiParam("邀请ID") @PathVariable Long invitationId) {
        try {
            InvitationResponse response = invitationService.resendInvitation(invitationId);
            return Result.success("邀请重发成功", response);
        } catch (Exception e) {
            log.error("重发邀请失败: {}", e.getMessage());
            return Result.error("重发邀请失败: " + e.getMessage());
        }
    }

    /**
     * 批量邀请
     */
    @PostMapping("/batch")
    @ApiOperation("批量邀请员工")
    @RequireRole({"owner", "admin"})
    public Result<String> batchInvite(@RequestBody @Validated InviteEmployeeRequest[] requests) {
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (InviteEmployeeRequest request : requests) {
                try {
                    invitationService.sendInvitation(request);
                    successCount++;
                } catch (Exception e) {
                    log.warn("批量邀请中单个邀请失败: phone={}, error={}", request.getPhone(), e.getMessage());
                    failCount++;
                }
            }
            
            String message = String.format("批量邀请完成，成功 %d 个，失败 %d 个", successCount, failCount);
            return Result.success(message);
        } catch (Exception e) {
            log.error("批量邀请失败: {}", e.getMessage());
            return Result.error("批量邀请失败: " + e.getMessage());
        }
    }
}