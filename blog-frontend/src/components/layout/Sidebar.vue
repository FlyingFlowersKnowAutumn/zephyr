<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import { listCategories, listTags } from '@/api/article'
import { getUserProfile, getAnnouncement, updateAnnouncement } from '@/api/profile'
import { getSiteStats } from '@/api/site'
import type { CategoryDTO, TagDTO } from '@/types'

const BLOGGER_ID = '6e2ca12d-5d59-43ee-a0b3-c7f50127fef8'

const articleStore = useArticleStore()
const authStore = useAuthStore()
const categories = ref<CategoryDTO[]>([])
const tags = ref<TagDTO[]>([])
const bloggerProfile = ref<{ avatarUrl?: string; displayName?: string; bio?: string } | null>(null)
const stats = ref({
  articles: 0,
  categories: 0,
  tags: 0
})
const latestArticles = ref<any[]>([])
const tagSphereRef = ref<HTMLElement | null>(null)
let animationFrameId: number | null = null
let angleX = 0
let angleY = 0

const tagColors: Record<string, string> = {}
const colorPalette = [
  '#8B9A6B', '#A67C52', '#4A5568', '#38A169', '#38B2AC', '#9F7AEA', '#63B3ED', 
  '#ED8936', '#E53E3E', '#DD6B20', '#D69E2E', '#48BB78', '#319795', '#805AD5', 
  '#553C9A', '#B794F4', '#ED64A6', '#F687B3', '#718096', '#2D3748'
]

function getTagColor(tagName: string): string {
  if (!tagColors[tagName]) {
    const index = tagName.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0) % colorPalette.length
    tagColors[tagName] = colorPalette[index]
  }
  return tagColors[tagName]
}

function initTagSphere() {
  if (!tagSphereRef.value || tags.value.length === 0) return

  const sphere = tagSphereRef.value
  const tagElements = sphere.querySelectorAll('.sphere-tag')
  const radius = 100
  const total = tags.value.length

  tagElements.forEach((el, index) => {
    const phi = Math.acos(-1 + (2 * index) / total)
    const theta = Math.sqrt(total * Math.PI) * phi
    const x = radius * Math.cos(theta) * Math.sin(phi)
    const y = radius * Math.sin(theta) * Math.sin(phi)
    const z = radius * Math.cos(phi)
    ;(el as HTMLElement).dataset.x = String(x)
    ;(el as HTMLElement).dataset.y = String(y)
    ;(el as HTMLElement).dataset.z = String(z)
  })

  animateSphere()
}

function animateSphere() {
  if (!tagSphereRef.value) {
    animationFrameId = requestAnimationFrame(animateSphere)
    return
  }

  const sphere = tagSphereRef.value
  const tagElements = sphere.querySelectorAll('.sphere-tag')

  angleX += 0.001
  angleY += 0.002

  const cosX = Math.cos(angleX)
  const sinX = Math.sin(angleX)
  const cosY = Math.cos(angleY)
  const sinY = Math.sin(angleY)

  tagElements.forEach((el) => {
    const elem = el as HTMLElement
    let x = parseFloat(elem.dataset.x || '0')
    let y = parseFloat(elem.dataset.y || '0')
    let z = parseFloat(elem.dataset.z || '0')

    const y1 = y * cosX - z * sinX
    const z1 = y * sinX + z * cosX
    const x2 = x * cosY + z1 * sinY
    const z2 = -x * sinY + z1 * cosY

    const scale = (z2 + 150) / 250
    const opacity = Math.max(0.3, Math.min(1, scale))

    elem.style.transform = `translate3d(${x2}px, ${y1}px, ${z2}px) scale(${scale})`
    elem.style.opacity = String(opacity)
    elem.style.zIndex = String(Math.round(z2 + 200))
  })

  animationFrameId = requestAnimationFrame(animateSphere)
}

const isBlogger = computed(() => authStore.user?.role === 'blogger')

