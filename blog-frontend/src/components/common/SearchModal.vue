<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import DOMPurify from 'dompurify'
import { useRouter } from 'vue-router'
import { listArticles, listCategories, listTags } from '@/api/article'
import type { ArticleDTO, CategoryDTO, TagDTO } from '@/types'

interface Props {
  isOpen: boolean
}

interface SearchResult {
  type: 'article' | 'category' | 'tag'
  data: ArticleDTO | CategoryDTO | TagDTO
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
}>()

const router = useRouter()
const searchInput = ref<HTMLInputElement | null>(null)
const searchQuery = ref('')
const searchResults = ref<SearchResult[]>([])
const isLoading = ref(false)
const selectedIndex = ref(-1)

const debouncedSearch = useDebounceFn(async (keyword: string) => {
  if (!keyword.trim()) {
    searchResults.value = []
    return
  }

  isLoading.value = true

  try {
    const [articleRes, categoryRes, tagRes] = await Promise.all([
      listArticles({ keyword, page: 1, size: 6 }),
      listCategories(),
      listTags()
    ])

    const results: SearchResult[] = []

    if (categoryRes.code === 200 && categoryRes.data) {
      const categories = categoryRes.data as CategoryDTO[]
      const matchedCategories = categories.filter(cat => 
        cat.name.toLowerCase().includes(keyword.toLowerCase())
      )
      matchedCategories.forEach(cat => {
        results.push({ type: 'category', data: cat })
      })
    }

    if (tagRes.code === 200 && tagRes.data) {
      const tags = tagRes.data as TagDTO[]
      const matchedTags = tags.filter(tag => 
        tag.name.toLowerCase().includes(keyword.toLowerCase())
      )
      matchedTags.forEach(tag => {
        results.push({ type: 'tag', data: tag })
      })
    }

    if (articleRes.code === 200 && articleRes.data) {
      const articles = articleRes.data.content || []
      articles.forEach(article => {
        results.push({ type: 'article', data: article })
      })
    }

    searchResults.value = results
  } catch (error) {
    searchResults.value = []
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
        handleResultClick(searchResults.value[selectedIndex.value])
      }
      break
    case 'Escape':
      emit('close')
      break
  }
}

