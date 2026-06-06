import axios from './index'
import type { UserProfileDTO, UpdateUserProfileDTO, Result } from '@/types'

export async function getUserProfile(userId: string): Promise<Result<UserProfileDTO>> {
  const response = await axios.get(`/v1/users/${userId}/profile`)
  return response.data
}

export async function updateUserProfile(userId: string, dto: UpdateUserProfileDTO): Promise<Result<UserProfileDTO>> {
  const response = await axios.put(`/v1/users/${userId}/profile`, dto)
  return response.data
}
