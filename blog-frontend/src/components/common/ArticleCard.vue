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
      <div class="card-body">
        <h2 class="card-title">{{ article.title }}</h2>
        <div class="card-meta">
          <span class="meta-date">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            <span>{{ formatDate(article.publishedAt || article.createdAt) }}</span>
          </span>
          <span v-if="article.categoryName" class="meta-divider">|</span>
          <RouterLink v-if="article.categoryName" :to="{ path: '/categories', query: { category: article.categoryName } }" class="meta-category">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
            <span>{{ article.categoryName }}</span>
          </RouterLink>
        </div>
        <p class="card-excerpt">
          {{ article.excerpt || (article.content ? article.content.slice(0, 150) + '...' : '') }}
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
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    box-shadow: 0 8px 28px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }
}

.card-content {
  display: flex;
  flex-direction: row;
  height: 220px;
}

.card-cover {
  width: 45%;
  flex-shrink: 0;
  height: 100%;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: center;
    transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  }

  .article-card:hover & img {
    transform: scale(1.05);
  }

  &.placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--color-bg-secondary);
  }
}

.card-body {
  flex: 1;
  padding: 1.5rem 1.75rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.card-title {
  font-size: 1.35rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 0.65rem;
  line-height: 1.5;
  transition: color 0.25s ease;
  cursor: pointer;

  &:hover {
    color: var(--color-accent);
  }
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.7rem;
  font-size: 0.675rem;
  color: var(--color-text-secondary);
  flex-wrap: wrap;
}

.meta-date {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  opacity: 0.75;
}

.meta-divider {
  color: var(--color-border);
}

.meta-category {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: all 0.25s ease;
  padding: 2px 6px;
  border-radius: 4px;

  &:hover {
    background: rgba(0, 212, 255, 0.08);
    color: var(--color-accent);
  }
}

.card-excerpt {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  line-height: 1.8;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  opacity: 0.85;
}

.image-right {
  .card-content {
    flex-direction: row-reverse;
  }
}

@media (max-width: 900px) {
  .card-content {
    flex-direction: column;
    height: auto;
  }

  .card-cover {
    width: 100%;
    height: 200px;
  }

  .card-body {
    padding: 1.5rem 1.75rem;
  }

  .card-title {
    font-size: 1.4rem;
  }

  .image-right {
    .card-content {
      flex-direction: column;
    }
  }
}

@media (max-width: 768px) {
  .card-cover {
    height: 170px;
  }

  .card-body {
    padding: 1.25rem;
  }

  .card-title {
    font-size: 1.15rem;
    margin-bottom: 0.7rem;
  }

  .card-meta {
    margin-bottom: 0.75rem;
    font-size: 0.675rem;
  }
}
</style>
