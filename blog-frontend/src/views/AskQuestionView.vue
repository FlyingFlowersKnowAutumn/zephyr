<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as questionApi from '@/api/question'
import type { QuestionCreateDTO } from '@/types'
import Header from '@/components/layout/Header.vue'

const router = useRouter()
const title = ref('')
const content = ref('')
const isPublic = ref(true)
const submitting = ref(false)

async function handleSubmit() {
  if (!title.value.trim() || !content.value.trim()) return
  submitting.value = true
  try {
    const dto: QuestionCreateDTO = {
      title: title.value.trim(),
      content: content.value.trim(),
      isPublic: isPublic.value
    }
    const result = await questionApi.createQuestion(dto)
    if (result.code === 0 && result.data) {
      router.push(`/questions/${result.data.id}`)
    }
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="ask-question-container">
    <Header />
    <header class="ask-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>提出新问题</h1>
    </header>

    <form class="ask-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>标题</label>
        <input
          v-model="title"
          placeholder="简明扼要地描述你的问题"
          maxlength="100"
        />
      </div>

      <div class="form-group">
        <label>详细描述</label>
        <textarea
          v-model="content"
          placeholder="详细描述你的问题..."
          rows="8"
        />
      </div>

      <div class="form-group">
        <label class="checkbox-label">
          <input v-model="isPublic" type="checkbox" />
          <span>公开提问（其他访客可以看到）</span>
        </label>
      </div>

      <button
        class="submit-btn"
        type="submit"
        :disabled="!title.trim() || !content.trim() || submitting"
      >
        {{ submitting ? '提交中...' : '提交提问' }}
      </button>
    </form>
  </div>
</template>

<style lang="scss" scoped>
.ask-question-container {
  min-height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.ask-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;

  h1 {
    font-size: 1.5rem;
    margin: 0;
  }
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

.ask-form {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;

  label {
    display: block;
    font-size: 0.875rem;
    font-weight: 500;
    margin-bottom: 0.5rem;
    color: var(--color-text-primary);
  }
}

input[type="text"] {
  width: 100%;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text-primary);
  font-size: 0.9rem;

  &:focus {
    outline: none;
    border-color: var(--color-accent);
  }
}

textarea {
  width: 100%;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text-primary);
  font-size: 0.9rem;
  resize: vertical;
  font-family: inherit;

  &:focus {
    outline: none;
    border-color: var(--color-accent);
  }
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 0.5rem;
  font-weight: 400 !important;
  cursor: pointer;

  input[type="checkbox"] {
    width: 1rem;
    height: 1rem;
    accent-color: var(--color-accent);
  }
}

.submit-btn {
  width: 100%;
  padding: 0.75rem;
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
