<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import * as questionApi from '@/api/question'
import type { QuestionDTO } from '@/types'
import Header from '@/components/layout/Header.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const questionId = route.params.id as string
const question = ref<QuestionDTO | null>(null)
const loading = ref(false)
const answerContent = ref('')

const isAdmin = computed(() => authStore.user?.role === 'admin' || authStore.user?.role === 'blogger')

onMounted(() => { fetchQuestion() })

async function fetchQuestion() {
  loading.value = true
  try {
    const result = await questionApi.getQuestionDetail(questionId)
    if (result.code === 0) {
      question.value = result.data
    }
  } finally {
    loading.value = false
  }
}

async function handleAnswer() {
  if (!answerContent.value.trim()) return
  const result = await questionApi.createAnswer(questionId, answerContent.value.trim())
  if (result.code === 0) {
    answerContent.value = ''
    fetchQuestion()
  }
}

async function handleAccept(answerId: string) {
  await questionApi.acceptAnswer(questionId, answerId)
  fetchQuestion()
}

function goBack() {
  router.back()
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="question-detail-container">
    <Header />
    <header class="detail-header">
      <button class="back-btn" @click="goBack">← 返回</button>
    </header>

    <div v-if="loading" class="loading">加载中...</div>

    <main v-else-if="question" class="detail-content">
      <div class="question-card">
        <h1 class="question-title">{{ question.title }}</h1>
        <div class="question-meta">
          <span>{{ question.userName || '匿名' }}</span>
          <span>{{ formatDate(question.createdAt) }}</span>
          <span class="status-badge" :class="question.status">
            {{ question.status === 'open' ? '待回答' : question.status === 'answered' ? '已回答' : '已关闭' }}
          </span>
        </div>
        <div class="question-body">{{ question.content }}</div>
      </div>

      <section class="answers-section">
        <h2>回答 ({{ question.answers?.length || 0 }})</h2>

        <div v-if="isAdmin" class="answer-form">
          <textarea
            v-model="answerContent"
            placeholder="写下你的回答..."
            rows="3"
          />
          <button
            class="submit-btn"
            :disabled="!answerContent.trim()"
            @click="handleAnswer"
          >
            提交回答
          </button>
        </div>

        <div v-if="!question.answers?.length" class="empty-answers">
          暂无回答
        </div>

        <div v-else class="answers-list">
          <div
            v-for="answer in question.answers"
            :key="answer.id"
            class="answer-card"
            :class="{ accepted: answer.isAccepted }"
          >
            <div class="answer-header">
              <span class="answer-author">{{ answer.userName || '博主' }}</span>
              <span class="answer-time">{{ formatDate(answer.createdAt) }}</span>
              <span v-if="answer.isAccepted" class="accepted-badge">已采纳</span>
            </div>
            <p class="answer-content">{{ answer.content }}</p>
            <div v-if="isAdmin && !answer.isAccepted" class="answer-actions">
              <button class="accept-btn" @click="handleAccept(answer.id)">
                采纳此回答
              </button>
            </div>
          </div>
        </div>
      </section>
    </main>

    <div v-else class="not-found">
      <p>提问不存在或无权查看</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.question-detail-container {
  min-height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.detail-header {
  margin-bottom: 2rem;
}

.back-btn {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 1rem;
  transition: color 0.3s;

  &:hover {
    color: var(--color-accent);
  }
}

.question-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.question-title {
  font-size: 1.5rem;
  margin: 0 0 1rem 0;
}

.question-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  margin-bottom: 1.5rem;
}

.status-badge {
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.75rem;

  &.open { background: rgba(34, 197, 94, 0.15); color: #22c55e; }
  &.answered { background: rgba(0, 212, 255, 0.15); color: #00d4ff; }
  &.closed { background: rgba(156, 163, 175, 0.15); color: #9ca3af; }
}

.question-body {
  line-height: 1.7;
  color: var(--color-text-primary);
  font-size: 1rem;
}

.answers-section {
  h2 {
    font-size: 1.25rem;
    margin-bottom: 1rem;
  }
}

.answer-form {
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

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.answer-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem;

  &.accepted {
    border-color: #22c55e;
    background: rgba(34, 197, 94, 0.05);
  }
}

.answer-header {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.8rem;
}

.answer-author {
  font-weight: 600;
  color: var(--color-accent);
}

.answer-time {
  color: var(--color-text-secondary);
}

.accepted-badge {
  padding: 0.15rem 0.5rem;
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
  border-radius: 10px;
  font-size: 0.7rem;
}

.answer-content {
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--color-text-primary);
  margin: 0 0 0.5rem 0;
}

.answer-actions {
  .accept-btn {
    background: none;
    border: 1px solid #22c55e;
    border-radius: 6px;
    padding: 0.25rem 0.75rem;
    color: #22c55e;
    font-size: 0.8rem;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: rgba(34, 197, 94, 0.15);
    }
  }
}

.empty-answers {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}

.loading, .not-found {
  text-align: center;
  padding: 4rem;
  color: var(--color-text-secondary);
}
</style>
