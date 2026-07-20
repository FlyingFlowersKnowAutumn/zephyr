import axios from './index'
import type { CommentDTO, CommentCreateDTO, Result } from '@/types'

export async function createComment(dto: CommentCreateDTO): Promise<Result<CommentDTO>> {
  const response = await axios.post('/v1/comments', dto)
  return response.data
}

export async function getComments(articleId: string, page = 0, size = 20): Promise<Result<{ content: CommentDTO[]; totalPages: number; totalElements: number }>> {
  const response = await axios.get('/v1/comments', { params: { articleId, page, size } })
  return response.data
}

export async function getReplies(commentId: string): Promise<Result<CommentDTO[]>> {
  const response = await axios.get(`/v1/comments/${commentId}/replies`)
  return response.data
}

export async function deleteComment(id: string): Promise<Result<void>> {
  const response = await axios.delete(`/v1/comments/${id}`)
  return response.data
}

export async function getCommentsByUserId(userId: string, page = 0, size = 20): Promise<Result<{ content: CommentDTO[]; totalPages: number; totalElements: number }>> {
  const response = await axios.get(`/v1/comments/user/${userId}`, { params: { page, size } })
  return response.data
}

export async function getAllComments(page = 0, size = 20): Promise<Result<{ content: CommentDTO[]; totalPages: number; totalElements: number }>> {
  const response = await axios.get('/v1/comments/all', { params: { page, size } })
  return response.data
}
