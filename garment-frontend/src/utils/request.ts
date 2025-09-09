/**
 * Axios请求封装
 */
import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// API响应类型
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
}

// 配置NProgress
NProgress.configure({
  showSpinner: false,
  minimum: 0.2,
  easing: 'ease',
  speed: 500
})

// 创建Axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    NProgress.start()
    
    const userStore = useUserStore()
    const token = userStore.token
    const tenantId = userStore.currentTenant?.id

    // 添加认证令牌
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 添加租户ID
    if (tenantId && config.headers) {
      config.headers['X-Tenant-Id'] = tenantId.toString()
    }

    return config
  },
  (error: AxiosError) => {
    NProgress.done()
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    NProgress.done()
    
    const res = response.data

    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务错误处理
    if (res.code !== 200 && !res.success) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 5000
      })

      // 特殊错误码处理
      switch (res.code) {
        case 401: // 未授权
          ElMessageBox.confirm(
            '您的登录已过期，请重新登录',
            '登录过期',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            const userStore = useUserStore()
            userStore.logout()
            router.push('/login')
          })
          break
        case 403: // 无权限
          ElMessage({
            message: '您没有权限执行此操作',
            type: 'error',
            duration: 5000
          })
          break
        case 404: // 资源不存在
          ElMessage({
            message: '请求的资源不存在',
            type: 'error',
            duration: 5000
          })
          break
        case 429: // 请求过于频繁
          ElMessage({
            message: '请求过于频繁，请稍后重试',
            type: 'warning',
            duration: 5000
          })
          break
        case 500: // 服务器错误
          ElMessage({
            message: '服务器错误，请稍后重试',
            type: 'error',
            duration: 5000
          })
          break
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res
  },
  (error: AxiosError) => {
    NProgress.done()
    
    // 网络错误处理
    if (error.message === 'Network Error') {
      ElMessage({
        message: '网络连接失败，请检查网络',
        type: 'error',
        duration: 5000
      })
    } else if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage({
        message: '请求超时，请稍后重试',
        type: 'error',
        duration: 5000
      })
    } else if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          ElMessageBox.confirm(
            '您的登录已过期，请重新登录',
            '登录过期',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            const userStore = useUserStore()
            userStore.logout()
            router.push('/login')
          })
          break
        case 403:
          ElMessage({
            message: '您没有权限执行此操作',
            type: 'error',
            duration: 5000
          })
          break
        case 404:
          ElMessage({
            message: '请求的资源不存在',
            type: 'error',
            duration: 5000
          })
          break
        case 500:
        case 502:
        case 503:
        case 504:
          ElMessage({
            message: '服务器错误，请稍后重试',
            type: 'error',
            duration: 5000
          })
          break
        default:
          ElMessage({
            message: `请求失败(${status})`,
            type: 'error',
            duration: 5000
          })
      }
    } else {
      ElMessage({
        message: error.message || '请求失败',
        type: 'error',
        duration: 5000
      })
    }

    return Promise.reject(error)
  }
)

// 导出请求方法
export default request

// 导出常用方法
export const get = <T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return request.get(url, { params, ...config })
}

export const post = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return request.post(url, data, config)
}

export const put = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return request.put(url, data, config)
}

export const del = <T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return request.delete(url, { params, ...config })
}

// 文件上传
export const upload = (url: string, file: File | Blob, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse> => {
  const formData = new FormData()
  formData.append('file', file)
  
  if (data) {
    Object.keys(data).forEach(key => {
      formData.append(key, data[key])
    })
  }

  return request.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    ...config
  })
}

// 文件下载
export const download = (url: string, params?: any, fileName?: string): Promise<void> => {
  return request.get(url, {
    params,
    responseType: 'blob'
  }).then((response: any) => {
    const blob = new Blob([response.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = fileName || 'download'
    link.click()
    URL.revokeObjectURL(link.href)
  })
}




