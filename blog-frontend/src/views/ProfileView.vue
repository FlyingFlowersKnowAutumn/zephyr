<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUserProfile, updateUserProfile } from '@/api/profile'
import { getGuestbookByUserId, deleteGuestbook, getGuestbookList } from '@/api/guestbook'
import { getCommentsByUserId, getAllComments, deleteComment } from '@/api/comment'
import { getUserReactions } from '@/api/reaction'
import { getUserFavorites } from '@/api/favorite'
import type { UserProfileDTO, GuestbookDTO, CommentDTO, UserArticleDTO } from '@/types'
import SeoHead from '@/components/seo/SeoHead.vue'
import Header from '@/components/layout/Header.vue'
import gsap from 'gsap'
import { ElMessage } from 'element-plus'

const route = useRoute()
const authStore = useAuthStore()

const backgroundImages = [
  '/Note/wallhaven-yxpdpk_2560x1440.png',
  '/Note/wallhaven-lykqqq_2560x1440.png',
  '/Note/wallhaven-8grwv1_2560x1440.png',
  '/Note/wallhaven-w5lr6x_2560x1440.png',
  '/Note/wallhaven-8gdg62_2560x1440.png',
  '/Note/wallhaven-qr3175_2560x1440.png',
  '/Note/wallhaven-jevgyp_2560x1440.png',
  '/Note/wallhaven-ml2y21_2560x1440.png',
  '/Note/wallhaven-zp5o8o_2560x1440.png',
  '/dl.png',
  '/blog.png'
]

const bgImage = ref('')

function getRandomBackground() {
  const randomIndex = Math.floor(Math.random() * backgroundImages.length)
  return backgroundImages[randomIndex]
}

const profile = ref<UserProfileDTO | null>(null)
const loading = ref(false)
const error = ref('')
const saving = ref(false)
const activeTab = ref('profile')
const form = ref({
  displayName: '',
  email: '',
  gender: 'secret' as 'male' | 'female' | 'secret',
  bio: '',
  avatarUrl: ''
})
const MAX_BIO_LENGTH = 500

const guestbooks = ref<GuestbookDTO[]>([])
const comments = ref<CommentDTO[]>([])
const guestbookLoading = ref(false)
const commentsLoading = ref(false)
const guestbookTotal = ref(0)
const commentsTotal = ref(0)

const likedArticles = ref<UserArticleDTO[]>([])
const favoritedArticles = ref<UserArticleDTO[]>([])
const likedLoading = ref(false)
const favoritedLoading = ref(false)
const likedTotal = ref(0)
const favoritedTotal = ref(0)

const avatarModalVisible = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const uploading = ref(false)

const targetUserId = computed(() => {
  const id = route.params.userId as string
  return id || authStore.user?.id || ''
})

function initMeteors() {
  const meteors = document.querySelectorAll('.meteor')
  meteors.forEach((m, index) => {
    gsap.killTweensOf(m)

    const startFromTop = Math.random() > 0.5
    let startX, startY, endX, endY

    if (startFromTop) {
      startX = Math.random() * (window.innerWidth + 200) - 100
      startY = -200
      const distance = window.innerHeight + 300
      endX = startX - distance
      endY = startY + distance
    } else {
      startX = window.innerWidth + 200
      startY = Math.random() * (window.innerHeight + 200) - 100
      const distance = window.innerWidth + 300
      endX = startX - distance
      endY = startY + distance
    }

    gsap.set(m, {
      x: startX,
      y: startY,
      rotation: 315,
      opacity: 0
    })
    gsap.to(m, {
      x: endX,
      y: endY,
      opacity: 0.7,
      duration: 5 + Math.random() * 4,
      delay: index * 0.15,
      repeat: -1,
      ease: 'linear'
    })
  })
}

