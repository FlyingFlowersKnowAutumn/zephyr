import { onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { guestLogin as guestLoginApi, deleteGuest as deleteGuestApi } from '@/api/guest'

export function useGuestAuth() {
  const authStore = useAuthStore()
  
  async function guestLogin() {
    try {
      const response = await guestLoginApi()
      authStore.token = response.data.token
      authStore.user = {
        id: response.data.userId,
        username: response.data.displayName,
        displayName: response.data.displayName,
        email: '',
        avatarUrl: '',
        role: response.data.role,
        isGuest: true,
        createdAt: new Date().toISOString()
      }
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(authStore.user))
      return response.data
    } catch (error) {
      console.warn('Guest login API failed, using mock data')
      const mockToken = 'mock-guest-token-' + Date.now()
      const mockUserId = 'guest-' + Date.now()
      const mockDisplayName = '游客_' + Math.random().toString(36).substring(2, 10)
      
      authStore.token = mockToken
      authStore.user = {
        id: mockUserId,
        username: mockDisplayName,
        displayName: mockDisplayName,
        email: '',
        avatarUrl: '',
        role: 'guest',
        isGuest: true,
        createdAt: new Date().toISOString()
      }
      localStorage.setItem('token', mockToken)
      localStorage.setItem('user', JSON.stringify(authStore.user))
      return { token: mockToken, userId: mockUserId, displayName: mockDisplayName, role: 'guest' }
    }
  }
  
  async function handleBeforeUnload() {
    if (authStore.user?.role === 'guest') {
      const xhr = new XMLHttpRequest()
      xhr.open('DELETE', '/api/v1/auth/guest', true)
      xhr.setRequestHeader('Authorization', `Bearer ${authStore.token}`)
      xhr.send()
    }
  }
  
  onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload)
  })
  
  onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
  })
  
  return {
    guestLogin,
    deleteGuest: deleteGuestApi
  }
}
