<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import { getArticle } from '@/api/article'
import SeoHead from '@/components/seo/SeoHead.vue'
import LikeButton from '@/components/interaction/LikeButton.vue'
import CommentSection from '@/components/interaction/CommentSection.vue'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'

const emit = defineEmits(['open-search'])

const router = useRouter()
const route = useRoute()
const articleStore = useArticleStore()
const authStore = useAuthStore()

const slugParam = computed(() => route.params.slug as string)
const article = computed(() => articleStore.currentArticle)
const articleId = computed(() => article.value?.id || slugParam.value)
const loading = computed(() => articleStore.loading)
const isUuid = computed(() => /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(slugParam.value))

onMounted(async () => {
  if (!slugParam.value) return

  if (isUuid.value) {
    try {
      const result = await getArticle(slugParam.value)
      const article = result.data
      if (article?.slug && article.slug !== slugParam.value) {
        router.replace({ params: { slug: article.slug } })
        return
      }
    } catch {
      // fallback to store
    }
  }
  articleStore.fetchArticle(slugParam.value)
})

function goBack() {
  router.back()
}

function handleEdit() {
  router.push(`/articles/${slugParam.value}/edit`)
}

function formatDate(dateString: string) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <LayoutWrapper :showSidebar="false" :showHero="false" @open-search="emit('open-search')">
    <SeoHead
      v-if="article"
      :title="article.title"
      :description="article.excerpt || article.content?.slice(0, 150)"
      :image="article.coverImage"
      type="article"
    />
    <SeoHead v-else title="文章详情" />

    <div class="article-detail-container">
      <header class="detail-header">
        <button class="back-btn" @click="goBack">← 返回</button>
        <button
          v-if="article?.authorId && authStore.user?.role === 'blogger'"
          class="edit-btn"
          @click="handleEdit"
        >
          编辑文章
        </button>
      </header>

      <main class="detail-content" v-if="!loading">
        <article v-if="article" class="article-detail">
          <div class="article-cover" v-if="article.coverImage">
            <img :src="article.coverImage" :alt="article.title" />
          </div>

          <div class="article-header">
            <h1 class="article-title">{{ article.title }}</h1>
            
            <div class="article-meta">
              <span class="author-info">
                <span class="author-name">{{ article.authorName }}</span>
                <span class="publish-date">{{ formatDate(article.createdAt) }}</span>
              </span>
              
              <div class="article-status">
                <span 
                  :class="{
                    'status-draft': article.status === 'DRAFT',
                    'status-published': article.status === 'PUBLISHED',
                    'status-archived': article.status === 'ARCHIVED'
                  }"
                  class="status-badge"
                >
                  {{ article.status === 'DRAFT' ? '草稿' : article.status === 'PUBLISHED' ? '已发布' : '已归档' }}
                </span>
              </div>
            </div>
          </div>

          <div class="article-tags">
            <span 
              v-for="tag in article.tags" 
              :key="tag" 
              class="tag-item"
            >
              {{ tag }}
            </span>
          </div>

          <div class="article-content">
            <p>{{ article.content }}</p>
          </div>

          <div class="article-footer">
            <div class="stats">
              <span class="stat-item">👁 {{ article.viewCount }}</span>
              <LikeButton :article-id="articleId" />
              <span class="stat-item">💬 {{ article.commentCount }}</span>
            </div>
          </div>

          <CommentSection :article-id="articleId" />
        </article>

        <div v-else class="not-found">
          <p>文章不存在或已被删除</p>
          <button class="btn-primary" @click="goBack">返回列表</button>
        </div>
      </main>

      <div v-if="loading" class="loading">加载中...</div>
    </div>
  </LayoutWrapper>
</template>

<style lang="scss" scoped>
.article-detail-container {
  min-height: 100vh;
  background: var(--color-bg-primary);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(22, 35, 55, 0.8);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 1rem;
  transition: color 0.3s;

  &:hover {
    color: var(--color-accent);
  }
}

.edit-btn {
  padding: 0.5rem 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text-primary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
    border-color: var(--color-accent);
  }
}

.detail-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.article-detail {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  overflow: hidden;
}

.article-cover {
  width: 100%;
  max-height: 400px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.article-header {
  padding: 2rem;
}

.article-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 1.5rem 0;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--color-text-secondary);
}

.author-info {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
}

.author-name {
  font-weight: 500;
}

.status-badge {
  padding: 0.375rem 0.875rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-draft {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
}

.status-published {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.status-archived {
  background: rgba(156, 163, 175, 0.15);
  color: #9ca3af;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding: 0 2rem;
  margin-bottom: 1.5rem;
}

.tag-item {
  padding: 0.375rem 0.875rem;
  background: rgba(0, 212, 255, 0.15);
  border: 1px solid var(--color-accent);
  border-radius: 20px;
  font-size: 0.8rem;
  color: var(--color-accent);
}

.article-content {
  padding: 0 2rem 2rem;
  line-height: 1.8;
  color: var(--color-text-secondary);
  font-size: 1.1rem;

  p {
    margin: 0 0 1.5rem 0;
  }

  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    font-weight: 600;
    margin: 1.5rem 0 1rem 0;
    color: var(--color-text-primary);
  }

  :deep(h1) { font-size: 1.5rem; }
  :deep(h2) { font-size: 1.375rem; }
  :deep(h3) { font-size: 1.25rem; }

  :deep(code) {
    background: rgba(255, 255, 255, 0.1);
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-family: monospace;
    font-size: 0.9em;
  }

  :deep(pre) {
    background: rgba(0, 0, 0, 0.3);
    padding: 1rem;
    border-radius: 8px;
    overflow-x: auto;
    margin: 1rem 0;
  }

  :deep(blockquote) {
    border-left: 4px solid var(--color-accent);
    padding-left: 1rem;
    margin: 1rem 0;
    color: var(--color-text-secondary);
    font-style: italic;
  }
}

.article-footer {
  padding: 1.5rem 2rem;
  border-top: 1px solid var(--color-border);
}

.stats {
  display: flex;
  gap: 2rem;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.like-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: color 0.3s;

  &.liked {
    color: #ef4444;
  }

  &:hover {
    color: #ef4444;
  }
}

.not-found {
  text-align: center;
  padding: 4rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;

  p {
    margin-bottom: 1rem;
    color: var(--color-text-secondary);
  }
}

.btn-primary {
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

.loading {
  text-align: center;
  padding: 4rem;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .article-title {
    font-size: 1.5rem;
  }

  .article-meta {
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }

  .detail-content {
    padding: 1rem;
  }

  .article-header, .article-content, .article-footer {
    padding: 1.5rem;
  }
}
</style>