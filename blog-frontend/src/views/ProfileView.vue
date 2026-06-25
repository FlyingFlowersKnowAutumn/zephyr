<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUserProfile, updateUserProfile } from '@/api/profile'
import type { UserProfileDTO } from '@/types'
import SeoHead from '@/components/seo/SeoHead.vue'
import Header from '@/components/layout/Header.vue'
import gsap from 'gsap'

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

const targetUserId = computed(() => {
  const id = route.params.userId as string
  return id || authStore.user?.id || ''
})
const isOwner = computed(() => {
  return targetUserId.value === authStore.user?.id
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
    gender: 'secret',
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
      avatarUrl: form.value.avatarUrl || undefined
    })
    if (result.code === 0 && result.data) {
      profile.value = result.data
    } else {
      error.value = result.message || '保存失败'
    }
  } catch {
    error.value = '保存失败'
  } finally {
    saving.value = false
  }
}

function switchTab(tab: string) {
  activeTab.value = tab
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
            <div class="info-box">
              <div class="empty-state">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <p>暂无留言与评论</p>
              </div>
            </div>
          </div>

          <div v-if="activeTab === 'profile'" class="tab-pane profile-pane">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="error" class="error">{{ error }}</div>

            <div v-else-if="profile" class="info-box">
                <div class="avatar-section">
                  <div class="avatar-wrapper">
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
                    <button type="button" class="modify-btn">修改</button>
                  </div>
                </div>

                <div class="form-row">
                  <span class="form-label">性别：</span>
                  <div class="form-value gender-row">
                    <label class="radio-item">
                      <input type="radio" v-model="form.gender" value="secret" />
                      <span class="radio-dot"></span>
                      <span>薛定谔的猫</span>
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
            <div class="info-box">
              <div class="empty-state">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <p>暂无点赞与收藏</p>
              </div>
            </div>
          </div>
        </div>
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
  padding: 30px 20px;
  width: 100%;
  flex: 1;
  overflow-y: auto;
  box-sizing: border-box;
}

.tab-pane {
  animation: fadeIn 0.3s ease;
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
  padding: 0.5rem 0;
}

.avatar-section {
  text-align: center;

  .avatar-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 1rem;
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
  }
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  max-width: 100%;
  margin: 0 auto;
  width: 100%;

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
    padding-top: 0.6rem;
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
        padding: 0.65rem 0.9rem;
        background: transparent;
        border: 1px solid transparent;
        border-radius: 8px;
        color: rgba(0, 0, 0, 0.8);
        font-size: 0.88rem;
      }

      .modify-btn {
        flex-shrink: 0;
        padding: 0.35rem 0.65rem;
        background: linear-gradient(135deg, #ffb347 0%, #ff8c00 100%);
        color: white;
        border: none;
        border-radius: 4px;
        font-size: 0.7rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 2px 8px rgba(255, 140, 0, 0.3);
        }
      }
    }

    &.gender-row {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding-top: 0.25rem;

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
          border: 2px solid rgba(255, 255, 255, 0.5);
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
</style>
