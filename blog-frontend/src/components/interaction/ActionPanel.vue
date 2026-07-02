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
    <div class="action-card interact-card">
      <div class="interact-row">
        <button class="like-icon" :class="{ liked }" :disabled="!authStore.isLoggedIn" @click="toggleLike">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
          <span class="interact-count">{{ likeCount }}</span>
        </button>
        <button class="favorite-icon" :class="{ favorited }" :disabled="!authStore.isLoggedIn" @click="toggleFavorite">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
          </svg>
          <span class="interact-count">{{ favoriteCount }}</span>
        </button>
      </div>
    </div>

    <div class="action-card download-card">
      <button class="download-btn" @click="downloadMarkdown">
        <span class="download-icon">📥</span>
        <span class="download-text">下载MD文档</span>
      </button>
    </div>
    
    <div class="action-card comment-card">
      <div class="comment-header">
        <span class="comment-title">留言版</span>
      </div>
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <span class="comment-author">{{ comment.author }}</span>
          <span class="comment-content">{{ comment.content }}</span>
          <span class="comment-time">{{ comment.createdAt }}</span>
        </div>
        <div v-if="comments.length === 0" class="no-comments">
          暂无留言
        </div>
      </div>
      <div class="comment-input-area">
        <textarea 
          v-model="commentText" 
          class="comment-input" 
          placeholder="写下你的留言..."
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
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.action-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.25rem;
}

.interact-card {
  padding: 1rem 1.25rem;
}

.interact-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.like-icon,
.favorite-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.25s ease;
  padding: 0;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  svg {
    width: 28px;
    height: 28px;
    stroke: var(--color-text-secondary);
    fill: transparent;
    transition: all 0.25s ease;
  }

  .interact-count {
    font-size: 0.8rem;
    color: var(--color-text-secondary);
    font-weight: 500;
  }
}

.like-icon {
  &:hover:not(:disabled) svg {
    stroke: #ef4444;
    transform: scale(1.1);
  }

  &.liked svg {
    stroke: #ef4444;
    fill: #ef4444;
  }

  &.liked .interact-count {
    color: #ef4444;
  }
}

.favorite-icon {
  &:hover:not(:disabled) svg {
    stroke: #f59e0b;
    transform: scale(1.1);
  }

  &.favorited svg {
    stroke: #f59e0b;
    fill: #f59e0b;
  }

  &.favorited .interact-count {
    color: #f59e0b;
  }
}

.download-card {
  padding: 0;
}

.download-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: rgba(0, 212, 255, 0.1);
  border: none;
  border-radius: 12px;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    background: rgba(0, 212, 255, 0.15);
    color: var(--color-accent);
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 212, 255, 0.2);
  }

  .download-icon {
    font-size: 1rem;
  }

  .download-text {
    font-weight: 500;
  }
}

.comment-card {
  padding: 1rem;
}

.comment-header {
  margin-bottom: 0.75rem;
}

.comment-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.comment-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 0.75rem;
  padding-right: 0.5rem;
}

.comment-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border);

  &:last-child {
    border-bottom: none;
  }

  .comment-author {
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--color-accent);
    margin-right: 0.5rem;
  }

  .comment-content {
    font-size: 0.85rem;
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
  font-size: 0.85rem;
  padding: 1rem 0;
}

.comment-input-area {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.comment-input {
  width: 100%;
  padding: 0.5rem;
  font-size: 0.85rem;
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  outline: none;
  resize: none;
  box-sizing: border-box;
  line-height: 1.4;

  &:focus {
    border-color: var(--color-accent);
  }

  &::placeholder {
    color: var(--color-text-secondary);
  }
}

.comment-submit {
  align-self: flex-end;
  padding: 0.4rem 1rem;
  font-size: 0.8rem;
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
</style>
