<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch, nextTick } from 'vue'
import { RouterLink } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useAuthStore } from '@/stores/auth'
import { useSidebarStore } from '@/stores/sidebar'
import { updateAnnouncement } from '@/api/profile'
import { generateTocFromMarkdown, scrollToHeading, type TocItem } from '@/utils/toc'
import type { CategoryDTO, TagDTO } from '@/types'

interface Props {
  articleContent?: string
  showToc?: boolean
  showRecentUpdates?: boolean
  isArticlePage?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  articleContent: '',
  showToc: false,
  showRecentUpdates: false,
  isArticlePage: false
})

const tocItems = ref<TocItem[]>([])
const activeTocId = ref('')

const currentTocLine = computed(() => {
  if (!activeTocId.value) return 1
  const active = tocItems.value.find((t) => t.id === activeTocId.value)
  return active?.lineNumber ?? 1
})

const visibleTocItems = computed(() => {
  const items = tocItems.value
  if (!items.length) return []

  // 没有 h2 时全部展示
  const hasH2 = items.some(item => item.level === 2)
  if (!hasH2) return items

  // 始终显示所有 h1 和 h2；仅当前活跃的 h2 章节展开其 h3 子项
  if (!activeTocId.value) {
    return items.filter(item => item.level <= 2)
  }

  const activeIdx = items.findIndex(t => t.id === activeTocId.value)
  if (activeIdx === -1) return items.filter(item => item.level <= 2)

  // 找到当前活跃标题所属的 h2 章节
  let chapterH2Idx = activeIdx
  if (items[chapterH2Idx].level > 2) {
    // h3 → 向前找最近的 h2
    while (chapterH2Idx >= 0 && items[chapterH2Idx].level !== 2) chapterH2Idx--
  } else if (items[chapterH2Idx].level < 2) {
    // h1 → 向后找第一个 h2
    while (chapterH2Idx < items.length && items[chapterH2Idx].level !== 2) chapterH2Idx++
  }

  if (chapterH2Idx < 0 || chapterH2Idx >= items.length || items[chapterH2Idx].level !== 2) {
    return items.filter(item => item.level <= 2)
  }

  // 找到该 h2 章节的结束位置（下一个 h2 前或末尾）
  let chapterEnd = chapterH2Idx
  while (chapterEnd < items.length - 1 && items[chapterEnd + 1].level > 2) {
    chapterEnd++
  }

  // h1/h2 全部 + 当前章节的 h3
  const result: TocItem[] = []
  for (let i = 0; i < items.length; i++) {
    if (items[i].level <= 2) {
      result.push(items[i])
    } else if (i >= chapterH2Idx && i <= chapterEnd) {
      result.push(items[i])
    }
  }
  return result
})

// 目录自动跟踪滚动
watch(activeTocId, () => {
  nextTick(() => {
    const el = document.querySelector('.toc-link.active')
    if (el) el.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
  })
})

watch([() => props.articleContent, () => props.showToc], ([content, showToc]) => {
  if (content && showToc) {
    const items = generateTocFromMarkdown(content)
    if (items.length > 0) {
      tocItems.value = items
    } else {
      setTimeout(() => {
        extractTocFromDom()
      }, 100)
    }
  }
}, { immediate: true })

function extractTocFromDom() {
  const articleContent = document.querySelector('.article-content')
  if (!articleContent) return
  
  const headings = articleContent.querySelectorAll('h1[id], h2[id], h3[id]')
  const items: TocItem[] = []
  
  headings.forEach((heading) => {
    const level = parseInt(heading.tagName.charAt(1))
    const id = heading.getAttribute('id')
    const text = heading.textContent?.trim()
    
    if (id && text) {
      items.push({ id, text, level, lineNumber: 0 })
    }
  })
  
  if (items.length > 0) {
    tocItems.value = items
  }
}

let scrollTicking = false

function updateActiveHeading() {
  const headings = document.querySelectorAll('.markdown-body h1[id], .markdown-body h2[id], .markdown-body h3[id]')
  if (!headings.length) return

  // 定位线：页面顶部（导航栏下方 80px）
  const topLine = 80
  // 找到最后一个顶部已越过定位线的标题，即为当前阅读位置
  // 没有任何标题越过定位线时为空，不展开任何章节
  let activeId = ''
  for (const h of headings) {
    if (h.getBoundingClientRect().top <= topLine) {
      activeId = h.id
    }
  }
  activeTocId.value = activeId
}

