<script setup lang="ts">
import { onMounted, onUnmounted, computed, ref, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { getArticle, getArticleBySlug } from '@/api/article'
import { renderMarkdown } from '@/utils/markdown'
import SeoHead from '@/components/seo/SeoHead.vue'

import CopyrightModal from '@/components/common/CopyrightModal.vue'
import ActionPanel from '@/components/interaction/ActionPanel.vue'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'

const emit = defineEmits(['open-search'])

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

function startMeteorAnimation() {
  const canvas = meteorCanvas.value
  if (!canvas) return
  
  resizeCanvas()
  animate()
  
  window.addEventListener('resize', resizeCanvas)
}

function stopMeteorAnimation() {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  window.removeEventListener('resize', resizeCanvas)
}

const router = useRouter()
const route = useRoute()
const articleStore = useArticleStore()

const slugParam = computed(() => route.params.slug as string)
const article = computed(() => articleStore.currentArticle)
const articleId = computed(() => article.value?.id || slugParam.value)
const loading = computed(() => articleStore.loading)
const renderedContent = computed(() => article.value ? renderMarkdown(article.value.content) : '')
const showCopyrightModal = ref(false)

onMounted(async () => {
  if (!slugParam.value) return
  
  try {
    const result = await getArticleBySlug(slugParam.value)
    if (result.code === 0 && result.data) {
      articleStore.currentArticle = result.data
      return
    }
    
    const uuidResult = await getArticle(slugParam.value)
    if (uuidResult.code === 0 && uuidResult.data) {
      articleStore.currentArticle = uuidResult.data
      if (uuidResult.data.slug && uuidResult.data.slug !== slugParam.value) {
        router.replace({ params: { slug: uuidResult.data.slug } })
      }
    }
  } catch {
    articleStore.fetchArticle(slugParam.value)
  }
})

watch(article, (newArticle) => {
  if (newArticle && newArticle.coverImage) {
    nextTick(() => {
      startMeteorAnimation()
    })
  }
}, { immediate: true })

onUnmounted(() => {
  stopMeteorAnimation()
})

function goBack() {
  router.back()
}

function formatDate(dateString: string) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\//g, '-')
}
</script>

