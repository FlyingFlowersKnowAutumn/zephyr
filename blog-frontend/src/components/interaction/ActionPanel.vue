<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import * as commentApi from '@/api/comment'
import * as favoriteApi from '@/api/favorite'
import * as reactionApi from '@/api/reaction'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'
import type { CommentDTO } from '@/types'

const props = defineProps<{
  articleId: string
  articleAuthorId?: string
  articleTitle?: string
  articleContent?: string
}>()

const authStore = useAuthStore()
const currentUserId = ref(authStore.user?.id || '')

const comments = ref<CommentDTO[]>([])
const commentPage = ref(0)
const hasMoreComments = ref(true)
const loadingComments = ref(false)
const loadingMore = ref(false)

const commentText = ref('')
const commentImages = ref<string[]>([])
const uploadingImage = ref(false)
const submittingComment = ref(false)

const liked = ref(false)
const likeCount = ref(0)
const commentCount = ref(0)
const favorited = ref(false)
const favoriteCount = ref(0)

const replyVisible = ref<Record<string, number>>({})

const replyingTo = ref<{ commentId: string; parentId: string; parentUserName?: string } | null>(null)
const replyText = ref('')
const replyImages = ref<string[]>([])
const uploadingReplyImage = ref(false)
const submittingReply = ref(false)

const commentSentinel = ref<HTMLElement | null>(null)
let loadMoreObserver: IntersectionObserver | null = null

onMounted(async () => {
  await loadData()
})

onUnmounted(() => {
  if (loadMoreObserver) {
    loadMoreObserver.disconnect()
    loadMoreObserver = null
  }
})

watch(() => props.articleId, async (newId) => {
  if (!newId) return
  comments.value = []
  commentPage.value = 0
  hasMoreComments.value = false
  commentCount.value = 0
  replyVisible.value = {}
  loadingMore.value = false
  replyingTo.value = null
  replyText.value = ''
  replyImages.value = []
  liked.value = false
  likeCount.value = 0
  favorited.value = false
  favoriteCount.value = 0
  await loadDataForArticle(newId)
}, { immediate: false })

async function loadData() {
  await loadDataForArticle(props.articleId)
}

async function loadDataForArticle(articleId: string) {
  liked.value = false
  likeCount.value = 0
  favorited.value = false
  favoriteCount.value = 0

  try {
    const [commentResult, favoriteResult, reactionResult] = await Promise.all([
      commentApi.getComments(articleId, 0, 20),
      favoriteApi.getFavoriteStatus(articleId),
      reactionApi.getReactionStatus(articleId)
    ])

    if (commentResult.code === 0 && commentResult.data) {
      comments.value = commentResult.data.content
      hasMoreComments.value = commentResult.data.totalPages > 1
      commentPage.value = 0
      commentCount.value = commentResult.data.totalElements
    }

    if (favoriteResult.code === 0 && favoriteResult.data) {
      favorited.value = !!favoriteResult.data.favorited
      favoriteCount.value = favoriteResult.data.favoriteCount || 0
    }

    if (reactionResult.code === 0 && reactionResult.data) {
      liked.value = !!reactionResult.data.liked
      likeCount.value = reactionResult.data.likeCount || 0
    }
  } catch (error) {
    console.error('Failed to load article data:', error)
  }

  await nextTick()
  setupLoadMoreObserver()
}

function setupLoadMoreObserver() {
  if (loadMoreObserver) loadMoreObserver.disconnect()
  if (!commentSentinel.value) return

  loadMoreObserver = new IntersectionObserver(
    async (entries) => {
      if (entries[0].isIntersecting && hasMoreComments.value && !loadingMore.value) {
        await loadMoreComments()
      }
    },
    { threshold: 0.1 }
  )
  loadMoreObserver.observe(commentSentinel.value)
}