function onTocScroll() {
  if (scrollTicking) return
  scrollTicking = true
  requestAnimationFrame(() => {
    scrollTicking = false
    updateActiveHeading()
  })
}

function setupTocObserver() {
  cleanupTocObserver()
  window.addEventListener('scroll', onTocScroll, { passive: true })
  updateActiveHeading()
  if (!activeTocId.value && tocItems.value.length > 0) {
    activeTocId.value = tocItems.value[0].id
  }
}

function cleanupTocObserver() {
  window.removeEventListener('scroll', onTocScroll)
}

watch([() => props.isArticlePage, tocItems], ([isArticle, items]) => {
  if (isArticle && items.length > 0) {
    setTimeout(() => {
      setupTocObserver()
    }, 300)
  } else if (!isArticle) {
    cleanupTocObserver()
  }
})

function handleTocClick(id: string) {
  activeTocId.value = id
  scrollToHeading(id)
}

function getTocIndent(level: number): string {
  const indents: Record<number, string> = {
    1: '0',
    2: '1rem',
    3: '2rem'
  }
  return indents[level] || '0'
}

const BLOGGER_ID = '243c28e9-40f4-492c-a271-5451fa2d8171'

const articleStore = useArticleStore()
const authStore = useAuthStore()
const sidebarStore = useSidebarStore()
const categories = ref<CategoryDTO[]>([])
const tags = ref<TagDTO[]>([])
const latestArticles = ref<any[]>([])
const archiveYears = ref<{ year: string; count: number }[]>([])
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

const isBlogger = computed(() => authStore.user?.role === 'blogger')

const profile = computed(() => {
  if (authStore.isLoggedIn && isBlogger.value && authStore.user) {
    return {
      avatar: authStore.user.avatarUrl,
      name: authStore.user.displayName || authStore.user.username || authStore.user.email || '用户',
      bio: authStore.user.bio || '记录技术与生活'
    }
  }
  return sidebarStore.profile
})

const announcement = computed(() => sidebarStore.announcement)

const editingAnnouncement = ref(false)
const announcementForm = ref({
  title: '',
  content: ''
})
const saving = ref(false)

const siteInfo = computed(() => sidebarStore.siteInfo)
const lastUpdateText = computed(() => sidebarStore.lastUpdateText)
const stats = computed(() => sidebarStore.stats)

onMounted(async () => {
  await sidebarStore.loadAllData()

  try {
    const articleResult = await articleStore.fetchArticles({ page: 1, size: 100, status: 'PUBLISHED' })
    sidebarStore.updateStatsArticleCount(articleResult.total)
    latestArticles.value = articleResult.articles.slice(0, 4)

    const yearMap: Record<string, number> = {}
    articleResult.articles.forEach((a: any) => {
      const y = new Date(a.publishedAt || a.createdAt).getFullYear().toString()
      yearMap[y] = (yearMap[y] || 0) + 1
    })
    archiveYears.value = Object.entries(yearMap)
      .map(([year, count]) => ({ year, count }))
      .sort((a, b) => b.year.localeCompare(a.year))
  } catch (e) {
    console.error('Failed to load articles:', e)
  }

  categories.value = sidebarStore.categories
  tags.value = sidebarStore.tags
})

