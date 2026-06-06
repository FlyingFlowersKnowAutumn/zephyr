<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import DOMPurify from 'dompurify'
import { useRouter } from 'vue-router'
import { listArticles } from '@/api/article'
import type { ArticleDTO } from '@/types'

interface Props {
  isOpen: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
}>()

const router = useRouter()
const searchInput = ref<HTMLInputElement | null>(null)
const searchQuery = ref('')
const searchResults = ref<ArticleDTO[]>([])
const isLoading = ref(false)
const selectedIndex = ref(-1)
const errorMessage = ref('')

const debouncedSearch = useDebounceFn(async (keyword: string) => {
  if (!keyword.trim()) {
    searchResults.value = []
    errorMessage.value = ''
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await listArticles({
      keyword,
      page: 1,
      size: 10
    })

    if (response.code === 200 && response.data) {
      searchResults.value = response.data.content || []
      if (searchResults.value.length === 0) {
        errorMessage.value = '未找到相关文章，请尝试其他关键词'
      }
    } else {
      errorMessage.value = '搜索失败，请稍后重试'
    }
  } catch (error) {
    errorMessage.value = '网络连接失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}, 300)

watch(searchQuery, (newValue) => {
  selectedIndex.value = -1
  debouncedSearch(newValue)
})

watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    nextTick(() => {
      searchInput.value?.focus()
    })
    searchQuery.value = ''
    searchResults.value = []
    errorMessage.value = ''
  }
})

function handleKeydown(e: KeyboardEvent) {
  if (!props.isOpen) return

  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      if (selectedIndex.value < searchResults.value.length - 1) {
        selectedIndex.value++
      }
      break
    case 'ArrowUp':
      e.preventDefault()
      if (selectedIndex.value > 0) {
        selectedIndex.value--
      }
      break
    case 'Enter':
      e.preventDefault()
      if (selectedIndex.value >= 0 && searchResults.value[selectedIndex.value]) {
        goToArticle(searchResults.value[selectedIndex.value])
      }
      break
    case 'Escape':
      emit('close')
      break
  }
}

function goToArticle(article: ArticleDTO) {
  emit('close')
  router.push(`/article/${article.slug}`)
}

function highlightKeyword(text: string, keyword: string): string {
  if (!keyword.trim()) return DOMPurify.sanitize(text)
  
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  const highlighted = text.replace(regex, '<mark>$1</mark>')
  return DOMPurify.sanitize(highlighted)
}

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="isOpen" class="search-modal-overlay" @click="emit('close')">
        <div class="search-modal-container" @click.stop>
          <div class="search-box">
            <svg class="search-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              ref="searchInput"
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="输入关键词搜索..."
            />
            <button class="close-btn" @click="emit('close')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <div class="search-results">
            <div v-if="isLoading" class="loading">
              <div class="spinner"></div>
              <span>搜索中...</span>
            </div>

            <div v-else-if="errorMessage" class="no-results">
              {{ errorMessage }}
            </div>

            <div v-else-if="searchResults.length > 0" class="results-list">
              <div
                v-for="(article, index) in searchResults"
                :key="article.id"
                class="result-item"
                :class="{ active: selectedIndex === index }"
                @click="goToArticle(article)"
                @mouseenter="selectedIndex = index"
              >
                <h3 class="result-title" v-html="highlightKeyword(article.title, searchQuery)"></h3>
                <p class="result-summary" v-html="highlightKeyword(article.excerpt, searchQuery)"></p>
                <span class="result-date">{{ formatDate(article.createdAt) }}</span>
              </div>
            </div>

            <div v-else-if="searchQuery" class="no-results">
              未找到相关文章，请尝试其他关键词
            </div>

            <div v-else class="search-tips">
              <p>输入关键词开始搜索文章</p>
              <p class="hot-search">热门搜索：Vue3、TypeScript、Node.js</p>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style lang="scss" scoped>
.search-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 15vh;
  z-index: 9999;
}

.search-modal-container {
  width: 100%;
  max-width: 600px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.search-box {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
  gap: 1rem;
}

.search-icon {
  color: #999;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 1.2rem;
  color: #333;
  background: transparent;

  &::placeholder {
    color: #bbb;
  }
}

.close-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.2s;

  &:hover {
    background: #f5f5f5;
    color: #666;
  }
}

.search-results {
  max-height: 400px;
  overflow-y: auto;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  gap: 0.5rem;

  span {
    color: #666;
    font-size: 0.9rem;
  }
}

.spinner {
  width: 32px;
  height: 32px;
  border: 2px solid #eee;
  border-top-color: #3498db;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.no-results {
  padding: 2rem;
  text-align: center;
  color: #999;
}

.results-list {
  padding: 0.5rem;
}

.result-item {
  padding: 1rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover,
  &.active {
    background: #f8f9fa;
  }

  &.active {
    background: #e3f2fd;
  }
}

.result-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
}

.result-summary {
  font-size: 0.9rem;
  color: #666;
  margin: 0 0 0.5rem 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-date {
  font-size: 0.8rem;
  color: #999;
}

.search-tips {
  padding: 2rem;
  text-align: center;
  color: #999;

  p {
    margin: 0 0 0.5rem 0;
    font-size: 0.9rem;
  }

  .hot-search {
    font-size: 0.8rem;
    color: #bbb;
  }
}

:deep(mark) {
  background: #fff3cd;
  color: #856404;
  padding: 0 4px;
  border-radius: 2px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active .search-modal-container,
.fade-leave-active .search-modal-container {
  transition: transform 0.3s ease;
}

.fade-enter-from .search-modal-container,
.fade-leave-to .search-modal-container {
  transform: scale(0.95);
}
</style>
