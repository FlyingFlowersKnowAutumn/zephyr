import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginDTO, RegisterDTO, UserDTO, LoginResponseDTO, GuestLoginDTO } from '@/types'
import * as authApi from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const savedUser = localStorage.getItem('user')
  const user = ref<UserDTO | null>(savedUser ? JSON.parse(savedUser) : null)
  const isLoggedIn = computed(() => !!token.value)
  const error = ref('')

  async function login(dto: LoginDTO): Promise<LoginResponseDTO> {
    error.value = ''
    const result = await authApi.login(dto)
    if (result.code !== 0 || !result.data) {
      error.value = result.message || '登录失败'
      throw new Error(error.value)
    }

    token.value = result.data.token
    refreshToken.value = result.data.refreshToken
    user.value = result.data.user
    localStorage.setItem('token', result.data.token)
    localStorage.setItem('refreshToken', result.data.refreshToken)
    localStorage.setItem('user', JSON.stringify(result.data.user))
    return result.data
  }

  async function guestLogin(dto: GuestLoginDTO): Promise<LoginResponseDTO> {
    error.value = ''
    const result = await authApi.guestLogin(dto)
    if (result.code !== 0 || !result.data) {
      error.value = result.message || '登录失败'
      throw new Error(error.value)
    }

    token.value = result.data.token
    refreshToken.value = result.data.refreshToken
    user.value = result.data.user
    localStorage.setItem('token', result.data.token)
    localStorage.setItem('refreshToken', result.data.refreshToken)
    localStorage.setItem('user', JSON.stringify(result.data.user))
    return result.data
  }

  async function register(dto: RegisterDTO): Promise<UserDTO> {
    error.value = ''
    const result = await authApi.register(dto)
    if (result.code !== 0 || !result.data) {
      error.value = result.message || '注册失败'
      throw new Error(error.value)
    }
    return result.data
  }

  async function fetchCurrentUser(): Promise<void> {
    if (!token.value) return
    error.value = ''
    try {
      const result = await authApi.getCurrentUser()
      if (result.code === 0 && result.data) {
        user.value = result.data
        localStorage.setItem('user', JSON.stringify(result.data))
      }
    } catch {
      // token 可能过期，静默失败
    }
  }

  async function logout(): Promise<void> {
    try {
      if (refreshToken.value) {
        await authApi.logout(refreshToken.value)
      }
    } catch {
      // 即使服务端退出失败也清理本地状态
    }
    token.value = null
    refreshToken.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  function updateUser(data: Partial<UserDTO>) {
    if (!user.value) return
    user.value = { ...user.value, ...data }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return {
    token,
    user,
    isLoggedIn,
    error,
    login,
    guestLogin,
    register,
    fetchCurrentUser,
    logout,
    updateUser
  }
})
