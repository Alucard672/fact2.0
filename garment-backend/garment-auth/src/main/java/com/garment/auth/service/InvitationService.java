package com.garment.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.auth.dto.AcceptInvitationRequest;
import com.garment.auth.dto.InvitationResponse;
import com.garment.auth.dto.InviteEmployeeRequest;
import com.garment.auth.dto.LoginResult;
import com.garment.auth.entity.Invitation;
import com.garment.auth.entity.Tenant;
import com.garment.auth.entity.TenantUser;
import com.garment.auth.entity.User;
import com.garment.auth.mapper.InvitationMapper;
import com.garment.auth.mapper.TenantMapper;
import com.garment.auth.mapper.TenantUserMapper;
import com.garment.auth.mapper.UserMapper;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 邀请服务
 *
 * @author garment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationMapper invitationMapper;
    private final UserMapper userMapper;
    private final TenantMapper tenantMapper;
    private final TenantUserMapper tenantUserMapper;
    private final SmsService smsService;
    private final WechatService wechatService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.invitation.base-url:http://localhost:8080}")
    private String invitationBaseUrl;

    /**
     * 发送邀请
     */
    @Transactional(rollbackFor = Exception.class)
    public InvitationResponse sendInvitation(InviteEmployeeRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long inviterId = TenantContext.getCurrentUserId();
        
        log.info("发送员工邀请: tenantId={}, phone={}, role={}", tenantId, request.getPhone(), request.getRole());

        // 1. 验证租户权限
        checkInvitePermission();

        // 2. 检查邀请限制
        validateInviteRequest(tenantId, request);

        // 3. 检查是否已存在邀请
        Invitation existingInvitation = invitationMapper.findByTenantIdAndPhone(tenantId, request.getPhone());
        if (existingInvitation != null && "pending".equals(existingInvitation.getStatus())) {
            throw BusinessException.badRequest("该手机号已有待处理的邀请");
        }

        // 4. 创建邀请记录
        Invitation invitation = new Invitation();
        BeanUtils.copyProperties(request, invitation);
        invitation.setTenantId(tenantId);
        invitation.setInviterId(inviterId);
        invitation.setInvitationCode(generateInvitationCode());
        invitation.setStatus("pending");
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7天有效期
        invitation.setCreatedAt(LocalDateTime.now());

        invitationMapper.insert(invitation);

        // 5. 发送邀请短信
        sendInvitationSms(invitation);

        log.info("邀请发送成功: id={}, code={}", invitation.getId(), invitation.getInvitationCode());
        
        return convertToResponse(invitation);
    }

    /**
     * 接受邀请
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResult acceptInvitation(AcceptInvitationRequest request) {
        log.info("接受邀请: invitationCode={}", request.getInvitationCode());

        // 1. 查询邀请记录
        Invitation invitation = invitationMapper.findByInvitationCode(request.getInvitationCode());
        if (invitation == null) {
            throw BusinessException.notFound("邀请不存在");
        }

        // 2. 验证邀请状态
        if (!"pending".equals(invitation.getStatus())) {
            throw BusinessException.badRequest("邀请已处理或已过期");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus("expired");
            invitationMapper.updateById(invitation);
            throw BusinessException.badRequest("邀请已过期");
        }

        // 3. 查找或创建用户
        User user = findOrCreateUser(invitation, request);

        // 4. 创建租户用户关联
        createTenantUser(invitation, user);

        // 5. 更新邀请状态
        invitation.setStatus("accepted");
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationMapper.updateById(invitation);

        // 6. 更新用户当前租户
        userMapper.updateCurrentTenant(user.getId(), invitation.getTenantId());

        // 7. 生成登录结果
        return generateLoginResult(user, invitation.getTenantId(), invitation.getRole());
    }

    /**
     * 拒绝邀请
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectInvitation(String invitationCode) {
        log.info("拒绝邀请: invitationCode={}", invitationCode);

        Invitation invitation = invitationMapper.findByInvitationCode(invitationCode);
        if (invitation == null) {
            throw BusinessException.notFound("邀请不存在");
        }

        if (!"pending".equals(invitation.getStatus())) {
            throw BusinessException.badRequest("邀请已处理");
        }

        invitation.setStatus("rejected");
        invitationMapper.updateById(invitation);

        log.info("邀请已拒绝: id={}", invitation.getId());
    }

    /**
     * 获取邀请详情
     */
    public InvitationResponse getInvitationByCode(String invitationCode) {
        Invitation invitation = invitationMapper.findByInvitationCode(invitationCode);
        if (invitation == null) {
            throw BusinessException.notFound("邀请不存在");
        }

        return convertToResponse(invitation);
    }

    /**
     * 获取租户邀请列表
     */
    public Page<InvitationResponse> getInvitationList(int page, int size, String status) {
        Long tenantId = TenantContext.getCurrentTenantId();
        
        Page<Invitation> pageRequest = new Page<>(page, size);
        QueryWrapper<Invitation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tenant_id", tenantId);
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<Invitation> invitationPage = invitationMapper.selectPage(pageRequest, queryWrapper);
        
        // 转换为响应DTO
        Page<InvitationResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(invitationPage, responsePage);
        
        List<InvitationResponse> responseList = invitationPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);
        
        return responsePage;
    }

    /**
     * 取消邀请
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelInvitation(Long invitationId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        
        Invitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null || !invitation.getTenantId().equals(tenantId)) {
            throw BusinessException.notFound("邀请不存在");
        }

        if (!"pending".equals(invitation.getStatus())) {
            throw BusinessException.badRequest("只能取消待处理的邀请");
        }

        // 检查权限
        checkInvitePermission();

        invitation.setStatus("cancelled");
        invitationMapper.updateById(invitation);

        log.info("邀请已取消: id={}", invitationId);
    }

    /**
     * 重发邀请
     */
    @Transactional(rollbackFor = Exception.class)
    public InvitationResponse resendInvitation(Long invitationId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        
        Invitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null || !invitation.getTenantId().equals(tenantId)) {
            throw BusinessException.notFound("邀请不存在");
        }

        if (!"pending".equals(invitation.getStatus())) {
            throw BusinessException.badRequest("只能重发待处理的邀请");
        }

        // 检查权限
        checkInvitePermission();

        // 延长过期时间
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitationMapper.updateById(invitation);

        // 重新发送短信
        sendInvitationSms(invitation);

        log.info("邀请已重发: id={}", invitationId);
        
        return convertToResponse(invitation);
    }

    /**
     * 验证邀请权限
     */
    private void checkInvitePermission() {
        String role = TenantContext.getCurrentUserRole();
        if (!"owner".equals(role) && !"admin".equals(role) && !"manager".equals(role)) {
            throw BusinessException.forbidden("权限不足，无法邀请员工");
        }
    }

    /**
     * 验证邀请请求
     */
    private void validateInviteRequest(Long tenantId, InviteEmployeeRequest request) {
        // 检查角色是否有效
        if (!isValidRole(request.getRole())) {
            throw BusinessException.badRequest("无效的角色");
        }

        // 检查租户用户数量限制
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant != null) {
            QueryWrapper<TenantUser> countQuery = new QueryWrapper<>();
            countQuery.eq("tenant_id", tenantId).eq("deleted", 0);
            int currentUserCount = tenantUserMapper.selectCount(countQuery).intValue();
            
            if (currentUserCount >= tenant.getMaxUsers()) {
                throw BusinessException.badRequest("已达到最大用户数限制");
            }
        }

        // 检查工号是否重复
        if (StringUtils.hasText(request.getEmployeeNo())) {
            QueryWrapper<TenantUser> employeeQuery = new QueryWrapper<>();
            employeeQuery.eq("tenant_id", tenantId)
                        .eq("employee_no", request.getEmployeeNo())
                        .eq("deleted", 0);
            if (tenantUserMapper.selectCount(employeeQuery) > 0) {
                throw BusinessException.badRequest("员工工号已存在");
            }
        }
    }

    /**
     * 查找或创建用户
     */
    private User findOrCreateUser(Invitation invitation, AcceptInvitationRequest request) {
        // 先通过手机号查找现有用户
        User user = userMapper.findByPhone(invitation.getPhone());
        
        if (user != null) {
            return user;
        }

        // 创建新用户
        user = new User();
        user.setPhone(invitation.getPhone());
        user.setName(invitation.getName());
        user.setStatus("active");

        if (StringUtils.hasText(request.getWechatCode())) {
            // 通过微信创建用户
            try {
                var wechatUserInfo = wechatService.getUserInfoByCode(request.getWechatCode());
                user.setWechatOpenid(wechatUserInfo.getOpenid());
                user.setWechatUnionid(wechatUserInfo.getUnionid());
            } catch (Exception e) {
                log.warn("获取微信用户信息失败: {}", e.getMessage());
            }
        }

        if (StringUtils.hasText(request.getUsername())) {
            // 检查用户名是否已存在
            User existUser = userMapper.findByUsername(request.getUsername());
            if (existUser != null) {
                throw BusinessException.badRequest("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.insert(user);
        return user;
    }

    /**
     * 创建租户用户关联
     */
    private void createTenantUser(Invitation invitation, User user) {
        // 检查是否已经是租户成员
        TenantUser existingTenantUser = tenantUserMapper.findByUserIdAndTenantId(
            user.getId(), invitation.getTenantId());
        
        if (existingTenantUser != null) {
            throw BusinessException.badRequest("用户已是租户成员");
        }

        TenantUser tenantUser = new TenantUser();
        tenantUser.setTenantId(invitation.getTenantId());
        tenantUser.setUserId(user.getId());
        tenantUser.setRole(invitation.getRole());
        tenantUser.setEmployeeNo(invitation.getEmployeeNo());
        tenantUser.setDepartment(invitation.getDepartment());
        tenantUser.setPosition(invitation.getPosition());
        tenantUser.setWorkshopId(invitation.getWorkshopId());
        tenantUser.setStatus("active");
        tenantUser.setInvitedBy(invitation.getInviterId());
        tenantUser.setInvitedAt(invitation.getCreatedAt());
        tenantUser.setJoinedAt(LocalDateTime.now());

        tenantUserMapper.insert(tenantUser);
    }

    /**
     * 生成邀请码
     */
    private String generateInvitationCode() {
        return "INV" + System.currentTimeMillis() + 
               UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 发送邀请短信
     */
    private void sendInvitationSms(Invitation invitation) {
        try {
            Tenant tenant = tenantMapper.selectById(invitation.getTenantId());
            String invitationUrl = invitationBaseUrl + "/invitation/" + invitation.getInvitationCode();
            
            String message = String.format("您好，%s邀请您加入%s，请点击链接完成注册：%s （7天内有效）", 
                invitation.getName(), tenant.getCompanyName(), invitationUrl);
            
            smsService.sendInvitationSms(invitation.getPhone(), message);
            
            log.info("邀请短信发送成功: phone={}", invitation.getPhone());
        } catch (Exception e) {
            log.error("发送邀请短信失败: {}", e.getMessage(), e);
            // 短信发送失败不影响邀请创建
        }
    }

    /**
     * 生成登录结果
     */
    private LoginResult generateLoginResult(User user, Long tenantId, String role) {
        // 设置租户上下文
        TenantContext.setCurrentUserId(user.getId());
        TenantContext.setCurrentTenantId(tenantId);
        TenantContext.setCurrentUserRole(role);
        
        // 调用认证服务生成令牌
        return authService.generateLoginResult(user, tenantId, role);
    }

    /**
     * 检查角色是否有效
     */
    private boolean isValidRole(String role) {
        return "admin".equals(role) || "manager".equals(role) || 
               "supervisor".equals(role) || "worker".equals(role);
    }

    /**
     * 转换为响应DTO
     */
    private InvitationResponse convertToResponse(Invitation invitation) {
        InvitationResponse response = new InvitationResponse();
        BeanUtils.copyProperties(invitation, response);
        
        // 设置邀请链接
        response.setInvitationUrl(invitationBaseUrl + "/invitation/" + invitation.getInvitationCode());
        
        // 获取租户名称
        Tenant tenant = tenantMapper.selectById(invitation.getTenantId());
        if (tenant != null) {
            response.setTenantName(tenant.getCompanyName());
        }
        
        // 获取邀请人名称
        User inviter = userMapper.selectById(invitation.getInviterId());
        if (inviter != null) {
            response.setInviterName(inviter.getName());
        }
        
        // TODO: 获取车间名称
        
        return response;
    }
}