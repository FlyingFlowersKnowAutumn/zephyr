<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'
import SeoHead from '@/components/seo/SeoHead.vue'
import ArticleCard from '@/components/common/ArticleCard.vue'
import { useRoute } from 'vue-router'
import type { CategoryDTO } from '@/types'

const showCreateModal = ref(false)
const newCategoryName = ref('')
const isBlogger = computed(() => authStore.user?.role === 'blogger')

const route = useRoute()
const articleStore = useArticleStore()
const authStore = useAuthStore()
const categories = ref<CategoryDTO[]>([])
const articles = ref<any[]>([])
const selectedCategory = ref('')
const loading = ref(true)

const filteredArticles = computed(() => {
  if (!selectedCategory.value) return articles.value
  return articles.value.filter(article => article.categoryName === selectedCategory.value)
})

async function createCategory() {
  if (!newCategoryName.value.trim()) return
  
  await articleStore.createCategory(newCategoryName.value.trim())
  categories.value = await articleStore.fetchCategories()
  newCategoryName.value = ''
  showCreateModal.value = false
}

async function deleteCategory(id: string) {
  if (!confirm('确定要删除这个分类吗？')) return
  
  await articleStore.deleteCategory(id)
  categories.value = await articleStore.fetchCategories()
  if (selectedCategory.value === categories.value.find(c => c.id === id)?.name) {
    selectedCategory.value = ''
  }
}

onMounted(async () => {
  try {
    const [catResult, articleResult] = await Promise.all([
      articleStore.fetchCategories(),
      articleStore.fetchArticles({ page: 1, size: 100, status: 'PUBLISHED' })
    ])
    const catData = catResult as any
    categories.value = typeof catData[0] === 'string'
      ? catData.map((name: string) => ({ id: name, name, description: '', articleCount: 0, createdAt: '' }))
      : catData
    articles.value = articleResult.articles
  
    const categoryFromUrl = route.query.category as string
    if (categoryFromUrl && categories.value.some(c => c.name === categoryFromUrl)) {
      selectedCategory.value = categoryFromUrl
    }
  } catch (e) {
    console.error('Failed to load categories:', e)
  }
  
  loading.value = false
})
</script>

<template>
  <LayoutWrapper heroTitle="分类" :showSidebar="true" heroSize="small" heroImage="/Note/wallhaven-w5lr6x_2560x1440.png">
    <SeoHead />
    
    <section class="category-content">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else class="category-container">
        <div class="category-sidebar">
          <div class="sidebar-header">
            <span class="sidebar-title">分类</span>
            <button v-if="isBlogger" @click="showCreateModal = true" class="create-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 4v16m8-8H4"/>
              </svg>
              <span>新建</span>
            </button>
          </div>
          <div class="category-item-wrapper">
            <div
              @click="selectedCategory = ''"
              class="category-item"
              :class="{ active: !selectedCategory }"
            >
              <span class="category-dot"></span>
              <span class="category-name">全部文章</span>
              <span class="category-right">
                <span class="category-count">{{ articles.length }}</span>
              </span>
            </div>
          </div>
          <div v-if="categories.length === 0" class="category-empty">
            还没有分类
          </div>
          <div
            v-for="category in categories"
            :key="category.id"
            class="category-item-wrapper"
          >
            <div
              @click="selectedCategory = category.name"
              class="category-item"
              :class="{ active: selectedCategory === category.name }"
            >
              <span class="category-dot"></span>
              <span class="category-name">{{ category.name }}</span>
              <span class="category-right">
                <span class="category-count">{{ category.articleCount || 0 }}</span>
                <button
                  v-if="isBlogger"
                  @click.stop="deleteCategory(category.id)"
                  class="delete-btn"
                  title="删除分类"
                >
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
              </span>
            </div>
          </div>
        </div>

        <div class="articles-main">
          <div v-if="filteredArticles.length === 0" class="empty-state">
            <p>{{ selectedCategory ? '该分类下还没有文章' : '还没有文章' }}</p>
          </div>

          <div v-else class="articles-list">
            <div v-if="selectedCategory" class="selected-header">
              <h2>{{ selectedCategory }}</h2>
              <span class="article-count">{{ filteredArticles.length }} 篇文章</span>
            </div>
            <ArticleCard 
              v-for="(article, index) in filteredArticles" 
              :key="article.id" 
              :article="article"
              :index="index"
            />
          </div>
        </div>
      </div>
    </section>

    <Teleport to="body">
      <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3>新建分类</h3>
            <button @click="showCreateModal = false" class="close-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <input 
              v-model="newCategoryName" 
              type="text" 
              placeholder="请输入分类名称" 
              class="modal-input"
              @keyup.enter="createCategory"
              autofocus
            />
          </div>
          <div class="modal-footer">
            <button @click="showCreateModal = false" class="btn-cancel">取消</button>
            <button @click="createCategory" class="btn-confirm">确认创建</button>
          </div>
        </div>
      </div>
    </Teleport>
  </LayoutWrapper>
</template>

<style lang="scss" scoped>
.category-content {
  max-width: 900px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary);
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

  span {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  svg {
    width: 0;
    height: 0;
  }
}

.category-item-wrapper {
  // container only, no extra styles needed
}

.category-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.delete-btn {
  width: 0;
  height: 0;
  overflow: hidden;
  opacity: 0;
  padding: 0;
  margin: 0;
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: 4px;
  transition: width 0.2s ease, height 0.2s ease, opacity 0.2s ease, padding 0.2s ease, margin-left 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .category-item-wrapper:hover & {
    width: 20px;
    height: 20px;
    opacity: 1;
    padding: 0.2rem;
    margin-left: 0.25rem;
  }

  &:hover {
    color: #ef4444;
    background: rgba(239, 68, 68, 0.1);
  }
}

.category-empty {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  padding: 0.5rem 0;
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

.category-container {
  display: flex;
  gap: 3rem;
}

.category-sidebar {
  width: 160px;
  flex-shrink: 0;
  padding-top: 1rem;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
  cursor: pointer;
  transition: color var(--transition-default);

  &:hover {
    color: var(--color-accent);
  }

  &.active {
    color: var(--color-accent);
    
    .category-dot {
      background: var(--color-accent);
    }
  }
}

.category-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-border);
  flex-shrink: 0;
}

.category-name {
  flex: 1;
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  
  .category-item:hover &,
  .category-item.active & {
    color: inherit;
  }
}

.category-count {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  font-weight: 500;
  
  .category-item:hover &,
  .category-item.active & {
    color: inherit;
  }
}

.articles-main {
  flex: 1;
  min-width: 0;
}

.selected-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border);

  h2 {
    font-size: 1.4rem;
    font-weight: 600;
    color: var(--color-text-primary);
    margin: 0;
  }
}

.article-count {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  padding: 0.25rem 0.75rem;
  background: var(--color-bg-secondary);
  border-radius: 12px;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding-top: 10px;
  padding-bottom: 10px;
}

@media (max-width: 768px) {
  .category-content {
    padding: 0 0.5rem;
  }

  .category-container {
    flex-direction: column;
    gap: 1.5rem;
  }

  .category-sidebar {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    padding-top: 0;
  }

  .category-item {
    padding: 0.4rem 0.8rem;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: 20px;
  }

  .category-dot {
    display: none;
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