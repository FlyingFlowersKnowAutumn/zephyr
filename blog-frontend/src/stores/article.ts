import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ArticleDTO, CreateArticleDTO, UpdateArticleDTO } from '@/types'
import * as articleApi from '@/api/article'

export const useArticleStore = defineStore('article', () => {
  const articles = ref<ArticleDTO[]>([])
  const currentArticle = ref<ArticleDTO | null>(null)
  const categories = ref<string[]>([])
  const tags = ref<string[]>([])
  const loading = ref(false)
  const error = ref('')

  async function fetchArticles(params?: {
    page?: number
    size?: number
    categoryId?: string
    tag?: string
    keyword?: string
    status?: string
  }) {
    loading.value = true
    error.value = ''
    try {
      const result = await articleApi.listArticles({
        page: params?.page,
        size: params?.size,
        categoryId: params?.categoryId,
        tag: params?.tag,
        keyword: params?.keyword,
        status: params?.status || 'PUBLISHED'
      })
      if (result.code === 0 && result.data) {
        articles.value = result.data.content
        return { articles: result.data.content, total: result.data.totalElements, page: result.data.number, size: result.data.size }
      }
      error.value = result.message || '获取文章列表失败'
      return { articles: [], total: 0, page: 0, size: 10 }
    } catch (e) {
      error.value = '获取文章列表失败'
      return { articles: [], total: 0, page: 0, size: 10 }
    } finally {
      loading.value = false
    }
  }

  async function fetchArticle(id: string) {
    loading.value = true
    error.value = ''
    try {
      const result = await articleApi.getArticle(id)
      if (result.code === 0 && result.data) {
        currentArticle.value = result.data
        return result.data
      }
      error.value = result.message || '文章不存在'
      return null
    } catch {
      error.value = '获取文章失败'
      return null
    } finally {
      loading.value = false
    }
  }

  async function saveArticle(dto: CreateArticleDTO): Promise<ArticleDTO | null> {
    loading.value = true
    error.value = ''
    try {
      const result = await articleApi.createArticle(dto)
      if (result.code === 0 && result.data) {
        return result.data
      }
      error.value = result.message || '发布失败'
      return null
    } catch {
      error.value = '发布失败'
      return null
    } finally {
      loading.value = false
    }
  }

  async function modifyArticle(id: string, dto: UpdateArticleDTO): Promise<ArticleDTO | null> {
    loading.value = true
    error.value = ''
    try {
      const result = await articleApi.updateArticle(id, dto)
      if (result.code === 0 && result.data) {
        if (currentArticle.value?.id === id) {
          currentArticle.value = result.data
        }
        return result.data
      }
      error.value = result.message || '更新失败'
      return null
    } catch {
      error.value = '更新失败'
      return null
    } finally {
      loading.value = false
    }
  }

  async function removeArticle(id: string): Promise<boolean> {
    loading.value = true
    error.value = ''
    try {
      const result = await articleApi.deleteArticle(id)
      if (result.code === 0) {
        if (currentArticle.value?.id === id) {
          currentArticle.value = null
        }
        return true
      }
      error.value = result.message || '删除失败'
      return false
    } catch {
      error.value = '删除失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function fetchCategories() {
    try {
      const result = await articleApi.listCategories()
      if (result.code === 0 && result.data) {
        categories.value = result.data
      }
    } catch {
      // 非关键数据，静默失败
    }
    return categories.value
  }

  async function fetchTags() {
    try {
      const result = await articleApi.listTags()
      if (result.code === 0 && result.data) {
        tags.value = result.data
      }
    } catch {
      // 非关键数据，静默失败
    }
    return tags.value
  }

  async function createCategory(name: string): Promise<boolean> {
    try {
      const result = await articleApi.createCategory(name)
      if (result.code === 0) {
        return true
      }
      error.value = result.message || '创建分类失败'
      return false
    } catch {
      error.value = '创建分类失败'
      return false
    }
  }

  async function createTag(name: string): Promise<boolean> {
    try {
      const result = await articleApi.createTag(name)
      if (result.code === 0) {
        return true
      }
      error.value = result.message || '创建标签失败'
      return false
    } catch {
      error.value = '创建标签失败'
      return false
    }
  }

  function clearError() {
    error.value = ''
  }

  return {
    articles,
    currentArticle,
    categories,
    tags,
    loading,
    error,
    fetchArticles,
    fetchArticle,
    saveArticle,
    modifyArticle,
    removeArticle,
    fetchCategories,
    fetchTags,
    createCategory,
    createTag,
    clearError
  }
})
