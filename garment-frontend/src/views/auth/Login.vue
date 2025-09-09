<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <img class="logo" src="/logo.png" alt="Logo" />
        <h1 class="title">服装生产管理系统</h1>
        <p class="subtitle">专业的服装计件生产流程管理平台</p>
      </div>

      <el-tabs v-model="loginType" class="login-tabs">
        <el-tab-pane label="账号密码登录" name="account">
          <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="accountForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="accountForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleAccountLogin"
              />
            </el-form-item>
            <el-form-item prop="tenantCode">
              <el-input
                v-model="accountForm.tenantCode"
                placeholder="请输入租户代码（选填）"
                prefix-icon="House"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-link class="forgot-password" type="primary">忘记密码？</el-link>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleAccountLogin"
                style="width: 100%"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="手机号登录" name="phone">
          <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" class="login-form">
            <el-form-item prop="phone">
              <el-input
                v-model="phoneForm.phone"
                placeholder="请输入手机号"
                prefix-icon="Cellphone"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item prop="smsCode">
              <div class="sms-code-wrapper">
                <el-input
                  v-model="phoneForm.smsCode"
                  placeholder="请输入验证码"
                  prefix-icon="Message"
                  size="large"
                  @keyup.enter="handlePhoneLogin"
                />
                <el-button
                  size="large"
                  :disabled="countdown > 0"
                  @click="handleSendCode"
                  class="send-code-btn"
                >
                  {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item prop="tenantCode">
              <el-input
                v-model="phoneForm.tenantCode"
                placeholder="请输入租户代码（选填）"
                prefix-icon="House"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handlePhoneLogin"
                style="width: 100%"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="login-footer">
        <p>还没有账户？<el-link type="primary" @click="handleRegister">立即注册</el-link></p>
        <div class="other-login">
          <el-divider>其他登录方式</el-divider>
          <div class="login-icons">
            <el-tooltip content="微信登录" placement="bottom">
              <div class="login-icon wechat" @click="handleWechatLogin">
                <i class="iconfont icon-wechat"></i>
              </div>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>

    <div class="copyright">
      <p>© 2024 服装生产管理系统 版权所有</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 登录类型
const loginType = ref<'account' | 'phone'>('account')
const loading = ref(false)
const rememberMe = ref(false)
const countdown = ref(0)
let timer: NodeJS.Timeout | null = null

// 账号密码表单
const accountFormRef = ref<FormInstance>()
const accountForm = reactive({
  username: '',
  password: '',
  tenantCode: ''
})

const accountRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 手机号表单
const phoneFormRef = ref<FormInstance>()
const phoneForm = reactive({
  phone: '',
  smsCode: '',
  tenantCode: ''
})

const phoneRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '请输入6位数字验证码', trigger: 'blur' }
  ]
}

// 账号密码登录
const handleAccountLogin = async () => {
  if (!accountFormRef.value) return
  
  await accountFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login(accountForm)
        if (success) {
          const redirect = route.query.redirect as string || '/'
          router.push(redirect)
        }
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 手机号登录
const handlePhoneLogin = async () => {
  if (!phoneFormRef.value) return
  
  await phoneFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login({
          phone: phoneForm.phone,
          smsCode: phoneForm.smsCode,
          tenantCode: phoneForm.tenantCode
        })
        if (success) {
          const redirect = route.query.redirect as string || '/'
          router.push(redirect)
        }
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 发送验证码
const handleSendCode = async () => {
  if (!phoneForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    const res = await sendSmsCode(phoneForm.phone)
    if (res.success) {
      ElMessage.success('验证码已发送')
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer!)
          timer = null
        }
      }, 1000)
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

// 微信登录
const handleWechatLogin = () => {
  ElMessage.info('微信登录功能开发中')
}

// 注册
const handleRegister = () => {
  ElMessage.info('请联系管理员注册账号')
}

// 清理定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .login-card {
    width: 480px;
    background: white;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);

    .login-header {
      text-align: center;
      margin-bottom: 40px;

      .logo {
        width: 80px;
        height: 80px;
        margin-bottom: 20px;
      }

      .title {
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 12px;
      }

      .subtitle {
        font-size: 14px;
        color: #909399;
      }
    }

    .login-tabs {
      :deep(.el-tabs__header) {
        margin-bottom: 32px;
      }
      
      :deep(.el-tabs__nav-wrap::after) {
        display: none;
      }
      
      :deep(.el-tabs__item) {
        font-size: 16px;
      }
    }

    .login-form {
      .sms-code-wrapper {
        display: flex;
        gap: 12px;

        .send-code-btn {
          width: 140px;
          flex-shrink: 0;
        }
      }

      .forgot-password {
        float: right;
      }
    }

    .login-footer {
      margin-top: 24px;
      text-align: center;

      p {
        color: #909399;
        font-size: 14px;
        margin-bottom: 20px;
      }

      .other-login {
        margin-top: 32px;

        .login-icons {
          display: flex;
          justify-content: center;
          gap: 24px;
          margin-top: 20px;

          .login-icon {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s;

            &.wechat {
              background: #07c160;
              color: white;
              
              &:hover {
                transform: scale(1.1);
                box-shadow: 0 4px 12px rgba(7, 193, 96, 0.4);
              }
            }

            i {
              font-size: 24px;
            }
          }
        }
      }
    }
  }

  .copyright {
    margin-top: 40px;
    text-align: center;
    color: white;
    font-size: 14px;
    opacity: 0.9;
  }
}

// 响应式适配
@media (max-width: 640px) {
  .login-container {
    padding: 20px;
    
    .login-card {
      width: 100%;
      padding: 30px 20px;
    }
  }
}
</style>