onUnmounted(() => {
  cleanupTocObserver()
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
    const userId = authStore.user?.id || BLOGGER_ID
    const result = await updateAnnouncement(userId, announcementForm.value)
    if (result.code === 0 && result.data) {
      await sidebarStore.loadAnnouncement()
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

    <div v-if="showToc && isArticlePage" class="sidebar-sticky-group">
      <div class="sidebar-card toc-card">
        <div class="toc-header">
          <svg class="toc-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="4" y1="6" x2="20" y2="6" />
            <line x1="4" y1="12" x2="20" y2="12" />
            <line x1="4" y1="18" x2="20" y2="18" />
          </svg>
          <span class="toc-title">目录</span>
          <span class="toc-count">{{ currentTocLine }}</span>
        </div>
        <div v-if="tocItems.length === 0" class="toc-empty">本文暂无目录</div>
        <nav v-else class="toc-nav">
          <ul class="toc-list">
            <li v-for="item in visibleTocItems" :key="item.id" class="toc-item">
              <button
                class="toc-link"
                :class="{ active: activeTocId === item.id }"
                :style="{ paddingLeft: getTocIndent(item.level) }"
                @click="handleTocClick(item.id)"
              >
                <span class="toc-text">{{ item.text }}</span>
              </button>
            </li>
          </ul>
        </nav>
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
    </div>

    <div v-if="showRecentUpdates && !isArticlePage && latestArticles.length" class="sidebar-card latest-articles-card">
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

    <div v-if="!isArticlePage" class="sidebar-card tag-sphere-card">
      <div class="tag-header">
        <span class="tag-title">标签</span>
        <RouterLink to="/tags" class="tag-arrow">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 18l6-6-6-6"/>
          </svg>
        </RouterLink>
      </div>
      <div v-if="tags.length" class="tag-cloud">
        <RouterLink
          v-for="(tag, idx) in tags"
          :key="tag.id"
          :to="{ path: '/tags', query: { tag: tag.name } }"
          class="cloud-tag"
          :style="{
            '--tag-color': getTagColor(tag.name),
            '--tag-index': idx
          }"
        >
          {{ tag.name }}
        </RouterLink>
      </div>
      <div v-else class="empty-text">暂无</div>
    </div>

    <div v-if="!isArticlePage" class="sidebar-card category-card">
      <div class="category-header">
        <span class="category-title">分类</span>
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

    <div v-if="!isArticlePage" class="sidebar-card archive-card">
      <div class="archive-header">
        <span class="archive-title">归档</span>
        <RouterLink to="/archive" class="archive-arrow">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 18l6-6-6-6"/>
          </svg>
        </RouterLink>
      </div>
      <ul v-if="archiveYears.length" class="archive-list">
        <li v-for="item in archiveYears" :key="item.year">
          <RouterLink :to="{ path: '/archive' }" class="archive-link">
            <span class="archive-name">{{ item.year }}</span>
            <span class="archive-count">{{ item.count }}</span>
          </RouterLink>
        </li>
      </ul>
      <div v-else class="empty-text">暂无</div>
    </div>

    <div v-if="!isArticlePage" class="sidebar-card site-info-card">
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
  margin-bottom: 0;
  padding: 0.75rem 0;
  border-bottom: 1px dashed var(--color-border);

  &:last-child {
    border-bottom: none;
  }
}

.article-link {
  display: flex;
  gap: 0.75rem;
  text-decoration: none;
  color: inherit;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.15), transparent);
    transition: left 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    transform: translateX(4px);
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(52, 152, 219, 0.08) 100%);
    box-shadow: 0 0 20px rgba(0, 212, 255, 0.15);

    &::after {
      left: 100%;
    }

    .article-title {
      color: var(--color-accent);
    }

    .article-cover img {
      transform: scale(1.1);
    }
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
    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
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
    font-size: 1rem;
    font-weight: 600;
    color: var(--color-text-primary);
    margin-bottom: 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;

    &::before {
      content: '📁';
      font-size: 1.1rem;
    }
  }

  .category-arrow {
    display: flex;
    align-items: center;
    color: var(--color-text-secondary);
    transition: color var(--transition-default), transform 0.3s ease;

    svg {
      width: 18px;
      height: 18px;
    }

    &:hover {
      color: var(--color-accent);
      transform: translateX(2px);
    }
  }
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    margin-bottom: 0.1rem;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.category-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.4rem 0.8rem;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  text-decoration: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  width: 100%;
  box-sizing: border-box;
  border-radius: 8px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.15), transparent);
    transition: left 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(52, 152, 219, 0.08) 100%);
    color: var(--color-accent);
    transform: translateX(4px);
    box-shadow: 0 0 20px rgba(0, 212, 255, 0.15);

    &::after {
      left: 100%;
    }

    .category-count {
      color: var(--color-accent);
      transform: scale(1.15);
      text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
    }

    .category-name {
      transform: translateX(3px);
      font-weight: 500;
    }
  }
}

.category-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.category-count {
  flex-shrink: 0;
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  margin-left: 0.5rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}


.tag-sphere-card {
  overflow: hidden;
  border-radius: 12px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border) !important;

  .tag-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.75rem;
    width: 100%;
  }

  .tag-title {
    font-size: 1rem;
    font-weight: 600;
    color: var(--color-text-primary);
    margin-bottom: 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;

    &::before {
      content: '🏷️';
      font-size: 1.1rem;
    }
  }

  .tag-arrow {
    display: flex;
    align-items: center;
    color: var(--color-text-secondary);
    transition: color var(--transition-default), transform 0.3s ease;

    svg {
      width: 18px;
      height: 18px;
    }

    &:hover {
      color: var(--color-accent);
      transform: translateX(2px);
    }
  }
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.25rem 0;
}