async function loadMoreComments() {
  if (loadingMore.value || !hasMoreComments.value) return
  loadingMore.value = true
  try {
    const nextPage = commentPage.value + 1
    const result = await commentApi.getComments(props.articleId, nextPage, 20)
    if (result.code === 0 && result.data) {
      comments.value.push(...result.data.content)
      commentPage.value = nextPage
      hasMoreComments.value = nextPage < result.data.totalPages
    }
  } catch {
    // silent
  } finally {
    loadingMore.value = false
  }
}

async function handleUploadImage(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploadingImage.value = true
  try {
    const url = await uploadImage(file)
    if (url) commentImages.value.push(url)
  } catch {
    ElMessage.error('图片上传失败')
  } finally {
    uploadingImage.value = false
    input.value = ''
  }
}

function insertEmoji() {
  commentText.value += '😊'
}

async function submitComment() {
  if (!commentText.value.trim() || submittingComment.value) return
  submittingComment.value = true
  try {
    const result = await commentApi.createComment({
      articleId: props.articleId,
      content: commentText.value,
      images: commentImages.value.length ? commentImages.value : undefined
    })
    if (result.code === 0 && result.data) {
      comments.value.unshift(result.data)
      commentText.value = ''
      commentImages.value = []
      ElMessage.success('评论已提交')
    } else {
      ElMessage.error(result.message || '评论失败')
    }
  } catch {
    ElMessage.error('评论失败')
  } finally {
    submittingComment.value = false
  }
}

