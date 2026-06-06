<script setup lang="ts">
import { ref } from 'vue'
import type { CommentDTO } from '@/types'

const props = defineProps<{
  comment: CommentDTO
  isAdmin: boolean
  currentUserId?: string
  depth?: number
}>()

const emit = defineEmits<{
  reply: [parentId: string, content: string]
  delete: [id: string]
}>()

const showReplyForm = ref(false)
const replyContent = ref('')
const maxDepth = 1

const canDelete = () => {
  return props.currentUserId === props.comment.userId || props.isAdmin
}

const canReply = () => {
  return (props.depth || 0) < maxDepth
}

async function handleReply() {
  if (!replyContent.value.trim()) return
  await emit('reply', props.comment.id, replyContent.value.trim())
  replyContent.value = ''
  showReplyForm.value = false
}

function formatDate(dateString: string) {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="comment-item" :class="{ 'is-reply': depth }">
    <div class="comment-body">
      <div class="comment-header">
        <span class="comment-author">{{ comment.userName || '匿名' }}</span>
        <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
      </div>
      <p class="comment-content">{{ comment.content }}</p>
      <div class="comment-actions">
        <button v-if="canReply()" class="action-btn" @click="showReplyForm = !showReplyForm">
          {{ showReplyForm ? '取消回复' : '回复' }}
        </button>
        <button v-if="canDelete()" class="action-btn delete-btn" @click="emit('delete', comment.id)">
          删除
        </button>
      </div>

      <div v-if="showReplyForm" class="reply-form">
        <textarea
          v-model="replyContent"
          placeholder="写下你的回复..."
          rows="2"
          maxlength="200"
        />
        <button class="reply-submit" :disabled="!replyContent.trim()" @click="handleReply">
          回复
        </button>
      </div>
    </div>

    <div v-if="comment.replies && comment.replies.length > 0" class="replies">
      <CommentItem
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
        :is-admin="isAdmin"
        :current-user-id="currentUserId"
        :depth="(depth || 0) + 1"
        @reply="emit('reply', $event, $event)"
        @delete="emit('delete', $event)"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.comment-item {
  padding: 1rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);

  &.is-reply {
    margin-left: 2rem;
  }
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-bottom: 0.5rem;
}

.comment-author {
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--color-accent);
}

.comment-time {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.comment-content {
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--color-text-primary);
  margin: 0 0 0.5rem 0;
}

.comment-actions {
  display: flex;
  gap: 1rem;
}

.action-btn {
  background: none;
  border: none;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 0;
  transition: color 0.2s;

  &:hover {
    color: var(--color-accent);
  }

  &.delete-btn:hover {
    color: #ef4444;
  }
}

.reply-form {
  margin-top: 0.75rem;

  textarea {
    width: 100%;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--color-border);
    border-radius: 6px;
    color: var(--color-text-primary);
    font-size: 0.8rem;
    resize: vertical;
    font-family: inherit;

    &:focus {
      outline: none;
      border-color: var(--color-accent);
    }
  }

  .reply-submit {
    margin-top: 0.375rem;
    padding: 0.375rem 1rem;
    background: var(--color-accent);
    border: none;
    border-radius: 6px;
    color: white;
    font-size: 0.8rem;
    cursor: pointer;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.replies {
  margin-top: 0.5rem;
  padding-left: 0.5rem;
  border-left: 2px solid rgba(0, 212, 255, 0.2);
}
</style>