.cloud-tag {
  --float-dur: calc(2.5s + var(--tag-index) * 0.15s);
  --float-delay: calc(var(--tag-index) * 0.12s);
  --shimmer-delay: calc(var(--tag-index) * 0.2s);

  display: inline-block;
  padding: 0.35rem 0.75rem;
  border-radius: 4px;
  font-size: 0.78rem;
  font-weight: 500;
  color: #fff;
  text-decoration: none;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  background: var(--tag-color);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.3s ease;
  animation: tagFloat var(--float-dur) ease-in-out var(--float-delay) infinite;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 60%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.25) 50%,
      transparent 100%
    );
    animation: tagShimmer 3s ease-in-out var(--shimmer-delay) infinite;
  }

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 4px;
    background: radial-gradient(
      circle at 30% 30%,
      rgba(255, 255, 255, 0.2) 0%,
      transparent 60%
    );
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    animation-play-state: paused;
    transform: translateY(-4px) scale(1.08);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
    text-decoration: none;
    color: #fff;
    z-index: 2;

    &::before {
      animation-play-state: paused;
    }

    &::after {
      opacity: 1;
    }
  }
}

@keyframes tagFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

@keyframes tagShimmer {
  0%, 100% {
    left: -100%;
  }
  50% {
    left: 120%;
  }
}

.archive-card {
  .archive-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.75rem;
    width: 100%;
  }

  .archive-title {
    font-size: 1rem;
    font-weight: 600;
    color: var(--color-text-primary);
    margin-bottom: 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;

    &::before {
      content: '🗄️';
      font-size: 1.1rem;
    }
  }

  .archive-arrow {
    display: flex;
    align-items: center;
    color: var(--color-text-secondary);
    transition: color var(--transition-default), transform 0.3s ease;

    svg {
      width: 18px;
      height: 18px;
    }

    &:hover {
      color: var(--color-accent);
      transform: translateX(2px);
    }
  }
}

.archive-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    margin-bottom: 0.1rem;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.archive-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.4rem 0.8rem;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  text-decoration: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  width: 100%;
  box-sizing: border-box;
  border-radius: 8px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.15), transparent);
    transition: left 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(52, 152, 219, 0.08) 100%);
    color: var(--color-accent);
    transform: translateX(4px);
    box-shadow: 0 0 20px rgba(0, 212, 255, 0.15);

    &::after {
      left: 100%;
    }

    .archive-count {
      color: var(--color-accent);
      transform: scale(1.15);
      text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
    }

    .archive-name {
      transform: translateX(3px);
      font-weight: 500;
    }
  }
}

.archive-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.archive-count {
  flex-shrink: 0;
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.site-info-card {
  .site-info {
    display: flex;
    flex-direction: column;
    gap: 0;
  }
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.25rem 0.2rem;
  font-size: 0.9rem;
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

.sidebar-sticky-group {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  position: sticky;
  top: 1.5rem;
  z-index: 10;
}

.toc-card {
  padding: 0;
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.toc-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);

  .toc-icon {
    width: 18px;
    height: 18px;
    color: #22c55e;
  }

  .toc-title {
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--color-text-primary);
    flex: 1;
  }

  .toc-count {
    font-size: 0.8rem;
    color: var(--color-text-secondary);
    font-weight: 600;
  }
}

.toc-nav {
  max-height: 400px;
  overflow-y: auto;
  padding: 0.5rem 0;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-item {
  margin-bottom: 0;
}

.toc-link {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  border: none;
  border-radius: 0;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  cursor: pointer;
  width: 100%;
  text-align: left;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &:hover {
    background: rgba(52, 211, 153, 0.08);
    color: #22c55e;
  }

  &.active {
    background: rgba(52, 211, 153, 0.15);
    color: #22c55e;
    font-weight: 500;
  }
}

.toc-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.toc-empty {
  padding: 1.5rem 1rem;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
}

.recent-updates-card {
  .card-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    &::before {
      content: '🔄';
      font-size: 1rem;
    }
  }
}

.recent-updates-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.recent-updates-list li {
  margin-bottom: 0.5rem;

  &:last-child {
    margin-bottom: 0;
  }
}

.update-link {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  padding: 0.4rem 0.5rem;
  border-radius: 6px;
  color: inherit;
  text-decoration: none;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(0, 212, 255, 0.1);

    .update-title {
      color: var(--color-accent);
    }
  }
}

.update-title {
  font-size: 0.85rem;
  color: var(--color-text-primary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.update-date {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

@media (max-width: 1024px) {
  .sidebar {
    width: 100%;
  }
}
</style>
