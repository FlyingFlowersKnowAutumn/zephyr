<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import type { CreateArticleDTO, UpdateArticleDTO } from '@/types'
import Header from '@/components/layout/Header.vue'

const router = useRouter()
const route = useRoute()
const articleStore = useArticleStore()

const isEdit = computed(() => !!route.params.slug)
const articleId = ref<string | null>(null)

const contentWordCount = computed(() => content.value.length)

const title = ref('')
const content = ref('')
const excerpt = ref('')
const coverImage = ref('')
const status = ref<'DRAFT' | 'PUBLISHED'>('DRAFT')
const selectedCategory = ref('')
const tagInput = ref('')
const selectedTags = ref<string[]>([])
const isSubmitting = ref(false)
const error = ref('')

const outline = ref<{ level: number; text: string; lineIndex: number }[]>([])
const meteorCanvas = ref<HTMLCanvasElement | null>(null)

interface Meteor {
  x: number
  y: number
  length: number
  speed: number
  size: number
  opacity: number
  angle: number
}

let meteors: Meteor[] = []
let animationId: number | null = null

function initMeteors(width: number, height: number) {
  meteors = []
  const count = Math.min(Math.floor(width / 80), 30)
  
  for (let i = 0; i < count; i++) {
    meteors.push(createMeteor(width, height, true))
  }
}

function createMeteor(width: number, height: number, randomY = false): Meteor {
  const depth = Math.random()
  const size = depth * 1.5 + 0.5
  const baseSpeed = 0.8
  const speed = baseSpeed * (depth * 1.5 + 0.3)
  
  return {
    x: Math.random() * width + width * 0.1,
    y: randomY ? Math.random() * height * 0.8 : -20,
    length: depth * 120 + 60,
    speed,
    size,
    opacity: depth * 0.6 + 0.2,
    angle: Math.PI / 4 + (Math.random() - 0.5) * 0.15
  }
}

function updateMeteors(width: number, height: number) {
  meteors.forEach((meteor, index) => {
    meteor.x -= Math.cos(meteor.angle) * meteor.speed
    meteor.y += Math.sin(meteor.angle) * meteor.speed
    
    if (meteor.x < -100 || meteor.y > height + 100) {
      meteors[index] = createMeteor(width, height)
    }
  })
}

