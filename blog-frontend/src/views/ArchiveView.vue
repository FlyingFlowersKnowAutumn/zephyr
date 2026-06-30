<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useArticleStore } from '@/stores/article'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'
import SeoHead from '@/components/seo/SeoHead.vue'
import { RouterLink } from 'vue-router'

const articleStore = useArticleStore()
const articles = ref<any[]>([])
const loading = ref(true)
const selectedYear = ref('')

const groupedArticles = computed(() => {
  const groups: Record<string, any[]> = {}

  articles.value.forEach(article => {
    const date = new Date(article.publishedAt || article.createdAt)
    const year = `${date.getFullYear()}`

    if (!groups[year]) {
      groups[year] = []
    }
    groups[year].push(article)
  })

  return Object.entries(groups)
    .sort(([a], [b]) => b.localeCompare(a))
    .map(([key, value]) => ({
      year: key,
      articles: value.sort((a, b) =>
        new Date(b.publishedAt || b.createdAt).getTime() -
        new Date(a.publishedAt || a.createdAt).getTime()
      )
    }))
})

const filteredArticles = computed(() => {
  if (!selectedYear.value) return groupedArticles.value
  return groupedArticles.value.filter(g => g.year === selectedYear.value)       
})

function formatDate(dateString?: string) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

function selectYear(year: string) {
  selectedYear.value = selectedYear.value === year ? '' : year
}

onMounted(async () => {
  const result = await articleStore.fetchArticles({ page: 1, size: 100, status: 'PUBLISHED' })
  articles.value = result.articles
  loading.value = false
})
</script>

<template>
  <LayoutWrapper heroTitle="归档" :showSidebar="true" heroSize="small" heroImage="/Note/wallhaven-w5lr6x_2560x1440.png">       
    <SeoHead />

    <section class="archive-content">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="articles.length === 0" class="empty-state">
        <p>暂无文章</p>
      </div>

      <div v-else class="archive-container">
        <div class="year-sidebar">
          <div
            @click="selectYear('')"
            class="year-item"
            :class="{ active: !selectedYear }"
          >
            <span class="year-dot"></span>
            <span class="year-label">全部文章</span>
            <span class="year-count">{{ articles.length }}</span>
          </div>
          <div
            v-for="group in groupedArticles"
            :key="group.year"
            @click="selectYear(group.year)"
            class="year-item"
            :class="{ active: selectedYear === group.year }"
          >
            <span class="year-dot"></span>
            <span class="year-label">{{ group.year }}</span>
            <span class="year-count">{{ group.articles.length }}</span>
          </div>
        </div>

        <div class="articles-main">
          <div v-for="group in filteredArticles" :key="group.year" class="year-section">
            <div class="year-header">
              <span class="year-title">{{ group.year }}</span>
              <span class="year-article-count">{{ group.articles.length }} 篇文章</span>
            </div>
            <div class="articles-list">
              <article
                v-for="article in group.articles"
                :key="article.id"
                class="archive-article"
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
                    <div class="article-header">
                      <span v-if="article.tags && article.tags.length" class="article-tag">{{ article.tags[0] }}</span>
                      <span class="article-date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                    </div>
                    <h3 class="article-title">{{ article.title }}</h3>
                    <p class="article-excerpt">
                      {{ article.excerpt || (article.content ? article.content.slice(0, 80) + '...' : '') }}
                    </p>
                  </div>
                </RouterLink>
              </article>
            </div>
          </div>
        </div>
      </div>
    </section>
  </LayoutWrapper>
</template>

<style lang="scss" scoped>
.archive-content {
  max-width: 900px;
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
  text-align: center;
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

.write-btn {
  display: inline-block;
  padding: 0.75rem 2rem;
  background: var(--color-accent);
  color: white;
  border-radius: var(--border-radius);
  font-size: 0.95rem;

  &:hover {
    text-decoration: none;
    opacity: 0.9;
  }
}

.archive-container {
  display: flex;
  gap: 3rem;
}

.year-sidebar {
  width: 140px;
  flex-shrink: 0;
  padding-top: 1rem;
  position: sticky;
  top: 2rem;
  height: fit-content;
}

.year-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
  cursor: pointer;
  transition: color var(--transition-default);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 3px;
    top: 1.2rem;
    bottom: -0.5rem;
    width: 2px;
    background: var(--color-border);
  }

  &:last-child::before {
    display: none;
  }

  &:hover {
    color: var(--color-accent);
  }

  &.active {
    color: var(--color-accent);

    .year-dot {
      background: var(--color-accent);
      box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.2);
    }

    &::before {
      background: var(--color-accent);
    }
  }
}

.year-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-border);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  transition: all var(--transition-default);
}

.year-label {
  flex: 1;
  font-size: 0.9rem;
  color: var(--color-text-secondary);

  .year-item:hover &,
  .year-item.active & {
    color: inherit;
  }
}

.year-count {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  font-weight: 500;

  .year-item:hover &,
  .year-item.active & {
    color: inherit;
  }
}

.articles-main {
  flex: 1;
  min-width: 0;
}

.year-section {
  margin-bottom: 3rem;
}

.year-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border);
}

.year-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.year-article-count {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  padding: 0.25rem 0.75rem;
  background: var(--color-bg-secondary);
  border-radius: 12px;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.archive-article {
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
  text-decoration: none;
  color: inherit;
}

.article-cover {
  width: 180px;
  height: 120px;
  flex-shrink: 0;
  overflow: hidden;

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
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.article-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  font-size: 0.8rem;
}

.article-tag {
  padding: 0.2rem 0.5rem;
  background: rgba(99, 102, 241, 0.1);
  color: var(--color-accent);
  border-radius: 4px;
  font-weight: 500;
  font-size: 0.75rem;
}

.article-date {
  color: var(--color-text-secondary);
}

.article-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 0.5rem;
  line-height: 1.5;
  transition: color var(--transition-default);

  &:hover {
    color: var(--color-accent);
  }
}

.article-excerpt {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

@media (max-width: 768px) {
  .archive-content {
    padding: 0 0.5rem;
  }

  .archive-container {
    flex-direction: column;
    gap: 1.5rem;
  }

  .year-sidebar {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    padding-top: 0;
    position: static;
  }

  .year-item {
    padding: 0.4rem 0.8rem;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: 20px;

    &::before {
      display: none;
    }
  }

  .year-dot {
    display: none;
  }

  .year-section {
    margin-bottom: 2rem;
  }

  .article-link {
    flex-direction: column;
  }

  .article-cover {
    width: 100%;
    height: 150px;
  }

  .article-info {
    padding: 1rem;
  }

  .article-title {
    font-size: 1.1rem;
  }
}
</style>