async function handleDeleteComment(id: string) {
  try {
    const result = await commentApi.deleteComment(id)
    if (result.code === 0) {
      function removeComment(arr: CommentDTO[]): CommentDTO[] {
        return arr.filter(c => c.id !== id).map(c => {
          if (c.replies?.length) {
            c.replies = removeComment(c.replies)
          }
          return c
        })
      }
      comments.value = removeComment(comments.value)
      ElMessage.success('评论已删除')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

function canDelete(comment: CommentDTO): boolean {
  return comment.userId === currentUserId.value || authStore.user?.role === 'blogger'
}

function isAuthor(comment: CommentDTO): boolean {
  return comment.userId === props.articleAuthorId
}

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

// ========== 回复相关 ==========

function toggleReplies(commentId: string) {
  const comment = comments.value.find(c => c.id === commentId)
  if (!comment?.replies) return
  const total = getAllReplies(comment.replies).length
  const current = replyVisible.value[commentId] || 0
  
  if (current === 0) {
    replyVisible.value[commentId] = Math.min(5, total)
  } else if (current < total) {
    replyVisible.value[commentId] = total
  } else {
    replyVisible.value[commentId] = 0
  }
}

function collapseReplies(commentId: string) {
  replyVisible.value[commentId] = 0
}

function startReply(commentId: string, parentUserName?: string, parentId?: string) {
  replyingTo.value = { commentId, parentId: parentId || commentId, parentUserName }
  replyText.value = ''
  replyImages.value = []
}

function getAllReplies(replies: CommentDTO[]): (CommentDTO & { level: number })[] {
  const result: (CommentDTO & { level: number })[] = []
  function flatten(arr: CommentDTO[], level: number) {
    arr.forEach(item => {
      result.push({ ...item, level })
      if (item.replies?.length) {
        flatten(item.replies, level + 1)
      }
    })
  }
  flatten(replies, 1)
  return result
}

function getTotalReplyCount(replies: CommentDTO[]): number {
  let count = 0
  function countRecursive(arr: CommentDTO[]) {
    arr.forEach(item => {
      count++
      if (item.replies?.length) {
        countRecursive(item.replies)
      }
    })
  }
  countRecursive(replies)
  return count
}

function getTotalCommentCount(): number {
  let count = comments.value.length
  comments.value.forEach(c => {
    count += getTotalReplyCount(c.replies || [])
  })
  return count
}

function cancelReply() {
  replyingTo.value = null
}

async function handleReplyUploadImage(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploadingReplyImage.value = true
  try {
    const url = await uploadImage(file)
    if (url) replyImages.value.push(url)
  } catch {
    ElMessage.error('图片上传失败')
  } finally {
    uploadingReplyImage.value = false
    input.value = ''
  }
}

function insertReplyEmoji() {
  replyText.value += '😊'
}

async function submitReply() {
  if (!replyText.value.trim() || !replyingTo.value || submittingReply.value) return
  submittingReply.value = true
  try {
    const result = await commentApi.createComment({
      articleId: props.articleId,
      parentId: replyingTo.value.parentId,
      content: replyText.value,
      images: replyImages.value.length ? replyImages.value : undefined
    })
    if (result.code === 0 && result.data) {
      const reply = result.data
      const commentId = replyingTo.value.commentId
      const parentId = replyingTo.value.parentId

      const comment = comments.value.find(c => c.id === commentId)
      if (comment) {
        if (parentId === commentId) {
          comment.replies.push(reply)
        } else {
          const findReply = (replies: CommentDTO[]): CommentDTO | undefined => {
            for (const r of replies) {
              if (r.id === parentId) return r
              if (r.replies?.length) {
                const found = findReply(r.replies)
                if (found) return found
              }
            }
            return undefined
          }
          const parentReply = findReply(comment.replies)
          if (parentReply) {
            if (!parentReply.replies) parentReply.replies = []
            parentReply.replies.push(reply)
          }
        }
      }

      replyVisible.value[commentId] = Math.min(
        (replyVisible.value[commentId] || 5),
        getAllReplies(comment?.replies || []).length + 1
      )
      replyingTo.value = null
      ElMessage.success('回复已提交')
    } else {
      ElMessage.error(result.message || '回复失败')
    }
  } catch {
    ElMessage.error('回复失败')
  } finally {
    submittingReply.value = false
  }
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

function formatTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="action-panel">
    <!-- 评论区 -->
    <div class="comment-section">
      <div class="comment-section-header">
        <div class="comment-header-left">
          <svg class="comment-section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          <span class="comment-section-title">评论</span>
          <span class="comment-section-count">{{ getTotalCommentCount() }}</span>
        </div>
        <div class="comment-header-actions">
          <button class="action-pill like-pill" :class="{ liked }" :disabled="!authStore.isLoggedIn" @click="toggleLike">
            <svg viewBox="0 0 24 24" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
            </svg>
            <span class="pill-count">{{ likeCount }}</span>
          </button>
          <button class="action-pill favorite-pill" :class="{ favorited }" :disabled="!authStore.isLoggedIn" @click="toggleFavorite">
            <svg viewBox="0 0 24 24" :fill="favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
            <span class="pill-count">{{ favoriteCount }}</span>
          </button>
          <button class="action-pill download-pill" @click="downloadMarkdown">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
          </button>
        </div>
      </div>

      <!-- 评论输入区域 -->
      <div class="comment-input-area">
        <textarea
          v-model="commentText"
          class="comment-input"
          placeholder="写下你的评论..."
          rows="3"
        ></textarea>
        <div v-if="commentImages.length" class="preview-images">
          <div v-for="(img, i) in commentImages" :key="i" class="preview-image-wrap">
            <img :src="img" alt="preview" />
            <button class="remove-img-btn" @click="commentImages.splice(i, 1)">×</button>
          </div>
        </div>
        <div class="input-toolbar">
          <div class="toolbar-icons">
            <label class="toolbar-icon-btn" title="图片">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                <circle cx="8.5" cy="8.5" r="1.5" />
                <polyline points="21 15 16 10 5 21" />
              </svg>
              <input type="file" accept="image/*" hidden @change="handleUploadImage" :disabled="uploadingImage" />
            </label>
            <button class="toolbar-icon-btn" title="表情" @click="insertEmoji">😊</button>
          </div>
          <button class="comment-submit-btn" :disabled="!commentText.trim() || submittingComment" @click="submitComment">
            {{ submittingComment ? '提交中...' : '发布评论' }}
          </button>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list">
        <div v-if="comments.length === 0 && !loadingComments" class="no-comments">
          <span class="no-comments-icon">💬</span>
          <span>还没有评论，来说两句吧</span>
        </div>

        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">
            <img v-if="comment.avatarUrl" :src="comment.avatarUrl" :alt="comment.userName" />
            <span v-else>{{ (comment.userName || '匿').charAt(0).toUpperCase() }}</span>
          </div>
          <div class="comment-body">
            <div class="comment-meta">
              <span class="comment-username">{{ comment.userName || '匿名' }}</span>
              <span v-if="isAuthor(comment)" class="author-badge">作者</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div v-if="comment.images && comment.images.length" class="comment-images">
              <img v-for="(img, i) in comment.images" :key="i" :src="img" alt="comment-image" />
            </div>
            <div class="comment-item-actions">
              <button class="reply-btn" @click="startReply(comment.id)">回复</button>
              <button v-if="canDelete(comment)" class="delete-btn" @click="handleDeleteComment(comment.id)">删除</button>
              <span v-if="comment.replies?.length && (!replyVisible[comment.id] || replyVisible[comment.id] === 0)" class="toggle-link expand-arrow" @click="toggleReplies(comment.id)">
                —— 展开{{ getTotalReplyCount(comment.replies) }}条回复
              </span>
            </div>

            <!-- 回复区域 -->
            <div v-if="replyVisible[comment.id] && replyVisible[comment.id] > 0 && comment.replies?.length" class="replies-section">
              <div
                v-for="(reply, _) in getAllReplies(comment.replies).slice(0, replyVisible[comment.id])"
                :key="reply.id"
                class="reply-item"
                :class="{ nested: reply.level > 1 }"
              >
                <div class="reply-avatar">
                  <img v-if="reply.avatarUrl" :src="reply.avatarUrl" :alt="reply.userName" />
                  <span v-else>{{ (reply.userName || '匿').charAt(0).toUpperCase() }}</span>
                </div>
                <div class="reply-body">
                  <div class="reply-meta">
                    <span class="reply-username">{{ reply.userName || '匿名' }}</span>
                    <span v-if="reply.level > 1 && reply.replyToUserName" class="reply-to">
                      <span class="arrow-up">▲</span> {{ reply.replyToUserName }}
                    </span>
                    <span v-if="isAuthor(reply)" class="author-badge">作者</span>
                    <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                  </div>
                  <div class="reply-content">{{ reply.content }}</div>
                  <div v-if="reply.images && reply.images.length" class="reply-images">
                    <img v-for="(img, i) in reply.images" :key="i" :src="img" alt="reply-image" />
                  </div>
                  <div class="reply-item-actions">
                    <button class="reply-btn" @click="startReply(comment.id, reply.userName, reply.id)">回复</button>
                    <button v-if="canDelete(reply)" class="delete-btn" @click="handleDeleteComment(reply.id)">删除</button>
                  </div>

                  <!-- 回复弹窗 - 回复二级回复时显示 -->
                  <div v-if="replyingTo?.commentId === comment.id && replyingTo.parentId === reply.id" class="reply-dialog">
                    <div class="reply-dialog-header">
                      <span v-if="replyingTo.parentUserName">回复 @{{ replyingTo.parentUserName }}</span>
                      <span v-else>发表回复</span>
                      <button class="reply-dialog-close" @click="cancelReply">×</button>
                    </div>
                    <textarea v-model="replyText" class="reply-textarea" placeholder="写下你的回复..." rows="2"></textarea>
                    <div v-if="replyImages.length" class="preview-images">
                      <div v-for="(img, i) in replyImages" :key="i" class="preview-image-wrap">
                        <img :src="img" alt="preview" />
                        <button class="remove-img-btn" @click="replyImages.splice(i, 1)">×</button>
                      </div>
                    </div>
                    <div class="reply-dialog-toolbar">
                      <div class="toolbar-icons">
                        <label class="toolbar-icon-btn" title="图片">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                            <circle cx="8.5" cy="8.5" r="1.5" />
                            <polyline points="21 15 16 10 5 21" />
                          </svg>
                          <input type="file" accept="image/*" hidden @change="handleReplyUploadImage" :disabled="uploadingReplyImage" />
                        </label>
                        <button class="toolbar-icon-btn" title="表情" @click="insertReplyEmoji">😊</button>
                      </div>
                      <button class="reply-submit-btn" :disabled="!replyText.trim() || submittingReply" @click="submitReply">
                        {{ submittingReply ? '提交中...' : '提交回复' }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="replies-toggle">
                <span
                  v-if="getAllReplies(comment.replies).length > replyVisible[comment.id]"
                  class="toggle-link expand-arrow"
                  @click="toggleReplies(comment.id)"
                >—— 展开全部</span>
                <span
                  v-if="getAllReplies(comment.replies).length > replyVisible[comment.id]"
                  class="toggle-link collapse-link collapse-arrow"
                  @click="collapseReplies(comment.id)"
                >—— 收起</span>
                <span
                  v-if="replyVisible[comment.id] >= getAllReplies(comment.replies).length"
                  class="toggle-link collapse-link collapse-arrow"
                  @click="collapseReplies(comment.id)"
                >—— 收起</span>
              </div>
            </div>

            <!-- 回复弹窗 - 回复一级评论时显示 -->
            <div v-if="replyingTo?.commentId === comment.id && replyingTo.parentId === comment.id" class="reply-dialog">
              <div class="reply-dialog-header">
                <span v-if="replyingTo.parentUserName">回复 @{{ replyingTo.parentUserName }}</span>
                <span v-else>发表回复</span>
                <button class="reply-dialog-close" @click="cancelReply">×</button>
              </div>
              <textarea v-model="replyText" class="reply-textarea" placeholder="写下你的回复..." rows="2"></textarea>
              <div v-if="replyImages.length" class="preview-images">
                <div v-for="(img, i) in replyImages" :key="i" class="preview-image-wrap">
                  <img :src="img" alt="preview" />
                  <button class="remove-img-btn" @click="replyImages.splice(i, 1)">×</button>
                </div>
              </div>
              <div class="reply-dialog-toolbar">
                <div class="toolbar-icons">
                  <label class="toolbar-icon-btn" title="图片">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                      <circle cx="8.5" cy="8.5" r="1.5" />
                      <polyline points="21 15 16 10 5 21" />
                    </svg>
                    <input type="file" accept="image/*" hidden @change="handleReplyUploadImage" :disabled="uploadingReplyImage" />
                  </label>
                  <button class="toolbar-icon-btn" title="表情" @click="insertReplyEmoji">😊</button>
                </div>
                <button class="reply-submit-btn" :disabled="!replyText.trim() || submittingReply" @click="submitReply">
                  {{ submittingReply ? '提交中...' : '提交回复' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="hasMoreComments" ref="commentSentinel" class="comment-sentinel">
          <span v-if="loadingMore">加载更多评论...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.action-panel {
  width: 100%;
  margin-top: 1.5rem;
}

// ========== 评论区头部 ==========
.comment-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
}

.comment-header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.comment-header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

// ========== 操作按钮 ==========
.action-pill {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.4rem 0.85rem;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  font-size: 0.825rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }

  svg {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    transition: transform 0.25s ease;
  }

  .pill-count {
    font-weight: 700;
    font-size: 0.75rem;
    min-width: 1.1rem;
    text-align: center;
  }

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  &:active:not(:disabled) {
    transform: translateY(0) scale(0.97);
  }
}

.like-pill {
  &:hover:not(:disabled) {
    color: #ef4444;
    border-color: rgba(239, 68, 68, 0.3);
    background: rgba(239, 68, 68, 0.05);
  }

  &.liked {
    color: #ef4444;
    border-color: rgba(239, 68, 68, 0.3);
    background: rgba(239, 68, 68, 0.08);
    .pill-count { color: #ef4444; }
    svg { fill: #ef4444; }
  }
}

.favorite-pill {
  &:hover:not(:disabled) {
    color: #f59e0b;
    border-color: rgba(245, 158, 11, 0.3);
    background: rgba(245, 158, 11, 0.05);
  }

  &.favorited {
    color: #f59e0b;
    border-color: rgba(245, 158, 11, 0.3);
    background: rgba(245, 158, 11, 0.08);
    .pill-count { color: #f59e0b; }
    svg { fill: #f59e0b; }
  }
}

.download-pill {
  &:hover:not(:disabled) {
    color: var(--color-accent);
    border-color: rgba(0, 212, 255, 0.3);
    background: rgba(0, 212, 255, 0.05);
    .pill-count { background: rgba(0, 212, 255, 0.08); }
  }
}

// ========== 评论区 ==========
.comment-section {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.comment-section-icon {
  width: 18px;
  height: 18px;
  color: var(--color-accent);
  flex-shrink: 0;
}

.comment-section-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.comment-section-count {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-text-secondary);
  background: var(--color-bg-primary);
  padding: 2px 10px;
  border-radius: 10px;
}

// ========== 评论输入 ==========
.comment-input-area {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.comment-input {
  width: 100%;
  padding: 0.875rem 1rem;
  font-size: 0.9rem;
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  outline: none;
  resize: none;
  box-sizing: border-box;
  line-height: 1.6;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus {
    border-color: var(--color-accent);
    box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
  }

  &::placeholder { color: var(--color-text-secondary); opacity: 0.6; }
}

.preview-images {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
  flex-wrap: wrap;
}

.preview-image-wrap {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--color-border);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .remove-img-btn {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.55);
    color: #fff;
    border: none;
    cursor: pointer;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;

    &:hover { background: #ef4444; }
  }
}

.input-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.75rem;
}

.toolbar-icons {
  display: flex;
  gap: 0.25rem;
}

.toolbar-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  border: none;
  background: transparent;
  padding: 0;

  svg {
    width: 18px;
    height: 18px;
    color: var(--color-text-secondary);
  }

  &:hover { background: var(--color-bg-primary); }
}

.comment-submit-btn {
  padding: 0.55rem 1.5rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
    box-shadow: none;
  }

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  }
}

// ========== 评论列表 ==========
.comment-list {
  display: flex;
  flex-direction: column;
}

.no-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 3rem 2rem;
  color: var(--color-text-secondary);
  font-size: 0.9rem;

  .no-comments-icon {
    font-size: 2rem;
    opacity: 0.5;
  }
}

.comment-item {
  display: flex;
  gap: 0.875rem;
  padding: 1.125rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  transition: background 0.15s;

  &:last-child { border-bottom: none; }

  &:hover { background: var(--color-bg-secondary); }
}

.comment-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(99, 102, 241, 0.3);

  img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
  }
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.375rem;
  flex-wrap: wrap;
}

.comment-username {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.author-badge {
  display: inline-block;
  padding: 1px 7px;
  font-size: 0.7rem;
  color: #fff;
  background: #ef4444;
  border-radius: 4px;
  font-weight: 600;
}

.comment-time {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  margin-left: auto;
}

.comment-content {
  font-size: 0.9rem;
  color: var(--color-text-primary);
  line-height: 1.65;
  margin-bottom: 0.5rem;
  word-break: break-word;
}

.comment-images {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;

  img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    cursor: pointer;
    border: 1px solid var(--color-border);
    transition: transform 0.2s;

    &:hover { transform: scale(1.05); }
  }
}

.comment-item-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;

  .toggle-link {
    margin-left: auto;
  }
}

.reply-btn {
  padding: 3px 12px;
  font-size: 0.8rem;
  color: var(--color-accent);
  background: rgba(0, 212, 255, 0.08);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;

  &:hover { background: rgba(0, 212, 255, 0.15); }
}

.delete-btn {
  padding: 3px 8px;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  background: transparent;
  border: none;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover { color: #ef4444; background: rgba(239, 68, 68, 0.08); }
}

// ========== 回复区域 ==========
.replies-section {
  margin-top: 0.75rem;
  padding-left: 0.75rem;
  border-left: 2px solid rgba(99, 102, 241, 0.2);
  border-radius: 0 6px 6px 0;
}

.reply-item {
  display: flex;
  gap: 0.5rem;
  padding: 0.6rem 0;

  &:first-child { padding-top: 0; }
}

.reply-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 700;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
  }
}

