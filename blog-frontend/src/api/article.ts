import axios from './index'
import type { ArticleDTO, CreateArticleDTO, UpdateArticleDTO, ArticleListDTO, Result, CategoryDTO, TagDTO } from '@/types'

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
 * 返回格式兼容：旧版返回 string[]，新版返回 CategoryDTO[]
 */
export async function listCategories(): Promise<Result<CategoryDTO[] | string[]>> {
  const response = await axios.get('/v1/categories')
  return response.data
}

/**
 * 获取标签列表
 * 返回格式兼容：旧版返回 string[]，新版返回 TagDTO[]
 */
export async function listTags(): Promise<Result<TagDTO[] | string[]>> {
  const response = await axios.get('/v1/tags')
  return response.data
}

/**
 * 创建分类（仅管理员）
 */
export async function createCategory(name: string, description?: string): Promise<Result<CategoryDTO>> {
  const response = await axios.post('/v1/categories', { name, description })
  return response.data
}

/**
 * 创建标签（仅管理员）
 */
export async function createTag(name: string): Promise<Result<TagDTO>> {
  const response = await axios.post('/v1/tags', { name })
  return response.data
}

/**
 * 更新分类（仅管理员）
 */
export async function updateCategory(id: string, name: string, description?: string): Promise<Result<CategoryDTO>> {
  const response = await axios.put(`/v1/categories/${id}`, { name, description })
  return response.data
}

/**
 * 更新标签（仅管理员）
 */
export async function updateTag(id: string, name: string): Promise<Result<TagDTO>> {
  const response = await axios.put(`/v1/tags/${id}`, { name })
  return response.data
}

/**
 * 删除分类（仅管理员）
 */
export async function deleteCategory(id: string): Promise<Result<void>> {
  const response = await axios.delete(`/v1/categories/${id}`)
  return response.data
}

/**
 * 删除标签（仅管理员）
 */
export async function deleteTag(id: string): Promise<Result<void>> {
  const response = await axios.delete(`/v1/tags/${id}`)
  return response.data
}

/**
 * 增加标签访问计数
 */
export async function incrementTagVisit(id: string): Promise<Result<void>> {
  const response = await axios.post(`/v1/tags/${id}/visit`)
  return response.data
}

/**
 * 获取热门标签
 */
export async function getHotTags(): Promise<Result<TagDTO[]>> {
  const response = await axios.get('/v1/tags/hot')
  return response.data
}

export async function getLatestArticleTime(): Promise<Result<{ latestTime: string | null }>> {
  const response = await axios.get('/v1/articles/latest-time')
  return response.data
}