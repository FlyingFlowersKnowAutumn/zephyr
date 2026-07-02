<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import { getArticle, getArticleBySlug } from '@/api/article'
import { renderMarkdown } from '@/utils/markdown'
import SeoHead from '@/components/seo/SeoHead.vue'

import CopyrightModal from '@/components/common/CopyrightModal.vue'
import ActionPanel from '@/components/interaction/ActionPanel.vue'
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
const renderedContent = computed(() => article.value ? renderMarkdown(article.value.content) : '')
const showCopyrightModal = ref(false)

onMounted(async () => {
  if (!slugParam.value) return
  
  try {
    const result = await getArticleBySlug(slugParam.value)
    if (result.code === 0 && result.data) {
      articleStore.currentArticle = result.data
      return
    }
    
    const uuidResult = await getArticle(slugParam.value)
    if (uuidResult.code === 0 && uuidResult.data) {
      articleStore.currentArticle = uuidResult.data
      if (uuidResult.data.slug && uuidResult.data.slug !== slugParam.value) {
        router.replace({ params: { slug: uuidResult.data.slug } })
      }
    }
  } catch {
    articleStore.fetchArticle(slugParam.value)
  }
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
    month: '2-digit',
    day: '2-digit'
  })
}

function formatDateTime(dateString: string) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <div class="article-detail-page">
    <header v-if="article && article.coverImage" class="article-hero">
      <div class="hero-bg" :style="{ backgroundImage: `url(${article.coverImage})` }"></div>
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta-row">
          <span class="meta-item">发表于 {{ formatDateTime(article.createdAt) }}</span>
          <span v-if="article.updatedAt !== article.createdAt" class="meta-divider">|</span>
          <span v-if="article.updatedAt !== article.createdAt" class="meta-item">更新于 {{ formatDateTime(article.updatedAt) }}</span>
          <span class="meta-divider">|</span>
          <RouterLink v-if="article.categoryName" :to="{ path: '/categories', query: { category: article.categoryName } }" class="meta-item category-link">
            {{ article.categoryName }}
          </RouterLink>
          <span v-else class="meta-item">未分类</span>
        </div>
        <div class="article-meta-row">
          <span class="meta-item">浏览量: {{ article.viewCount }}</span>
        </div>
      </div>
    </header>

    <LayoutWrapper 
        :showSidebar="true" 
        :showHero="false"
        :articleContent="article?.content || ''"
        :showToc="true"
        :showRecentUpdates="true"
        :showRightPanel="true"
        :isArticlePage="true"
        @open-search="emit('open-search')"
      >
      <SeoHead
        v-if="article"
        :title="article.title"
        :description="article.excerpt || article.content?.slice(0, 150)"
        :image="article.coverImage"
        type="article"
      />

      <div v-if="article && !article.coverImage" class="article-header-no-bg">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="meta-item">发表于 {{ formatDateTime(article.createdAt) }}</span>
          <span v-if="article.updatedAt !== article.createdAt" class="meta-item">更新于 {{ formatDateTime(article.updatedAt) }}</span>
          <RouterLink v-if="article.categoryName" :to="{ path: '/categories', query: { category: article.categoryName } }" class="meta-item category-link">
            {{ article.categoryName }}
          </RouterLink>
          <span v-else class="meta-item">未分类</span>
          <span class="meta-item">浏览量: {{ article.viewCount }}</span>
        </div>
      </div>

      <div class="article-body">
        <article v-if="article && !loading" class="article-content">
          <div v-if="article.excerpt" class="article-excerpt">
            "{{ article.excerpt }}"
          </div>

          <div class="markdown-body" v-html="renderedContent"></div>

          <div v-if="article.tags.length" class="article-tags">
            <RouterLink v-for="tag in article.tags" :key="tag" :to="{ path: '/tags', query: { tag } }" class="tag-btn">{{ tag }}</RouterLink>
          </div>

          <div class="copyright-area">
            <div class="copyright-header">
              <span class="copyright-icon">📝</span>
              <span class="copyright-label">作者：zephyr</span>
            </div>
            <ul class="copyright-list">
              <li>
                <span class="list-number">1.</span>
                <span class="list-text">本网站部分内容可能来源于网络，仅供大家学习与参考，如有侵权，请联系站长进行删除处理。</span>
              </li>
              <li>
                <span class="list-number">2.</span>
                <span class="list-text">本网站一切内容不代表本站立场，并不代表本站赞同其观点和对其真实性负责。</span>
              </li>
              <li>
                <span class="list-number">3.</span>
                <span class="list-text">版权&许可请参阅</span>
                <button class="copyright-link" @click="showCopyrightModal = true">版权声明</button>
              </li>
            </ul>
          </div>
        </article>

        <div v-else-if="loading" class="loading">加载中...</div>

        <div v-else class="not-found">
          <p>文章不存在或已被删除</p>
          <button class="btn-primary" @click="goBack">返回列表</button>
        </div>
      </div>

      <CopyrightModal :visible="showCopyrightModal" @close="showCopyrightModal = false" />
      
      <template #right-panel>
        <ActionPanel 
          :article-id="articleId" 
          :article-title="article?.title"
          :article-content="article?.content"
        />
      </template>
    </LayoutWrapper>
  </div>
