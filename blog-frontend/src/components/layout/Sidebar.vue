<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import { listCategories, listTags } from '@/api/article'

const articleStore = useArticleStore()
const authStore = useAuthStore()
const categories = ref<string[]>([])
const tags = ref<string[]>([])
const stats = ref({
  articles: 0,
  categories: 0,
  tags: 0
})
const latestArticles = ref<any[]>([])

const profile = computed(() => {
  if (authStore.isLoggedIn && authStore.user) {
    return {
      avatar: authStore.user.avatarUrl || '/blog.png',
      name: authStore.user.displayName || authStore.user.username || authStore.user.email || '用户',
      bio: '记录技术与生活'
    }
  }
  return {
    avatar: '/blog.png',
    name: '博主名字',
    bio: '记录技术与生活'
  }
})

const announcement = ref({
  title: '公告',
  content: '即使再小的帆也能远航⛵'
})

const siteInfo = ref({
  visitorCount: 0,
  viewCount: 0,
  lastUpdate: '2024-01-01'
})

onMounted(async () => {
  const [catResult, tagResult, articleResult] = await Promise.all([
    listCategories(),
    listTags(),
    articleStore.fetchArticles({ page: 1, size: 5, status: 'PUBLISHED' })
  ])
  
  if (catResult.code === 0 && catResult.data) {
    categories.value = catResult.data
    stats.value.categories = catResult.data.length
  }
  if (tagResult.code === 0 && tagResult.data) {
    tags.value = tagResult.data
    stats.value.tags = tagResult.data.length
  }
  stats.value.articles = articleResult.total
  latestArticles.value = articleResult.articles.slice(0, 3)
})

function formatDate(dateString?: string) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-card profile-card">
      <div class="avatar">
        <img :src="profile.avatar" alt="avatar" />
      </div>
      <h3 class="profile-name">{{ profile.name }}</h3>
      <p class="profile-bio">{{ profile.bio }}</p>
      
      <div class="stats">
        <div class="stat-item">
          <span class="stat-num">{{ stats.articles }}</span>
          <span class="stat-label">文章</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.tags }}</span>
          <span class="stat-label">标签</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.categories }}</span>
          <span class="stat-label">分类</span>
        </div>
      </div>
      
      
    </div>

    <div class="sidebar-card announcement-card">
      <div class="announcement-header">
        <span class="announcement-icon">📢</span>
        <span class="announcement-title">{{ announcement.title }}</span>
      </div>
      <p class="announcement-content">{{ announcement.content }}</p>
    </div>

    <div v-if="latestArticles.length" class="sidebar-card latest-articles-card">
      <h3 class="card-title">最新文章</h3>
      <ul class="latest-articles-list">
        <li v-for="article in latestArticles" :key="article.id">
          <RouterLink :to="`/articles/${article.slug || article.id}`" class="article-link">
            <div v-if="article.coverImage" class="article-cover">
              <img :src="article.coverImage" :alt="article.title" />
            </div>
            <div v-else class="article-cover placeholder">
              <span>📝</span>
            </div>
            <div class="article-info">
              <span class="article-title">{{ article.title.slice(0, 20) }}{{ article.title.length > 20 ? '...' : '' }}</span>
              <span class="article-date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
            </div>
          </RouterLink>
        </li>
      </ul>
    </div>

    <div class="sidebar-card">
      <h3 class="card-title">分类</h3>
      <ul v-if="categories.length" class="category-list">
        <li>
          <RouterLink to="/articles">全部</RouterLink>
        </li>
        <li v-for="cat in categories" :key="cat">
          <RouterLink :to="{ path: '/articles', query: { category: cat } }">{{ cat }}</RouterLink>
        </li>
      </ul>
      <div v-else class="empty-text">暂无</div>
    </div>

    <div class="sidebar-card">
      <h3 class="card-title">标签</h3>
      <div v-if="tags.length" class="tag-cloud">
        <RouterLink 
          v-for="tag in tags.slice(0, 10)" 
          :key="tag"
          :to="{ path: '/search', query: { q: tag } }"
          class="tag"
        >
          {{ tag }}
        </RouterLink>
      </div>
      <div v-else class="empty-text">暂无</div>
    </div>

    <div class="sidebar-card">
      <h3 class="card-title">归档</h3>
      <ul v-if="latestArticles.length" class="archive-list">
        <li v-for="article in latestArticles" :key="article.id">
          <RouterLink :to="`/articles/${article.slug || article.id}`">{{ formatDate(article.publishedAt || article.createdAt) }}</RouterLink>
        </li>
      </ul>
      <div v-else class="empty-text">暂无</div>
    </div>

    <div class="sidebar-card site-info-card">
      <h3 class="card-title">网站信息</h3>
      <div class="site-info">
        <div class="info-item">
          <span class="info-label">文章数目</span>
          <span class="info-value">{{ stats.articles }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">本站访客数</span>
          <span class="info-value">{{ siteInfo.visitorCount }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">本站总浏览量</span>
          <span class="info-value">{{ siteInfo.viewCount }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">最后更新时间</span>
          <span class="info-value">{{ siteInfo.lastUpdate }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<style lang="scss" scoped>
.sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.sidebar-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: 1.5rem;
  box-shadow: var(--shadow-card);
  transition: box-shadow var(--transition-default);

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.profile-card {
  text-align: center;
}

.avatar {
  width: 90px;
  height: 90px;
  margin: 0 auto 1rem;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--color-border);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.profile-name {
  font-size: 1.35rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--color-text-primary);
}

.profile-bio {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  margin-bottom: 1.5rem;
  line-height: 1.5;
}

.stats {
  display: flex;
  justify-content: center;
  gap: 2rem;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
  margin-bottom: 1rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--color-accent);
}

.stat-label {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  margin-top: 0.25rem;
}

.follow-btn {
  width: 180px;
  height: 33px;
  padding: 0;
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.9rem;
  font-weight: 400;
  cursor: pointer;
  transition: transform 0.3s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: #f97316;
    transition: left 0.1s ease;
    z-index: 0;
  }

  span {
    position: relative;
    z-index: 1;
  }

  &:hover {
    transform: scale(1.05);

    &::before {
      left: 0;
    }
  }
}

.announcement-card {
  background: var(--color-bg-card);
  border: none;
}

.announcement-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.announcement-icon {
  font-size: 0.95rem;
}

.announcement-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-accent);
}

