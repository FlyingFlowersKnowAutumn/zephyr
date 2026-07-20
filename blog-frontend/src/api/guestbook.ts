import axios from './index'
import type { GuestbookDTO, GuestbookCreateDTO, Result } from '@/types'

export async function createGuestbook(dto: GuestbookCreateDTO): Promise<Result<GuestbookDTO>> {
  const response = await axios.post('/v1/guestbook', dto)
  return response.data
}

export async function getGuestbookList(params: {
  page?: number
  size?: number
}): Promise<Result<{ content: GuestbookDTO[]; totalPages: number; totalElements: number }>> {
  const response = await axios.get('/v1/guestbook', { params })
  return response.data
}

export async function deleteGuestbook(id: string): Promise<Result<void>> {
  const response = await axios.delete(`/v1/guestbook/${id}`)
  return response.data
}

export async function getGuestbookByUserId(userId: string, page = 0, size = 20): Promise<Result<{ content: GuestbookDTO[]; totalPages: number; totalElements: number }>> {
  const response = await axios.get(`/v1/guestbook/user/${userId}`, { params: { page, size } })
  return response.data
}