<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useSidebarStore } from '@/stores/sidebar'
import { renderMarkdown } from '@/utils/markdown'
import { uploadImage } from '@/api/upload'
import type { CreateArticleDTO, UpdateArticleDTO } from '@/types'
import { ElMessage } from 'element-plus'
import Header from '@/components/layout/Header.vue'

const route = useRoute()
const articleStore = useArticleStore()
const sidebarStore = useSidebarStore()

const isEdit = computed(() => !!route.params.slug)
const articleId = ref<string | null>(null)

const contentWordCount = computed(() => content.value.length)

const title = ref('')
const content = ref('')
const excerpt = ref('')
const coverImage = ref('')
const status = ref<'DRAFT' | 'PUBLISHED'>('DRAFT')
const selectedCategory = ref('')
const selectedTags = ref<string[]>([])
const selectedTagIds = ref<string[]>([])
const showCategoryModal = ref(false)
const showTagModal = ref(false)
const showTagCreateModal = ref(false)
const newCategoryName = ref('')
const newTagName = ref('')
const isSubmitting = ref(false)
const previewMode = ref(false)
const previewHtml = computed(() => renderMarkdown(content.value))

const outline = ref<{ level: number; text: string; lineIndex: number }[]>([])
const activeOutlineIndex = ref(-1)

const numberedOutline = computed(() => {
  const counters = [0, 0, 0, 0, 0, 0]
  let lastLevel = 0
  return outline.value.map((item, index) => {
    if (item.level > lastLevel) {
      counters[item.level - 1] = 0
    }
    counters[item.level - 1]++
    for (let i = item.level; i < 6; i++) counters[i] = 0
    lastLevel = item.level

    const parts = []
    for (let i = 0; i < item.level; i++) {
      parts.push(counters[i])
    }
    return { ...item, number: parts.join('.'), index }
  })
})

const outlineListRef = ref<HTMLUListElement | null>(null)

function updateActiveOutline() {
  const textarea = document.querySelector('.content-textarea') as HTMLTextAreaElement
  if (!textarea || outline.value.length === 0) {
    activeOutlineIndex.value = -1
    return
  }

  const cursorPos = textarea.selectionStart
  const lines = content.value.substring(0, cursorPos).split('\n')
  const currentLine = lines.length

  let active = -1
  for (let i = 0; i < outline.value.length; i++) {
    if (outline.value[i].lineIndex < currentLine) {
      active = i
    } else {
      break
    }
  }
  activeOutlineIndex.value = active
  scrollActiveOutlineIntoView()
}

function scrollActiveOutlineIntoView() {
  if (activeOutlineIndex.value < 0) return
  nextTick(() => {
    const el = outlineListRef.value?.querySelector('.outline-item.active') as HTMLElement
    el?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
  })
}

