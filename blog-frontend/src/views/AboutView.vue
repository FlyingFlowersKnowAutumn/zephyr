<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getAbout, updateAbout } from '@/api/about'
import type { AboutDTO } from '@/types'
import SeoHead from '@/components/seo/SeoHead.vue'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'

const emit = defineEmits(['open-search'])

const authStore = useAuthStore()

const about = ref<AboutDTO | null>(null)
const loading = ref(false)
const error = ref('')
const editing = ref(false)
const saving = ref(false)

const form = ref({
  content: '',
  skills: '',
  socialLinks: '{}'
})

const isBlogger = computed(() => authStore.user?.role === 'blogger')
const socialLinksObj = computed(() => {
  try {
    return about.value?.socialLinks ? JSON.parse(about.value.socialLinks) : {}
  } catch {
    return {}
  }
})

onMounted(() => {
  loadAbout()
})

async function loadAbout() {
  loading.value = true
  try {
    const result = await getAbout()
    if (result.code === 0 && result.data) {
      about.value = result.data
      const links = result.data.socialLinks || '{}'
      form.value = {
        content: result.data.content || '',
        skills: (result.data.skills || []).join(', '),
        socialLinks: links
      }
    }
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

function startEdit() {
  editing.value = true
}

function cancelEdit() {
  if (about.value) {
    form.value = {
      content: about.value.content || '',
      skills: (about.value.skills || []).join(', '),
      socialLinks: about.value.socialLinks || '{}'
    }
  }
  editing.value = false
}

async function saveAbout() {
  saving.value = true
  try {
    const skillsArray = form.value.skills
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0)

    const result = await updateAbout({
      content: form.value.content || undefined,
      skills: skillsArray.length > 0 ? skillsArray : undefined,
      socialLinks: form.value.socialLinks || undefined
    })
    if (result.code === 0 && result.data) {
      about.value = result.data
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
  <LayoutWrapper :showSidebar="false" :showHero="false" @open-search="emit('open-search')">
    <SeoHead title="关于" description="了解更多关于博主的信息" />

    <div class="about-container">
      <header class="about-header">
        <h1>关于</h1>
        <button v-if="isBlogger && !editing" class="edit-btn" @click="startEdit">编辑</button>
      </header>

      <main class="about-content">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">{{ error }}</div>

        <div v-else class="about-body">
          <div v-if="editing" class="edit-form">
            <div class="form-group">
              <label>自我介绍</label>
              <textarea v-model="form.content" rows="12" placeholder="支持 Markdown 格式..."></textarea>
            </div>
            <div class="form-group">
              <label>技能标签（逗号分隔）</label>
              <input v-model="form.skills" type="text" placeholder="Java, Spring Boot, Vue.js..." />
            </div>
            <div class="form-group">
              <label>社交链接（JSON 格式）</label>
              <textarea v-model="form.socialLinks" rows="4" placeholder='{"github": "...", "twitter": "..."}'></textarea>
            </div>
            <div class="form-actions">
              <button class="save-btn" :disabled="saving" @click="saveAbout">
                {{ saving ? '保存中...' : '保存' }}
              </button>
              <button class="cancel-btn" @click="cancelEdit">取消</button>
            </div>
          </div>

          <div v-else-if="about" class="about-display">
            <section v-if="about.content" class="content-section">
              <div class="markdown-body" v-text="about.content"></div>
            </section>

            <section v-if="about.skills?.length" class="skills-section">
              <h2>技能</h2>
              <div class="skills-tags">
                <span v-for="skill in about.skills" :key="skill" class="skill-tag">{{ skill }}</span>
              </div>
            </section>

            <section v-if="Object.keys(socialLinksObj).length" class="social-section">
              <h2>社交链接</h2>
              <div class="social-links">
                <a
                  v-for="(url, platform) in socialLinksObj"
                  :key="platform"
                  :href="url"
                  target="_blank"
                  class="social-link"
                >
                  {{ platform }}
                </a>
              </div>
            </section>

            <p v-if="!about.content && !about.skills?.length && !Object.keys(socialLinksObj).length" class="empty">
              暂无介绍内容
            </p>
          </div>
        </div>
      </main>
    </div>
  </LayoutWrapper>
</template>

<style lang="scss" scoped>
.about-container {
  min-height: 100vh;
  background: var(--color-bg-primary);
}

.about-header {
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

.about-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  color: var(--color-text-secondary);
}

.about-display {
  section {
    margin-bottom: 2.5rem;

    h2 {
      font-size: 1.15rem;
      font-weight: 600;
      margin-bottom: 1rem;
      padding-bottom: 0.5rem;
      border-bottom: 1px solid var(--color-border);
    }
  }
}

.content-section {
  .markdown-body {
    color: var(--color-text-primary);
    line-height: 1.8;
    font-size: 0.95rem;
    white-space: pre-wrap;
  }
}

.skills-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;

  .skill-tag {
    padding: 0.3rem 0.85rem;
    background: rgba(88, 166, 255, 0.12);
    color: var(--color-accent);
    border-radius: 20px;
    font-size: 0.85rem;
  }
}

.social-links {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;

  .social-link {
    padding: 0.4rem 1rem;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: 8px;
    color: var(--color-text-primary);
    text-decoration: none;
    font-size: 0.9rem;
    text-transform: capitalize;
    transition: border-color 0.3s;

    &:hover { border-color: var(--color-accent); }
  }
}

.edit-form {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.25rem;

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
  .about-content { padding: 1rem; }
}
</style>
