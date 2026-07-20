import axios from './index'
import type { ReactionResponseDTO, Result, UserArticleDTO } from '@/types'

export async function toggleReaction(articleId: string): Promise<Result<ReactionResponseDTO>> {
  const response = await axios.post(`/v1/articles/${articleId}/reaction`)
  return response.data
}

export async function getReactionStatus(articleId: string): Promise<Result<ReactionResponseDTO>> {
  const response = await axios.get(`/v1/articles/${articleId}/reaction`)
  return response.data
}

export async function getUserReactions(userId: string): Promise<Result<UserArticleDTO[]>> {
  const response = await axios.get(`/v1/reactions/user/${userId}`)
  return response.data
}

export async function countUserReactions(userId: string): Promise<Result<number>> {
  const response = await axios.get(`/v1/reactions/user/${userId}/count`)
  return response.data
}
