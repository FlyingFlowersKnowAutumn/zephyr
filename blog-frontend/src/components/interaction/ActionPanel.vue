<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import * as reactionApi from '@/api/reaction'
import * as favoriteApi from '@/api/favorite'

const props = defineProps<{
  articleId: string
  articleTitle?: string
  articleContent?: string
}>()

const authStore = useAuthStore()

const liked = ref(false)
const likeCount = ref(0)
const favorited = ref(false)
const favoriteCount = ref(0)

const commentText = ref('')
const comments = ref<Array<{ id: string; content: string; author: string; createdAt: string }>>([])

onMounted(async () => {
  const [reactionResult, favoriteResult] = await Promise.all([
    reactionApi.getReactionStatus(props.articleId),
    favoriteApi.getFavoriteStatus(props.articleId)
  ])
  
  if (reactionResult.code === 0 && reactionResult.data) {
    liked.value = reactionResult.data.liked
    likeCount.value = reactionResult.data.likeCount
  }
  
  if (favoriteResult.code === 0 && favoriteResult.data) {
    favorited.value = favoriteResult.data.favorited
    favoriteCount.value = favoriteResult.data.favoriteCount
  }
})

async function toggleLike() {
  if (!authStore.isLoggedIn) return
  const result = await reactionApi.toggleReaction(props.articleId)
  if (result.code === 0 && result.data) {
    liked.value = result.data.liked
    likeCount.value = result.data.likeCount
  }
}

async function toggleFavorite() {
  if (!authStore.isLoggedIn) return
  const result = await favoriteApi.toggleFavorite(props.articleId)
  if (result.code === 0 && result.data) {
    favorited.value = result.data.favorited
    favoriteCount.value = result.data.favoriteCount
  }
}
function submitComment() {
  if (!commentText.value.trim() || !authStore.isLoggedIn) return
  
  comments.value.unshift({
    id: Date.now().toString(),
    content: commentText.value,
    author: authStore.user?.username || '用户',
    createdAt: new Date().toLocaleString('zh-CN')
  })
  commentText.value = ''
}
function downloadMarkdown() {
  const content = `# ${props.articleTitle || '文章标题'}\n\n${props.articleContent || ''}`
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${props.articleTitle || 'article'}.md`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
</script>

<template>
  <div class="action-panel">
    <div class="comment-section">
      <div class="comment-header">
        <div class="comment-title-wrapper">
          <svg class="comment-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          <span class="comment-title">评论</span>
        </div>
        
        <div class="comment-actions">
          <button class="action-btn like-btn" :class="{ liked }" :disabled="!authStore.isLoggedIn" @click="toggleLike">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
            </svg>
            <span class="action-count">{{ likeCount }}</span>
          </button>
          
          <button class="action-btn favorite-btn" :class="{ favorited }" :disabled="!authStore.isLoggedIn" @click="toggleFavorite">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
            <span class="action-count">{{ favoriteCount }}</span>
          </button>
          
          <button class="action-btn download-btn" @click="downloadMarkdown">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
          </button>
        </div>
      </div>
      
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <span class="comment-author">{{ comment.author }}</span>
          <span class="comment-content">{{ comment.content }}</span>
          <span class="comment-time">{{ comment.createdAt }}</span>
        </div>
        <div v-if="comments.length === 0" class="no-comments">
          暂无评论
        </div>
      </div>
      
      <div class="comment-input-area">
        <textarea 
          v-model="commentText" 
          class="comment-input" 
          placeholder="写下你的评论..."
          rows="3"
        ></textarea>
        <button class="comment-submit" :disabled="!commentText.trim()" @click="submitComment">
          发表
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.action-panel {
  width: 100%;
  margin-top: 1rem;
}

.comment-section {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1rem 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.comment-title-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.comment-icon {
  width: 18px;
  height: 18px;
  color: var(--color-accent);
}

.comment-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.25s ease;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  svg {
    width: 18px;
    height: 18px;
    stroke: var(--color-text-secondary);
    fill: transparent;
    transition: all 0.25s ease;
  }

  .action-count {
    font-size: 0.8rem;
    color: var(--color-text-secondary);
    font-weight: 600;
    min-width: 1.25rem;
    text-align: center;
  }

  &:hover:not(:disabled) {
    background: var(--color-bg-secondary);
  }
}

.like-btn {
  &:hover:not(:disabled) svg {
    stroke: #ef4444;
  }

  &.liked {
    svg {
      stroke: #ef4444;
      fill: #ef4444;
    }

    .action-count {
      color: #ef4444;
    }
  }
}

.favorite-btn {
  &:hover:not(:disabled) svg {
    stroke: #f59e0b;
  }

  &.favorited {
    svg {
      stroke: #f59e0b;
      fill: #f59e0b;
    }

    .action-count {
      color: #f59e0b;
    }
  }
}

.download-btn {
  &:hover:not(:disabled) svg {
    stroke: var(--color-accent);
  }
}

.comment-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 1rem;
  padding-right: 0.5rem;
}

.comment-item {
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--color-border);

  &:last-child {
    border-bottom: none;
  }

  .comment-author {
    font-size: 0.8rem;
    font-weight: 600;
    color: var(--color-accent);
    margin-right: 0.5rem;
  }

  .comment-content {
    font-size: 0.875rem;
    color: var(--color-text-primary);
    display: block;
    margin-top: 0.25rem;
  }

  .comment-time {
    font-size: 0.7rem;
    color: var(--color-text-secondary);
    display: block;
    margin-top: 0.25rem;
  }
}

.no-comments {
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  padding: 1.5rem 0;
}

.comment-input-area {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.comment-input {
  width: 100%;
  padding: 0.75rem;
  font-size: 0.875rem;
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  outline: none;
  resize: none;
  box-sizing: border-box;
  line-height: 1.5;

  &:focus {
    border-color: var(--color-accent);
  }

  &::placeholder {
    color: var(--color-text-secondary);
  }
}

.comment-submit {
  align-self: flex-end;
  padding: 0.5rem 1.25rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: white;
  background: var(--color-accent);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &:hover:not(:disabled) {
    opacity: 0.9;
  }
}

@media (max-width: 768px) {
  .comment-section {
    padding: 0.75rem 1rem;
  }

  .comment-actions {
    gap: 1rem;
  }

  .action-btn {
    gap: 0.25rem;
    padding: 0.25rem 0.5rem;

    svg {
      width: 14px;
      height: 14px;
    }

    .action-count {
      font-size: 0.7rem;
    }
  }
}
</style>