function onTextareaScroll() {
  const textarea = document.querySelector('.content-textarea') as HTMLTextAreaElement
  if (!textarea || outline.value.length === 0) return

  const lineHeight = parseFloat(getComputedStyle(textarea).lineHeight) || 24
  const firstVisibleLine = Math.floor(textarea.scrollTop / lineHeight)

  let active = -1
  for (let i = 0; i < outline.value.length; i++) {
    if (outline.value[i].lineIndex <= firstVisibleLine) {
      active = i
    } else {
      break
    }
  }
  activeOutlineIndex.value = active
}
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
  const baseSpeed = 0.35
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
  const text = content.value.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  const lines = text.split('\n')
  const result: { level: number; text: string; lineIndex: number }[] = []
  let inCodeBlock = false

  lines.forEach((line, index) => {
    if (/^```/.test(line)) {
      inCodeBlock = !inCodeBlock
      return
    }

    if (inCodeBlock) return

    const match = line.match(/^(#{1,6})\s+(.+?)(?:\s+#+\s*)?$/)
    if (match) {
      const level = match[1].length
      const headingText = match[2].trim()
      if (headingText) {
        result.push({
          level,
          text: headingText,
          lineIndex: index
        })
      }
    }
  })

  outline.value = result
}

watch(content, () => {
  generateOutline()
})

function onTextareaScrollEvent() {
  onTextareaScroll()
}

onMounted(async () => {
  await articleStore.fetchCategories()
  await articleStore.fetchTags()

  if (isEdit.value) {
    await loadArticle()
  }
  generateOutline()
  await nextTick()
  updateActiveOutline()

  const textarea = document.querySelector('.content-textarea') as HTMLTextAreaElement
  textarea?.addEventListener('scroll', onTextareaScrollEvent)

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
  const textarea = document.querySelector('.content-textarea') as HTMLTextAreaElement
  textarea?.removeEventListener('scroll', onTextareaScrollEvent)
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
    selectedCategory.value = article.categoryId || ''
    selectedTags.value = article.tags || []
  }
}

function removeTag(tag: string) {
  selectedTags.value = selectedTags.value.filter(t => t !== tag)
}

function toggleTag(tagId: string, tagName: string) {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index === -1) {
    selectedTagIds.value.push(tagId)
    if (!selectedTags.value.includes(tagName)) {
      selectedTags.value.push(tagName)
    }
  } else {
    selectedTagIds.value.splice(index, 1)
    selectedTags.value = selectedTags.value.filter(t => t !== tagName)
  }
}

async function handleCreateCategory() {
  if (!newCategoryName.value.trim()) return
  
  const success = await articleStore.createCategory(newCategoryName.value.trim())
  if (success) {
    ElMessage.success('分类创建成功')
    newCategoryName.value = ''
    showCategoryModal.value = false
  }
}

async function handleCreateTag() {
  if (!newTagName.value.trim()) return
  
  const success = await articleStore.createTag(newTagName.value.trim())
  if (success) {
    ElMessage.success('标签创建成功')
    newTagName.value = ''
    showTagCreateModal.value = false
  }
}

async function handleSubmit(isDraft = false) {
  const actionLabel = isEdit.value ? '更新' : (isDraft ? '保存' : '发布')

  if (!title.value.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!content.value.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  isSubmitting.value = true
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
      ElMessage.success(isDraft ? '草稿已保存' : '文章已发布')
      if (!isDraft) {
        await sidebarStore.refreshLastUpdateTime()
      }
    } else {
      ElMessage.error(articleStore.error || `${actionLabel}失败`)
    }
  } catch (e) {
    ElMessage.error(`${actionLabel}失败，请稍后重试`)
    console.error(e)
  } finally {
    isSubmitting.value = false
  }
}

function goToHeading(lineIndex: number, outlineIdx: number) {
  const textarea = document.querySelector('.content-textarea') as HTMLTextAreaElement
  if (textarea) {
    const lines = content.value.split('\n')
    let charCount = 0
    for (let i = 0; i < lineIndex; i++) {
      charCount += lines[i].length + 1
    }
    const lineHeight = parseFloat(getComputedStyle(textarea).lineHeight) || 24
    textarea.selectionStart = charCount
    textarea.selectionEnd = charCount
    textarea.focus()
    textarea.scrollTop = Math.max(0, lineIndex - 3) * lineHeight
    activeOutlineIndex.value = outlineIdx
    scrollActiveOutlineIntoView()
  }
}

function onTextareaSelect() {
  updateActiveOutline()
}

const coverUploading = ref(false)
const contentUploading = ref(false)
const coverFileInput = ref<HTMLInputElement | null>(null)
const contentFileInput = ref<HTMLInputElement | null>(null)
const contentTextarea = ref<HTMLTextAreaElement | null>(null)

function triggerCoverUpload() {
  coverFileInput.value?.click()
}

async function handleCoverUpload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    target.value = ''
    return
  }

  coverUploading.value = true
  try {
    const url = await uploadImage(file)
    if (url) coverImage.value = url
  } catch (e: any) {
    ElMessage.error(e.message || '封面上传失败')
  } finally {
    coverUploading.value = false
    target.value = ''
  }
}

function triggerContentUpload() {
  contentFileInput.value?.click()
}

async function handleContentUpload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    target.value = ''
    return
  }

  contentUploading.value = true
  try {
    const url = await uploadImage(file)
    if (url) insertImageToContent(url, file.name)
  } catch (e: any) {
    ElMessage.error(e.message || '图片上传失败')
  } finally {
    contentUploading.value = false
    target.value = ''
  }
}

function insertImageToContent(url: string, alt: string) {
  const md = `![${alt}](${url})\n`
  const textarea = contentTextarea.value
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    content.value = content.value.substring(0, start) + md + content.value.substring(end)
    nextTick(() => {
      textarea.focus()
      const pos = start + md.length
      textarea.setSelectionRange(pos, pos)
    })
  } else {
    content.value += md
  }
}

function importMd() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.md,.txt'
  input.onchange = (e) => {
    const target = e.target as HTMLInputElement
    const file = target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = (ev) => {
      const text = ev.target?.result as string
      if (text) {
        content.value = text
        generateOutline()
        const titleMatch = text.match(/^#\s+(.+)$/m)
        if (titleMatch && !title.value) {
          title.value = titleMatch[1].replace(/[`*_]/g, '')
        }
        nextTick(() => updateActiveOutline())
      }
    }
    reader.readAsText(file)
  }
  input.click()
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
          <ul ref="outlineListRef" v-if="numberedOutline.length > 0" class="outline-list">
            <li
              v-for="item in numberedOutline"
              :key="item.index"
              class="outline-item"
              :class="['level-' + item.level, { active: activeOutlineIndex === item.index }]"
              @click="goToHeading(item.lineIndex, item.index)"
            >
              <span class="outline-number">{{ item.number }}</span>
              <span class="outline-text">{{ item.text }}</span>
            </li>
          </ul>
          <p v-else class="outline-empty">输入标题以生成目录</p>
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
                v-if="!previewMode"
                ref="contentTextarea"
                v-model="content"
                placeholder="开始撰写你的文章..."
                rows="25"
                class="content-textarea"
                @mouseup="onTextareaSelect"
                @keyup="onTextareaSelect"
              ></textarea>
              <div
                v-else
                class="preview-area markdown-body"
                v-html="previewHtml"
              ></div>
            </div>
          </div>

          <div class="form-section">
            <div class="form-group">
              <label>封面图片</label>
              <div class="cover-area">
                <div v-if="!coverImage" class="cover-dropzone" @click="triggerCoverUpload">
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="17 8 12 3 7 8"/>
                    <line x1="12" y1="3" x2="12" y2="15"/>
                  </svg>
                  <p>{{ coverUploading ? '上传中...' : '点击上传封面图片' }}</p>
                  <span class="cover-hint">支持 jpg、png、webp，≤ 5MB</span>
                </div>
                <div v-else class="cover-preview">
                  <img :src="coverImage" alt="封面预览" />
                  <button class="cover-remove" @click="coverImage = ''" title="移除封面">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>
                <input ref="coverFileInput" type="file" accept="image/*" style="display:none" @change="handleCoverUpload" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group flex-1">
                <label>分类</label>
                <div class="form-group-actions">
                  <select v-model="selectedCategory" class="category-select">
                    <option value="">请选择分类</option>
                    <option v-for="cat in articleStore.categories" :key="cat.id" :value="cat.id">
                      {{ cat.name }}
                    </option>
                  </select>
                  <button class="btn-add" @click="showCategoryModal = true">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="12" y1="5" x2="12" y2="19"/>
                      <line x1="5" y1="12" x2="19" y2="12"/>
                    </svg>
                    <span>新增</span>
                  </button>
                </div>
              </div>
              <div class="form-group flex-1">
                <label>标签</label>
                <div class="form-group-actions">
                  <button class="btn-select-tag" @click="showTagModal = true">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                      <line x1="7" y1="7" x2="7.01" y2="7"/>
                    </svg>
                    <span>{{ selectedTags.length > 0 ? `已选${selectedTags.length}个` : '选择标签' }}</span>
                  </button>
                  <button class="btn-add" @click="showTagCreateModal = true">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="12" y1="5" x2="12" y2="19"/>
                      <line x1="5" y1="12" x2="19" y2="12"/>
                    </svg>
                    <span>新增</span>
                  </button>
                </div>
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

          </div>
      </main>

      <Teleport to="body">
        <div v-if="showCategoryModal" class="modal-overlay" @click.self="showCategoryModal = false">
          <div class="modal-container">
            <div class="modal-header">
              <h3>新增分类</h3>
              <button class="modal-close" @click="showCategoryModal = false">×</button>
            </div>
            <div class="modal-body">
              <input
                v-model="newCategoryName"
                type="text"
                placeholder="请输入分类名称"
                class="modal-input"
                @keyup.enter="handleCreateCategory"
                autofocus
              />
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showCategoryModal = false">取消</button>
              <button class="btn-confirm" @click="handleCreateCategory">确认创建</button>
            </div>
          </div>
        </div>

        <div v-if="showTagModal" class="modal-overlay" @click.self="showTagModal = false">
          <div class="modal-container tag-modal">
            <div class="modal-header">
              <h3>选择标签</h3>
              <button class="modal-close" @click="showTagModal = false">×</button>
            </div>
            <div class="modal-body">
              <div class="tag-list">
                <button
                  v-for="tag in articleStore.tags"
                  :key="tag.id"
                  class="tag-item-btn"
                  :class="{ selected: selectedTagIds.includes(tag.id) }"
                  @click="toggleTag(tag.id, tag.name)"
                >
                  {{ tag.name }}
                </button>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showTagModal = false">取消</button>
              <button class="btn-confirm" @click="showTagModal = false">确认选择</button>
            </div>
          </div>
        </div>

        <div v-if="showTagCreateModal" class="modal-overlay" @click.self="showTagCreateModal = false">
          <div class="modal-container">
            <div class="modal-header">
              <h3>新增标签</h3>
              <button class="modal-close" @click="showTagCreateModal = false">×</button>
            </div>
            <div class="modal-body">
              <input
                v-model="newTagName"
                type="text"
                placeholder="请输入标签名称"
                class="modal-input"
                @keyup.enter="handleCreateTag"
                autofocus
              />
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showTagCreateModal = false">取消</button>
              <button class="btn-confirm" @click="handleCreateTag">确认创建</button>
            </div>
          </div>
        </div>
      </Teleport>

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
          <button class="toolbar-btn" title="插入图片" :disabled="contentUploading" @click="triggerContentUpload">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
          </button>
        </div>
        <input ref="contentFileInput" type="file" accept="image/*" style="display:none" @change="handleContentUpload" />
        <div class="toolbar-divider"></div>
        <div class="toolbar-group">
          <button class="toolbar-btn" title="预览" :class="{ active: previewMode }" @click="previewMode = !previewMode">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
              <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
            </svg>
          </button>
        </div>
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
          <RouterLink to="/articles/manage?tab=draft" class="footer-nav-item">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 11H5"/>
              <path d="M12 19c-3.866 0-7-3.134-7-7s3.134-7 7-7 7 3.134 7 7-3.134 7-7 7z"/>
            </svg>
            <span>草稿箱</span>
          </RouterLink>
          <RouterLink to="/articles/manage?tab=manage" class="footer-nav-item">
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
  background: var(--color-bg-secondary);
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
  background: var(--color-bg-card);
  border-right: 1px solid var(--color-border);
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
  color: var(--color-text-primary);
  margin-bottom: 0.75rem;
}