onMounted(() => {
  bgImage.value = getRandomBackground()
  loadProfile()
  initMeteors()
  window.addEventListener('resize', initMeteors)

  gsap.to('.page-mask', {
    opacity: 0,
    duration: 1,
    ease: 'power2.out',
    onComplete: () => {
      const mask = document.querySelector('.page-mask')
      if (mask) {
        mask.setAttribute('style', 'pointer-events: none; opacity: 0;')
      }
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', initMeteors)
})

async function loadProfile() {
  if (!targetUserId.value) return
  loading.value = true
  error.value = ''
  try {
    const result = await getUserProfile(targetUserId.value)
    if (result.code === 0 && result.data) {
      profile.value = result.data
      initForm(result.data)
    } else {
      error.value = result.message || '加载失败'
    }
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

function initForm(data: UserProfileDTO) {
  form.value = {
    displayName: data.displayName || authStore.user?.username || '',
    email: authStore.user?.email || '',
    gender: (data.gender as 'male' | 'female' | 'secret') || 'secret',
    bio: data.bio || '',
    avatarUrl: data.avatarUrl || authStore.user?.avatarUrl || ''
  }
}

async function saveProfile() {
  if (!targetUserId.value) return
  if (form.value.bio.length > MAX_BIO_LENGTH) {
    return
  }
  saving.value = true
  try {
    const result = await updateUserProfile(targetUserId.value, {
      displayName: form.value.displayName || undefined,
      bio: form.value.bio || undefined,
      avatarUrl: form.value.avatarUrl || undefined,
      gender: form.value.gender || undefined
    })
    if (result.code === 0 && result.data) {
      profile.value = result.data
      const updated: { displayName?: string; avatarUrl?: string; bio?: string } = {}
      if (result.data.displayName) updated.displayName = result.data.displayName
      if (result.data.avatarUrl) updated.avatarUrl = result.data.avatarUrl
      if (result.data.bio !== undefined) updated.bio = result.data.bio
      authStore.updateUser(updated)
      ElMessage.success('保存成功')
    } else {
      error.value = result.message || '保存失败'
      ElMessage.error(result.message || '保存失败')
    }
  } catch {
    error.value = '保存失败'
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function openAvatarModal() {
  avatarModalVisible.value = true
}

function closeAvatarModal() {
  avatarModalVisible.value = false
  selectedFile.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

function triggerFileInput() {
  fileInputRef.value?.click()
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    validateAndSelectFile(file)
  }
}

function handleDrop(event: DragEvent) {
  const file = event.dataTransfer?.files[0]
  if (file) {
    validateAndSelectFile(file)
  }
}

function validateAndSelectFile(file: File) {
  const maxSize = 5 * 1024 * 1024
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  
  if (!validTypes.includes(file.type)) {
    alert('只支持 jpg、jpeg、png、gif、webp 格式的图片')
    return
  }
  
  if (file.size > maxSize) {
    alert('图片大小不能超过 5MB')
    return
  }
  
  selectedFile.value = file
}

async function uploadAvatar() {
  if (!selectedFile.value || !targetUserId.value) return
  
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    
    const response = await fetch(`/api/v1/users/${targetUserId.value}/avatar`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      },
      body: formData
    })
    
    const result = await response.json()
    if (result.code === 0 && result.data?.avatarUrl) {
      form.value.avatarUrl = result.data.avatarUrl
      authStore.updateUser({ avatarUrl: result.data.avatarUrl })
      closeAvatarModal()
    } else {
      alert(result.message || '上传失败')
    }
  } catch {
    alert('上传失败')
  } finally {
    uploading.value = false
  }
}

function switchTab(tab: string) {
  activeTab.value = tab
  if (tab === 'comments') {
    loadGuestbooks()
    loadComments()
  } else if (tab === 'favorites') {
    loadLikedArticles()
    loadFavoritedArticles()
  }
}

async function loadGuestbooks() {
  guestbookLoading.value = true
  try {
    const userId = targetUserId.value
    const isBlogger = authStore.user?.role === 'blogger' || authStore.user?.role === 'admin'
    
    if (isBlogger) {
      const result = await getGuestbookList({ page: 0, size: 50 })
      if (result.code === 0 && result.data) {
        guestbooks.value = result.data.content
        guestbookTotal.value = result.data.totalElements
      }
    } else {
      const result = await getGuestbookByUserId(userId, 0, 50)
      if (result.code === 0 && result.data) {
        guestbooks.value = result.data.content
        guestbookTotal.value = result.data.totalElements
      }
    }
  } catch {
    guestbooks.value = []
  } finally {
    guestbookLoading.value = false
  }
}

async function loadComments() {
  commentsLoading.value = true
  try {
    const userId = targetUserId.value
    const isBlogger = authStore.user?.role === 'blogger' || authStore.user?.role === 'admin'
    
    if (isBlogger) {
      const result = await getAllComments(0, 50)
      if (result.code === 0 && result.data) {
        comments.value = result.data.content
        commentsTotal.value = result.data.totalElements
      }
    } else {
      const result = await getCommentsByUserId(userId, 0, 50)
      if (result.code === 0 && result.data) {
        comments.value = result.data.content
        commentsTotal.value = result.data.totalElements
      }
    }
  } catch {
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

async function handleDeleteGuestbook(id: string) {
  if (!confirm('确定要删除这条留言吗？')) return
  try {
    const result = await deleteGuestbook(id)
    if (result.code === 0) {
      guestbooks.value = guestbooks.value.filter(g => g.id !== id)
      guestbookTotal.value--
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleDeleteComment(id: string) {
  if (!confirm('确定要删除这条评论吗？')) return
  try {
    const result = await deleteComment(id)
    if (result.code === 0) {
      comments.value = comments.value.filter(c => c.id !== id)
      commentsTotal.value--
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function loadLikedArticles() {
  likedLoading.value = true
  try {
    const userId = targetUserId.value
    const result = await getUserReactions(userId)
    if (result.code === 0 && result.data) {
      likedArticles.value = result.data
      likedTotal.value = result.data.length
    }
  } catch {
    likedArticles.value = []
  } finally {
    likedLoading.value = false
  }
}

async function loadFavoritedArticles() {
  favoritedLoading.value = true
  try {
    const userId = targetUserId.value
    const result = await getUserFavorites(userId)
    if (result.code === 0 && result.data) {
      favoritedArticles.value = result.data
      favoritedTotal.value = result.data.length
    }
  } catch {
    favoritedArticles.value = []
  } finally {
    favoritedLoading.value = false
  }
}
</script>

<template>
  <div class="profile-container">
    <div class="page-mask"></div>
    <div class="bg-layer" :style="{ backgroundImage: `url(${bgImage})` }"></div>

    <div class="meteors-container">
      <div v-for="i in 40" :key="i" class="meteor"></div>
    </div>

    <Header />

    <SeoHead
      :title="profile?.displayName || '个人中心'"
      :description="profile?.bio || '个人资料'"
    />

    <div class="profile-wrapper">
      <div class="tabs-card">
        <div class="tabs-row">
          <div
            class="tab-item"
            :class="{ active: activeTab === 'comments' }"
            @click="switchTab('comments')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span>留言与评论</span>
            <div class="tab-underline"></div>
          </div>

          <div
            class="tab-item"
            :class="{ active: activeTab === 'profile' }"
            @click="switchTab('profile')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span>个人信息</span>
            <div class="tab-underline"></div>
          </div>

          <div
            class="tab-item"
            :class="{ active: activeTab === 'favorites' }"
            @click="switchTab('favorites')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <span>点赞与收藏</span>
            <div class="tab-underline"></div>
          </div>
        </div>
      </div>

      <div class="profile-card">
        <div class="tab-content">
          <div v-if="activeTab === 'comments'" class="tab-pane comments-pane">
            <div class="comments-layout">
              <div class="comments-left">
                <div class="section-header">
                  <h3>留言</h3>
                  <span class="section-count">{{ guestbookTotal }}</span>
                </div>
                
                <div v-if="guestbookLoading" class="section-loading">加载中...</div>
                
                <div v-else-if="guestbooks.length === 0" class="section-empty">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  <p>暂无留言</p>
                </div>
                
                <div v-else class="guestbook-list">
                  <div
                    v-for="guestbook in guestbooks"
                    :key="guestbook.id"
                    class="guestbook-item"
                  >
                    <div class="guestbook-content">{{ guestbook.content }}</div>
                    <div class="guestbook-meta">
                      <span class="guestbook-date">{{ formatDate(guestbook.createdAt) }}</span>
                      <button class="delete-btn" @click="handleDeleteGuestbook(guestbook.id)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="comments-right">
                <div class="section-header">
                  <h3>评论</h3>
                  <span class="section-count">{{ commentsTotal }}</span>
                </div>
                
                <div v-if="commentsLoading" class="section-loading">加载中...</div>
                
                <div v-else-if="comments.length === 0" class="section-empty">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                  </svg>
                  <p>暂无评论</p>
                </div>
                
                <div v-else class="comment-list">
                  <div
                    v-for="comment in comments"
                    :key="comment.id"
                    class="comment-item"
                  >
                    <RouterLink 
                      v-if="comment.articleTitle"
                      :to="`/articles/${comment.articleSlug || comment.articleId}`"
                      class="article-link"
                    >
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                        <polyline points="14 2 14 8 20 8"/>
                      </svg>
                      {{ comment.articleTitle }}
                    </RouterLink>
                    <div class="comment-content">{{ comment.content }}</div>
                    <div class="comment-meta">
                      <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                      <button class="delete-btn" @click="handleDeleteComment(comment.id)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="activeTab === 'profile'" class="tab-pane profile-pane">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="error" class="error">{{ error }}</div>

            <div v-else-if="profile" class="info-box">
                <div class="avatar-section">
                  <div class="avatar-wrapper cursor-pointer" @click="openAvatarModal">
                    <img
                      v-if="form.avatarUrl"
                      :src="form.avatarUrl"
                      :alt="form.displayName"
                      class="avatar"
                    />
                    <div v-else class="avatar-placeholder">
                      {{ (form.displayName || 'U').charAt(0).toUpperCase() }}
                    </div>
                  </div>
                </div>

                <div class="edit-form">
                <div class="form-row">
                  <span class="form-label">用户名：</span>
                  <div class="form-value">
                    <input v-model="form.displayName" type="text" placeholder="请输入用户名" />
                  </div>
                </div>

                <div class="form-row">
                  <span class="form-label">邮箱：</span>
                  <div class="form-value email-row">
                    <span>{{ form.email }}</span>
                  </div>
                </div>

                <div class="form-row">
                  <span class="form-label">性别：</span>
                  <div class="form-value gender-row">
                    <label class="radio-item">
                      <input type="radio" v-model="form.gender" value="secret" />
                      <span class="radio-dot"></span>
                      <span>量子叠加态</span>
                    </label>
                    <label class="radio-item">
                      <input type="radio" v-model="form.gender" value="male" />
                      <span class="radio-dot"></span>
                      <span>男</span>
                    </label>
                    <label class="radio-item">
                      <input type="radio" v-model="form.gender" value="female" />
                      <span class="radio-dot"></span>
                      <span>女</span>
                    </label>
                  </div>
                </div>

                <div class="form-row">
                  <span class="form-label">简介：</span>
                  <div class="form-value bio-row">
                    <textarea
                      v-model="form.bio"
                      :maxlength="MAX_BIO_LENGTH"
                      rows="3"
                      placeholder="介绍一下自己吧"
                    ></textarea>
                    <span class="char-count">{{ form.bio.length }}/{{ MAX_BIO_LENGTH }}</span>
                  </div>
                </div>

                <div class="form-actions">
                  <button class="save-btn" :disabled="saving" @click="saveProfile">
                    <span class="btn-text">{{ saving ? '保存中...' : '提交' }}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="activeTab === 'favorites'" class="tab-pane favorites-pane">
            <div class="favorites-layout">
              <div class="favorites-left">
                <div class="section-header">
                  <h3>点赞</h3>
                  <span class="section-count">{{ likedTotal }}</span>
                </div>
                
                <div v-if="likedLoading" class="section-loading">加载中...</div>
                
                <div v-else-if="likedArticles.length === 0" class="section-empty">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                  <p>暂无点赞文章</p>
                </div>
                
                <div v-else class="article-list">
                  <RouterLink
                    v-for="article in likedArticles"
                    :key="article.id"
                    :to="`/articles/${article.articleSlug || article.articleId}`"
                    class="article-card"
                  >
                    <div class="article-card-content">
                      <h4 class="article-card-title">{{ article.articleTitle || '未命名文章' }}</h4>
                      <p class="article-card-date">{{ formatDate(article.createdAt) }}</p>
                    </div>
                    <div class="article-card-arrow">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 18l6-6-6-6"/>
                      </svg>
                    </div>
                  </RouterLink>
                </div>
              </div>
              
              <div class="divider-line"></div>
              
              <div class="favorites-right">
                <div class="section-header">
                  <h3>收藏</h3>
                  <span class="section-count">{{ favoritedTotal }}</span>
                </div>
                
                <div v-if="favoritedLoading" class="section-loading">加载中...</div>
                
                <div v-else-if="favoritedArticles.length === 0" class="section-empty">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                  <p>暂无收藏文章</p>
                </div>
                
                <div v-else class="article-list">
                  <RouterLink
                    v-for="article in favoritedArticles"
                    :key="article.id"
                    :to="`/articles/${article.articleSlug || article.articleId}`"
                    class="article-card"
                  >
                    <div class="article-card-content">
                      <h4 class="article-card-title">{{ article.articleTitle || '未命名文章' }}</h4>
                      <p class="article-card-date">{{ formatDate(article.createdAt) }}</p>
                    </div>
                    <div class="article-card-arrow">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 18l6-6-6-6"/>
                      </svg>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-if="avatarModalVisible" class="avatar-modal-overlay" @click="closeAvatarModal">
    <div class="avatar-modal" @click.stop>
      <div class="avatar-modal-header">
        <span class="modal-title">修改头像</span>
        <button class="modal-close" @click="closeAvatarModal">×</button>
      </div>
      <div class="avatar-modal-body">
        <div
          class="upload-area"
          :class="{ 'has-file': selectedFile }"
          @click="triggerFileInput"
          @dragover.prevent
          @drop.prevent="handleDrop"
        >
          <template v-if="selectedFile">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span class="file-name">{{ selectedFile.name }}</span>
            <span class="file-size">{{ (selectedFile.size / 1024).toFixed(1) }} KB</span>
          </template>
          <template v-else>
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
            <span>拖拽上传 / 点击上传</span>
          </template>
        </div>
        <p class="upload-tip">一次最多上传1张图片，且每张图片不超过2M！</p>
        <input 
          ref="fileInputRef"
          type="file" 
          accept="image/jpeg,image/png,image/gif,image/webp" 
          class="file-input"
          @change="handleFileSelect"
        />
      </div>
      <div class="avatar-modal-footer">
        <button class="upload-btn" :disabled="uploading || !selectedFile" @click="uploadAvatar">
          {{ uploading ? '上传中...' : '上传' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.page-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 9999;
  opacity: 1;
  pointer-events: none;
}

.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: -2;
}

.meteors-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  overflow: hidden;
  z-index: -1;
}

.meteor {
  position: absolute;
  width: 150px;
  height: 2px;
  background: linear-gradient(315deg, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.9));
  border-radius: 50%;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.7);
  opacity: 0.7;
}

.profile-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-height: 100vh;
  padding: 60px 0 40px;
  box-sizing: border-box;
}

.tabs-card {
  width: 100%;
  max-width: 1200px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
  padding: 1rem 0;
}

.profile-card {
  width: 100%;
  max-width: 1200px;
  min-height: calc(100vh - 200px);
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tabs-row {
  display: flex;
  justify-content: space-between;
  padding: 0 100px;
  position: relative;
}

.tab-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0.25rem 0.5rem;
  font-size: 0.85rem;
  color: rgba(0, 0, 0, 0.5);
  cursor: pointer;
  transition: color 0.3s ease;

  svg {
    width: 15px;
    height: 15px;
  }

  .tab-underline {
    position: absolute;
    bottom: -0.75rem;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 3px;
    background: linear-gradient(90deg, #ff6b9d, #e91e63);
    border-radius: 2px;
    transition: width 0.3s ease;
  }

  &:hover {
    color: rgba(0, 0, 0, 0.75);
  }

  &.active {
    color: rgba(0, 0, 0, 0.85);
    font-weight: 600;

    .tab-underline {
      width: 60px;
    }
  }
}

.tab-content {
  padding: 15px 20px;
  width: 100%;
  flex: 1;
  overflow-y: auto;
  box-sizing: border-box;
}

.tab-pane {
  animation: fadeIn 0.3s ease;
}

.profile-pane {
  padding-top: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: #999;

  svg {
    margin-bottom: 1rem;
    opacity: 0.5;
  }

  p {
    font-size: 0.9rem;
  }
}

.info-box {
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  padding: 0;
}

.avatar-section {
  text-align: center;
  margin-bottom: 0.5rem;
  padding-top: 0;
  padding-bottom: 0;

  .avatar-wrapper {
    position: relative;
    display: inline-block;

    .avatar,
    .avatar-placeholder {
      transition: transform 0.3s ease;
    }

    &:hover .avatar,
    &:hover .avatar-placeholder {
      transform: rotate(360deg);
    }
  }

  .avatar {
    width: 70px;
    height: 70px;
    border-radius: 50%;
    object-fit: cover;
  }

  .avatar-placeholder {
    width: 70px;
    height: 70px;
    border-radius: 50%;
    background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.3rem;
    font-weight: 600;
    padding-top: 0;
    padding-bottom: 0;
  }
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  max-width: 400px;
  margin: 0 auto;
  width: 100%;
  padding-top: 10px;
  padding-bottom: 10px;

  .form-row {
    display: flex;
    align-items: flex-start;
    gap: 0.85rem;
  }

  .form-label {
    flex-shrink: 0;
    width: 65px;
    text-align: right;
    font-size: 0.88rem;
    color: rgba(0, 0, 0, 0.7);
    padding-top: 9px;
    padding-bottom: 9px;
  }

  .form-value {
    flex: 1;

    input[type="text"] {
      width: 100%;
      padding: 0.65rem 0.9rem;
      background: rgba(255, 255, 255, 0.35);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: 8px;
      color: rgba(0, 0, 0, 0.8);
      font-size: 0.88rem;
      outline: none;
      transition: all 0.2s ease;
      box-sizing: border-box;
      font-family: inherit;

      &:focus {
        border-color: rgba(255, 255, 255, 0.5);
        background: rgba(255, 255, 255, 0.45);
      }

      &::placeholder {
        color: rgba(0, 0, 0, 0.35);
      }
    }

    &.readonly {
      padding: 0.65rem 0.9rem;
      background: rgba(255, 255, 255, 0.35);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: 8px;
      color: rgba(0, 0, 0, 0.6);
      font-size: 0.88rem;
    }

    &.email-row {
      display: flex;
      align-items: center;
      gap: 0.6rem;

      span {
        flex: 1;
        padding: 0.65rem 0;
        background: transparent;
        border: 1px solid transparent;
        border-radius: 8px;
        color: rgba(0, 0, 0, 0.8);
        font-size: 0.88rem;
      }
    }

    &.gender-row {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding-top: 10px;
      padding-bottom: 10px;

      .radio-item {
        display: flex;
        align-items: center;
        gap: 5px;
        cursor: pointer;
        font-size: 0.78rem;
        color: rgba(0, 0, 0, 0.7);

        input[type="radio"] {
          display: none;
        }

        .radio-dot {
          width: 14px;
          height: 14px;
          border-radius: 50%;
          background: white;
          border: 2px solid white;
          position: relative;
          transition: all 0.2s ease;

          &::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%) scale(0);
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: linear-gradient(135deg, #4a90d9 0%, #357abd 100%);
            transition: transform 0.2s ease;
          }
        }

        input[type="radio"]:checked + .radio-dot {
          border-color: #4a90d9;
          background: white;

          &::before {
            transform: translate(-50%, -50%) scale(1);
          }
        }
      }
    }

    &.bio-row {
      position: relative;

      textarea {
        width: 100%;
        padding: 0.65rem 0.9rem;
        background: rgba(255, 255, 255, 0.35);
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 8px;
        color: rgba(0, 0, 0, 0.8);
        font-size: 0.88rem;
        outline: none;
        transition: all 0.2s ease;
        box-sizing: border-box;
        font-family: inherit;
        resize: vertical;
        min-height: 85px;

        &:focus {
          border-color: rgba(255, 255, 255, 0.5);
          background: rgba(255, 255, 255, 0.45);
        }

        &::placeholder {
          color: rgba(0, 0, 0, 0.35);
        }
      }

      .char-count {
        position: absolute;
        bottom: 8px;
        right: 12px;
        font-size: 0.7rem;
        color: rgba(0, 0, 0, 0.4);
      }
    }
  }

  .form-actions {
    display: flex;
    gap: 0.75rem;
    margin-top: 1.25rem;
    justify-content: center;

    button {
      padding: 0.65rem 3rem;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.82rem;
      font-weight: 500;
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;

      .btn-text {
        position: relative;
        z-index: 1;
      }

      &:hover {
        transform: translateY(-1px);
      }
    }
  }

  .save-btn {
    background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
    color: white;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -120%;
      width: 220%;
      height: 100%;
      background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
      transform: skewX(-45deg) translateX(-100%);
      transition: transform 0.4s ease;
      z-index: 0;
    }

    &:hover::before {
      transform: skewX(-45deg) translateX(45%);
    }

    &:hover {
      box-shadow: 0 4px 15px rgba(231, 76, 60, 0.3);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;

      &::before {
        transform: skewX(-45deg) translateX(-100%);
      }
    }
  }

  .cancel-btn {
    background: rgba(0, 0, 0, 0.06);
    color: #666;

    &:hover {
      background: rgba(0, 0, 0, 0.1);
    }
  }
}

.loading,
.error {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .profile-wrapper {
    padding: 70px 1rem 1rem;
  }

  .tab-item span {
    display: none;
  }

  .tab-item {
    padding: 0.5rem;
  }

  .tab-content {
    padding: 1.5rem 1rem;
  }
}

.comments-layout {
  display: flex;
  gap: 2rem;
  height: calc(100vh - 300px);

  .comments-left,
  .comments-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    overflow-y: auto;
  }

  .divider-line {
    width: 1px;
    background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.05) 50%, rgba(0, 0, 0, 0.1) 100%);
    margin: 1rem 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  flex-shrink: 0;

  h3 {
    font-size: 0.95rem;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    margin: 0;
  }

  .section-count {
    font-size: 0.75rem;
    color: rgba(0, 0, 0, 0.4);
    background: rgba(0, 0, 0, 0.05);
    padding: 2px 8px;
    border-radius: 10px;
  }
}

.section-loading,
.section-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: rgba(0, 0, 0, 0.4);
  font-size: 0.85rem;

  svg {
    opacity: 0.5;
    margin-bottom: 0.5rem;
  }
}

.guestbook-list,
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.guestbook-item,
.comment-item {
  background: rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  padding: 1rem;
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    background: rgba(255, 255, 255, 0.6);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
}

.guestbook-content,
.comment-content {
  font-size: 0.85rem;
  color: rgba(0, 0, 0, 0.7);
  line-height: 1.6;
  margin-bottom: 0.75rem;
  word-break: break-word;
}

.guestbook-meta,
.comment-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.guestbook-date,
.comment-date {
  font-size: 0.72rem;
  color: rgba(0, 0, 0, 0.35);
}

.delete-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
  }
}

.article-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.78rem;
  color: #4a90d9;
  text-decoration: none;
  margin-bottom: 0.5rem;
  padding: 4px 8px;
  background: rgba(74, 144, 217, 0.08);
  border-radius: 4px;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(74, 144, 217, 0.15);
    color: #357abd;
  }

  svg {
    flex-shrink: 0;
  }
}