function drawMeteors(ctx: CanvasRenderingContext2D) {
  ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
  
  meteors.forEach(meteor => {
    ctx.beginPath()
    ctx.moveTo(meteor.x, meteor.y)
    ctx.lineTo(
      meteor.x + Math.cos(meteor.angle) * meteor.length,
      meteor.y - Math.sin(meteor.angle) * meteor.length
    )
    
    const gradient = ctx.createLinearGradient(
      meteor.x, meteor.y,
      meteor.x + Math.cos(meteor.angle) * meteor.length,
      meteor.y - Math.sin(meteor.angle) * meteor.length
    )
    gradient.addColorStop(0, `rgba(255, 255, 255, ${meteor.opacity})`)
    gradient.addColorStop(1, 'rgba(255, 255, 255, 0)')
    
    ctx.strokeStyle = gradient
    ctx.lineWidth = meteor.size
    ctx.lineCap = 'round'
    ctx.stroke()
    
    ctx.beginPath()
    ctx.arc(meteor.x, meteor.y, meteor.size / 2, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255, 255, 255, ${meteor.opacity})`
    ctx.shadowColor = '#fff'
    ctx.shadowBlur = meteor.size * 2
    ctx.fill()
    ctx.shadowBlur = 0
  })
}

function animate() {
  const canvas = meteorCanvas.value
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  updateMeteors(canvas.width, canvas.height)
  drawMeteors(ctx)
  
  animationId = requestAnimationFrame(animate)
}

function resizeCanvas() {
  const canvas = meteorCanvas.value
  if (!canvas) return
  
  const heroHeight = Math.max(window.innerHeight * 0.333, 250)
  canvas.width = window.innerWidth
  canvas.height = heroHeight
  
  initMeteors(canvas.width, canvas.height)
}

function generateOutline() {
  const lines = content.value.split('\n')
  const result: { level: number; text: string; lineIndex: number }[] = []
  
  lines.forEach((line, index) => {
    const match = line.match(/^(#{1,3})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      result.push({
        level,
        text,
        lineIndex: index
      })
    }
  })
  
  outline.value = result
}

watch(content, () => {
  generateOutline()
})

onMounted(async () => {
  await articleStore.fetchCategories()
  await articleStore.fetchTags()
  
  if (isEdit.value) {
    await loadArticle()
  }
  generateOutline()
  
  setTimeout(() => {
    resizeCanvas()
    animate()
    window.addEventListener('resize', resizeCanvas)
  }, 100)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', resizeCanvas)
})

async function loadArticle() {
  const slug = route.params.slug as string
  const article = await articleStore.fetchArticle(slug)
  if (article) {
    articleId.value = article.id
    title.value = article.title
    content.value = article.content
    excerpt.value = article.excerpt || ''
    coverImage.value = article.coverImage || ''
    status.value = article.status as 'DRAFT' | 'PUBLISHED'
    selectedCategory.value = article.categoryName || ''
    selectedTags.value = article.tags || []
  }
}

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
    tagInput.value = ''
  }
}

function removeTag(tag: string) {
  selectedTags.value = selectedTags.value.filter(t => t !== tag)
}

async function handleSubmit(isDraft = false) {
  if (!title.value.trim()) {
    error.value = '请输入文章标题'
    return
  }
  if (!content.value.trim()) {
    error.value = '请输入文章内容'
    return
  }

  isSubmitting.value = true
  error.value = ''
  status.value = isDraft ? 'DRAFT' : 'PUBLISHED'

  try {
    let result = null
    
    if (isEdit.value && articleId.value) {
      const dto: UpdateArticleDTO = {
        title: title.value.trim(),
        content: content.value.trim(),
        excerpt: excerpt.value.trim() || undefined,
        coverImage: coverImage.value.trim() || undefined,
        status: status.value,
        categoryId: selectedCategory.value || undefined,
        tags: selectedTags.value
      }
      result = await articleStore.modifyArticle(articleId.value, dto)
    } else {
      const dto: CreateArticleDTO = {
        title: title.value.trim(),
        content: content.value.trim(),
        excerpt: excerpt.value.trim() || undefined,
        coverImage: coverImage.value.trim() || undefined,
        status: status.value,
        categoryId: selectedCategory.value || undefined,
        tags: selectedTags.value
      }
      result = await articleStore.saveArticle(dto)
    }
    
    if (result) {
      router.push(`/articles/${result.slug || result.id}`)
    } else {
      error.value = articleStore.error || (isEdit.value ? '更新失败' : '发布失败')
    }
  } catch (e) {
    error.value = `${isEdit.value ? '更新' : '发布'}失败，请稍后重试`
    console.error(e)
  } finally {
    isSubmitting.value = false
  }
}

function goToHeading(lineIndex: number) {
  const textarea = document.querySelector('.content-textarea') as HTMLTextAreaElement
  if (textarea) {
    const lines = content.value.split('\n')
    let charCount = 0
    for (let i = 0; i < lineIndex; i++) {
      charCount += lines[i].length + 1
    }
    textarea.selectionStart = charCount
    textarea.selectionEnd = charCount
    textarea.focus()
    textarea.scrollTop = (lineIndex - 5) * 24
  }
}

const fileInput = ref<HTMLInputElement | null>(null)

function importMd() {
  fileInput.value?.click()
}

function handleFileImport(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    const text = e.target?.result as string
    if (text) {
      content.value = text
      // 自动提取标题
      const titleMatch = text.match(/^#\s+(.+)$/m)
      if (titleMatch && !title.value) {
        title.value = titleMatch[1].replace(/[`*_]/g, '')
      }
    }
  }
  reader.readAsText(file)
  target.value = ''
}

