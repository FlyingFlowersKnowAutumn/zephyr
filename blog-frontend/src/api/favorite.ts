import axios from './index'
import type { FavoriteResponseDTO, Result } from '@/types'

export async function toggleFavorite(articleId: string): Promise<Result<FavoriteResponseDTO>> {
  const response = await axios.post(`/v1/articles/${articleId}/favorite`)
  return response.data
}

export async function getFavoriteStatus(articleId: string): Promise<Result<FavoriteResponseDTO>> {
  const response = await axios.get(`/v1/articles/${articleId}/favorite`)
  return response.data
}
