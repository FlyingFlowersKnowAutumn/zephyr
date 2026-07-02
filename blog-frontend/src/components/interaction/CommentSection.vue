<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useCommentStore } from '@/stores/comment'
import { useAuthStore } from '@/stores/auth'
import CommentItem from './CommentItem.vue'
import type { CommentCreateDTO } from '@/types'

const props = defineProps<{ articleId: string }>()
const commentStore = useCommentStore()
const authStore = useAuthStore()

const newComment = ref('')
const isExpanded = ref(false)

const commentCount = computed(() => commentStore.comments.length)

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

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}
</script>

<template>
  <div class="comment-sidebar-wrapper">
    <button class="toggle-btn" @click="toggleExpand">
      <span class="toggle-icon">💬</span>
      <span class="toggle-label">评论</span>
      <span class="toggle-count">{{ commentCount }}</span>
      <span class="toggle-arrow" :class="{ expanded: isExpanded }">›</span>
    </button>

    <div class="comment-sidebar" :class="{ expanded: isExpanded }">
      <div class="sidebar-header">
        <h3 class="section-title">评论 ({{ commentCount }})</h3>
        <button class="close-btn" @click="toggleExpand">×</button>
      </div>

      <div class="sidebar-content">
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
    </div>

    <div v-if="isExpanded" class="overlay" @click="toggleExpand"></div>
  </div>
</template>

<style lang="scss" scoped>
.comment-sidebar-wrapper {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1000;
}

.toggle-btn {
  position: relative;
  right: 0;
  background: rgba(22, 35, 55, 0.95);
  border: 1px solid var(--color-border);
  border-right: none;
  border-radius: 8px 0 0 8px;
  padding: 0.625rem 0.75rem;
  color: var(--color-text-primary);
  font-size: 0.875rem;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  transition: all 0.3s;
  backdrop-filter: blur(12px);
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.2);

  &:hover {
    background: rgba(0, 212, 255, 0.2);
    border-color: var(--color-accent);
  }

  .toggle-icon {
    font-size: 1rem;
  }

  .toggle-label {
    font-size: 0.75rem;
  }

  .toggle-count {
    background: var(--color-accent);
    color: white;
    font-size: 0.65rem;
    padding: 0.125rem 0.375rem;
    border-radius: 10px;
    min-width: 18px;
    text-align: center;
  }

  .toggle-arrow {
    font-size: 1rem;
    transition: transform 0.3s;
    color: var(--color-text-secondary);

    &.expanded {
      transform: rotate(180deg);
    }
  }
}

.comment-sidebar {
  position: fixed;
  right: -400px;
  top: 120px;
  bottom: 60px;
  width: 400px;
  background: var(--color-bg-card);
  border-left: 1px solid var(--color-border);
  box-shadow: -8px 0 30px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  transition: right 0.35s ease;
  z-index: 1001;

  &.expanded {
    right: 0;
  }

  .sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    border-bottom: 1px solid var(--color-border);
    background: var(--color-bg-secondary);

    .section-title {
      font-size: 1.1rem;
      margin: 0;
    }

    .close-btn {
      background: none;
      border: none;
      color: var(--color-text-secondary);
      font-size: 1.5rem;
      cursor: pointer;
      padding: 0;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      transition: all 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: var(--color-text-primary);
      }
    }
  }

  .sidebar-content {
    flex: 1;
    overflow-y: auto;
    padding: 1.5rem;
  }
}

.comment-form {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--color-border);

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
    margin-top: 0.75rem;
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
      box-shadow: 0 4px 15px rgba(0, 212, 255, 0.4);
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

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@media (max-width: 768px) {
  .comment-sidebar {
    width: 100%;
    right: -100%;
    top: 100px;
  }

  .toggle-btn {
    padding: 0.5rem 0.5rem;

    .toggle-label {
      display: none;
    }
  }
}
</style>
