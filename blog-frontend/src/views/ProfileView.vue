<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUserProfile, updateUserProfile } from '@/api/profile'
import type { UserProfileDTO } from '@/types'
import SeoHead from '@/components/seo/SeoHead.vue'
import Header from '@/components/layout/Header.vue'

const route = useRoute()
const authStore = useAuthStore()

const profile = ref<UserProfileDTO | null>(null)
const loading = ref(false)
const error = ref('')
const editing = ref(false)
const saving = ref(false)
const form = ref({
  displayName: '',
  bio: '',
  avatarUrl: '',
  website: '',
  github: '',
  twitter: '',
  about: ''
})

const targetUserId = computed(() => {
  const id = route.params.userId as string
  return id || authStore.user?.id || ''
})
const isOwner = computed(() => {
  return targetUserId.value === authStore.user?.id
})

onMounted(() => {
  loadProfile()
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
    displayName: data.displayName || '',
    bio: data.bio || '',
    avatarUrl: data.avatarUrl || '',
    website: data.website || '',
    github: data.github || '',
    twitter: data.twitter || '',
    about: data.about || ''
  }
}

function startEdit() {
  if (profile.value) initForm(profile.value)
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function saveProfile() {
  if (!targetUserId.value) return
  saving.value = true
  try {
    const result = await updateUserProfile(targetUserId.value, {
      displayName: form.value.displayName || undefined,
      bio: form.value.bio || undefined,
      avatarUrl: form.value.avatarUrl || undefined,
      website: form.value.website || undefined,
      github: form.value.github || undefined,
      twitter: form.value.twitter || undefined,
      about: form.value.about || undefined
    })
    if (result.code === 0 && result.data) {
      profile.value = result.data
      editing.value = false
    } else {
      error.value = result.message || '保存失败'
    }
  } catch {
    error.value = '保存失败'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-container">
    <Header />
    <SeoHead
      :title="profile?.displayName || '个人中心'"
      :description="profile?.bio || '个人资料'"
    />

    <header class="profile-header">
      <h1>{{ isOwner ? '个人中心' : (profile?.displayName || '用户资料') }}</h1>
      <button v-if="isOwner && !editing" class="edit-btn" @click="startEdit">编辑资料</button>
    </header>

    <main class="profile-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>

      <div v-else-if="profile" class="profile-card">
        <div class="profile-avatar">
          <img
            :src="profile.avatarUrl || '/default-avatar.svg'"
            :alt="profile.displayName"
          />
        </div>

        <div v-if="editing" class="edit-form">
          <div class="form-group">
            <label>显示名称</label>
            <input v-model="form.displayName" type="text" />
          </div>
          <div class="form-group">
            <label>个人简介</label>
            <textarea v-model="form.bio" rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>头像 URL</label>
            <input v-model="form.avatarUrl" type="text" />
          </div>
          <div class="form-group">
            <label>个人网站</label>
            <input v-model="form.website" type="text" />
          </div>
          <div class="form-group">
            <label>GitHub</label>
            <input v-model="form.github" type="text" />
          </div>
          <div class="form-group">
            <label>Twitter</label>
            <input v-model="form.twitter" type="text" />
          </div>
          <div class="form-group">
            <label>关于我</label>
            <textarea v-model="form.about" rows="6"></textarea>
          </div>
          <div class="form-actions">
            <button class="save-btn" :disabled="saving" @click="saveProfile">
              {{ saving ? '保存中...' : '保存' }}
            </button>
            <button class="cancel-btn" @click="cancelEdit">取消</button>
          </div>
        </div>

        <div v-else class="profile-info">
          <h2 class="display-name">{{ profile.displayName || '未设置' }}</h2>
          <p v-if="profile.bio" class="bio">{{ profile.bio }}</p>

          <div v-if="profile.website || profile.github || profile.twitter" class="social-links">
            <a v-if="profile.website" :href="profile.website" target="_blank" class="social-link">🌐 网站</a>
            <a v-if="profile.github" :href="profile.github" target="_blank" class="social-link">💻 GitHub</a>
            <a v-if="profile.twitter" :href="profile.twitter" target="_blank" class="social-link">🐦 Twitter</a>
          </div>

          <div v-if="profile.about" class="about-section">
            <h3>关于我</h3>
            <p>{{ profile.about }}</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  background: var(--color-bg-primary);
}

.profile-header {
  padding: 1.5rem 2rem;
  background: rgba(22, 35, 55, 0.8);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;

  h1 { font-size: 1.25rem; font-weight: 500; }
}

.edit-btn {
  padding: 0.5rem 1.25rem;
  background: var(--color-accent);
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: opacity 0.3s;
  &:hover { opacity: 0.85; }
}

.profile-content {
  max-width: 720px;
  margin: 0 auto;
  padding: 2rem;
}

.loading, .error {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}

.profile-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 2rem;
}

.profile-avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 1.5rem;
  background: var(--color-border);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.profile-info {
  text-align: center;
}

.display-name {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.bio {
  color: var(--color-text-secondary);
  font-size: 0.95rem;
  margin-bottom: 1rem;
}

.social-links {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1.5rem;

  .social-link {
    color: var(--color-accent);
    text-decoration: none;
    font-size: 0.9rem;
    transition: opacity 0.3s;
    &:hover { opacity: 0.7; }
  }
}

.about-section {
  text-align: left;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;

  h3 { font-size: 1rem; font-weight: 500; margin-bottom: 0.5rem; }
  p { color: var(--color-text-secondary); font-size: 0.9rem; line-height: 1.6; white-space: pre-wrap; }
}

.edit-form {
  text-align: left;
}

.form-group {
  margin-bottom: 1rem;

  label {
    display: block;
    font-size: 0.85rem;
    color: var(--color-text-secondary);
    margin-bottom: 0.35rem;
  }

  input, textarea {
    width: 100%;
    padding: 0.6rem 0.75rem;
    background: var(--color-bg-primary);
    border: 1px solid var(--color-border);
    border-radius: 8px;
    color: var(--color-text-primary);
    font-size: 0.9rem;
    outline: none;
    transition: border-color 0.3s;
    box-sizing: border-box;

    &:focus { border-color: var(--color-accent); }
  }
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.5rem;

  button {
    padding: 0.6rem 1.5rem;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: opacity 0.3s;
    &:hover { opacity: 0.85; }
  }
}

.save-btn {
  background: var(--color-accent);
  color: #fff;
  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

.cancel-btn {
  background: var(--color-border);
  color: var(--color-text-primary);
}

@media (max-width: 768px) {
  .profile-content { padding: 1rem; }
}
</style>
