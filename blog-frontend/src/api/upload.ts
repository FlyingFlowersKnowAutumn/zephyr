import { useAuthStore } from '@/stores/auth'

export async function uploadImage(file: File): Promise<string | null> {
  const authStore = useAuthStore()
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('/api/v1/upload/image', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${authStore.token}`
    },
    body: formData
  })

  const result = await response.json()
  if (result.code === 0 && result.data?.url) {
    return result.data.url
  }
  throw new Error(result.message || '上传失败')
}
