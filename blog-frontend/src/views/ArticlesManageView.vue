<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { ElMessage } from 'element-plus'
import Header from '@/components/layout/Header.vue'

const router = useRouter()
const route = useRoute()
const articleStore = useArticleStore()

const activeTab = ref<'draft' | 'manage'>(
  (route.query.tab as string) === 'manage' ? 'manage' : 'draft'
)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const confirmDeleteId = ref<string | null>(null)
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

const articles = computed(() => articleStore.articles)

onMounted(() => {
  fetchArticles()
  
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

watch(() => route.query.tab, (newTab) => {
  const tab = (newTab as string) === 'manage' ? 'manage' : 'draft'
  if (activeTab.value !== tab) {
    activeTab.value = tab
  }
})

watch(activeTab, (newTab) => {
  currentPage.value = 1
  router.replace({ query: { tab: newTab } })
  fetchArticles()
})

async function fetchArticles() {
  loading.value = true
  try {
    const status = activeTab.value === 'draft' ? 'DRAFT' : 'PUBLISHED'
    const result = await articleStore.fetchArticles({
      page: currentPage.value,
      size: pageSize.value,
      status
    })
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchArticles()
}

function handleEdit(article: any) {
  router.push(`/articles/${article.id}/edit`)
}

function handleDelete(id: string) {
  confirmDeleteId.value = id
}

async function confirmDelete() {
  if (!confirmDeleteId.value) return

  const success = await articleStore.removeArticle(confirmDeleteId.value)
  if (success) {
    ElMessage.success('文章已删除')
    fetchArticles()
  } else {
    ElMessage.error(articleStore.error || '删除失败')
  }
  confirmDeleteId.value = null
}

async function handlePublish(article: any) {
  if (!article.id) return

  const result = await articleStore.modifyArticle(article.id, { status: 'PUBLISHED' })
  if (result) {
    ElMessage.success('文章已发布')
    fetchArticles()
  } else {
    ElMessage.error(articleStore.error || '发布失败')
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function getRelativeTime(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()

  const dateDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const nowDay = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const diffDays = Math.floor((nowDay.getTime() - dateDay.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`
  if (diffDays < 365) return `${Math.floor(diffDays / 30)}个月前`
  return `${Math.floor(diffDays / 365)}年前`
}
</script>

<template>
  <div class="write-container">
    <Header />

    <div class="write-hero">
      <div class="hero-bg" :style="{ backgroundImage: 'url(/Note/wallhaven-w5lr6x_2560x1440.png)' }"></div>
      <canvas ref="meteorCanvas" class="meteor-canvas"></canvas>
      <div class="hero-content">
        <h1 class="hero-title">{{ activeTab === 'draft' ? '草稿箱' : '文章管理' }}</h1>
      </div>
    </div>

    <div class="write-body">
      <aside class="write-sidebar">
        <div class="sidebar-tabs">
          <RouterLink to="/write" class="tab-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span>写文章</span>
          </RouterLink>
          <button 
            class="tab-btn"
            :class="{ active: activeTab === 'draft' }"
            @click="activeTab = 'draft'"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span>草稿箱</span>
          </button>
          <button 
            class="tab-btn"
            :class="{ active: activeTab === 'manage' }"
            @click="activeTab = 'manage'"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 19v-6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2z"/>
              <path d="M19 19v-6a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2z"/>
              <line x1="12" y1="5" x2="12" y2="19"/>
            </svg>
            <span>文章管理</span>
          </button>
        </div>
      </aside>

      <main class="write-main">
        <div class="articles-section">
          <div v-if="loading" class="loading">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>

          <div v-else-if="articles.length === 0" class="empty-state">
            <p>{{ activeTab === 'draft' ? '暂无草稿' : '暂无已发布文章' }}</p>
          </div>

          <div v-else class="articles-table">
            <table>
              <thead>
                <tr>
                  <th class="col-title">标题</th>
                  <th class="col-category">分类</th>
                  <th class="col-date">创建时间</th>
                  <th class="col-actions">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="article in articles" :key="article.id">
                  <td class="col-title">
                    <span class="article-title">{{ article.title }}</span>
                  </td>
                  <td class="col-category">
                    <span class="category-tag">{{ article.categoryName || '未分类' }}</span>
                  </td>
                  <td class="col-date">
                    <span class="date-text">{{ formatDate(article.createdAt) }}</span>
                    <span class="relative-time">{{ getRelativeTime(article.createdAt) }}</span>
                  </td>
                  <td class="col-actions">
                    <div class="action-buttons">
                      <button class="action-btn btn-edit" @click="handleEdit(article)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                        <span>编辑</span>
                      </button>
                      <button 
                        v-if="activeTab === 'draft'" 
                        class="action-btn btn-publish" 
                        @click="handlePublish(article)"
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                          <polyline points="17 8 12 3 7 8"/>
                          <line x1="12" y1="3" x2="12" y2="15"/>
                        </svg>
                        <span>发布</span>
                      </button>
                      <button class="action-btn btn-delete" @click="handleDelete(article.id)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                        </svg>
                        <span>删除</span>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="total > pageSize" class="pagination">
            <button 
              :disabled="currentPage === 1"
              class="page-btn"
              @click="handlePageChange(currentPage - 1)"
            >
              上一页
            </button>
            <span class="page-info">{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
            <button 
              :disabled="currentPage >= Math.ceil(total / pageSize)"
              class="page-btn"
              @click="handlePageChange(currentPage + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </main>
    </div>

    <div v-if="confirmDeleteId" class="modal-overlay" @click.self="confirmDeleteId = null">
      <div class="modal-content">
        <h3>确认删除</h3>
        <p>确定要删除这篇文章吗？此操作不可撤销。</p>
        <div class="modal-actions">
          <button class="modal-btn btn-cancel" @click="confirmDeleteId = null">取消</button>
          <button class="modal-btn btn-confirm" @click="confirmDelete">删除</button>
        </div>
      </div>
    </div>
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
}

.write-sidebar {
  width: 220px;
  background: #ffffff;
  border-right: 1px solid #e8e8e8;
  padding: 3.5rem 0 2rem;
  position: sticky;
  top: 0;
  align-self: flex-start;
  height: calc(100vh - 250px);
  overflow-y: auto;
  margin-top: 32px;
}

.sidebar-tabs {
  display: flex;
  flex-direction: column;
  padding: 0 1rem;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  margin-bottom: 0.25rem;
  border: none;
  background: none;
  border-radius: 6px;
  color: #666;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  text-decoration: none;

  &:hover {
    background: #f5f5f5;
    color: #1a1a1a;
    text-decoration: none;
  }

  &.active {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(168, 85, 247, 0.1) 100%);
    color: var(--color-accent);
    font-weight: 500;
    text-decoration: none;
  }
}

.write-main {
  flex: 1;
  padding: 2rem;
}

.articles-section {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 1.5rem;
}

.loading {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--color-text-secondary);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #999;

  p {
    font-size: 1rem;
  }
}

.articles-table {
  overflow-x: auto;
}

.articles-table table {
  width: 100%;
  border-collapse: collapse;
}

.articles-table th,
.articles-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e8e8e8;
}

.articles-table th {
  background: #fafafa;
  font-weight: 600;
  color: #333;
  font-size: 0.875rem;
}

.col-title {
  width: 40%;
}

.article-title {
  font-size: 0.95rem;
  color: #333;
  font-weight: 500;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-category {
  width: 15%;
}

.category-tag {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid var(--color-accent);
  border-radius: 20px;
  color: var(--color-accent);
  font-size: 0.8rem;
}

.col-date {
  width: 25%;
}

.date-text {
  display: block;
  font-size: 0.875rem;
  color: #666;
}

.relative-time {
  display: block;
  font-size: 0.75rem;
  color: #999;
  margin-top: 0.25rem;
}

.col-actions {
  width: 20%;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.btn-edit {
  background: #f5f5f5;
  color: #666;

  &:hover {
    background: #e8e8e8;
  }
}

.btn-publish {
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  color: white;
}

.btn-delete {
  background: #fef2f2;
  color: #ef4444;

  &:hover {
    background: #fee2e2;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.page-btn {
  padding: 0.625rem 1.5rem;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  color: #666;
  font-size: 0.9rem;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    border-color: var(--color-accent);
    color: var(--color-accent);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.page-info {
  color: #999;
  font-size: 0.95rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #ffffff;
  border-radius: 8px;
  padding: 2rem;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-content h3 {
  margin: 0 0 1rem;
  font-size: 1.25rem;
  color: #333;
}

.modal-content p {
  margin: 0 0 1.5rem;
  color: #666;
  font-size: 0.95rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.modal-btn {
  padding: 0.5rem 1.5rem;
  border-radius: 6px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;

  &:hover {
    background: #e8e8e8;
  }
}

.btn-confirm {
  background: #ef4444;
  color: white;

  &:hover {
    background: #dc2626;
  }
}

@media (max-width: 768px) {
  .write-sidebar {
    display: none;
  }

  .write-main {
    padding: 1rem;
  }

  .articles-table {
    overflow-x: auto;
  }

  .col-actions {
    width: auto;
  }

  .action-buttons {
    flex-direction: column;
    gap: 0.25rem;
  }

  .action-btn span {
    display: none;
  }

  .hero-title {
    font-size: 2rem;
  }
}
</style>