</template>

<style lang="scss" scoped>
.article-detail-page {
  min-height: 100vh;
  background: var(--color-bg-primary);
}

.article-hero {
  position: relative;
  width: 100vw;
  height: 33.33vh;
  min-height: 280px;
  max-height: 400px;
  overflow: hidden;
  margin-left: calc(-50vw + 50%);

  .hero-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-size: cover;
    background-position: center;
    transition: transform 0.6s ease;
  }

  &:hover .hero-bg {
    transform: scale(1.05);
  }

  .hero-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.8));
  }

  .hero-content {
    position: relative;
    z-index: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    padding: 2rem 4rem;
    max-width: 1200px;
    margin: 0 auto;
    box-sizing: border-box;
  }

  .article-title {
    font-size: 2.25rem;
    font-weight: 700;
    color: white;
    margin: 0 0 1rem 0;
    line-height: 1.4;
    text-shadow: 0 2px 20px rgba(0, 0, 0, 0.4);
  }

  .article-meta-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.75rem;
    color: rgba(255, 255, 255, 0.85);
    font-size: 0.875rem;
    margin-top: 0.5rem;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 0.375rem;

      .meta-icon {
        font-size: 0.85rem;
      }
    }
  }

  .meta-divider {
    color: rgba(255, 255, 255, 0.6);
  }
}

.article-header-no-bg {
  margin-bottom: 1rem;

  .article-title {
    font-size: 2.25rem;
    font-weight: 700;
    color: var(--color-text-primary);
    margin: 0 0 1rem 0;
    line-height: 1.4;
  }

  .article-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 1.5rem;
    color: var(--color-text-secondary);
    font-size: 0.875rem;
  }
}

.category-link {
  color: inherit;
  text-decoration: none;

  &:hover {
    color: var(--color-accent);
    text-decoration: underline;
  }
}

.article-body {
  width: 100%;
}

