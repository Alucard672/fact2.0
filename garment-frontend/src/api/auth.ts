/**
 * 认证相关API
 */
import request from '@/utils/request'
import { LoginRequest, LoginResult, ApiResponse } from '@/types/api'

// 登录接口
export function login(data: LoginRequest): Promise<ApiResponse<LoginResult>> {
  return request.post('/auth/login', data)
}

// 手机号登录
export function phoneLogin(data: { phone: string; smsCode: string; tenantCode?: string }): Promise<ApiResponse<LoginResult>> {
  return request.post('/auth/phone-login', data)
}

// 微信登录
export function wechatLogin(data: { code: string; tenantCode?: string }): Promise<ApiResponse<LoginResult>> {
  return request.post('/auth/wechat-login', data)
}

// 发送短信验证码
export function sendSmsCode(phone: string): Promise<ApiResponse<void>> {
  return request.post('/auth/send-sms', null, { params: { phone } })
}

// 获取用户信息
export function getUserInfo(): Promise<ApiResponse<any>> {
  return request.get('/auth/user-info')
}

// 刷新令牌
export function refreshToken(refreshToken: string): Promise<ApiResponse<any>> {
  return request.post('/auth/refresh-token', { refreshToken })
}

// 登出
export function logout(): Promise<ApiResponse<void>> {
  return request.post('/auth/logout')
}

// 绑定手机号
export function bindPhone(data: { userId: number; phone: string; smsCode: string }): Promise<ApiResponse<void>> {
  return request.post('/auth/bind-phone', null, { params: data })
}

// 修改密码
export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<void>> {
  return request.post('/auth/change-password', data)
}

// 重置密码
export function resetPassword(data: { phone: string; smsCode: string; newPassword: string }): Promise<ApiResponse<void>> {
  return request.post('/auth/reset-password', data)
}