<template>
  <div class="article-detail-page">
    <header v-if="article && article.coverImage" class="article-hero">
      <div class="hero-bg" :style="{ backgroundImage: `url(${article.coverImage})` }"></div>
      <div class="hero-overlay"></div>
      <canvas ref="meteorCanvas" class="meteor-canvas"></canvas>
      <div class="hero-content">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta-row">
          <span class="meta-item">📅 发表于 {{ formatDate(article.createdAt) }}</span>
          <span class="meta-divider">|</span>
          <span class="meta-item">🔄 更新于 {{ formatDate(article.updatedAt || article.createdAt) }}</span>
          <span class="meta-divider">|</span>
          <RouterLink v-if="article.categoryName" :to="{ path: '/categories', query: { category: article.categoryName } }" class="meta-item category-link">
            📁 {{ article.categoryName }}
          </RouterLink>
          <span v-else class="meta-item">📁 未分类</span>
        </div>
        <div class="article-meta-row">
          <span class="meta-item">👁️ 浏览量: {{ article.viewCount }}</span>
        </div>
      </div>
    </header>

    <LayoutWrapper 
        :showSidebar="true" 
        :showHero="false"
        :articleContent="article?.content || ''"
        :showToc="true"
        :showRecentUpdates="true"
        :showRightPanel="false"
        :isArticlePage="true"
        @open-search="emit('open-search')"
      >
      <SeoHead
        v-if="article"
        :title="article.title"
        :description="article.excerpt || article.content?.slice(0, 150)"
        :image="article.coverImage"
        type="article"
      />

      <div v-if="article && !article.coverImage" class="article-header-no-bg">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="meta-item">📅 发表于 {{ formatDate(article.createdAt) }}</span>
          <span class="meta-item">🔄 更新于 {{ formatDate(article.updatedAt || article.createdAt) }}</span>
          <RouterLink v-if="article.categoryName" :to="{ path: '/categories', query: { category: article.categoryName } }" class="meta-item category-link">
            📁 {{ article.categoryName }}
          </RouterLink>
          <span v-else class="meta-item">📁 未分类</span>
          <span class="meta-item">👁️ 浏览量: {{ article.viewCount }}</span>
        </div>
      </div>

      <div class="article-body">
        <article v-if="article && !loading" class="article-content">
          <div v-if="article.excerpt" class="article-excerpt">
            "{{ article.excerpt }}"
          </div>

          <div class="markdown-body" v-html="renderedContent"></div>

          <div v-if="article.tags.length" class="article-tags">
            <RouterLink v-for="tag in article.tags" :key="tag" :to="{ path: '/tags', query: { tag } }" class="tag-btn">{{ tag }}</RouterLink>
          </div>

          <div class="copyright-area">
            <div class="copyright-header">
              <span class="copyright-icon">📝</span>
              <span class="copyright-label">作者：zephyr</span>
            </div>
            <ul class="copyright-list">
              <li>
                <span class="list-number">1.</span>
                <span class="list-text">本网站部分内容可能来源于网络，仅供大家学习与参考，如有侵权，请联系站长进行删除处理。</span>
              </li>
              <li>
                <span class="list-number">2.</span>
                <span class="list-text">本网站一切内容不代表本站立场，并不代表本站赞同其观点和对其真实性负责。</span>
              </li>
              <li>
                <span class="list-number">3.</span>
                <span class="list-text">版权&许可请参阅</span>
                <button class="copyright-link" @click="showCopyrightModal = true">版权声明</button>
              </li>
            </ul>
          </div>
        </article>

        <div v-else-if="loading" class="loading">加载中...</div>

        <div v-else class="not-found">
          <p>文章不存在或已被删除</p>
          <button class="btn-primary" @click="goBack">返回列表</button>
        </div>
      </div>

      <ActionPanel
        v-if="article && !loading"
        :article-id="articleId"
        :article-author-id="article?.authorId"
        :article-title="article?.title"
        :article-content="article?.content"
      />

      <CopyrightModal :visible="showCopyrightModal" @close="showCopyrightModal = false" />
    </LayoutWrapper>
  </div>
</template>

<style lang="scss" scoped>
.article-detail-page {
  min-height: 100vh;
  background: var(--color-bg-primary);
}

.article-hero {
  position: relative;
  width: 100vw;
  height: 33.33vh;
  min-height: 280px;
  max-height: 400px;
  overflow: hidden;
  margin-left: calc(-50vw + 50%);

  .hero-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-size: cover;
    background-position: center;
    transition: transform 0.6s ease;
    filter: brightness(1);
  }

  &:hover .hero-bg {
    transform: scale(1.05);
  }

  .hero-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.8));
    z-index: 0;
  }

  .meteor-canvas {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 1;
  }

  .hero-content {
    position: relative;
    z-index: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    padding: 2rem 4rem;
    max-width: 1200px;
    margin: 0 auto;
    box-sizing: border-box;
  }

  .article-title {
    font-size: 2.25rem;
    font-weight: 700;
    color: white;
    margin: 0 0 1rem 0;
    line-height: 1.4;
    text-shadow: 0 2px 20px rgba(0, 0, 0, 0.4);
  }

  .article-meta-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.75rem;
    color: rgba(255, 255, 255, 0.85);
    font-size: 0.875rem;
    margin-top: 0.5rem;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 0.375rem;

      .meta-icon {
        width: 16px;
        height: 16px;
      }
    }

    .category-link {
      color: rgba(255, 255, 255, 0.97);
      transition: color 0.3s ease;
      &:hover {
        color: rgb(0, 251, 255);
      }
    }
  }

  .meta-divider {
    color: rgba(255, 255, 255, 0.6);
  }
}