.article-content {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.article-excerpt {
  background: rgba(200, 230, 255, 0.5);
  border-left: 4px solid var(--color-accent);
  padding-left: 15px;
  padding-right: 15px;
  padding-top: 10px;
  padding-bottom: 10px;
  margin-left: 32px;
  margin-right: 32px;
  margin-top: 15px;
  margin-bottom: 15px;
  color: var(--color-text-secondary);
  font-size: 1rem;
  border-radius: 0 8px 8px 0;
  line-height: 1.6;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 0;
  margin-left: 35px;
  margin-right: 35px;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
}

.tag-btn {
  padding: 0.4rem 1rem;
  background: transparent;
  border: 1px solid var(--color-accent);
  border-radius: 20px;
  font-size: 0.85rem;
  color: var(--color-accent);
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;

  &:hover {
    background: rgba(0, 212, 255, 0.15);
    transform: translateY(-2px);
  }
}

.markdown-body {
  padding: 0 2rem;
  margin-top: 15px;
  margin-bottom: 15px;
  line-height: 1.8;
  color: var(--color-text-secondary);
  font-size: 1.1rem;

  h1, h2, h3, h4, h5, h6 {
    font-weight: 600;
    margin: 2rem 0 1rem 0;
    color: var(--color-text-primary);
    line-height: 1.3;
    scroll-margin-top: 120px;

    &:first-child { margin-top: 0; }
  }

  h1 { font-size: 1.75rem; border-bottom: 1px solid var(--color-border); padding-bottom: 0.5rem; }
  h2 { font-size: 1.5rem; border-bottom: 1px solid var(--color-border); padding-bottom: 0.4rem; }
  h3 { font-size: 1.25rem; }
  h4 { font-size: 1.1rem; }

  p {
    margin: 0 0 1rem 0;
  }

  a {
    color: var(--color-accent);
    text-decoration: none;

    &:hover { text-decoration: underline; }
  }

  ul, ol {
    padding-left: 1.5rem;
    margin: 0.5rem 0 1rem 0;
  }

  li {
    margin: 0.25rem 0;
  }

  code {
    background: rgba(255, 255, 255, 0.08);
    padding: 0.2rem 0.4rem;
    border-radius: 3px;
    font-family: 'Fira Code', 'Consolas', monospace;
    font-size: 0.875em;
    color: #e83e8c;
  }

  pre {
    background: #0d1117;
    padding: 1rem 1.25rem;
    border-radius: 8px;
    overflow-x: auto;
    margin: 1rem 0;

    code {
      background: none;
      padding: 0;
      color: #c9d1d9;
      font-size: 0.875rem;
      line-height: 1.6;
    }
  }

  blockquote {
    border-left: 4px solid var(--color-accent);
    padding: 0.5rem 0 0.5rem 1rem;
    margin: 1rem 0;
    color: var(--color-text-secondary);
    background: rgba(0, 212, 255, 0.05);
    border-radius: 0 6px 6px 0;

    p:last-child { margin-bottom: 0; }
  }

  table {
    width: 100%;
    border-collapse: collapse;
    margin: 1rem 0;
    font-size: 0.95rem;
  }

  th, td {
    border: 1px solid var(--color-border);
    padding: 0.625rem 0.875rem;
    text-align: left;
  }

  th {
    background: rgba(255, 255, 255, 0.05);
    font-weight: 600;
  }

  tr:nth-child(even) {
    background: rgba(255, 255, 255, 0.02);
  }

  hr {
    border: none;
    border-top: 1px solid var(--color-border);
    margin: 2rem 0;
  }

  img {
    max-width: 100%;
    border-radius: 8px;
    margin: 1rem 0;
  }

  strong {
    font-weight: 600;
    color: var(--color-text-primary);
  }
}

.copyright-area {
  background: rgba(248, 250, 252, 0.8);
  border-left: 4px solid var(--color-accent);
  padding: 1.25rem 1.5rem;
  margin: 15px 2rem;
  border-radius: 0 8px 8px 0;

  :deep(.dark) & {
    background: rgba(0, 212, 255, 0.05);
  }
}

.copyright-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;

  .copyright-icon {
    font-size: 1rem;
  }

  .copyright-label {
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--color-text-primary);
  }
}

.copyright-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.copyright-list li {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  line-height: 1.6;

  &:last-child {
    margin-bottom: 0;
  }
}

.list-number {
  flex-shrink: 0;
  font-weight: 500;
}

.list-text {
  flex: 1;
}

.copyright-link {
  background: none;
  border: none;
  color: var(--color-accent);
  font-size: inherit;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;

  &:hover {
    opacity: 0.8;
  }
}

.loading, .not-found {
  text-align: center;
  padding: 4rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  color: var(--color-text-secondary);
}

.not-found p {
  margin-bottom: 1rem;
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

@media (max-width: 768px) {
  .article-hero {
    height: 250px;

    .hero-content {
      padding: 1.5rem;
    }

    .article-title {
      font-size: 1.5rem;
    }
  }

  .article-header-no-bg {
    .article-title {
      font-size: 1.5rem;
    }
  }

  .markdown-body {
    padding: 1.5rem;
    font-size: 1rem;
  }

  .article-meta {
    gap: 1rem;
  }

  .copyright-area {
    margin: 1.5rem;
    padding: 1rem;
  }
}
</style>
