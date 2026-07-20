import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CommentDTO, CommentCreateDTO } from '@/types'
import * as commentApi from '@/api/comment'

export const useCommentStore = defineStore('comment', () => {
  const comments = ref<CommentDTO[]>([])
  const loading = ref(false)

  async function fetchComments(articleId: string, page = 0) {
    loading.value = true
    try {
      const result = await commentApi.getComments(articleId, page, 20)
      if (result.code === 0) {
        comments.value = result.data.content
        return result.data
      }
    } finally {
      loading.value = false
    }
    return null
  }

  async function addComment(dto: CommentCreateDTO) {
    const result = await commentApi.createComment(dto)
    if (result.code === 0 && result.data) {
      if (dto.parentId) {
        const parent = comments.value.find(c => c.id === dto.parentId)
        if (parent) {
          parent.replies = parent.replies || []
          parent.replies.push(result.data)
        }
      } else {
        comments.value.unshift(result.data)
      }
    }
    return result
  }

  async function removeComment(id: string) {
    const result = await commentApi.deleteComment(id)
    if (result.code === 0) {
      comments.value = comments.value.filter(c => c.id !== id)
      for (const comment of comments.value) {
        comment.replies = (comment.replies || []).filter(r => r.id !== id)
      }
    }
    return result
  }

  return {
    comments,
    loading,
    fetchComments,
    addComment,
    removeComment
  }
})
