<script setup lang="ts">
import { RouterLink } from 'vue-router'

interface Props {
  article: {
    id: string
    slug?: string
    title: string
    excerpt?: string
    content?: string
    coverImage?: string
    tags?: string[]
    authorName?: string
    createdAt?: string
    publishedAt?: string
    viewCount?: number
  }
}

defineProps<Props>()

function formatDate(dateString?: string) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<template>
  <article class="article-card">
    <RouterLink :to="`/articles/${article.slug || article.id}`" class="card-link">
      <div class="card-content">
        <div class="card-header">
          <span v-if="article.tags && article.tags.length" class="card-tag">{{ article.tags[0] }}</span>
          <span class="card-date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
        </div>
        
        <h3 class="card-title">{{ article.title }}</h3>
        
        <p class="card-excerpt">
          {{ article.excerpt || (article.content ? article.content.slice(0, 100) + '...' : '') }}
        </p>
        
        <div class="card-footer">
          <span class="read-more">阅读全文</span>
          <span class="view-count">{{ article.viewCount || 0 }} 阅读</span>
        </div>
      </div>
    </RouterLink>
  </article>
</template>

<style lang="scss" scoped>
.article-card {
  padding: 2rem 0;
  border-bottom: 1px solid var(--color-border);
  transition: all var(--transition-default);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    transform: translateX(8px);
  }
}

.card-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.card-content {
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  font-size: 0.85rem;
}

.card-tag {
  padding: 0.25rem 0.75rem;
  background: var(--color-accent);
  color: white;
  border-radius: 4px;
  font-weight: 500;
}

.card-date {
  color: var(--color-text-secondary);
}

.card-title {
  font-size: 1.35rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 0.75rem;
  line-height: 1.5;
  transition: color var(--transition-default);

  &:hover {
    color: var(--color-accent);
  }
}

.card-excerpt {
  font-size: 0.95rem;
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin-bottom: 1rem;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.85rem;
}

.read-more {
  color: var(--color-accent);
  font-weight: 500;
  transition: color var(--transition-default);

  &:hover {
    color: var(--color-accent-secondary);
    text-decoration: underline;
  }
}

.view-count {
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .article-card {
    padding: 1.5rem 0;
  }

  .card-title {
    font-size: 1.2rem;
  }
}
</style>
