<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'
import SeoHead from '@/components/seo/SeoHead.vue'
import { useRoute, RouterLink } from 'vue-router'
import { incrementTagVisit } from '@/api/article'
import type { TagDTO } from '@/types'

const route = useRoute()
const articleStore = useArticleStore()
const authStore = useAuthStore()
const tags = ref<TagDTO[]>([])
const articles = ref<any[]>([])
const selectedTag = ref('')
const loading = ref(true)
const hoveredTag = ref('')
const isBlogger = computed(() => authStore.user?.role === 'blogger')

function formatDate(dateString?: string) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const tagColors: Record<string, string> = {}
const colorPalette = [
  '#8B9A6B', '#A67C52', '#4A5568', '#38A169', '#38B2AC', '#9F7AEA', '#63B3ED', 
  '#ED8936', '#E53E3E', '#DD6B20', '#D69E2E', '#48BB78', '#319795', '#805AD5', 
  '#553C9A', '#B794F4', '#ED64A6', '#F687B3', '#718096', '#2D3748'
]

function getTagColor(tagName: string): string {
  if (!tagColors[tagName]) {
    const index = tagName.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0) % colorPalette.length
    tagColors[tagName] = colorPalette[index]
  }
  return tagColors[tagName]
}

const showCreateModal = ref(false)
const newTagName = ref('')

async function createTag() {
  if (!newTagName.value.trim()) return
  
  await articleStore.createTag(newTagName.value.trim())
  tags.value = await articleStore.fetchTags()
  newTagName.value = ''
  showCreateModal.value = false
}

async function deleteTag(id: string) {
  if (!confirm('确定要删除这个标签吗？')) return
  
  await articleStore.deleteTag(id)
  tags.value = await articleStore.fetchTags()
  if (selectedTag.value === tags.value.find(t => t.id === id)?.name) {
    selectedTag.value = ''
  }
}

async function selectTag(tag: TagDTO) {
  if (selectedTag.value === tag.name) {
    selectedTag.value = ''
    return
  }
  selectedTag.value = tag.name
  try {
    await incrementTagVisit(tag.id)
  } catch (error) {
    console.error('Failed to increment tag visit:', error)
  }
}

const filteredArticles = computed(() => {
  if (!selectedTag.value) return []
  return articles.value.filter(article => article.tags?.includes(selectedTag.value))
})

const sortedTags = computed(() => {
  return [...tags.value].sort((a, b) => (b.visitCount || 0) - (a.visitCount || 0))
})

const tagLengthRange = computed(() => {
  const lengths = tags.value.map(t => t.name.length)
  return { min: Math.min(...lengths, 0), max: Math.max(...lengths, 0) }
})

function getTagSize(name: string): number {
  const { min, max } = tagLengthRange.value
  if (max === min) return 0.95
  return 0.8 + ((name.length - min) / (max - min)) * 0.3
}

onMounted(async () => {
  try {
    const [tagResult, articleResult] = await Promise.all([
      articleStore.fetchTags(),
      articleStore.fetchArticles({ page: 1, size: 100, status: 'PUBLISHED' })
    ])
    // 兼容新旧格式
    const tagData = tagResult as any
    tags.value = typeof tagData[0] === 'string'
      ? tagData.map((name: string) => ({ id: name, name, articleCount: 0, visitCount: 0, createdAt: '' }))
      : tagData
    articles.value = articleResult.articles
  
    const tagFromUrl = route.query.tag as string
    if (tagFromUrl) {
      const matchedTag = tags.value.find(t => t.name === tagFromUrl)
      if (matchedTag) {
        selectedTag.value = tagFromUrl
        try {
          await incrementTagVisit(matchedTag.id)
        } catch (error) {
          console.error('Failed to increment tag visit:', error)
        }
      }
    }
  } catch (e) {
    console.error('Failed to load tags:', e)
  }
  
  loading.value = false
})
</script>

