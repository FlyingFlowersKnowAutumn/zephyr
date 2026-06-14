import axios from './index'
import type { ArticleDTO, CreateArticleDTO, UpdateArticleDTO, ArticleListDTO, Result } from '@/types'

/**
 * 创建文章（仅管理员）
 */
export async function createArticle(dto: CreateArticleDTO): Promise<Result<ArticleDTO>> {
  const response = await axios.post('/v1/articles', dto)
  return response.data
}

/**
 * 更新文章（仅管理员）
 */
export async function updateArticle(id: string, dto: UpdateArticleDTO): Promise<Result<ArticleDTO>> {
  const response = await axios.put(`/v1/articles/${id}`, dto)
  return response.data
}

/**
 * 删除文章（仅管理员）
 */
export async function deleteArticle(id: string): Promise<Result<void>> {
  const response = await axios.delete(`/v1/articles/${id}`)
  return response.data
}

/**
 * 获取文章详情
 */
export async function getArticle(id: string): Promise<Result<ArticleDTO>> {
  const response = await axios.get(`/v1/articles/${id}`)
  return response.data
}

/**
 * 根据Slug获取文章
 */
export async function getArticleBySlug(slug: string): Promise<Result<ArticleDTO>> {
  const response = await axios.get(`/v1/articles/slug/${slug}`)
  return response.data
}

/**
 * 获取文章列表
 */
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

/**
 * 获取分类列表
 */
export async function listCategories(): Promise<Result<string[]>> {
  const response = await axios.get('/v1/articles/categories')
  return response.data
}

/**
 * 获取标签列表
 */
export async function listTags(): Promise<Result<string[]>> {
  const response = await axios.get('/v1/tags')
  return response.data
}

/**
 * 创建分类（仅管理员）
 */
export async function createCategory(name: string): Promise<Result<string>> {
  const response = await axios.post('/v1/categories', { name })
  return response.data
}

/**
 * 创建标签（仅管理员）
 */
export async function createTag(name: string): Promise<Result<string>> {
  const response = await axios.post('/v1/tags', { name })
  return response.data
}