@media (max-width: 768px) {
  .comments-layout {
    flex-direction: column;
    height: auto;

    .divider-line {
      width: 100%;
      height: 1px;
      margin: 1rem 0;
    }

    .comments-left,
    .comments-right {
      overflow-y: visible;
    }
  }
}

.avatar-section {
  text-align: center;

  .avatar-wrapper {
    margin: 0 auto;
  }

  .cursor-pointer {
    cursor: pointer;
  }

  .avatar-hint {
    display: block;
    font-size: 0.75rem;
    color: rgba(0, 0, 0, 0.4);
    margin-top: 0.5rem;
  }
}

.avatar-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.avatar-modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.avatar-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;

  .modal-title {
    font-size: 1rem;
    font-weight: 600;
    color: #333;
  }

  .modal-close {
    width: 28px;
    height: 28px;
    border: none;
    background: transparent;
    font-size: 1.5rem;
    color: #999;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      color: #666;
    }
  }
}

.avatar-modal-body {
  padding: 20px;

  .upload-area {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    padding: 30px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    color: #999;
    transition: all 0.3s ease;

    svg {
      color: #999;
    }

    span {
      font-size: 0.85rem;
    }

    &:hover {
      border-color: #4a90d9;
      color: #4a90d9;

      svg {
        color: #4a90d9;
      }
    }

    &.has-file {
      border-color: #52c41a;
      border-style: solid;
      background: #f6ffed;
      color: #52c41a;

      svg {
        color: #52c41a;
      }

      span {
        color: #333;
      }
    }
  }

  .file-name {
    font-weight: 500;
    word-break: break-all;
    text-align: center;
  }

  .file-size {
    font-size: 0.75rem;
    color: #999;
  }

  .upload-tip {
    text-align: center;
    font-size: 0.75rem;
    color: #999;
    margin-top: 12px;
  }

  .file-input {
    display: none;
  }
}

