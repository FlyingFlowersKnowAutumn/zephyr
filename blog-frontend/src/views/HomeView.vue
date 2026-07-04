<script setup lang="ts">
import { onMounted, onUnmounted, ref, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'
import ArticleCard from '@/components/common/ArticleCard.vue'
import Pagination from '@/components/common/Pagination.vue'
import SeoHead from '@/components/seo/SeoHead.vue'
import { RouterLink } from 'vue-router'

const emit = defineEmits(['open-search'])

const route = useRoute()
const articleStore = useArticleStore()
const authStore = useAuthStore()
const articles = ref<any[]>([])
const loading = ref(true)
const currentPage = ref(1)
const totalArticles = ref(0)
const pageSize = 10
const articlesSection = ref<HTMLElement>()

async function loadPage(page: number, scroll: boolean = true) {
  loading.value = true
  currentPage.value = page
  const result = await articleStore.fetchArticles({ page, size: pageSize, status: 'PUBLISHED' })
  articles.value = result.articles
  totalArticles.value = result.total
  loading.value = false
  if (scroll) {
    await nextTick()
    articlesSection.value?.scrollIntoView({ behavior: 'smooth' })
  }
}

function resetToFirstPage() {
  if (currentPage.value !== 1) {
    loadPage(1, false)
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  loadPage(1, false)
  window.addEventListener('home-reset', resetToFirstPage)
})

onUnmounted(() => {
  window.removeEventListener('home-reset', resetToFirstPage)
})

watch(() => route.path, (path) => {
  if (path === '/' && currentPage.value !== 1) {
    loadPage(1, false)
  }
})
</script>

<template>
  <LayoutWrapper heroTitle="微风" heroSubtitle="La brise écrit mes pensées" heroImage="/Note/wallhaven-w5lr6x_2560x1440.png" :showSidebar="true" @open-search="emit('open-search')">
    <SeoHead />

    <section ref="articlesSection" class="home-content">

      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="articles.length === 0" class="empty-state">
        <template v-if="authStore.user?.role === 'blogger'">
          <p>还没有文章，快去写第一篇吧</p>
          <RouterLink to="/write" class="write-btn">开始写作</RouterLink>
        </template>
        <p v-else>还没有文章，敬请期待</p>
      </div>

      <div v-else class="articles-list">
        <ArticleCard
          v-for="(article, index) in articles"
          :key="article.id"
          :article="article"
          :index="index"
        />
      </div>

      <Pagination
        v-if="totalArticles > pageSize"
        :current="currentPage"
        :total="totalArticles"
        :size="pageSize"
        @change="loadPage"
      />

    </section>
  </LayoutWrapper>
</template>

<style lang="scss" scoped>
.home-content {
  width: 100%;
  scroll-margin-top: 2rem;
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

  p {
    margin-bottom: 1.5rem;
    color: var(--color-text-secondary);
    font-size: 1rem;
  }
}

.write-btn {
  display: inline-block;
  padding: 0.75rem 2rem;
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  color: white;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s;

  &:hover {
    text-decoration: none;
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
  }
}

.articles-list {
  margin-bottom: 3rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.view-more-section {
  text-align: center;
  padding-top: 1rem;
}


@media (max-width: 768px) {
  .home-content {
    padding: 0 0.5rem;
  }

  .content-header h2 {
    font-size: 1.6rem;
  }
}
</style>
