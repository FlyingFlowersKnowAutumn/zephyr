<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import Header from './Header.vue'
import Sidebar from './Sidebar.vue'
import FloatingActions from './FloatingActions.vue'

interface Props {
  showSidebar?: boolean
  showHero?: boolean
  heroTitle?: string
  heroSubtitle?: string
  heroImage?: string
  heroSize?: 'full' | 'small'
  articleContent?: string
  showToc?: boolean
  showRecentUpdates?: boolean
  showRightPanel?: boolean
  isArticlePage?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showSidebar: true,
  showHero: true,
  heroTitle: '微风日志',
  heroSubtitle: '微风书写我的心事',
  heroImage: '/blog.png',
  heroSize: 'full',
  articleContent: '',
  showToc: false,
  showRecentUpdates: true,
  showRightPanel: false,
  isArticlePage: false
})

const emit = defineEmits(['open-search'])

const currentSubtitle = ref('')
const chineseText = '微风书写我的心事'
const frenchText = 'La brise écrit mes pensées'
let typingInterval: number | null = null

const texts = [chineseText, frenchText]
let currentTextIndexRef = 0

function startTyping() {
  const currentText = texts[currentTextIndexRef]
  currentSubtitle.value = ''
  let charIndex = 0
  let isWaiting = false
  let isDeleting = false

  if (typingInterval) clearInterval(typingInterval)

  typingInterval = window.setInterval(() => {
    if (!isWaiting && !isDeleting) {
      if (charIndex < currentText.length) {
        currentSubtitle.value += currentText[charIndex]
        charIndex++
      } else {
        isWaiting = true
        setTimeout(() => {
          isWaiting = false
          isDeleting = true
        }, 1000)
      }
    } else if (isDeleting) {
      if (charIndex > 0) {
        charIndex--
        currentSubtitle.value = currentText.substring(0, charIndex)
      } else {
        clearInterval(typingInterval!)
        currentTextIndexRef = (currentTextIndexRef + 1) % texts.length
        setTimeout(() => {
          startTyping()
        }, 500)
      }
    }
  }, 100)
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
  
  const heroHeight = props.heroSize === 'full' 
    ? window.innerHeight 
    : Math.max(window.innerHeight * 0.333, 250)
  canvas.width = window.innerWidth
  canvas.height = heroHeight
  
  initMeteors(canvas.width, canvas.height)
}

onMounted(() => {
  startTyping()
  
  setTimeout(() => {
    resizeCanvas()
    animate()
    window.addEventListener('resize', resizeCanvas)
  }, 100)
})

onUnmounted(() => {
  if (typingInterval) clearInterval(typingInterval)
  if (animationId) cancelAnimationFrame(animationId)
  window.removeEventListener('resize', resizeCanvas)
})

function scrollDown() {
  const mainWrapper = document.querySelector('.main-wrapper')
  if (mainWrapper) {
    mainWrapper.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<template>
  <div class="layout-wrapper">
    <Header @open-search="emit('open-search')" />

    <div v-if="showHero" class="hero-section" :class="{ 'hero-small': heroSize === 'small' || isArticlePage }">
      <div class="hero-bg" :style="{ backgroundImage: `url(${heroImage})` }"></div>

      <canvas ref="meteorCanvas" class="meteor-canvas"></canvas>

      <div class="hero-content">
        <slot name="hero-content">
          <h1 class="hero-title">{{ heroTitle }}</h1>
          <p v-if="heroSubtitle && heroSize === 'full'" class="hero-subtitle">{{ currentSubtitle }}<span class="cursor"></span></p>
          <div v-if="heroSize === 'full'" class="scroll-indicator" @click="scrollDown">
            <div class="scroll-arrow"></div>
          </div>
        </slot>
      </div>
    </div>

    <div class="main-wrapper">
      <div class="main-inner">
        <Sidebar 
          v-if="showSidebar" 
          :articleContent="articleContent"
          :showToc="showToc"
          :showRecentUpdates="showRecentUpdates"
          :isArticlePage="isArticlePage"
        />
        <main class="main-content">
          <slot></slot>
        </main>
      </div>
    </div>

    <FloatingActions />

    <footer class="footer">
      <div class="footer-inner">
        <p>&copy; {{ new Date().getFullYear() }} 微风日志. All rights reserved.</p>
        <div class="footer-icp">
          <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer" class="icp-link">
            <span class="icp-icon">📋</span>
            <span class="icp-text">粤ICP备2025XXXXXX号</span>
          </a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.layout-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.hero-section {
  position: relative;
  height: 100vh;
  overflow: hidden;

  &.hero-small {
    height: 33.33vh;
    min-height: 250px;
  }
}

.hero-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  filter: brightness(0.6);
  z-index: -1;
  width: 100vw;
  height: 100vh;
}

.hero-small {
  .hero-bg {
    position: absolute;
    background-position: center;
    width: 100%;
    height: 100%;
  }

  .hero-content {
    height: 100%;
  }

  .hero-title {
    font-size: 2.5rem;
    margin-bottom: 0;
  }
}

.meteor-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 0;
}

.hero-content {
  position: relative;
  z-index: 1;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;
  padding: 2rem;
}

.hero-title {
  font-size: 45px;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}

.hero-subtitle {
  font-size: 22px;
  opacity: 0.9;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  min-height: 2rem;
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 1.5rem;
  background-color: white;
  margin-left: 2px;
  animation: blink 1s infinite;
  vertical-align: middle;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.meteor-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 0;
}

.hero-small {
  .meteor-canvas {
    position: absolute;
    width: 100%;
    height: 100%;
  }
}

.scroll-indicator {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  cursor: pointer;
  z-index: 10;
  animation: containerBounce 3s infinite;
}

.scroll-arrow {
  width: 14px;
  height: 14px;
  border-right: 2px solid white;
  border-bottom: 2px solid white;
  transform: rotate(45deg);
}

@keyframes containerBounce {
  0%, 100% {
    transform: translateX(-50%) translateY(0);
    opacity: 1;
  }
  50% {
    transform: translateX(-50%) translateY(20px);
    opacity: 0.4;
  }
}

.main-wrapper {
  flex: 1;
  padding: 3rem 14rem;
  background: var(--color-bg-primary);
  position: relative;
  z-index: 1;
}

.main-inner {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 2.5rem;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.footer {
  border-top: 1px solid var(--color-border);
  padding: 2rem;
  margin-top: auto;
  background: var(--color-bg-primary);
  position: relative;
  z-index: 1;
}

.footer-inner {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.9rem;

  p {
    margin-bottom: 0.75rem;
  }
}

.footer-icp {
  margin-top: 0.75rem;
}

.icp-link {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 0.8rem;
  transition: all 0.3s;

  &:hover {
    color: var(--color-accent);
  }

  .icp-icon {
    font-size: 0.75rem;
  }
}

@media (max-width: 1024px) {
  .main-inner {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1.1rem;
  }

  .main-wrapper {
    padding: 2rem 1rem;
  }
}
</style>