.avatar-modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: center;

  .upload-btn {
    padding: 10px 40px;
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

.favorites-layout {
  display: flex;
  gap: 2rem;
  height: calc(100vh - 300px);

  .favorites-left,
  .favorites-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    overflow-y: auto;
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.article-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 1rem 1.25rem;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(255, 255, 255, 0.5);
    border-color: rgba(255, 255, 255, 0.4);
    transform: translateX(8px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  .article-card-content {
    flex: 1;
    min-width: 0;
  }

  .article-card-title {
    font-size: 0.9rem;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.8);
    margin: 0 0 4px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: color 0.3s ease;
  }

  .article-card-date {
    font-size: 0.72rem;
    color: rgba(0, 0, 0, 0.35);
    margin: 0;
  }

  .article-card-arrow {
    flex-shrink: 0;
    margin-left: 1rem;
    color: rgba(0, 0, 0, 0.3);
    transition: all 0.3s ease;
  }

  &:hover {
    .article-card-title {
      color: #4a90d9;
    }

    .article-card-arrow {
      color: #4a90d9;
      transform: translateX(4px);
    }
  }
}

@media (max-width: 768px) {
  .favorites-layout {
    flex-direction: column;
    height: auto;

    .favorites-left,
    .favorites-right {
      overflow-y: visible;
    }
  }
}
</style>