.reply-body { flex: 1; min-width: 0; display: flex; flex-direction: column; }

.reply-meta {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  margin-bottom: 0.25rem;
  flex-wrap: wrap;
}

.reply-username {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.reply-time {
  font-size: 0.7rem;
  color: var(--color-text-secondary);
  margin-left: auto;
}

.reply-to {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 2px;

  .arrow-up {
    transform: rotate(-180deg);
    font-size: 0.65rem;
  }
}

.reply-item.nested {
  padding: 0.4rem 0;
  border-top: 1px dashed var(--color-border-light);

  &:first-child {
    padding-top: 0.4rem;
    border-top: none;
  }
}

.reply-content {
  font-size: 0.85rem;
  color: var(--color-text-primary);
  line-height: 1.55;
  margin-bottom: 0.375rem;
}

.reply-images {
  display: flex;
  gap: 0.375rem;
  margin-bottom: 0.375rem;

  img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid var(--color-border);
  }
}

.reply-item-actions {
  display: flex;
  gap: 0.5rem;
}

.replies-toggle {
  padding: 0.375rem 0;
  display: flex;
  gap: 1rem;
  align-items: center;

  .collapse-link {
    margin-left: auto;
  }
}

.toggle-link {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s;
  display: flex;
  align-items: center;

  &:hover { color: var(--color-accent); }

  &.expand-arrow::after {
    content: '';
    display: inline-block;
    width: 5px;
    height: 5px;
    border-right: 1.5px solid currentColor;
    border-bottom: 1.5px solid currentColor;
    transform: rotate(45deg);
    margin-left: 4px;
    vertical-align: middle;
  }

  &.collapse-arrow::after {
    content: '';
    display: inline-block;
    width: 5px;
    height: 5px;
    border-right: 1.5px solid currentColor;
    border-bottom: 1.5px solid currentColor;
    transform: rotate(-135deg);
    margin-left: 4px;
    vertical-align: middle;
  }
}