.announcement-content {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.latest-articles-card {
  .card-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    &::before {
      content: '';
      width: 4px;
      height: 14px;
      background: var(--color-accent);
      border-radius: 2px;
    }
  }
}

.latest-articles-list {
  list-style: none;
}

.latest-articles-list li {
  margin-bottom: 0.75rem;

  &:last-child {
    margin-bottom: 0;
  }
}

.article-link {
  display: flex;
  gap: 0.75rem;
  text-decoration: none;
  color: inherit;
  transition: opacity var(--transition-default);

  &:hover {
    opacity: 0.8;
  }
}

.article-cover {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--color-bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.article-cover.placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
}

.article-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 0.85rem;
  color: var(--color-text-primary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 0.25rem;
}

.article-date {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.card-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--color-text-primary);
}

.category-list {
  list-style: none;

  li {
    margin-bottom: 0.5rem;

    a {
      display: block;
      padding: 0.4rem 0;
      color: var(--color-text-secondary);
      font-size: 0.9rem;
      transition: color var(--transition-default);

      &:hover {
        color: var(--color-accent);
        text-decoration: none;
      }
    }
  }
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.tag {
  padding: 0.3rem 0.65rem;
  background: var(--color-bg-secondary);
  border-radius: 16px;
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  transition: all var(--transition-default);

  &:hover {
    background: var(--color-accent);
    color: white;
    text-decoration: none;
  }
}

.archive-list {
  list-style: none;

  li {
    margin-bottom: 0.5rem;

    a {
      display: block;
      padding: 0.4rem 0;
      color: var(--color-text-secondary);
      font-size: 0.9rem;
      transition: color var(--transition-default);

      &:hover {
        color: var(--color-accent);
        text-decoration: none;
      }
    }
  }
}

.site-info-card {
  .site-info {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.3rem 0;
  font-size: 0.85rem;
}

.info-label {
  color: var(--color-text-secondary);
}

.info-value {
  color: var(--color-text-primary);
  font-weight: 500;
}

.empty-text {
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  padding: 0.5rem 0;
}

@media (max-width: 1024px) {
  .sidebar {
    width: 100%;
  }
}
</style>