const profile = computed(() => {
  if (authStore.isLoggedIn && isBlogger.value && authStore.user) {
    return {
      avatar: authStore.user.avatarUrl,
      name: authStore.user.displayName || authStore.user.username || authStore.user.email || '用户',
      bio: authStore.user.bio || '记录技术与生活'
    }
  }
  if (bloggerProfile.value) {
    return {
      avatar: bloggerProfile.value.avatarUrl || null,
      name: bloggerProfile.value.displayName || '博主',
      bio: bloggerProfile.value.bio || '记录技术与生活'
    }
  }
  return {
    avatar: null,
    name: '博主名字',
    bio: '记录技术与生活'
  }
})

const announcement = ref({
  title: '公告',
  content: '即使再小的帆也能远航⛵'
})

const editingAnnouncement = ref(false)
const announcementForm = ref({
  title: '',
  content: ''
})
const saving = ref(false)

const siteInfo = ref({
  visitorCount: 0,
  viewCount: 0,
  lastUpdate: '2024-01-01'
})

const lastUpdateText = computed(() => formatRelativeTime(siteInfo.value.lastUpdate))

onMounted(async () => {
  try {
    const [catResult, tagResult, articleResult] = await Promise.all([
      listCategories(),
      listTags(),
      articleStore.fetchArticles({ page: 1, size: 5, status: 'PUBLISHED' })
    ])

    if (catResult.code === 0 && catResult.data) {
      // 兼容新旧格式：string[] 或 CategoryDTO[]
      const catData = catResult.data as any
      categories.value = typeof catData[0] === 'string' 
        ? catData.map((name: string) => ({ id: name, name, articleCount: 0, createdAt: '' }))
        : catData
      stats.value.categories = categories.value.length
    }
    if (tagResult.code === 0 && tagResult.data) {
      // 兼容新旧格式：string[] 或 TagDTO[]
      const tagData = tagResult.data as any
      tags.value = typeof tagData[0] === 'string'
        ? tagData.map((name: string) => ({ id: name, name, articleCount: 0, createdAt: '' }))
        : tagData
      stats.value.tags = tags.value.length
    }
    stats.value.articles = articleResult.total
    latestArticles.value = articleResult.articles.slice(0, 3)

    setTimeout(() => {
      initTagSphere()
    }, 100)
  } catch (e) {
    console.error('Failed to load sidebar data:', e)
  }

  if (!isBlogger.value) {
    try {
      const result = await getUserProfile(BLOGGER_ID)
      if (result.code === 0 && result.data) {
        bloggerProfile.value = result.data
      }
    } catch { /* use placeholder */ }
  }

  try {
    const result = await getAnnouncement(BLOGGER_ID)
    if (result.code === 0 && result.data) {
      announcement.value = result.data
    }
  } catch { /* use default */ }

  try {
    const result = await getSiteStats()
    if (result.code === 0 && result.data) {
      siteInfo.value.visitorCount = result.data.visitorCount
      siteInfo.value.viewCount = result.data.viewCount
      if (result.data.lastUpdate) {
        siteInfo.value.lastUpdate = result.data.lastUpdate
      }
    }
  } catch { /* use default */ }
})

onUnmounted(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
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

function formatRelativeTime(dateString?: string): string {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffSeconds = Math.floor(diffMs / 1000)
  const diffMinutes = Math.floor(diffSeconds / 60)
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)
  const diffWeeks = Math.floor(diffDays / 7)
  const diffMonths = Math.floor(diffDays / 30)
  const diffYears = Math.floor(diffDays / 365)

  if (diffSeconds < 60) {
    return '刚刚'
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  } else if (diffHours < 24) {
    return `${diffHours}小时前`
  } else if (diffDays < 30) {
    return `${diffDays}天前`
  } else if (diffMonths < 12) {
    return `${diffMonths}个月前`
  } else {
    return `${diffYears}年前`
  }
}

function startEditAnnouncement() {
  announcementForm.value = {
    title: announcement.value.title,
    content: announcement.value.content
  }
  editingAnnouncement.value = true
}

