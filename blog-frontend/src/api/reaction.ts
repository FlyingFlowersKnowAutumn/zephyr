import axios from './index'
import type { ReactionResponseDTO, Result } from '@/types'

export async function toggleReaction(articleId: string): Promise<Result<ReactionResponseDTO>> {
  const response = await axios.post(`/v1/articles/${articleId}/reaction`)
  return response.data
}

export async function getReactionStatus(articleId: string): Promise<Result<ReactionResponseDTO>> {
  const response = await axios.get(`/v1/articles/${articleId}/reaction`)
  return response.data
}
