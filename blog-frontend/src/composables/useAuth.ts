import { useAuthStore } from '@/stores/auth'
import type { LoginDTO, RegisterDTO } from '@/types'

export function useAuth() {
  const authStore = useAuthStore()
  
  async function login(dto: LoginDTO) {
    return await authStore.login(dto)
  }
  
  async function register(dto: RegisterDTO) {
    return await authStore.register(dto)
  }
  
  async function fetchUser() {
    await authStore.fetchCurrentUser()
  }
  
  function logout() {
    authStore.logout()
  }
  
  return {
    user: authStore.user,
    isLoggedIn: authStore.isLoggedIn,
    login,
    register,
    fetchUser,
    logout
  }
}