.outline-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.outline-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.45rem 0.75rem;
  color: var(--color-text-secondary);
  font-size: 0.8rem;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  line-height: 1.4;

  &:hover {
    background: rgba(52, 211, 153, 0.08);
    color: var(--color-text-primary);
  }

  &.active {
    background: rgba(52, 211, 153, 0.15);
    color: #22c55e;
    font-weight: 500;

    .outline-number {
      color: #22c55e;
    }
  }

  &.level-1 {
    font-weight: 600;
    color: var(--color-text-primary);
  }

  &.level-2 {
    padding-left: 1rem;
  }

  &.level-3 {
    padding-left: 2rem;
    font-size: 0.75rem;
    color: var(--color-text-secondary);
  }

  &.level-4 {
    padding-left: 2.5rem;
    font-size: 0.72rem;
    color: var(--color-text-secondary);
  }

  &.level-5 {
    padding-left: 3rem;
    font-size: 0.7rem;
    color: var(--color-text-secondary);
  }

  &.level-6 {
    padding-left: 3.5rem;
    font-size: 0.68rem;
    color: var(--color-text-secondary);
  }
}

.outline-number {
  flex-shrink: 0;
  min-width: 1.5rem;
  font-weight: 600;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.outline-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.outline-empty {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
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
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
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
  color: var(--color-text-primary);
  background: transparent;

  &::placeholder {
    color: var(--color-text-secondary);
  }
}

.char-count {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.content-area {
  border-top: 1px solid var(--color-border);
  padding-top: 1.5rem;
}

.content-textarea {
  width: 100%;
  min-height: 400px;
  border: none;
  outline: none;
  font-size: 1rem;
  line-height: 1.8;
  color: var(--color-text-primary);
  resize: vertical;
  font-family: inherit;
  background: transparent;

  &::placeholder {
    color: var(--color-text-secondary);
  }
}

.preview-area {
  min-height: 400px;
  font-size: 1rem;
  line-height: 1.8;
  color: var(--color-text-secondary);

  :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
    font-weight: 600;
    margin: 1.5rem 0 0.75rem 0;
    color: var(--color-text-primary);
    line-height: 1.3;

    &:first-child { margin-top: 0; }
  }
  :deep(h1) { font-size: 1.75rem; border-bottom: 1px solid var(--color-border); padding-bottom: 0.5rem; }
  :deep(h2) { font-size: 1.5rem; border-bottom: 1px solid var(--color-border); padding-bottom: 0.4rem; }
  :deep(h3) { font-size: 1.25rem; }
  :deep(h4) { font-size: 1.1rem; }

  :deep(p) { margin: 0 0 0.875rem 0; }
  :deep(a) { color: var(--color-accent); }
  :deep(ul), :deep(ol) { padding-left: 1.5rem; margin: 0.5rem 0 1rem 0; }
  :deep(li) { margin: 0.25rem 0; }

  :deep(code) {
    background: var(--color-hover-bg);
    padding: 0.2rem 0.4rem;
    border-radius: 3px;
    font-family: 'Fira Code', 'Consolas', monospace;
    font-size: 0.875em;
  }

  :deep(pre) {
    background: var(--color-hover-bg);
    padding: 1rem 1.25rem;
    border-radius: 8px;
    overflow-x: auto;
    margin: 1rem 0;

    code { background: none; padding: 0; color: var(--color-text-primary); font-size: 0.875rem; line-height: 1.6; }
  }

  :deep(blockquote) {
    border-left: 4px solid var(--color-border);
    padding: 0.25rem 0 0.25rem 1rem;
    margin: 1rem 0;
    color: var(--color-text-secondary);
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 1rem 0;
    font-size: 0.95rem;
  }
  :deep(th), :deep(td) {
    border: 1px solid var(--color-border);
    padding: 0.5rem 0.75rem;
    text-align: left;
  }
  :deep(th) { background: var(--color-bg-secondary); font-weight: 600; }
  :deep(img) { max-width: 100%; border-radius: 6px; }
}

.form-section {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
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
    color: var(--color-text-primary);
    margin-bottom: 0.5rem;
  }

  input, select, textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid var(--color-border);
    border-radius: 6px;
    font-size: 0.875rem;
    color: var(--color-text-primary);
    transition: border-color 0.2s;
    background: var(--color-bg-card);

    &:focus {
      outline: none;
      border-color: var(--color-accent);
    }

    &::placeholder {
      color: var(--color-text-secondary);
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

.form-group-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;

  select {
    flex: 1;
  }
}

.btn-add {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-bg-card);
  color: var(--color-accent);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--color-accent);
    background: var(--color-hover-bg);
  }
}

