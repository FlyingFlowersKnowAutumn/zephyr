<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'
import ArticleCard from '@/components/common/ArticleCard.vue'
import SeoHead from '@/components/seo/SeoHead.vue'

const emit = defineEmits(['open-search'])

const route = useRoute()
const articleStore = useArticleStore()

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const articles = computed(() => articleStore.articles)
const loading = computed(() => articleStore.loading)

onMounted(() => {
  fetchArticles()
})

async function fetchArticles() {
  const params = {
    page: currentPage.value,
    size: pageSize.value,
    keyword: (route.query.q as string) || undefined,
    category: (route.query.category as string) || undefined
  }
  const result = await articleStore.fetchArticles(params)
  total.value = result.total
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <LayoutWrapper 
    :showHero="true"
    heroTitle="所有文章"
    heroSubtitle="浏览所有技术文章"
    @open-search="emit('open-search')"
  >
    <SeoHead title="文章列表" description="浏览所有技术文章" />

    <section class="articles-section">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="articles.length === 0" class="empty-state">
        <p>暂无文章</p>
      </div>

      <div v-else class="articles-list">
        <ArticleCard 
          v-for="article in articles" 
          :key="article.id" 
          :article="article"
        />
      </div>

      <div v-if="total > pageSize" class="pagination">
        <button 
          :disabled="currentPage === 1"
          class="page-btn"
          @click="handlePageChange(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button 
          :disabled="currentPage >= Math.ceil(total / pageSize)"
          class="page-btn"
          @click="handlePageChange(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </section>
  </LayoutWrapper>
</template>

<style lang="scss" scoped>
.articles-section {
  padding: 0;
}

.loading {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--color-text-secondary);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
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
  padding: 4rem 2rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);

  p {
    color: var(--color-text-secondary);
  }
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
}

.page-btn {
  padding: 0.625rem 1.5rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  color: var(--color-text-primary);
  font-size: 0.9rem;
  transition: all var(--transition-default);

  &:hover:not(:disabled) {
    border-color: var(--color-accent);
    color: var(--color-accent);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.page-info {
  color: var(--color-text-secondary);
  font-size: 0.95rem;
}
</style>
