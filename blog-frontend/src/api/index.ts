import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

instance.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    // 身份由网关从 token 解析后注入，前端不再自报 X-User-Id（否则可被伪造）
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 这些接口的 401 是"业务性"的（密码错误、验证码失效等），不能当成登录过期处理，
 * 否则用户在登录页输错一次密码就会被踢走、还会看到"登录已过期"的错误提示。
 */
const AUTH_ENDPOINTS = [
  '/v1/auth/login',
  '/v1/auth/guest-login',
  '/v1/auth/register',
  '/v1/auth/refresh',
  '/v1/auth/send-code',
  '/v1/auth/password'
]

function isAuthEndpoint(config?: InternalAxiosRequestConfig): boolean {
  const url = config?.url || ''
  return AUTH_ENDPOINTS.some(path => url.includes(path))
}

// 同一时间可能有多个请求一起 401，只跳转一次、只提示一次
let handlingUnauthorized = false

async function redirectToLogin() {
  const current = `${window.location.pathname}${window.location.search}${window.location.hash}`
  if (current.startsWith('/login')) return
  if (handlingUnauthorized) return
  handlingUnauthorized = true

  ElMessage.warning('登录已过期，请重新登录')

  try {
    const { default: router } = await import('@/router')
    await router.push({ path: '/login', query: { redirect: current } })
  } catch {
    window.location.href = `/login?redirect=${encodeURIComponent(current)}`
  } finally {
    // 跳转完成后放开，避免刷新后仍被锁死
    setTimeout(() => { handlingUnauthorized = false }, 1000)
  }
}

instance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401 && !isAuthEndpoint(error.config)) {
      const authStore = useAuthStore()
      const wasLoggedIn = authStore.isLoggedIn
      authStore.logout()
      // 本来就是游客态访问公开页面，不必打扰用户
      if (wasLoggedIn) {
        void redirectToLogin()
      }
    }
    return Promise.reject(error)
  }
)

export default instance