.btn-select-tag {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
  flex: 1;
  justify-content: flex-start;

  &:hover {
    border-color: var(--color-accent);
    color: var(--color-accent);
  }
}

.cover-area {
  width: 100%;
}

.cover-dropzone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
  border: 2px dashed var(--color-border);
  border-radius: 8px;
  background: var(--color-bg-secondary);
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s;

  &:hover {
    border-color: var(--color-accent);
    color: var(--color-accent);
    background: var(--color-hover-bg);
  }

  p {
    margin: 0;
    font-size: 0.875rem;
  }
}

.cover-hint {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.cover-preview {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  max-height: 200px;
  display: inline-block;

  img {
    display: block;
    max-height: 200px;
    width: auto;
    border-radius: 8px;
  }
}

.cover-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;

  .cover-preview:hover & {
    opacity: 1;
  }

  &:hover {
    background: #ef4444;
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

.write-toolbar {
  width: 60px;
  background: var(--color-bg-card);
  border-left: 1px solid var(--color-border);
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
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: var(--color-hover-bg);
    color: var(--color-text-primary);
  }

  &.active {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.15) 0%, rgba(168, 85, 247, 0.15) 100%);
    color: var(--color-accent);
  }
}

.toolbar-divider {
  height: 1px;
  width: 24px;
  background: var(--color-border);
  margin: 0.5rem 0;
}