<template>
  <LayoutWrapper heroTitle="标签" :showSidebar="true" heroSize="small" heroImage="/Note/wallhaven-w5lr6x_2560x1440.png">
    <SeoHead />
    
    <section class="tag-content">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else class="tag-wrapper">
        <div class="tag-cloud-section">
          <div class="section-header">
            <h3 class="section-title">所有标签</h3>
            <button v-if="isBlogger" @click="showCreateModal = true" class="create-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 4v16m8-8H4"/>
              </svg>
              <span>新建</span>
            </button>
          </div>
          <div v-if="tags.length === 0" class="tag-empty">
            还没有标签
          </div>
          <div v-else class="tag-cloud" @mouseleave="hoveredTag = ''">
            <div 
              v-for="tag in sortedTags" 
              :key="tag.id"
              class="tag-wrapper-item"
              :class="{ 'tag-cloud-hover': hoveredTag !== '' && hoveredTag !== tag.name }"
            >
              <button
                @click="selectTag(tag)"
                @mouseenter="hoveredTag = tag.name"
                class="tag"
                :class="{ active: selectedTag === tag.name }"
                :style="{ '--tag-color': getTagColor(tag.name), fontSize: `${getTagSize(tag.name)}rem` }"
              >
                <span class="tag-name">{{ tag.name }}</span>
                <span class="tag-count">{{ tag.articleCount || 0 }}</span>
                <button 
                  v-if="isBlogger" 
                  @click.stop="deleteTag(tag.id)" 
                  class="delete-btn"
                  title="删除标签"
                >
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
              </button>
            </div>
          </div>
        </div>

        <div v-if="!selectedTag" class="welcome-section">
          <div class="icon">🏷️</div>
          <h2>选择一个标签</h2>
          <p>点击上方标签来查看相关文章</p>
        </div>

        <div v-else-if="filteredArticles.length === 0" class="empty-state">
          <p>该标签下还没有文章</p>
        </div>

        <div v-else>
          <div class="selected-tag-header">
            <h3>
              相关文章
              <span class="tag-badge" :style="{ '--tag-color': getTagColor(selectedTag) }">{{ selectedTag }}</span>
            </h3>
            <span class="article-count">{{ filteredArticles.length }} 篇文章</span>
          </div>
          <div class="articles-list">
            <article
              v-for="article in filteredArticles"
              :key="article.id"
              class="tag-article"
            >
              <RouterLink :to="`/articles/${article.slug || article.id}`" class="article-link">
                <div class="article-cover" v-if="article.coverImage">
                  <img :src="article.coverImage" :alt="article.title" />      
                </div>
                <div class="article-cover placeholder" v-else>
                  <svg width="100%" height="100%" viewBox="0 0 200 120" fill="none">
                    <rect width="200" height="120" fill="var(--color-bg-secondary)"/>
                    <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" fill="var(--color-text-secondary)" font-size="12">暂无封面</text>
                  </svg>
                </div>
                <div class="article-info">
                  <span class="article-date">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                      <line x1="16" y1="2" x2="16" y2="6"/>
                      <line x1="8" y1="2" x2="8" y2="6"/>
                      <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                    <span>{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                  </span>
                  <h3 class="article-title">{{ article.title }}</h3>
                </div>
              </RouterLink>
            </article>
          </div>
        </div>
      </div>
    </section>

    <Teleport to="body">
      <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3>创建标签</h3>
            <button @click="showCreateModal = false" class="close-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <input 
              v-model="newTagName" 
              type="text" 
              placeholder="请输入标签名称" 
              class="modal-input"
              @keyup.enter="createTag"
              autofocus
            />
          </div>
          <div class="modal-footer">
            <button @click="showCreateModal = false" class="btn-cancel">取消</button>
            <button @click="createTag" class="btn-confirm">确认创建</button>
          </div>
        </div>
      </div>
    </Teleport>
  </LayoutWrapper>
</template>

<style lang="scss" scoped>
.tag-content {
  max-width: 900px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.create-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  padding: 0.35rem 0.75rem;
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: opacity var(--transition-default);

  &:hover {
    opacity: 0.9;
  }

  svg {
    width: 0;
    height: 0;
  }
}

.loading {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--color-text-secondary);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6rem 2rem;

  .empty-icon {
    color: var(--color-text-secondary);
    margin-bottom: 1.5rem;
    opacity: 0.5;
  }

  p {
    margin-bottom: 1.5rem;
    color: var(--color-text-secondary);
    font-size: 1.1rem;
  }
}

.tag-wrapper {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.tag-cloud-section {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: 1.5rem;
}

.section-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--color-text-primary);
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.65rem;
}

