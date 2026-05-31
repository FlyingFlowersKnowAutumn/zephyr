import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getCurrentUser } from '@/api/auth'
import type { LoginDTO, RegisterDTO, UserDTO, LoginResponseDTO } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<UserDTO | null>(null)
  const isLoggedIn = computed(() => !!token.value)
  
  async function login(dto: LoginDTO): Promise<LoginResponseDTO> {
    const response = await loginApi(dto)
    token.value = response.data.token
    user.value = response.data.user
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('refreshToken', response.data.refreshToken)
    return response.data
  }
  
  async function register(dto: RegisterDTO): Promise<UserDTO> {
    const response = await registerApi(dto)
    return response.data
  }
  
  async function fetchCurrentUser(): Promise<void> {
    if (!token.value) return
    try {
      const response = await getCurrentUser()
      user.value = response.data
    } catch {
      logout()
    }
  }
  
  function logout(): void {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }
  
  return {
    token,
    user,
    isLoggedIn,
    login,
    register,
    fetchCurrentUser,
    logout
  }
})
