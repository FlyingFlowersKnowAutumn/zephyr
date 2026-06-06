<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useCommentStore } from '@/stores/comment'
import { useAuthStore } from '@/stores/auth'
import CommentItem from './CommentItem.vue'
import type { CommentCreateDTO } from '@/types'

const props = defineProps<{ articleId: string }>()
const commentStore = useCommentStore()
const authStore = useAuthStore()

const newComment = ref('')

onMounted(() => {
  commentStore.fetchComments(props.articleId)
})

watch(() => props.articleId, (id) => {
  if (id) commentStore.fetchComments(id)
})

async function handleSubmit() {
  if (!newComment.value.trim()) return
  const dto: CommentCreateDTO = {
    articleId: props.articleId,
    content: newComment.value.trim()
  }
  const result = await commentStore.addComment(dto)
  if (result.code === 0) {
    newComment.value = ''
  }
}

async function handleReply(parentId: string, content: string) {
  const dto: CommentCreateDTO = {
    articleId: props.articleId,
    parentId,
    content
  }
  return commentStore.addComment(dto)
}

async function handleDelete(id: string) {
  await commentStore.removeComment(id)
}
</script>

<template>
  <div class="comment-section">
    <h3 class="section-title">评论 ({{ commentStore.comments.length }})</h3>

    <div v-if="authStore.isLoggedIn" class="comment-form">
      <textarea
        v-model="newComment"
        placeholder="写下你的评论..."
        rows="3"
        maxlength="500"
      />
      <button
        class="submit-btn"
        :disabled="!newComment.trim()"
        @click="handleSubmit"
      >
        发布评论
      </button>
    </div>
    <p v-else class="login-hint">请登录后发表评论</p>

    <div v-if="commentStore.loading" class="loading">加载评论中...</div>

    <div v-else-if="commentStore.comments.length === 0" class="empty">
      暂无评论，来发表第一条评论吧
    </div>

    <div v-else class="comment-list">
      <CommentItem
        v-for="comment in commentStore.comments"
        :key="comment.id"
        :comment="comment"
        :is-admin="authStore.user?.role === 'admin' || authStore.user?.role === 'blogger'"
        :current-user-id="authStore.user?.id"
        @reply="handleReply"
        @delete="handleDelete"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.comment-section {
  padding: 2rem;
  border-top: 1px solid var(--color-border);
}

.section-title {
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
}

.comment-form {
  margin-bottom: 1.5rem;

  textarea {
    width: 100%;
    padding: 0.75rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--color-border);
    border-radius: 8px;
    color: var(--color-text-primary);
    font-size: 0.875rem;
    resize: vertical;
    font-family: inherit;

    &:focus {
      outline: none;
      border-color: var(--color-accent);
    }
  }

  .submit-btn {
    margin-top: 0.5rem;
    padding: 0.5rem 1.5rem;
    background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
    border: none;
    border-radius: 8px;
    color: white;
    font-size: 0.875rem;
    cursor: pointer;
    transition: all 0.3s;

    &:hover:not(:disabled) {
      transform: translateY(-1px);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.login-hint {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.loading, .empty {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}
</style>