.article-header-no-bg {
  margin-bottom: 1.5rem;
  padding: 0 0.5rem;

  .article-title {
    font-size: 2rem;
    font-weight: 800;
    color: #111827;
    margin: 0 0 0.75rem 0;
    line-height: 1.35;
    letter-spacing: -0.02em;
  }

  .article-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 1.25rem;
    color: #6b7280;
    font-size: 0.875rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #f3f4f6;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 0.375rem;
    }
  }
}

.category-link {
  color: #6b7280;
  text-decoration: none;
  transition: color 0.2s;

  &:hover {
    color: #2563eb;
  }
}

.article-body {
  width: 100%;
}

.article-content {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.article-excerpt {
  background: #f0f7ff;
  border-left: 4px solid #6366f1;
  padding: 1rem 1.25rem;
  margin: 1.25rem 2rem;
  color: #4b5563;
  font-size: 1rem;
  border-radius: 0 8px 8px 0;
  line-height: 1.7;
  font-style: italic;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin: 1rem 2rem 1.5rem;
}

.tag-btn {
  padding: 0.3rem 0.85rem;
  background: #f3f4f6;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;

  &:hover {
    background: #e5e7eb;
    color: #111827;
  }
}

:deep(.markdown-body) {
  padding: 0 2rem;
  margin: 1.5rem 0;
  line-height: 1.88;
  color: #24292f;
  font-size: 1rem;
  font-family: "Noto Sans SC", "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;

  // ── 标题 ────────────────────────
  h1, h2, h3, h4 {
    color: #1a1a2e;
    line-height: 1.35;
    scroll-margin-top: 80px;
    margin: 2rem 0 0.75rem;
    &:first-child { margin-top: 0; }
  }

  h1 {
    font-size: 1.75rem;
    font-weight: 700;
    padding-bottom: 0.6rem;
    border-bottom: 1px solid #eaecef;
  }

  h2 {
    font-size: 1.35rem;
    font-weight: 600;
    padding-bottom: 0.4rem;
    border-bottom: 1px solid #eaecef;
  }

  h3 {
    font-size: 1.15rem;
    font-weight: 600;
    color: #2c3e50;
  }

  h4 {
    font-size: 1.05rem;
    font-weight: 600;
    color: #34495e;
  }

  // ── 段落 ────────────────────────
  p {
    margin: 0 0 1.25rem 0;
  }

  // ── 链接 ────────────────────────
  a {
    color: #0969da;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }

  // ── 内联代码 ────────────────────────
  code {
    background: #f0f0f0;
    padding: 0.2em 0.45em;
    border-radius: 3px;
    font-family: "JetBrains Mono", "Fira Code", "Consolas", monospace;
    font-size: 0.875em;
    color: #1a1a2e;
  }

  // ── 代码块 ────────────────────────
  pre {
    background: #f6f8fa;
    border: 1px solid #d8dee4;
    border-radius: 6px;
    padding: 1rem 1.25rem;
    margin: 1.25rem 0;
    overflow-x: auto;
    line-height: 1.55;

    code {
      background: none;
      padding: 0;
      color: #24292f;
      font-size: 0.85rem;
    }
  }

  // ── 引用块 ────────────────────────
  blockquote {
    border-left: 3px solid #d0d7de;
    padding: 0.5rem 0 0.5rem 1rem;
    margin: 1.25rem 0;
    color: #57606a;

    p:last-child { margin-bottom: 0; }
  }

  // ── 列表 ────────────────────────
  ul, ol {
    padding-left: 1.5rem;
    margin: 0.5rem 0 1.25rem 0;
  }

  li {
    margin: 0.3rem 0;
    line-height: 1.8;
    &::marker { color: #8b949e; }
  }

  // ── 表格 ────────────────────────
  table {
    width: 100%;
    border-collapse: collapse;
    margin: 1.25rem 0;
    font-size: 0.9rem;
  }

  th, td {
    padding: 0.6rem 0.85rem;
    border: 1px solid #d8dee4;
    text-align: left;
  }

  th {
    background: #f6f8fa;
    font-weight: 600;
    color: #24292f;
    font-size: 0.85rem;
  }

  td {
    color: #24292f;
  }

  tbody tr:nth-child(even) td {
    background: #fafbfc;
  }

  // ── 分割线 ────────────────────────
  hr {
    border: none;
    height: 1px;
    background: #d8dee4;
    margin: 2rem 0;
  }

  // ── 图片 ────────────────────────
  img {
    max-width: 100%;
    border-radius: 6px;
    margin: 1.25rem auto;
    display: block;
  }

  // ── 强调 ────────────────────────
  strong {
    font-weight: 600;
    color: #1a1a2e;
  }

  em {
    color: #57606a;
  }
}

.copyright-area {
  background: rgba(248, 250, 252, 0.8);
  border-left: 4px solid var(--color-accent);
  padding: 1.25rem 1.5rem;
  margin: 15px 2rem;
  border-radius: 0 8px 8px 0;

  :deep(.dark) & {
    background: rgba(0, 212, 255, 0.05);
  }
}

.copyright-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;

  .copyright-icon {
    font-size: 1rem;
  }

  .copyright-label {
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--color-text-primary);
  }
}

.copyright-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.copyright-list li {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  line-height: 1.6;

  &:last-child {
    margin-bottom: 0;
  }
}

.list-number {
  flex-shrink: 0;
  font-weight: 500;
}

.list-text {
  flex: 1;
}

.copyright-link {
  background: none;
  border: none;
  color: var(--color-accent);
  font-size: inherit;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;

  &:hover {
    opacity: 0.8;
  }
}

.loading, .not-found {
  text-align: center;
  padding: 4rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  color: var(--color-text-secondary);
}

.not-found p {
  margin-bottom: 1rem;
}

.btn-primary {
  padding: 0.75rem 2rem;
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
  }
}