.tag-empty {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  padding: 1rem 0;
}

.tag-wrapper-item {
  display: inline-flex;
  align-items: center;
  gap: 0;
  transition: opacity 0.3s ease;

  &.tag-cloud-hover {
    opacity: 0.4;
  }
}

.delete-btn {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s ease;

  .tag:hover & {
    opacity: 1;
  }

  &:hover {
    background: #ef4444;
  }
}

.tag {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.35rem 0.8rem;
  background: var(--tag-color);
  border-radius: 6px;
  font-size: 0.85rem;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;

  &:hover {
    transform: scale(1.05);
    background: #ED8936;
    color: white;
  }

  &.active {
    background: #ED8936;
    transform: scale(1.05);
  }
}

.tag-name {
  font-weight: 500;
}

.tag-count {
  font-size: 0.8em;
  padding: 0.1rem 0.4rem;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;

  .tag.active & {
    background: rgba(255, 255, 255, 0.2);
  }
}

.welcome-section {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);

  .icon {
    font-size: 3rem;
    margin-bottom: 1rem;
  }

  h2 {
    font-size: 1.5rem;
    margin-bottom: 0.5rem;
    color: var(--color-text-primary);
  }

  p {
    color: var(--color-text-secondary);
    font-size: 0.95rem;
  }
}

.selected-tag-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;

  h3 {
    font-size: 1.4rem;
    color: var(--color-text-primary);
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding-left: 21px;
    padding-right: 21px;
  }

  .tag-badge {
    display: inline-block;
    padding: 0.3rem 0.75rem;
    background: var(--tag-color);
    color: white;
    border-radius: 6px;
    font-size: 0.85rem;
    font-weight: normal;
  }

  .article-count {
    font-size: 0.85rem;
    color: var(--color-text-secondary);
    padding: 0.25rem 0.75rem;
    background: var(--color-bg-secondary);
    border-radius: 12px;
  }
}

.articles-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.tag-article {
  background: var(--color-bg-card);
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all var(--transition-default);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.article-link {
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
}

.article-cover {
  width: 100%;
  height: 150px;
  flex-shrink: 0;
  overflow: hidden;
  background: var(--color-bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform var(--transition-default);
  }

  &:hover img {
    transform: scale(1.05);
  }

  &.placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--color-bg-secondary);
  }
}

.article-info {
  flex: 1;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.article-date {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  margin-bottom: 0.5rem;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.article-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
  transition: color var(--transition-default);

  &:hover {
    color: var(--color-accent);
  }
}

@media (max-width: 768px) {
  .tag-content {
    padding: 0 0.5rem;
  }

  .tag-cloud-section {
    padding: 1rem;
  }

  .tag {
    padding: 0.35rem 0.65rem;
    font-size: 0.8rem;
  }

  .welcome-section {
    padding: 3rem 1.5rem;
  }

  .selected-tag-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .articles-list {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 480px) {
    .articles-list {
      grid-template-columns: 1fr;
    }
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: var(--color-bg-card);
  border-radius: var(--border-radius);
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.2s ease;
}

@keyframes slideUp {
  from { 
    opacity: 0;
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--color-border);

  h3 {
    margin: 0;
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--color-text-primary);
  }
}

.close-btn {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all var(--transition-default);

  &:hover {
    color: var(--color-text-primary);
    background: var(--color-bg-secondary);
  }
}

.modal-body {
  padding: 1.5rem;
}

.modal-input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  font-size: 1rem;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  box-sizing: border-box;
  transition: border-color var(--transition-default);

  &:focus {
    outline: none;
    border-color: var(--color-accent);
  }

  &::placeholder {
    color: var(--color-text-secondary);
  }
}

.modal-footer {
  display: flex;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--color-border);
  justify-content: flex-end;
}

.btn-cancel {
  padding: 0.625rem 1.25rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all var(--transition-default);

  &:hover {
    background: var(--color-bg-secondary);
    color: var(--color-text-primary);
  }
}

.btn-confirm {
  padding: 0.625rem 1.25rem;
  border: none;
  border-radius: var(--border-radius);
  background: var(--color-accent);
  color: white;
  font-size: 0.9rem;
  cursor: pointer;
  transition: opacity var(--transition-default);

  &:hover {
    opacity: 0.9;
  }
}
</style>