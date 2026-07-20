<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { listArticles } from '@/api/article'
import type { ArticleDTO } from '@/types'
import SeoHead from '@/components/seo/SeoHead.vue'
import Header from '@/components/layout/Header.vue'

const route = useRoute()

const keyword = ref((route.query.q as string) || '')
const articles = ref<ArticleDTO[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const seoDescription = computed(() => `搜索 "${keyword.value}" 的结果`)

watch(() => route.query.q, (newQ) => {
  keyword.value = (newQ as string) || ''
  currentPage.value = 1
  search()
})

onMounted(() => {
  search()
})

async function search() {
  if (!keyword.value.trim()) {
    articles.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const result = await listArticles({
      keyword: keyword.value.trim(),
      page: currentPage.value - 1,
      size: pageSize
    })
    articles.value = result.data?.content || []
    total.value = result.data?.totalElements || 0
  } catch {
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  search()
}

function formatDate(dateString: string) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<template>
  <div class="search-container">
    <Header />
    <SeoHead :title="`搜索: ${keyword}`" :description="seoDescription" />

    <header class="search-header">
      <h1>
        搜索结果：<span class="keyword">{{ keyword }}</span>
      </h1>
      <span v-if="!loading" class="result-count">{{ total }} 篇文章</span>
    </header>

    <main class="search-content">
      <div v-if="loading" class="loading">搜索中...</div>

      <div v-else-if="articles.length === 0" class="empty-state">
        <p>未找到相关文章</p>
      </div>

      <div v-else class="articles-list">
        <article
          v-for="article in articles"
          :key="article.id"
          class="article-card"
        >
          <h2 class="article-title">
            <router-link :to="`/articles/${article.slug || article.id}`">
              {{ article.title }}
            </router-link>
          </h2>
          <p class="article-excerpt">
            {{ article.excerpt || article.content?.slice(0, 200) }}
          </p>
          <div class="article-meta">
            <span>{{ article.authorName }}</span>
            <span>{{ formatDate(article.createdAt) }}</span>
          </div>
        </article>
      </div>

      <div v-if="total > pageSize" class="pagination">
        <button
          :disabled="currentPage === 1"
          class="page-btn"
          @click="handlePageChange(currentPage - 1)"
        >
          ←
        </button>
        <span class="page-info">{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button
          :disabled="currentPage >= Math.ceil(total / pageSize)"
          class="page-btn"
          @click="handlePageChange(currentPage + 1)"
        >
          →
        </button>
      </div>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.search-container {
  min-height: 100vh;
  background: var(--color-bg-primary);
}

.search-header {
  padding: 1.5rem 2rem;
  background: rgba(22, 35, 55, 0.8);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;

  h1 {
    font-size: 1.25rem;
    font-weight: 500;
  }
}

.keyword {
  color: var(--color-accent);
}

.result-count {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

.search-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}

.empty-state {
  text-align: center;
  padding: 4rem;
  color: var(--color-text-secondary);
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.article-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
  transition: border-color 0.3s;

  &:hover {
    border-color: var(--color-accent);
  }
}

.article-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 0.5rem;

  a {
    color: var(--color-text-primary);
    text-decoration: none;
    transition: color 0.3s;

    &:hover {
      color: var(--color-accent);
    }
  }
}

.article-excerpt {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  line-height: 1.5;
  margin-bottom: 0.75rem;
}

.article-meta {
  display: flex;
  gap: 1rem;
  color: var(--color-text-secondary);
  font-size: 0.8rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text-primary);
  cursor: pointer;
  transition: all 0.3s;

  &:hover:not(:disabled) {
    border-color: var(--color-accent);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.page-info {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .search-content {
    padding: 1rem;
  }
}
</style>
