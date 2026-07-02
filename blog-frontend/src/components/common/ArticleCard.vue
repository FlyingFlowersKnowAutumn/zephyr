<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

interface Props {
  article: {
    id: string
    slug?: string
    title: string
    excerpt?: string
    content?: string
    coverImage?: string
    tags?: string[]
    categoryName?: string
    authorName?: string
    createdAt?: string
    publishedAt?: string
    viewCount?: number
  }
  index?: number
}

const props = withDefaults(defineProps<Props>(), {
  index: 0
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

function handleClick() {
  router.push(`/articles/${props.article.slug || props.article.id}`)
}
</script>

<template>
  <article class="article-card" :class="{ 'image-right': props.index % 2 === 1 }" @click="handleClick">
    <div class="card-content">
      <div class="card-cover" v-if="article.coverImage">
        <img :src="article.coverImage" :alt="article.title" />      
      </div>
      <div class="card-cover placeholder" v-else>
        <svg width="100%" height="100%" viewBox="0 0 200 140" fill="none">
          <rect width="200" height="140" fill="var(--color-bg-secondary)"/>
          <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" fill="var(--color-text-secondary)" font-size="12">暂无封面</text>
        </svg>
      </div>
      <div class="card-info">
        <h3 class="card-title">{{ article.title }}</h3>
        
        <div class="card-meta">
          <span class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            <span>发表于 {{ formatDate(article.publishedAt || article.createdAt) }}</span>
          </span>
          <span class="meta-divider">|</span>
          <RouterLink v-if="article.categoryName" :to="{ path: '/categories', query: { category: article.categoryName } }" class="meta-item category-link">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
            <span>{{ article.categoryName }}</span>
          </RouterLink>
          <span v-else class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
            <span>未分类</span>
          </span>
        </div>
        
        <p class="card-excerpt">
          {{ article.excerpt || (article.content ? article.content.slice(0, 120) + '...' : '') }}
        </p>
      </div>
    </div>
  </article>
</template>

<style lang="scss" scoped>
.article-card {
  background: var(--color-bg-card);
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all var(--transition-default);
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.card-content {
  display: flex;
  height: 200px;
}

.card-cover {
  width: 39%;
  flex-shrink: 0;
  overflow: hidden;
  height: 200px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: center;
    transition: transform var(--transition-default);
  }

  &:hover img {
    transform: scale(1.08);
  }

  &.placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--color-bg-secondary);
  }
}

.card-info {
  flex: 1;
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.card-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 0.5rem;
  line-height: 1.5;
  transition: color var(--transition-default);

  &:hover {
    color: var(--color-accent);
  }
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.85rem;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.category-link {
  color: inherit;
  text-decoration: none;

  &:hover {
    color: var(--color-accent);
    text-decoration: underline;
  }
}

.meta-divider {
  color: var(--color-border);
}

.card-excerpt {
  font-size: 0.95rem;
  color: var(--color-text-secondary);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-right {
  .card-content {
    flex-direction: row-reverse;
  }
}

@media (max-width: 768px) {
  .card-content {
    flex-direction: column !important;
  }

  .card-cover {
    width: 100%;
    height: 180px;
  }

  .card-info {
    padding: 1.25rem;
  }

  .card-title {
    font-size: 1.2rem;
  }

  .card-excerpt {
    -webkit-line-clamp: 2;
  }
}
</style>