function handleResultClick(result: SearchResult) {
  emit('close')
  
  switch (result.type) {
    case 'article':
      const article = result.data as ArticleDTO
      router.push(`/article/${article.slug}`)
      break
    case 'category':
      const category = result.data as CategoryDTO
      router.push({ path: '/categories', query: { category: category.name } })
      break
    case 'tag':
      const tag = result.data as TagDTO
      router.push({ path: '/tags', query: { tag: tag.name } })
      break
  }
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

function handleHotSearch(keyword: string) {
  searchQuery.value = keyword
}

function goToCategory(name: string) {
  emit('close')
  router.push({ path: '/categories', query: { category: name } })
}

function goToTag(name: string) {
  emit('close')
  router.push({ path: '/tags', query: { tag: name } })
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
            <div class="search-icon-wrapper">
              <svg class="search-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
            </div>
            <input
              ref="searchInput"
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="输入关键词搜索文章、分类或标签..."
            />
            <button class="close-btn" @click="emit('close')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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

            <div v-else-if="searchResults.length > 0" class="results-list">
              <div
                v-for="(result, index) in searchResults"
                :key="`${result.type}-${result.data.id}`"
                class="result-item"
                :class="{ active: selectedIndex === index }"
                @click="handleResultClick(result)"
                @mouseenter="selectedIndex = index"
              >
                <div class="result-header">
                  <span class="result-type" :class="result.type">
                    {{ result.type === 'category' ? '分类' : result.type === 'tag' ? '标签' : '文章' }}
                  </span>
                </div>
                <template v-if="result.type === 'article'">
                  <h3 class="result-title" v-html="highlightKeyword((result.data as ArticleDTO).title, searchQuery)"></h3>
                  <p class="result-summary" v-html="highlightKeyword((result.data as ArticleDTO).excerpt || '', searchQuery)"></p>
                  <span class="result-date">{{ formatDate((result.data as ArticleDTO).createdAt) }}</span>
                </template>
                <template v-else>
                  <h3 class="result-title" v-html="highlightKeyword((result.data as CategoryDTO | TagDTO).name, searchQuery)"></h3>
                  <p class="result-summary">
                    {{ result.type === 'category' ? `查看该分类下的文章` : `查看该标签下的文章` }}
                  </p>
                </template>
              </div>
            </div>

            <div v-else-if="searchQuery" class="no-results">
              未找到相关内容，请尝试其他关键词
            </div>

            <div v-else class="search-tips">
              <div class="hot-section">
                <span class="hot-label">热门搜索</span>
                <div class="hot-tags">
                  <span class="hot-tag cyan" @click="handleHotSearch('Vue3')">Vue3</span>
                  <span class="hot-tag purple" @click="handleHotSearch('TypeScript')">TypeScript</span>
                  <span class="hot-tag green" @click="handleHotSearch('Node.js')">Node.js</span>
                  <span class="hot-tag orange" @click="handleHotSearch('Java')">Java</span>
                  <span class="hot-tag pink" @click="handleHotSearch('Spring Boot')">Spring Boot</span>
                </div>
              </div>
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
  background: rgba(15, 20, 30, 0.75);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: calc(60px + 2rem);
  z-index: 9999;
}

.search-modal-container {
  width: 100%;
  max-width: 620px;
  background: var(--color-bg-card, #ffffff);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  border: 1px solid var(--color-border, rgba(0, 0, 0, 0.08));
}

.search-box {
  display: flex;
  align-items: center;
  padding: 1.75rem 2rem;
  gap: 1.25rem;
  position: relative;
  background: var(--color-bg-primary, #ffffff);
}

.search-box::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 2rem;
  right: 2rem;
  height: 1px;
  background: var(--color-border, rgba(0, 0, 0, 0.08));
  transition: opacity 0.3s ease;
}

.search-box:focus-within::after {
  background: var(--color-accent, #00d4ff);
  opacity: 0.4;
}

.search-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--color-bg-secondary, #f5f5f5);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-box:focus-within .search-icon-wrapper {
  background: rgba(0, 212, 255, 0.08);
  transform: scale(1.05);
}

.search-icon {
  color: var(--color-accent, #00d4ff);
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  .search-box:focus-within & {
    transform: scale(1.1);
  }
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 1.25rem;
  color: var(--color-text-primary, #1a1a1a);
  background: transparent;
  font-weight: 500;
  letter-spacing: 0.01em;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &::placeholder {
    color: var(--color-text-secondary, #999);
    font-weight: 400;
    letter-spacing: 0;
    transition: opacity 0.3s ease;
  }

  .search-box:focus-within &::placeholder {
    opacity: 0.6;
  }
}

.close-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  color: var(--color-text-secondary, #999);
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: var(--color-hover-bg, #f5f5f5);
    color: var(--color-text-primary, #333);
    transform: rotate(360deg);
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
    color: var(--color-text-secondary, #666);
    font-size: 0.9rem;
  }
}

.spinner {
  width: 32px;
  height: 32px;
  border: 2px solid var(--color-border, #eee);
  border-top-color: var(--color-accent, #3498db);
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
  color: var(--color-text-secondary, #999);
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
    background: var(--color-hover-bg, #f8f9fa);
  }
}

.result-header {
  margin-bottom: 0.5rem;
}

.result-type {
  font-size: 0.75rem;
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  font-weight: 500;

  &.category {
    background: rgba(52, 152, 219, 0.1);
    color: #3498db;
  }

  &.tag {
    background: rgba(155, 89, 182, 0.1);
    color: #9b59b6;
  }

  &.article {
    background: rgba(46, 204, 113, 0.1);
    color: #2ecc71;
  }
}

.result-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary, #1a1a1a);
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
}

.result-summary {
  font-size: 0.9rem;
  color: var(--color-text-secondary, #666);
  margin: 0 0 0.5rem 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-date {
  font-size: 0.8rem;
  color: var(--color-text-secondary, #999);
}

.search-tips {
  padding: 1.5rem 2rem 2rem;
}

.tips-text {
  text-align: center;
  color: var(--color-text-secondary, #999);
  font-size: 0.9rem;
  margin: 0 0 1.5rem 0;
}

.hot-section {
  margin-bottom: 1.25rem;
}

.hot-label {
  display: block;
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-text-secondary, #999);
  margin-bottom: 0.75rem;
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.hot-tag {
  padding: 0.45rem 1rem;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--color-text-secondary, #999);

  &.cyan {
    background: rgba(0, 212, 255, 0.08);

    &:hover {
      background: rgba(0, 212, 255, 0.25);
      color: #000000;
      transform: translateY(-2px);
    }
  }

  &.purple {
    background: rgba(155, 89, 182, 0.08);

    &:hover {
      background: rgba(155, 89, 182, 0.25);
      color: #000000;
      transform: translateY(-2px);
    }
  }

  &.green {
    background: rgba(46, 204, 113, 0.08);

    &:hover {
      background: rgba(46, 204, 113, 0.25);
      color: #000000;
      transform: translateY(-2px);
    }
  }

  &.orange {
    background: rgba(243, 156, 18, 0.08);

    &:hover {
      background: rgba(243, 156, 18, 0.25);
      color: #000000;
      transform: translateY(-2px);
    }
  }

  &.pink {
    background: rgba(231, 76, 60, 0.08);

    &:hover {
      background: rgba(231, 76, 60, 0.25);
      color: #000000;
      transform: translateY(-2px);
    }
  }
}

:deep(mark) {
  background: rgba(0, 212, 255, 0.3);
  color: var(--color-text-primary, #856404);
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