function exportMd() {
  const blob = new Blob([content.value], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${title.value || '文章'}.md`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
</script>

<template>
  <div class="write-container">
    <Header />

    <div class="write-hero">
      <div class="hero-bg" :style="{ backgroundImage: 'url(/Note/wallhaven-w5lr6x_2560x1440.png)' }"></div>
      <canvas ref="meteorCanvas" class="meteor-canvas"></canvas>
      <div class="hero-content">
        <h1 class="hero-title">{{ isEdit ? '编辑文章' : '写文章' }}</h1>
      </div>
    </div>

    <div class="write-body">
      <aside class="write-sidebar">
        <div class="sidebar-outline">
          <h3 class="outline-title">目录</h3>
          <ul v-if="outline.length > 0" class="outline-list">
            <li 
              v-for="(item, index) in outline" 
              :key="index" 
              class="outline-item"
              :class="'level-' + item.level"
              @click="goToHeading(item.lineIndex)"
            >
              {{ item.text }}
            </li>
          </ul>
          <p v-else class="outline-empty">暂无目录</p>
        </div>
      </aside>

      <main class="write-main">
        <div class="editor-section">
          <div class="title-area">
            <input 
              v-model="title"
              type="text"
              placeholder="请输入文章标题（1-100字）"
              class="title-input"
            />
            <span class="char-count">{{ title.length }}/100</span>
          </div>

          <div class="content-area">
            <textarea 
              v-model="content"
              placeholder="开始撰写你的文章..."
              rows="25"
              class="content-textarea"
            ></textarea>
          </div>
        </div>

        <div class="form-section">
          <div class="form-group">
            <label>封面图片</label>
            <div class="cover-upload">
              <input 
                v-model="coverImage"
                type="text"
                placeholder="请输入图片URL或上传图片"
                class="cover-input"
              />
              <button class="upload-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="17 8 12 3 7 8"/>
                  <line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
                <span>上传图片</span>
              </button>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group flex-1">
              <label>分类</label>
              <select v-model="selectedCategory" class="category-select">
                <option value="">请选择分类</option>
                <option v-for="cat in articleStore.categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>
            <div class="form-group flex-1">
              <label>标签</label>
              <input 
                v-model="tagInput"
                type="text"
                placeholder="输入标签后回车添加"
                @keyup.enter.prevent="addTag"
                class="tag-input"
              />
            </div>
          </div>

          <div v-if="selectedTags.length > 0" class="selected-tags">
            <span 
              v-for="tag in selectedTags" 
              :key="tag" 
              class="tag-item"
            >
              {{ tag }}
              <button class="tag-remove" @click="removeTag(tag)">×</button>
            </span>
          </div>

          <div class="form-group">
            <label>摘要</label>
            <textarea 
              v-model="excerpt"
              placeholder="请输入文章摘要（可选）"
              rows="3"
              class="excerpt-textarea"
            ></textarea>
          </div>

          <p v-if="error" class="error-message">{{ error }}</p>
        </div>
      </main>

      <aside class="write-toolbar">
        <div class="toolbar-group">
          <button class="toolbar-btn active" title="Markdown" disabled>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
          </button>
        </div>
        <div class="toolbar-divider"></div>
        <div class="toolbar-group">
          <button class="toolbar-btn" title="导入Markdown" @click="importMd">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
          </button>
          <button class="toolbar-btn" title="导出Markdown" @click="exportMd">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
          </button>
        </div>
        <div class="toolbar-divider"></div>
        <div class="toolbar-group">
          <button class="toolbar-btn" title="预览">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
              <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
            </svg>
          </button>
        </div>
        <input ref="fileInput" type="file" accept=".md,.txt" style="display: none" @change="handleFileImport" />
      </aside>
    </div>

    <footer class="write-footer">
      <div class="footer-inner">
        <nav class="footer-nav">
          <RouterLink to="/write" class="footer-nav-item" :class="{ active: !isEdit }">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span>写文章</span>
          </RouterLink>
          <RouterLink to="/articles/manage" class="footer-nav-item">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 11H5"/>
              <path d="M12 19c-3.866 0-7-3.134-7-7s3.134-7 7-7 7 3.134 7 7-3.134 7-7 7z"/>
            </svg>
            <span>草稿箱</span>
          </RouterLink>
          <RouterLink to="/articles/manage" class="footer-nav-item">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 19v-6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2z"/>
              <path d="M19 19v-6a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2z"/>
              <line x1="12" y1="5" x2="12" y2="19"/>
            </svg>
            <span>文章管理</span>
          </RouterLink>
        </nav>
        <div class="footer-divider"></div>
        <div class="footer-actions">
          <button 
            class="footer-btn btn-draft" 
            :disabled="isSubmitting"
            @click="handleSubmit(true)"
          >
            {{ isSubmitting ? '保存中...' : '保存草稿' }}
          </button>
          <button 
            class="footer-btn btn-publish" 
            :disabled="isSubmitting"
            @click="handleSubmit(false)"
          >
            {{ isSubmitting ? (isEdit ? '更新中...' : '发布中...') : (isEdit ? '更新文章' : '发布文章') }}
          </button>
          <span class="word-count">共{{ contentWordCount }}字</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.write-container {
  min-height: 100vh;
  background: #f6f7f8;
  display: flex;
  flex-direction: column;
}

.write-hero {
  position: relative;
  height: 33.33vh;
  min-height: 250px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  filter: brightness(0.6);
  z-index: 0;
}

.meteor-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;
  padding: 2rem;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}

.write-body {
  flex: 1;
  display: flex;
  gap: 0;
  padding-left: 10px;
  padding-right: 10px;
  padding-top: 10px;
  padding-bottom: 10px;
  margin-left: 0;
  margin-right: 0;
}

.write-sidebar {
  width: 220px;
  background: #ffffff;
  border-right: 1px solid #e8e8e8;
  padding-left: 24px;
  padding-right: 24px;
  padding-top: 32px;
  padding-bottom: 32px;
  position: sticky;
  top: 0;
  align-self: flex-start;
  height: calc(100vh - 250px - 80px);
  overflow-y: auto;
  margin-top: 20px;
  margin-bottom: 20px;
  margin-left: 0;
  margin-right: 0;
  border-radius: 8px;
}

.sidebar-outline {
  padding: 0 1rem;
}

.outline-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.75rem;
}

.outline-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.outline-item {
  padding: 0.5rem 0.75rem;
  color: #666;
  font-size: 0.8rem;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: #f5f5f5;
    color: #1a1a1a;
  }

  &.level-1 {
    padding-left: 0.75rem;
    font-weight: 500;
    color: #333;
  }

  &.level-2 {
    padding-left: 1.75rem;
  }

  &.level-3 {
    padding-left: 2.75rem;
    font-size: 0.75rem;
    color: #999;
  }
}

.outline-empty {
  font-size: 0.8rem;
  color: #999;
  text-align: center;
  padding: 1rem 0;
}

.write-main {
  flex: 1;
  padding: 2rem;
  padding-top: 20px;
  padding-bottom: 20px;
  padding-left: 24px;
  padding-right: 24px;
  margin-top: 0;
  margin-bottom: 0;
}

.editor-section {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.title-area {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.title-input {
  flex: 1;
  font-size: 1.5rem;
  font-weight: 600;
  border: none;
  outline: none;
  color: #1a1a1a;
  background: transparent;

  &::placeholder {
    color: #ccc;
  }
}

.char-count {
  font-size: 0.75rem;
  color: #999;
}

.content-area {
  border-top: 1px solid #e8e8e8;
  padding-top: 1.5rem;
}

.content-textarea {
  width: 100%;
  min-height: 400px;
  border: none;
  outline: none;
  font-size: 1rem;
  line-height: 1.8;
  color: #333;
  resize: vertical;
  font-family: inherit;
  background: transparent;

  &::placeholder {
    color: #ccc;
  }
}

.form-section {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 1.5rem;
  margin-left: 0;
  margin-right: 0;
  margin-top: -2px;
  margin-bottom: -2px;
}

.form-group {
  margin-bottom: 1.5rem;

  label {
    display: block;
    font-size: 0.875rem;
    font-weight: 500;
    color: #333;
    margin-bottom: 0.5rem;
  }

  input, select, textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #e8e8e8;
    border-radius: 6px;
    font-size: 0.875rem;
    color: #333;
    transition: border-color 0.2s;
    background: #ffffff;

    &:focus {
      outline: none;
      border-color: var(--color-accent);
    }

    &::placeholder {
      color: #ccc;
    }
  }
}

.form-row {
  display: flex;
  gap: 1rem;
}

.flex-1 {
  flex: 1;
}

.cover-upload {
  display: flex;
  gap: 0.75rem;
}

.cover-input {
  flex: 1;
}

.upload-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px dashed #ccc;
  border-radius: 6px;
  background: #fafafa;
  color: #666;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--color-accent);
    color: var(--color-accent);
    background: rgba(0, 212, 255, 0.05);
  }
}

.category-select {
  max-width: 200px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid var(--color-accent);
  border-radius: 20px;
  color: var(--color-accent);
  font-size: 0.8rem;
}

.tag-remove {
  background: none;
  border: none;
  color: var(--color-accent);
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;

  &:hover {
    color: #ef4444;
  }
}

.excerpt-textarea {
  resize: vertical;
}

.error-message {
  color: #ef4444;
  font-size: 0.875rem;
  text-align: center;
  margin-top: 1rem;
}

.write-toolbar {
  width: 60px;
  background: #ffffff;
  border-left: 1px solid #e8e8e8;
  padding: 1.5rem 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  position: sticky;
  top: 0;
  align-self: flex-start;
  height: fit-content;
  max-height: calc(100vh - 250px - 60px);
  overflow-y: auto;
  margin-top: 32px;
  margin-bottom: 32px;
  border-radius: 8px;
}

.toolbar-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  border: none;
  background: none;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: #f5f5f5;
    color: #1a1a1a;
  }

  &.active {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.15) 0%, rgba(168, 85, 247, 0.15) 100%);
    color: var(--color-accent);
  }
}

.toolbar-divider {
  height: 1px;
  width: 24px;
  background: #e8e8e8;
  margin: 0.5rem 0;
}

.write-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #ffffff;
  border-top: 1px solid #e8e8e8;
  z-index: 100;
}

.footer-inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.footer-nav {
  display: flex;
  gap: 0.5rem;
}

.footer-nav-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  color: #666;
  font-size: 0.875rem;
  border-radius: 6px;
  transition: all 0.2s;
  text-decoration: none;

  &:hover {
    background: #f5f5f5;
    color: #1a1a1a;
    text-decoration: none;
  }

  &.active {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(168, 85, 247, 0.1) 100%);
    color: var(--color-accent);
    text-decoration: none;
  }
}

.footer-divider {
  width: 1px;
  height: 24px;
  background: #e8e8e8;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.word-count {
  font-size: 0.875rem;
  color: #999;
  margin-left: 0.5rem;
}

.footer-btn {
  padding: 0.5rem 1.5rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-draft {
  background: #f5f5f5;
  color: #666;

  &:hover:not(:disabled) {
    background: #e8e8e8;
  }
}

.btn-publish {
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  color: white;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 212, 255, 0.3);
  }
}

@media (max-width: 1024px) {
  .write-sidebar {
    width: 60px;
  }

  .sidebar-outline {
    display: none;
  }

  .write-toolbar {
    display: none;
  }

  .footer-nav-item span {
    display: none;
  }
}

@media (max-width: 768px) {
  .write-sidebar {
    display: none;
  }

  .write-main {
    padding: 1rem;
    padding-bottom: 80px;
  }

  .form-row {
    flex-direction: column;
  }

  .hero-title {
    font-size: 2rem;
  }

  .footer-inner {
    padding: 0 1rem;
  }

  .footer-nav-item span {
    display: none;
  }
}
</style>