.write-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: var(--color-bg-card);
  border-top: 1px solid var(--color-border);
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
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  border-radius: 6px;
  transition: all 0.2s;
  text-decoration: none;

  &:hover {
    background: var(--color-hover-bg);
    color: var(--color-text-primary);
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
  background: var(--color-border);
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.word-count {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
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
  background: var(--color-hover-bg);
  color: var(--color-text-secondary);

  &:hover:not(:disabled) {
    background: var(--color-border);
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background: var(--color-bg-card);
  border-radius: 8px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  overflow: hidden;

  &.tag-modal {
    max-width: 600px;
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--color-border);

  h3 {
    margin: 0;
    font-size: 1rem;
    font-weight: 600;
    color: var(--color-text-primary);
  }
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  line-height: 1;

  &:hover {
    color: var(--color-text-primary);
  }
}

.modal-body {
  padding: 1.25rem;
}

.modal-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 0.9rem;
  box-sizing: border-box;
  background: var(--color-bg-card);
  color: var(--color-text-primary);

  &:focus {
    outline: none;
    border-color: var(--color-accent);
  }
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  max-height: 300px;
  overflow-y: auto;
}

.tag-item-btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--color-accent);
    color: var(--color-accent);
  }

  &.selected {
    background: var(--color-accent);
    border-color: var(--color-accent);
    color: #fff;
  }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 1rem 1.25rem;
  border-top: 1px solid var(--color-border);
}

.btn-cancel {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--color-hover-bg);
  }
}

.btn-confirm {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  background: var(--color-accent);
  color: #fff;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    opacity: 0.85;
  }
}
</style>