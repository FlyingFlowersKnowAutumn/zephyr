<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import type { CreateArticleDTO, UpdateArticleDTO } from '@/types'

const router = useRouter()
const route = useRoute()
const articleStore = useArticleStore()

const isEdit = computed(() => !!route.params.slug)
const articleId = ref<string | null>(null)

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

onMounted(async () => {
  await articleStore.fetchCategories()
  await articleStore.fetchTags()
  
  if (isEdit.value) {
    await loadArticle()
  }
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

function goBack() {
  router.back()
}
</script>

<template>
  <div class="write-container">
    <aside class="write-sidebar">
      <div class="sidebar-header">
        <span class="sidebar-title">文章</span>
      </div>
      <nav class="sidebar-nav">
        <button class="nav-item active">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          <span>写文章</span>
        </button>
        <button class="nav-item">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 11H5"/>
            <path d="M12 19c-3.866 0-7-3.134-7-7s3.134-7 7-7 7 3.134 7 7-3.134 7-7 7z"/>
          </svg>
          <span>草稿箱</span>
        </button>
        <button class="nav-item">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 19v-6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2z"/>
            <path d="M19 19v-6a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2z"/>
            <line x1="12" y1="5" x2="12" y2="19"/>
          </svg>
          <span>文章管理</span>
        </button>
      </nav>
      <div class="sidebar-divider"></div>
      <div class="sidebar-stats">
        <div class="stat-item">
          <span class="stat-value">0</span>
          <span class="stat-label">字数统计</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">0</span>
          <span class="stat-label">阅读时长</span>
        </div>
      </div>
    </aside>

    <main class="write-main">
      <header class="write-header">
        <button class="back-btn" @click="goBack">← 返回</button>
        <h1 class="page-title">{{ isEdit ? '编辑文章' : '写文章' }}</h1>
        <div class="actions">
          <button 
            class="btn btn-draft" 
            :disabled="isSubmitting"
            @click="handleSubmit(true)"
          >
            {{ isSubmitting ? '保存中...' : '保存草稿' }}
          </button>
          <button 
            class="btn btn-publish" 
            :disabled="isSubmitting"
            @click="handleSubmit(false)"
          >
            {{ isSubmitting ? (isEdit ? '更新中...' : '发布中...') : (isEdit ? '更新文章' : '发布文章') }}
          </button>
        </div>
      </header>

      <div class="editor-section">
        <div class="title-area">
          <input 
            v-model="title"
            type="text"
            placeholder="请输入文章标题（1-100字）"
            class="title-input"
          />
          <div class="title-actions">
            <span class="char-count">{{ title.length }}/100</span>
            <button class="action-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
            <button class="action-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"/>
              </svg>
            </button>
          </div>
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
            <button class="tag-remove">×</button>
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
        <button class="toolbar-btn active" title="富文本">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
        </button>
        <button class="toolbar-btn" title="Markdown">
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
        <button class="toolbar-btn" title="粗体">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 4h6a4 4 0 0 1 4 4v12M6 4v16m0 0v0m0-0v0"/>
          </svg>
        </button>
        <button class="toolbar-btn" title="斜体">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 4h6M14 20h6M4 14h12a2 2 0 0 1 2 2v4M4 14v-4a2 2 0 0 1 2-2h2.5"/>
          </svg>
        </button>
        <button class="toolbar-btn" title="标题">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 12h8M4 16h6M4 8h8"/>
            <path d="M16 4v16"/>
          </svg>
        </button>
      </div>
      <div class="toolbar-divider"></div>
      <div class="toolbar-group">
        <button class="toolbar-btn" title="列表">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="8" y1="6" x2="21" y2="6"/>
            <line x1="8" y1="12" x2="21" y2="12"/>
            <line x1="8" y1="18" x2="21" y2="18"/>
            <line x1="3" y1="6" x2="3.01" y2="6"/>
            <line x1="3" y1="12" x2="3.01" y2="12"/>
            <line x1="3" y1="18" x2="3.01" y2="18"/>
          </svg>
        </button>
        <button class="toolbar-btn" title="引用">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="21 15 18 15 21 21 21 15"/>
            <path d="M4 18h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2z"/>
          </svg>
        </button>
        <button class="toolbar-btn" title="代码">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="16 18 22 12 16 6"/>
            <polyline points="8 6 2 12 8 18"/>
          </svg>
        </button>
      </div>
      <div class="toolbar-divider"></div>
      <div class="toolbar-group">
        <button class="toolbar-btn" title="链接">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
          </svg>
        </button>
        <button class="toolbar-btn" title="图片">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
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
    </aside>
  </div>
</template>

<style lang="scss" scoped>
.write-container {
  min-height: 100vh;
  background: #f6f7f8;
  display: flex;
}

.write-sidebar {
  width: 200px;
  background: #ffffff;
  border-right: 1px solid #e8e8e8;
  padding: 1.5rem 0;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
}

.sidebar-header {
  padding: 0 1.5rem 1.5rem;
  border-bottom: 1px solid #e8e8e8;
}

.sidebar-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a1a1a;
}

.sidebar-nav {
  padding: 1rem 0;
}

.nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.5rem;
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  font-size: 0.875rem;
  transition: all 0.2s;

  &:hover {
    background: #f5f5f5;
    color: #1a1a1a;
  }

  &.active {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(168, 85, 247, 0.1) 100%);
    color: var(--color-accent);
    border-left: 2px solid var(--color-accent);
  }
}

.sidebar-divider {
  height: 1px;
  background: #e8e8e8;
  margin: 0 1rem;
}

.sidebar-stats {
  padding: 1.5rem 1.5rem;
  display: flex;
  gap: 1.5rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-label {
  font-size: 0.75rem;
  color: #999;
}

.write-main {
  flex: 1;
  margin-left: 200px;
  margin-right: 60px;
  padding: 2rem;
  padding-bottom: 3rem;
}

.write-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 2rem;
}

.back-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 1rem;
  transition: color 0.2s;

  &:hover {
    color: var(--color-accent);
  }
}

.page-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
}

.actions {
  display: flex;
  gap: 1rem;
}

.btn {
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

.title-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.char-count {
  font-size: 0.75rem;
  color: #999;
  padding-right: 0.5rem;
  border-right: 1px solid #e8e8e8;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: none;
  background: #f5f5f5;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: #e8e8e8;
    color: #1a1a1a;
  }
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
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
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

@media (max-width: 1024px) {
  .write-sidebar {
    width: 60px;
  }

  .sidebar-header,
  .sidebar-nav span,
  .sidebar-stats {
    display: none;
  }

  .nav-item {
    justify-content: center;
    padding: 0.75rem;
    border-left: none;

    &.active {
      border-left: none;
      border-radius: 6px;
    }
  }

  .write-main {
    margin-left: 60px;
  }

  .write-toolbar {
    display: none;
  }
}

@media (max-width: 768px) {
  .write-sidebar {
    display: none;
  }

  .write-main {
    margin-left: 0;
    padding: 1rem;
  }

  .write-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .form-row {
    flex-direction: column;
  }
}
</style>
