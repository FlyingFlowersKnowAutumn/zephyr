<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'
import ArticleCard from '@/components/common/ArticleCard.vue'
import SeoHead from '@/components/seo/SeoHead.vue'
import { RouterLink } from 'vue-router'

const emit = defineEmits(['open-search'])

const articleStore = useArticleStore()
const authStore = useAuthStore()
const articles = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  const result = await articleStore.fetchArticles({ page: 1, size: 8, status: 'PUBLISHED' })
  articles.value = result.articles
  loading.value = false
})
</script>

<template>
  <LayoutWrapper heroTitle="微风" heroSubtitle="La brise écrit mes pensées" :showSidebar="true" @open-search="emit('open-search')">
    <SeoHead />

    <section class="home-content">

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
          v-for="article in articles" 
          :key="article.id" 
          :article="article"
        />
      </div>

      <div v-if="articles.length > 0" class="view-more-section">
        <RouterLink to="/articles" class="view-more-btn">查看全部文章</RouterLink>
      </div>
    </section>
  </LayoutWrapper>
</template>

<style lang="scss" scoped>
.home-content {
  max-width: 700px;
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
}

.view-more-section {
  text-align: center;
  padding-top: 1rem;
}

.view-more-btn {
  display: inline-block;
  padding: 0.875rem 2.5rem;
  border: 1px solid var(--color-accent);
  color: var(--color-accent);
  border-radius: var(--border-radius);
  font-size: 0.95rem;
  font-weight: 500;
  transition: all var(--transition-default);

  &:hover {
    background: var(--color-accent);
    color: white;
    text-decoration: none;
  }
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
