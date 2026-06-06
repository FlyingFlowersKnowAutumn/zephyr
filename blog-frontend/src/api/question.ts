import axios from './index'
import type { QuestionDTO, QuestionCreateDTO, AnswerDTO, Result } from '@/types'

export async function createQuestion(dto: QuestionCreateDTO): Promise<Result<QuestionDTO>> {
  const response = await axios.post('/v1/questions', dto)
  return response.data
}

export async function getQuestions(params: {
  status?: string
  isPublic?: boolean
  page?: number
  size?: number
}): Promise<Result<{ content: QuestionDTO[]; totalPages: number; totalElements: number }>> {
  const response = await axios.get('/v1/questions', { params })
  return response.data
}

export async function getQuestionDetail(id: string): Promise<Result<QuestionDTO>> {
  const response = await axios.get(`/v1/questions/${id}`)
  return response.data
}

export async function closeQuestion(id: string): Promise<Result<void>> {
  const response = await axios.put(`/v1/questions/${id}/close`)
  return response.data
}

export async function createAnswer(questionId: string, content: string): Promise<Result<AnswerDTO>> {
  const response = await axios.post(`/v1/questions/${questionId}/answers`, { content })
  return response.data
}

export async function acceptAnswer(questionId: string, answerId: string): Promise<Result<void>> {
  const response = await axios.put(`/v1/questions/${questionId}/answers/${answerId}/accept`)
  return response.data
}