function cancelEditAnnouncement() {
  editingAnnouncement.value = false
}

async function saveAnnouncement() {
  saving.value = true
  try {
    const result = await updateAnnouncement(BLOGGER_ID, announcementForm.value)
    if (result.code === 0 && result.data) {
      announcement.value = result.data
      editingAnnouncement.value = false
    } else {
      console.error('Failed to save announcement:', result.message)
    }
  } catch (e) {
    console.error('Failed to save announcement:', e)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-card profile-card">
      <div class="avatar">
        <img v-if="profile.avatar" :src="profile.avatar" alt="avatar" />
        <span v-else class="avatar-placeholder">{{ profile.name.charAt(0).toUpperCase() }}</span>
      </div>
      <h3 class="profile-name">{{ profile.name }}</h3>
      <p class="profile-bio">{{ profile.bio }}</p>
      
      <div class="stats">
        <RouterLink to="/articles" class="stat-link">
          <span class="stat-label">文章</span>
          <span class="stat-num">{{ stats.articles }}</span>
        </RouterLink>
        <RouterLink to="/tags" class="stat-link">
          <span class="stat-label">标签</span>
          <span class="stat-num">{{ stats.tags }}</span>
        </RouterLink>
        <RouterLink to="/categories" class="stat-link">
          <span class="stat-label">分类</span>
          <span class="stat-num">{{ stats.categories }}</span>
        </RouterLink>
      </div>
      
      
    </div>

    <div class="sidebar-card announcement-card">
      <div v-if="!editingAnnouncement" class="announcement-header">
        <span class="announcement-icon">📢</span>
        <span class="announcement-title">{{ announcement.title }}</span>
        <button v-if="isBlogger" class="edit-announcement-btn" @click="startEditAnnouncement">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
        </button>
      </div>
      <div v-else class="announcement-edit-header">
        <input v-model="announcementForm.title" class="announcement-title-input" placeholder="公告标题" />
      </div>
      
      <div v-if="!editingAnnouncement">
        <p class="announcement-content">{{ announcement.content }}</p>
      </div>
      <div v-else class="announcement-edit-content">
        <textarea v-model="announcementForm.content" rows="3" placeholder="公告内容"></textarea>
        <div class="announcement-edit-actions">
          <button class="save-announcement-btn" :disabled="saving" @click="saveAnnouncement">
            {{ saving ? '发布中...' : '发布' }}
          </button>
          <button class="cancel-announcement-btn" @click="cancelEditAnnouncement">取消</button>
        </div>
      </div>
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

    <div class="sidebar-card tag-sphere-card">
      <h3 class="card-title">🏷️ 标签</h3>
      <div v-if="tags.length" 
           ref="tagSphereRef" 
           class="tag-sphere"
      >
        <RouterLink 
          v-for="tag in tags" 
          :key="tag.id"
          :to="{ path: '/tags', query: { tag: tag.name } }"
          class="sphere-tag"
          :style="{ '--tag-color': getTagColor(tag.name) }"
        >
          <span class="sphere-tag-inner">{{ tag.name }}</span>
        </RouterLink>
      </div>
      <div v-else class="empty-text">暂无</div>
    </div>

    <div class="sidebar-card category-card">
      <div class="category-header">
        <span class="category-title">📁 分类</span>
        <RouterLink to="/categories" class="category-arrow">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 18l6-6-6-6"/>
          </svg>
        </RouterLink>
      </div>
      <ul v-if="categories.length" class="category-list">
        <li v-for="cat in categories" :key="cat.id">
          <RouterLink :to="{ path: '/categories', query: { category: cat.name } }" class="category-link">
            <span class="category-name">{{ cat.name }}</span>
            <span class="category-count">{{ cat.articleCount || 0 }}</span>
          </RouterLink>
        </li>
      </ul>
      <div v-else class="empty-text">暂无</div>
    </div>

    <div class="sidebar-card">
      <h3 class="card-title">归档</h3>
      <ul class="archive-list">
        <li>
          <RouterLink to="/archive">按时间浏览文章</RouterLink>
        </li>
      </ul>
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
          <span class="info-value">{{ lastUpdateText }}</span>
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
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }

  .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-size: 1.5rem;
    font-weight: 600;
    transition: transform 0.5s ease;
  }

  &:hover img,
  &:hover .avatar-placeholder {
    transform: rotate(360deg);
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
  justify-content: space-around;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
  margin-bottom: 1rem;
}

