<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import type { CreateArticleDTO, UpdateArticleDTO } from '@/types'
import Header from '@/components/layout/Header.vue'

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

async function handleSubmit() {
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
    <Header />
    <header class="write-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1 class="page-title">{{ isEdit ? '编辑文章' : '写文章' }}</h1>
      <div class="actions">
        <button 
          class="btn btn-draft" 
          :disabled="isSubmitting"
          @click="status = 'DRAFT'; handleSubmit()"
        >
          {{ isSubmitting ? '保存中...' : '保存草稿' }}
        </button>
        <button 
          class="btn btn-publish" 
          :disabled="isSubmitting"
          @click="status = 'PUBLISHED'; handleSubmit()"
        >
          {{ isSubmitting ? (isEdit ? '更新中...' : '发布中...') : (isEdit ? '更新文章' : '发布文章') }}
        </button>
      </div>
    </header>

    <main class="write-content">
      <div class="form-section">
        <div class="form-group">
          <label>标题</label>
          <input 
            v-model="title"
            type="text"
            placeholder="请输入文章标题"
            class="title-input"
          />
        </div>

        <div class="form-row">
          <div class="form-group flex-1">
            <label>封面图片</label>
            <input 
              v-model="coverImage"
              type="text"
              placeholder="请输入图片URL"
            />
          </div>
          <div class="form-group">
            <label>文章状态</label>
            <select v-model="status" class="status-select">
              <option value="DRAFT">草稿</option>
              <option value="PUBLISHED">发布</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group flex-1">
            <label>分类</label>
            <select v-model="selectedCategory" class="category-select">
              <option value="">请选择分类</option>
              <option v-for="cat in articleStore.categories" :key="cat" :value="cat">
                {{ cat }}
              </option>
            </select>
          </div>
          <div class="form-group flex-1">
            <label>标签</label>
            <div class="tag-input-container">
              <input 
                v-model="tagInput"
                type="text"
                placeholder="输入标签后回车"
                @keyup.enter.prevent="addTag"
                class="tag-input"
              />
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

        <div class="form-group">
          <label>内容</label>
          <textarea 
            v-model="content"
            placeholder="请输入文章内容"
            rows="20"
            class="content-textarea"
          ></textarea>
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>
      </div>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.write-container {
  min-height: 100vh;
  background: var(--color-bg-primary);
  display: flex;
  flex-direction: column;
}

.write-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
  background: rgba(22, 35, 55, 0.8);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 1rem;
  transition: color 0.3s;

  &:hover {
    color: var(--color-accent);
  }
}

.page-title {
  font-size: 1.25rem;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.5rem 1.5rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border: none;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-draft {
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);

  &:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.15);
  }
}

.btn-publish {
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  color: white;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
  }
}

.write-content {
  flex: 1;
  padding: 2rem;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

.form-section {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;

  label {
    display: block;
    font-size: 0.875rem;
    color: var(--color-text-secondary);
    margin-bottom: 0.5rem;
  }

  input, select, textarea {
    width: 100%;
    padding: 0.75rem 1rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--color-border);
    border-radius: 8px;
    color: var(--color-text-primary);
    font-size: 1rem;
    transition: border-color 0.3s;

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

.title-input {
  font-size: 1.25rem;
  font-weight: 600;
}

.excerpt-textarea {
  resize: vertical;
}

.content-textarea {
  resize: vertical;
  font-family: inherit;
  line-height: 1.6;
}

.status-select, .category-select {
  max-width: 200px;
}

.tag-input-container {
  position: relative;
}

.tag-input {
  width: 100%;
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
  gap: 0.5rem;
  padding: 0.375rem 0.75rem;
  background: rgba(0, 212, 255, 0.15);
  border: 1px solid var(--color-accent);
  border-radius: 20px;
  color: var(--color-accent);
  font-size: 0.875rem;
}

.tag-remove {
  background: none;
  border: none;
  color: var(--color-accent);
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;

  &:hover {
    color: var(--color-error);
  }
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  text-align: center;
  margin-top: 1rem;
}

@media (max-width: 768px) {
  .write-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .form-row {
    flex-direction: column;
  }

  .write-content {
    padding: 1rem;
  }

  .form-section {
    padding: 1.5rem;
  }
}
</style>