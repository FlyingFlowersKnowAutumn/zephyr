<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as questionApi from '@/api/question'
import type { QuestionDTO } from '@/types'
import Header from '@/components/layout/Header.vue'

const router = useRouter()
const questions = ref<QuestionDTO[]>([])
const loading = ref(false)
const activeTab = ref<'open' | 'answered' | 'all'>('open')

const tabs = [
  { key: 'open' as const, label: '待回答' },
  { key: 'answered' as const, label: '已回答' },
  { key: 'all' as const, label: '全部' }
]

onMounted(() => { fetchQuestions() })

async function fetchQuestions() {
  loading.value = true
  try {
    const params: any = { page: 0, size: 20, isPublic: true }
    if (activeTab.value !== 'all') {
      params.status = activeTab.value
    }
    const result = await questionApi.getQuestions(params)
    if (result.code === 0) {
      questions.value = result.data.content
    }
  } finally {
    loading.value = false
  }
}

function switchTab(tab: typeof activeTab.value) {
  activeTab.value = tab
  fetchQuestions()
}

function goToDetail(id: string) {
  router.push(`/questions/${id}`)
}

function goToAsk() {
  router.push('/questions/ask')
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="questions-container">
    <Header />
    <header class="questions-header">
      <h1>提问区</h1>
      <button class="ask-btn" @click="goToAsk">我要提问</button>
    </header>

    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="questions.length === 0" class="empty">
      暂无提问
    </div>

    <div v-else class="question-list">
      <div
        v-for="q in questions"
        :key="q.id"
        class="question-card"
        @click="goToDetail(q.id)"
      >
        <div class="q-header">
          <h3 class="q-title">{{ q.title }}</h3>
          <span class="q-status" :class="q.status">
            {{ q.status === 'open' ? '待回答' : q.status === 'answered' ? '已回答' : '已关闭' }}
          </span>
        </div>
        <p class="q-excerpt">{{ q.content.slice(0, 150) }}{{ q.content.length > 150 ? '...' : '' }}</p>
        <div class="q-meta">
          <span>{{ q.userName || '匿名' }}</span>
          <span>{{ formatDate(q.createdAt) }}</span>
          <span>{{ q.answers?.length || 0 }} 个回答</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.questions-container {
  min-height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.questions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;

  h1 {
    font-size: 2rem;
  }
}

.ask-btn {
  padding: 0.625rem 1.5rem;
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
  }
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 0.5rem;
}

.tab-btn {
  padding: 0.5rem 1rem;
  background: none;
  border: none;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
  }

  &.active {
    background: rgba(0, 212, 255, 0.15);
    color: var(--color-accent);
  }
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.question-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: var(--color-accent);
    transform: translateX(4px);
  }
}

.q-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.q-title {
  font-size: 1.125rem;
  margin: 0;
}

.q-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  white-space: nowrap;

  &.open {
    background: rgba(34, 197, 94, 0.15);
    color: #22c55e;
  }

  &.answered {
    background: rgba(0, 212, 255, 0.15);
    color: #00d4ff;
  }

  &.closed {
    background: rgba(156, 163, 175, 0.15);
    color: #9ca3af;
  }
}

.q-excerpt {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  line-height: 1.6;
  margin: 0 0 0.75rem 0;
}

.q-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.loading, .empty {
  text-align: center;
  padding: 4rem;
  color: var(--color-text-secondary);
}
</style>
