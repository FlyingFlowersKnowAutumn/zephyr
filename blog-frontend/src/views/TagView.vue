<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useArticleStore } from '@/stores/article'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'
import SeoHead from '@/components/seo/SeoHead.vue'
import ArticleCard from '@/components/common/ArticleCard.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const articleStore = useArticleStore()
const tags = ref<string[]>([])
const articles = ref<any[]>([])
const selectedTag = ref('')
const loading = ref(true)

const showCreateModal = ref(false)
const newTagName = ref('')

async function createTag() {
  if (!newTagName.value.trim()) return
  
  await articleStore.createTag(newTagName.value.trim())
  tags.value = await articleStore.fetchTags()
  newTagName.value = ''
  showCreateModal.value = false
}

const filteredArticles = computed(() => {
  if (!selectedTag.value) return []
  return articles.value.filter(article => article.tags?.includes(selectedTag.value))
})

const tagArticleCount = computed(() => {
  const counts: Record<string, number> = {}
  articles.value.forEach(article => {
    article.tags?.forEach((tag: string) => {
      counts[tag] = (counts[tag] || 0) + 1
    })
  })
  return counts
})

const sortedTags = computed(() => {
  return [...tags.value].sort((a, b) => (tagArticleCount.value[b] || 0) - (tagArticleCount.value[a] || 0))
})

onMounted(async () => {
  const [tagResult, articleResult] = await Promise.all([
    articleStore.fetchTags(),
    articleStore.fetchArticles({ page: 1, size: 100, status: 'PUBLISHED' })
  ])
  tags.value = tagResult
  articles.value = articleResult.articles
  
  const tagFromUrl = route.query.tag as string
  if (tagFromUrl && tags.value.includes(tagFromUrl)) {
    selectedTag.value = tagFromUrl
  }
  
  loading.value = false
})
</script>

<template>
  <LayoutWrapper heroTitle="标签" :showSidebar="true" heroSize="small">
    <SeoHead />
    
    <section class="tag-content">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="tags.length === 0" class="empty-state">
        <p>还没有标签</p>
        <button @click="showCreateModal = true" class="create-btn">创建标签</button>
      </div>

      <div v-else class="tag-wrapper">
        <div class="tag-cloud-section">
          <div class="section-header">
            <h3 class="section-title">所有标签</h3>
            <button @click="showCreateModal = true" class="create-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 4v16m8-8H4"/>
              </svg>
              <span>新建</span>
            </button>
          </div>
          <div class="tag-cloud">
            <button 
              v-for="tag in sortedTags" 
              :key="tag"
              @click="selectedTag = selectedTag === tag ? '' : tag"
              class="tag"
              :class="{ active: selectedTag === tag }"
            >
              <span class="tag-name">{{ tag }}</span>
              <span class="tag-count">{{ tagArticleCount[tag] || 0 }}</span>
            </button>
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

        <div v-else class="articles-list">
          <div class="selected-tag-header">
            <h2>
              <span class="tag-badge">{{ selectedTag }}</span>
              相关文章
            </h2>
            <span class="article-count">{{ filteredArticles.length }} 篇文章</span>
          </div>
          <ArticleCard 
            v-for="article in filteredArticles" 
            :key="article.id" 
            :article="article"
          />
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

  .create-btn {
    display: inline-block;
    padding: 0.75rem 2rem;
    background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
    }
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
  gap: 0.6rem;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.8rem;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-default);

  &:hover {
    border-color: var(--color-accent);
    color: var(--color-accent);
  }

  &.active {
    background: var(--color-accent);
    border-color: var(--color-accent);
    color: white;
  }
}

.tag-name {
  font-weight: 500;
}

.tag-count {
  font-size: 0.7rem;
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

  h2 {
    font-size: 1.4rem;
    color: var(--color-text-primary);
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .tag-badge {
    display: inline-block;
    padding: 0.3rem 0.75rem;
    background: var(--color-accent);
    color: white;
    border-radius: 16px;
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
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
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