.replies-loading {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  padding: 0.5rem 0;
}

// ========== 回复弹窗 ==========
.reply-dialog {
  margin-top: 0.75rem;
  padding: 0.875rem;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  align-self: stretch;
}

.reply-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.reply-dialog-close {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover { color: #ef4444; background: rgba(239, 68, 68, 0.08); }
}

.reply-textarea {
  width: 100%;
  padding: 0.625rem 0.75rem;
  font-size: 0.85rem;
  color: var(--color-text-primary);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  outline: none;
  resize: none;
  box-sizing: border-box;
  line-height: 1.5;
  transition: border-color 0.2s;

  &:focus { border-color: var(--color-accent); }
}

.reply-dialog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.5rem;
}

.reply-submit-btn {
  padding: 0.4rem 1.125rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.2s;

  &:disabled { opacity: 0.4; cursor: not-allowed; }
  &:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
}

.comment-sentinel {
  text-align: center;
  padding: 1rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

// ========== 响应式 ==========
@media (max-width: 768px) {
  .comment-header-actions {
    gap: 0.375rem;
  }

  .action-pill {
    gap: 0.25rem;
    padding: 0.35rem 0.65rem;
    font-size: 0.75rem;
    border-radius: 16px;

    svg { width: 14px; height: 14px; }
    .pill-count { font-size: 0.7rem; }
  }

  .comment-section-header { padding: 0.875rem 1rem; }
  .comment-input-area { padding: 1rem; }
  .comment-item { padding: 1rem; gap: 0.625rem; }
  .comment-avatar { width: 32px; height: 32px; font-size: 0.75rem; }
}
</style>
