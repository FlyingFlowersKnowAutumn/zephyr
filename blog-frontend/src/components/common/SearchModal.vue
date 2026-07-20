<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import DOMPurify from 'dompurify'
import { useRouter } from 'vue-router'
import { listArticles, getHotTags, incrementTagVisit } from '@/api/article'
import type { ArticleDTO, TagDTO } from '@/types'

interface Props {
  isOpen: boolean
}

interface SearchResult {
  type: 'article'
  data: ArticleDTO
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
}>()

const router = useRouter()
const searchInput = ref<HTMLInputElement | null>(null)
const searchQuery = ref('')
const searchResults = ref<SearchResult[]>([])
const totalArticles = ref(0)
const isLoading = ref(false)
const selectedIndex = ref(-1)
const hotTags = ref<TagDTO[]>([])
const tagColors: Record<string, string> = {}

const colorPalette = [
  '#00D4FF', '#9B59B6', '#2ECC71', '#F39C12', '#E74C3C',
  '#3498DB', '#1ABC9C', '#E67E22', '#95A5A6', '#16A085',
  '#D35400', '#C0392B', '#8E44AD', '#27AE60', '#F1C40F'
]

function getTagColor(tagName: string): string {
  if (!tagColors[tagName]) {
    const index = tagName.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0) % colorPalette.length
    tagColors[tagName] = colorPalette[index]
  }
  return tagColors[tagName]
}

async function loadHotTags() {
  try {
    const result = await getHotTags()
    if (result.code === 0 && result.data) {
      hotTags.value = (result.data as TagDTO[]).slice(0, 8)
    }
  } catch (error) {
    console.error('Failed to load hot tags:', error)
    hotTags.value = []
  }
}