@media (max-width: 768px) {
  .article-hero {
    height: 250px;

    .hero-content {
      padding: 1.5rem;
    }

    .article-title {
      font-size: 1.5rem;
    }
  }

  .article-header-no-bg {
    .article-title {
      font-size: 1.5rem;
    }
  }

  :deep(.markdown-body) {
    padding: 1.5rem;
    font-size: 1rem;
  }

  .article-meta {
    gap: 1rem;
  }

  .copyright-area {
    margin: 1.5rem;
    padding: 1rem;
  }
}
</style>

<style lang="scss">
html.dark {
  .article-header-no-bg .article-title {
    color: var(--color-text-primary);
  }
  .article-header-no-bg .article-meta {
    color: var(--color-text-secondary);
    border-bottom-color: var(--color-border);
  }
  .category-link {
    color: var(--color-text-secondary);
    &:hover { color: var(--color-accent); }
  }
  .article-content {
    background: var(--color-bg-card);
    border-color: var(--color-border);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  }
  .article-excerpt {
    background: rgba(99, 102, 241, 0.08);
    color: var(--color-text-secondary);
    border-left-color: #818cf8;
  }
  .tag-btn {
    background: var(--color-bg-secondary);
    color: var(--color-text-secondary);
    &:hover {
      background: var(--color-hover-bg);
      color: var(--color-text-primary);
    }
  }
}

html.dark .markdown-body {
  color: #c9d1d9;
  background: var(--color-bg-card);

  h1, h2, h3, h4 {
    color: #e2e8f0;
  }
  h1 { border-bottom-color: #30363d; }
  h2 { border-bottom-color: #30363d; }
  h3 { color: #c9d1d9; }
  h4 { color: #b0b8c4; }

  a { color: #58a6ff; }

  code {
    background: #1e293b;
    color: #e2e8f0;
  }

  pre {
    background: #161b22;
    border-color: #30363d;
    code { color: #c9d1d9; }
  }

  blockquote {
    border-left-color: #30363d;
    color: #8b949e;
  }

  li::marker { color: #6e7681; }

  th, td {
    border-color: #30363d;
  }
  th {
    background: #1a2740;
    color: #e2e8f0;
  }
  td { color: #c9d1d9; }
  tbody tr:nth-child(even) td { background: rgba(255, 255, 255, 0.02); }

  hr { background: #30363d; }

  strong { color: #e2e8f0; }
  em { color: #8b949e; }
}
</style>
