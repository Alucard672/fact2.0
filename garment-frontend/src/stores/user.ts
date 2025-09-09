/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo, refreshToken } from '@/api/auth'
import { LoginRequest, UserInfo, TenantInfo } from '@/types/api'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const refreshTokenValue = ref<string>(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfo | null>(null)
  const currentTenant = ref<TenantInfo | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.name || userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const isSystemAdmin = computed(() => userInfo.value?.isSystemAdmin || false)
  const hasRole = computed(() => (role: string) => roles.value.includes(role))
  const hasPermission = computed(() => (permission: string) => permissions.value.includes(permission))

  // 登录
  async function loginAction(loginForm: LoginRequest) {
    try {
      const res = await login(loginForm)
      if (res.success) {
        const { tokenInfo, userInfo: user, tenantInfo } = res.data
        
        // 保存Token
        token.value = tokenInfo.accessToken
        refreshTokenValue.value = tokenInfo.refreshToken
        localStorage.setItem('token', tokenInfo.accessToken)
        localStorage.setItem('refreshToken', tokenInfo.refreshToken)
        
        // 保存用户信息
        userInfo.value = user
        currentTenant.value = tenantInfo || null
        roles.value = user.roles || []
        permissions.value = user.permissions || []
        
        ElMessage.success('登录成功')
        return true
      }
      return false
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }

  // 获取用户信息
  async function getUserInfoAction() {
    // 开发环境：直接使用本地模拟用户，避免触发 /api 代理请求造成终端噪音
    if (import.meta.env.DEV) {
      userInfo.value = {
        id: 1,
        phone: '13800000000',
        username: 'demo_admin',
        name: '演示管理员',
        avatar: '',
        isSystemAdmin: true
      } as UserInfo
      roles.value = ['owner']
      permissions.value = ['*']
      return true
    }

    try {
      const res = await getUserInfo()
      if (res.success) {
        const { user, tenant, roles: userRoles, permissions: userPermissions } = res.data
        
        userInfo.value = user
        currentTenant.value = tenant || null
        roles.value = userRoles || []
        permissions.value = userPermissions || []
        
        return true
      }
      return false
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return false
    }
  }

  // 刷新令牌
  async function refreshTokenAction() {
    try {
      const res = await refreshToken(refreshTokenValue.value)
      if (res.success) {
        const { accessToken, refreshToken: newRefreshToken } = res.data
        
        token.value = accessToken
        refreshTokenValue.value = newRefreshToken
        localStorage.setItem('token', accessToken)
        localStorage.setItem('refreshToken', newRefreshToken)
        
        return true
      }
      return false
    } catch (error) {
      console.error('刷新令牌失败:', error)
      return false
    }
  }

  // 登出
  async function logoutAction() {
    try {
      await logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 清除本地数据
      token.value = ''
      refreshTokenValue.value = ''
      userInfo.value = null
      currentTenant.value = null
      roles.value = []
      permissions.value = []
      
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      
      ElMessage.success('已退出登录')
    }
  }

  // 切换租户
  function switchTenant(tenant: TenantInfo) {
    currentTenant.value = tenant
    ElMessage.success(`已切换到 ${tenant.companyName}`)
  }

  // 更新用户信息
  function updateUserInfo(info: Partial<UserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
    }
  }

  return {
    // 状态
    token,
    refreshTokenValue,
    userInfo,
    currentTenant,
    permissions,
    roles,
    
    // 计算属性
    isLoggedIn,
    username,
    avatar,
    isSystemAdmin,
    hasRole,
    hasPermission,
    
    // 方法
    login: loginAction,
    getUserInfo: getUserInfoAction,
    refreshToken: refreshTokenAction,
    logout: logoutAction,
    switchTenant,
    updateUserInfo
  }
})




