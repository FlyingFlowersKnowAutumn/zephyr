import axios from './index'
import type { ArticleDTO, CreateArticleDTO, UpdateArticleDTO, ArticleListDTO, Result } from '@/types'

export async function createArticle(dto: CreateArticleDTO): Promise<Result<ArticleDTO>> {
  const response = await axios.post('/v1/articles', dto)
  return response.data
}

export async function updateArticle(id: string, dto: UpdateArticleDTO): Promise<Result<ArticleDTO>> {
  const response = await axios.put(`/v1/articles/${id}`, dto)
  return response.data
}

export async function deleteArticle(id: string): Promise<Result<void>> {
  const response = await axios.delete(`/v1/articles/${id}`)
  return response.data
}

export async function getArticle(id: string): Promise<Result<ArticleDTO>> {
  const response = await axios.get(`/v1/articles/${id}`)
  return response.data
}

export async function getArticleBySlug(slug: string): Promise<Result<ArticleDTO>> {
  const response = await axios.get(`/v1/articles/slug/${slug}`)
  return response.data
}

export async function listArticles(params: {
  page?: number
  size?: number
  categoryId?: string
  tag?: string
  keyword?: string
  status?: string
}): Promise<Result<ArticleListDTO>> {
  const response = await axios.get('/v1/articles', { params })
  return response.data
}

export async function listCategories(): Promise<Result<string[]>> {
  const response = await axios.get('/v1/articles/categories')
  return response.data
}

export async function listTags(): Promise<Result<string[]>> {
  const response = await axios.get('/v1/tags')
  return response.data
}

export async function createCategory(name: string): Promise<Result<string>> {
  const response = await axios.post('/v1/categories', { name })
  return response.data
}

export async function createTag(name: string): Promise<Result<string>> {
  const response = await axios.post('/v1/tags', { name })
  return response.data
}