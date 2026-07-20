import axios from './index'
import type { FavoriteResponseDTO, Result, UserArticleDTO } from '@/types'

export async function toggleFavorite(articleId: string): Promise<Result<FavoriteResponseDTO>> {
  const response = await axios.post(`/v1/articles/${articleId}/favorite`)
  return response.data
}

export async function getFavoriteStatus(articleId: string): Promise<Result<FavoriteResponseDTO>> {
  const response = await axios.get(`/v1/articles/${articleId}/favorite`)
  return response.data
}

export async function getUserFavorites(userId: string): Promise<Result<UserArticleDTO[]>> {
  const response = await axios.get(`/v1/favorites/user/${userId}`)
  return response.data
}

export async function countUserFavorites(userId: string): Promise<Result<number>> {
  const response = await axios.get(`/v1/favorites/user/${userId}/count`)
  return response.data
}