.stat-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: inherit;
  transition: opacity var(--transition-default);

  &:hover {
    opacity: 0.7;
  }
}

.stat-num {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--color-accent);
  margin-top: 0.25rem;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
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

.edit-announcement-btn {
  margin-left: auto;
  padding: 0.25rem;
  background: transparent;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: 4px;
  transition: color var(--transition-default), background var(--transition-default);

  &:hover {
    color: var(--color-accent);
    background: var(--color-bg-secondary);
  }
}

.announcement-edit-header {
  margin-bottom: 0.75rem;
}

.announcement-title-input {
  width: 100%;
  padding: 0.5rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-accent);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  outline: none;
  transition: border-color var(--transition-default);
  box-sizing: border-box;

  &:focus {
    border-color: var(--color-accent);
  }

  &::placeholder {
    color: var(--color-text-secondary);
  }
}

.announcement-edit-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.announcement-edit-content textarea {
  width: 100%;
  padding: 0.75rem;
  font-size: 0.85rem;
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  outline: none;
  resize: none;
  transition: border-color var(--transition-default);
  box-sizing: border-box;
  line-height: 1.5;

  &:focus {
    border-color: var(--color-accent);
  }

  &::placeholder {
    color: var(--color-text-secondary);
  }
}

.announcement-edit-actions {
  display: flex;
  gap: 0.5rem;
}

.save-announcement-btn {
  flex: 1;
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: white;
  background: var(--color-accent);
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: opacity var(--transition-default);

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &:hover:not(:disabled) {
    opacity: 0.9;
  }
}

.cancel-announcement-btn {
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: color var(--transition-default), border-color var(--transition-default);

  &:hover {
    color: var(--color-text-primary);
    border-color: var(--color-accent);
  }
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

.category-card {
  .category-header {
    display: flex !important;
    align-items: center !important;
    justify-content: space-between !important;
    margin-bottom: 0.75rem !important;
    width: 100% !important;
    padding: 0 !important;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }

  .category-title {
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--color-text-primary);
    margin-bottom: 0;
  }

  .category-arrow {
    display: flex;
    align-items: center;
    color: var(--color-text-secondary);
    transition: color var(--transition-default), transform 0.3s ease;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      color: var(--color-accent);
      transform: scale(1.2);
    }
  }
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    margin-bottom: 0.35rem;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.category-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.35rem 0;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  text-decoration: none;
  transition: color var(--transition-default);
  width: 100%;
  box-sizing: border-box;

  &:hover {
    color: var(--color-accent);
  }
}

.category-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-count {
  flex-shrink: 0;
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  margin-left: 0.5rem;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.tag-sphere-card {
  .card-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
}

.tag-sphere {
  position: relative;
  width: 100%;
  height: 220px;
  perspective: 800px;
  transform-style: preserve-3d;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sphere-tag {
  position: absolute;
  left: 50%;
  top: 50%;
  transform-origin: center center;
  text-decoration: none;
  transform-style: preserve-3d;
  pointer-events: auto;

  .sphere-tag-inner {
    display: inline-block;
    padding: 0.25rem 0.6rem;
    background: var(--tag-color);
    opacity: 0.85;
    color: white;
    border-radius: 4px;
    font-size: 0.75rem;
    white-space: nowrap;
    cursor: pointer;
    transition: transform 0.3s ease, opacity 0.3s ease;
  }

  &:hover .sphere-tag-inner {
    transform: scale(1.15);
    opacity: 1;
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
