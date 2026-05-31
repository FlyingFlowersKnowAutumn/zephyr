import { onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { guestLogin as guestLoginApi, deleteGuest as deleteGuestApi } from '@/api/guest'

export function useGuestAuth() {
  const authStore = useAuthStore()
  
  async function guestLogin() {
    const response = await guestLoginApi()
    authStore.token = response.data.token
    localStorage.setItem('token', response.data.token)
    return response.data
  }
  
  async function handleBeforeUnload() {
    if (authStore.user?.role === 'guest') {
      navigator.sendBeacon('/api/v1/auth/guest', new Blob([]), {
        headers: {
          'Authorization': `Bearer ${authStore.token}`
        }
      })
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
