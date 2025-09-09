package com.garment.auth.controller;

import com.garment.common.result.Result;
import com.garment.auth.service.AuthService;
import com.garment.auth.dto.LoginRequest;
import com.garment.auth.dto.PhoneLoginRequest;
import com.garment.auth.dto.WechatLoginRequest;
import com.garment.auth.dto.RefreshTokenRequest;
import com.garment.auth.dto.LoginResult;
import com.garment.auth.dto.TokenInfo;
import com.garment.auth.dto.WechatPhoneLoginRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证管理")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 微信登录
     */
    @PostMapping("/wechat-login")
    @ApiOperation("微信登录")
    public Result<LoginResult> wechatLogin(@RequestBody @Validated WechatLoginRequest request) {
        try {
            LoginResult result = authService.wechatLogin(request);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("微信登录失败: {}", e.getMessage());
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 微信手机号授权登录
     */
    @PostMapping("/wechat-phone-login")
    @ApiOperation("微信手机号授权登录")
    public Result<LoginResult> wechatPhoneLogin(@RequestBody @Validated WechatPhoneLoginRequest request) {
        try {
            LoginResult result = authService.wechatPhoneLogin(request);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("微信手机号登录失败: {}", e.getMessage());
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 绑定手机号
     */
    @PostMapping("/bind-phone")
    @ApiOperation("绑定手机号")
    public Result<Void> bindPhone(@RequestParam Long userId, 
                                      @RequestParam String phone, 
                                      @RequestParam String smsCode) {
        try {
            authService.bindPhone(userId, phone, smsCode);
            return Result.success();
        } catch (Exception e) {
            log.error("绑定手机号失败: {}", e.getMessage());
            return Result.error("绑定失败: " + e.getMessage());
        }
    }

    /**
     * 手机号登录
     */
    @PostMapping("/phone-login")
    @ApiOperation("手机号登录")
    public Result<LoginResult> phoneLogin(@RequestBody @Validated PhoneLoginRequest request) {
        try {
            LoginResult result = authService.phoneLogin(request);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("手机号登录失败: {}", e.getMessage());
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户名密码登录
     */
    @PostMapping("/login")
    @ApiOperation("用户名密码登录")
    public Result<LoginResult> login(@RequestBody @Validated LoginRequest request) {
        try {
            LoginResult result = authService.login(request);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("用户名登录失败: {}", e.getMessage());
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh-token")
    @ApiOperation("刷新令牌")
    public Result<TokenInfo> refreshToken(@RequestBody @Validated RefreshTokenRequest request) {
        try {
            TokenInfo tokenInfo = authService.refreshToken(request.getRefreshToken());
            return Result.success("令牌刷新成功", tokenInfo);
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            return Result.error("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            authService.logout(token);
            return Result.success();
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            return Result.error("登出失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user-info")
    @ApiOperation("获取用户信息")
    public Result<Object> getUserInfo() {
        try {
            Object userInfo = authService.getCurrentUserInfo();
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-sms")
    @ApiOperation("发送短信验证码")
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        try {
            authService.sendSmsCode(phone);
            return Result.success();
        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage());
            return Result.error("发送验证码失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @ApiOperation("健康检查")
    public Result<String> health() {
        return Result.success("认证服务运行正常");
    }
}