const debouncedSearch = useDebounceFn(async (keyword: string) => {
  if (!keyword.trim()) {
    searchResults.value = []
    return
  }

  isLoading.value = true

  try {
    const articleRes = await listArticles({ keyword, page: 0, size: 50 })

    const results: SearchResult[] = []

    if (articleRes.code === 0 && articleRes.data) {
      const articles = articleRes.data.content || []
      totalArticles.value = articleRes.data.totalElements || articles.length
      articles.forEach(article => {
        results.push({ type: 'article', data: article })
      })
    }

    searchResults.value = results
  } catch (error) {
    console.error('Search API error:', error)
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
    document.body.style.overflow = 'hidden'
    loadHotTags()
  } else {
    document.body.style.overflow = ''
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
  
  const article = result.data as ArticleDTO
  router.push(`/articles/${article.slug || article.id}`)
}

function extractRelevantSentence(content: string, keyword: string): string {
  if (!content || !keyword.trim()) return ''

  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(escapedKeyword, 'gi')

  const sentences = content.split(/[。！？.!?]/)
  
  let bestSentence = ''
  let maxCount = 0

  sentences.forEach(sentence => {
    const matches = sentence.match(regex)
    const count = matches ? matches.length : 0
    if (count > maxCount && sentence.trim().length > 5) {
      maxCount = count
      bestSentence = sentence.trim()
    }
  })

  if (!bestSentence && sentences.length > 0) {
    for (const sentence of sentences) {
      if (sentence.trim().length > 5) {
        bestSentence = sentence.trim()
        break
      }
    }
  }

  return bestSentence
}

function highlightKeyword(text: string, keyword: string): string {
  if (!keyword.trim()) return DOMPurify.sanitize(text)
  
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  const highlighted = text.replace(regex, '<mark>$1</mark>')
  return DOMPurify.sanitize(highlighted)
}

function getSearchSummary(article: ArticleDTO, keyword: string): string {
  const content = article.content || ''
  const excerpt = article.excerpt || ''
  
  const relevantSentence = extractRelevantSentence(content, keyword)
  
  if (relevantSentence) {
    return highlightKeyword(relevantSentence, keyword)
  }
  
  return highlightKeyword(excerpt, keyword)
}

async function handleHotSearch(tag: TagDTO) {
  searchQuery.value = tag.name
  try {
    await incrementTagVisit(tag.id)
  } catch (error) {
    console.error('Failed to increment tag visit:', error)
  }
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
              placeholder="搜索文章..."
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
                <div class="result-content">
                  <span class="result-number">{{ index + 1 }}.</span>
                  <div class="result-text">
                    <h3 class="result-title" v-html="highlightKeyword(result.data.title, searchQuery)"></h3>
                    <p class="result-summary" v-html="getSearchSummary(result.data, searchQuery)"></p>
                  </div>
                </div>
              </div>
            </div>

            <div v-else-if="searchQuery" class="no-results">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="no-results-icon">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <p>未找到相关文章</p>
              <p class="no-results-hint">试试其他关键词？</p>
            </div>

            <div v-else class="search-tips">
              <div class="hot-section">
                <span class="hot-label">热门标签</span>
                <div class="hot-tags">
                  <span
                    v-for="tag in hotTags"
                    :key="tag.id"
                    class="hot-tag"
                    :style="{ 
                      background: `${getTagColor(tag.name)}20`,
                      '--tag-color': getTagColor(tag.name)
                    }"
                    @click="handleHotSearch(tag)"
                  >{{ tag.name }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="searchResults.length > 0" class="search-footer">
            <div class="footer-divider"></div>
            <div class="footer-count">共找到 {{ totalArticles }} 篇文章</div>
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
  max-height: calc(100vh - 2 * (60px + 2rem));
  background: var(--color-bg-card, #ffffff);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  border: 1px solid var(--color-border, rgba(0, 0, 0, 0.08));
  display: flex;
  flex-direction: column;
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
  background: repeating-linear-gradient(
    to right,
    transparent,
    transparent 8px,
    var(--color-border, rgba(0, 0, 0, 0.1)) 8px,
    var(--color-border, rgba(0, 0, 0, 0.1)) 12px
  );
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
  flex: 1;
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
  padding: 3rem 2rem;
  text-align: center;
  color: var(--color-text-secondary, #999);

  .no-results-icon {
    color: var(--color-text-secondary, #999);
    margin-bottom: 1rem;
    opacity: 0.5;
  }

  p {
    margin: 0 0 0.5rem 0;
    font-size: 1rem;
  }

  .no-results-hint {
    font-size: 0.85rem;
    opacity: 0.7;
    margin-bottom: 0;
  }
}

.results-list {
  padding: 0.5rem 1.5rem;
}

.result-item {
  padding: 0.75rem 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover,
  &.active {
    background: var(--color-hover-bg, #f8f9fa);

    .result-number {
      color: #3498db;
    }

    .result-title {
      color: #3498db;
    }
  }
}

.result-content {
  display: flex;
  gap: 0.75rem;
}

.result-number {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-secondary, #999);
  flex-shrink: 0;
  line-height: 1.5;
}

.result-text {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary, #1a1a1a);
  margin: 0 0 0.25rem 0;
  line-height: 1.5;
  transition: color 0.2s;
}

.result-summary {
  font-size: 0.85rem;
  color: var(--color-text-secondary, #666);
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

  &:hover {
    background: var(--tag-color, rgba(0, 212, 255, 0.15)) !important;
    color: #000000;
    transform: translateY(-2px);
  }
}

.search-footer {
  padding: 0.75rem 2rem 1rem;
  flex-shrink: 0;
}

.footer-divider {
  height: 1px;
  background: repeating-linear-gradient(
    to right,
    transparent,
    transparent 8px,
    var(--color-border, rgba(0, 0, 0, 0.1)) 8px,
    var(--color-border, rgba(0, 0, 0, 0.1)) 12px
  );
  margin-bottom: 0.75rem;
}

.footer-count {
  font-size: 0.85rem;
  color: var(--color-text-secondary, #999);
  text-align: center;
}

:deep(mark) {
  background: transparent;
  color: #ff6b35;
  padding: 0;
  border-radius: 0;
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
