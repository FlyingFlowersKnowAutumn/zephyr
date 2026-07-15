import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CategoryDTO, TagDTO, UserProfileDTO, AnnouncementDTO } from '@/types'
import { listCategories, listTags, getLatestArticleTime } from '@/api/article'
import { getUserProfile, getAnnouncement } from '@/api/profile'
import { getSiteStats } from '@/api/site'

const BLOGGER_ID = '243c28e9-40f4-492c-a271-5451fa2d8171'
const CACHE_TTL = 5 * 60 * 1000

interface SidebarCache {
  bloggerProfile: UserProfileDTO | null
  announcement?: AnnouncementDTO
  categories?: CategoryDTO[]
  tags?: TagDTO[]
  stats?: {
    articles: number
    categories: number
    tags: number
  }
  siteInfo?: {
    visitorCount: number
    viewCount: number
    lastUpdate: string
  }
  cachedAt?: number
}

export const useSidebarStore = defineStore('sidebar', () => {
  const bloggerProfile = ref<UserProfileDTO | null>(null)
  const announcement = ref<AnnouncementDTO>({
    title: '公告',
    content: '即使再小的帆也能远航⛵'
  })
  const categories = ref<CategoryDTO[]>([])
  const tags = ref<TagDTO[]>([])
  const stats = ref({
    articles: 0,
    categories: 0,
    tags: 0
  })
  const siteInfo = ref<{
    visitorCount: number
    viewCount: number
    lastUpdate: string
  }>({
    visitorCount: 0,
    viewCount: 0,
    lastUpdate: '2024-01-01'
  })
  const isLoading = ref(false)

  const profile = computed(() => {
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

  const lastUpdateText = computed(() => {
    const dateString = siteInfo.value.lastUpdate
    if (!dateString) return ''
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffSeconds = Math.floor(diffMs / 1000)
    const diffMinutes = Math.floor(diffSeconds / 60)
    const diffHours = Math.floor(diffMinutes / 60)
    const diffDays = Math.floor(diffHours / 24)
    const diffMonths = Math.floor(diffDays / 30)
    const diffYears = Math.floor(diffDays / 365)

    if (diffSeconds < 60) return '刚刚发布'
    if (diffMinutes < 60) return `${diffMinutes}分钟前`
    if (diffHours < 24) return `${diffHours}小时前`
    if (diffDays < 30) return `${diffDays}天前`
    if (diffMonths < 12) return `${diffMonths}个月前`
    return `${diffYears}年前`
  })

  function loadCache() {
    try {
      const cached = localStorage.getItem('sidebarCache')
      if (cached) {
        const data: SidebarCache = JSON.parse(cached)
        if (data.cachedAt && Date.now() - data.cachedAt < CACHE_TTL) {
          if (data.bloggerProfile) bloggerProfile.value = data.bloggerProfile
          if (data.announcement) announcement.value = data.announcement
          if (data.categories) categories.value = data.categories
          if (data.tags) tags.value = data.tags
          if (data.stats) stats.value = data.stats
          if (data.siteInfo) siteInfo.value = data.siteInfo
          if (data.bloggerProfile) return true
        }
      }
    } catch {
      // ignore parse errors
    }
    return false
  }

  function saveCache() {
    try {
      if (!bloggerProfile.value) return
      const data: SidebarCache = {
        bloggerProfile: bloggerProfile.value,
        announcement: announcement.value,
        categories: categories.value,
        tags: tags.value,
        stats: stats.value,
        siteInfo: siteInfo.value,
        cachedAt: Date.now()
      }
      localStorage.setItem('sidebarCache', JSON.stringify(data))
    } catch {
      // ignore storage errors
    }
  }

  async function loadAllData(forceRefresh = false) {
    const hasCache = loadCache()
    if (hasCache && !forceRefresh) return

    isLoading.value = true
    try {
      await Promise.all([
        loadBloggerProfile(),
        loadAnnouncement(),
        loadCategoriesAndTags(),
        loadSiteInfo()
      ])
      saveCache()
    } catch (e) {
      console.error('Failed to load sidebar data:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function loadBloggerProfile() {
    try {
      const result = await getUserProfile(BLOGGER_ID)
      if (result.code === 0 && result.data) {
        bloggerProfile.value = result.data
      }
    } catch {
      // use cache or default
    }
  }

  async function loadAnnouncement() {
    try {
      const result = await getAnnouncement(BLOGGER_ID)
      if (result.code === 0 && result.data) {
        announcement.value = result.data
      }
    } catch {
      // use default
    }
  }

  async function loadCategoriesAndTags() {
    try {
      const [catResult, tagResult] = await Promise.all([
        listCategories(),
        listTags()
      ])

      if (catResult.code === 0 && catResult.data) {
        const catData = catResult.data as any
        categories.value = typeof catData[0] === 'string'
          ? catData.map((name: string) => ({ id: name, name, articleCount: 0, createdAt: '' }))
          : catData
        stats.value.categories = categories.value.length
      }

      if (tagResult.code === 0 && tagResult.data) {
        const tagData = tagResult.data as any
        tags.value = typeof tagData[0] === 'string'
          ? tagData.map((name: string) => ({ id: name, name, articleCount: 0, createdAt: '' }))
          : tagData
        stats.value.tags = tags.value.length
      }
    } catch {
      // use cache or default
    }
  }

  async function loadSiteInfo() {
    try {
      const result = await getSiteStats()
      if (result.code === 0 && result.data) {
        siteInfo.value.visitorCount = result.data.visitorCount
        siteInfo.value.viewCount = result.data.viewCount
        if (result.data.lastUpdate) {
          siteInfo.value.lastUpdate = result.data.lastUpdate
        }
      }
    } catch {
      // use cache or default
    }

    try {
      const result = await getLatestArticleTime()
      if (result.code === 0 && result.data && result.data.latestTime) {
        siteInfo.value.lastUpdate = result.data.latestTime
      }
    } catch {
      // use cache or default
    }
  }

  function updateStatsArticleCount(count: number) {
    stats.value.articles = count
    saveCache()
  }

  async function refreshLastUpdateTime() {
    try {
      const result = await getLatestArticleTime()
      if (result.code === 0 && result.data && result.data.latestTime) {
        siteInfo.value.lastUpdate = result.data.latestTime
        saveCache()
      }
    } catch {
      // ignore errors
    }
  }

  async function refreshCategoriesAndTags() {
    await loadCategoriesAndTags()
    saveCache()
  }

  return {
    bloggerProfile,
    announcement,
    categories,
    tags,
    stats,
    siteInfo,
    profile,
    lastUpdateText,
    isLoading,
    loadAllData,
    loadBloggerProfile,
    loadAnnouncement,
    loadCategoriesAndTags,
    loadSiteInfo,
    refreshLastUpdateTime,
    refreshCategoriesAndTags,
    updateStatsArticleCount,
    saveCache
  }
})