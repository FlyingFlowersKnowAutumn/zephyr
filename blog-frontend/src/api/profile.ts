import axios from './index'
import type { UserProfileDTO, UpdateUserProfileDTO, AnnouncementDTO, UpdateAnnouncementDTO, Result } from '@/types'

export async function getUserProfile(userId: string): Promise<Result<UserProfileDTO>> {
  const response = await axios.get(`/v1/users/${userId}/profile`)
  return response.data
}

export async function updateUserProfile(userId: string, dto: UpdateUserProfileDTO): Promise<Result<UserProfileDTO>> {
  const response = await axios.put(`/v1/users/${userId}/profile`, dto)
  return response.data
}

/**
 * 管理员修改用户签名
 */
export async function updateUserBio(userId: string, bio: string): Promise<Result<{ userId: string; bio: string }>> {
  const response = await axios.put(`/v1/admin/users/${userId}/bio`, { bio })
  return response.data
}

export async function getAnnouncement(userId: string): Promise<Result<AnnouncementDTO>> {
  const response = await axios.get(`/v1/users/${userId}/announcement`)
  return response.data
}

export async function updateAnnouncement(userId: string, dto: UpdateAnnouncementDTO): Promise<Result<AnnouncementDTO>> {
  const response = await axios.put(`/v1/users/${userId}/announcement`, dto)
  return response.data
}
