<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { createGuestbook, getGuestbookList } from '@/api/guestbook'
import { getUserProfile } from '@/api/profile'
import Header from '@/components/layout/Header.vue'

const LANE_COUNT = 14
const LANE_START_PCT = 8
const LANE_GAP_PCT = 6

interface DanmakuItem {
  id: number
  userId?: string
  avatarUrl?: string
  text: string
  top: number
  duration: number
  laneIndex: number
  paused: boolean
  isReplay: boolean
  animDelay: number
}

const router = useRouter()
const authStore = useAuthStore()

const message = ref('')
const danmakuList = ref<DanmakuItem[]>([])
const isInputFocused = ref(false)
const loading = ref(false)
const userAvatarMap = ref<Record<string, string>>({})

let danmakuId = 0
const backgroundImages = [
  '/Note/wallhaven-8gdg62_2560x1440.png',
  '/Note/wallhaven-8grwv1_2560x1440.png',
  '/Note/wallhaven-jevgyp_2560x1440.png',
  '/Note/wallhaven-lykqqq_2560x1440.png',
  '/Note/wallhaven-ml2y21_2560x1440.png',
  '/Note/wallhaven-qr3175_2560x1440.png',
  '/Note/wallhaven-w5lr6x_2560x1440.png',
  '/Note/wallhaven-yxpdpk_2560x1440.png',
  '/Note/wallhaven-zp5o8o_2560x1440.png',
  '/dl.png',
  '/blog.png'
]

const currentBackground = ref('')
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
  
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  
  initMeteors(canvas.width, canvas.height)
}

function getRandomBackground() {
  return backgroundImages[Math.floor(Math.random() * backgroundImages.length)]
}

function calcDuration(text: string): number {
  const BASE = 26
  const MIN = 20
  const FACTOR = -0.04
  return Math.max(MIN, BASE + text.length * FACTOR)
}

// CSS animation 弹幕管理
const laneReady = new Array(LANE_COUNT).fill(0)

const replayPool: DanmakuItem[] = []

function launchOnLane(item: DanmakuItem) {
  const now = Date.now()

  // 收集空闲轨道和最早就绪轨道
  const free: number[] = []
  let earliest = laneReady[0]
  for (let i = 0; i < LANE_COUNT; i++) {
    if (laneReady[i] <= now) free.push(i)
    if (laneReady[i] < earliest) earliest = laneReady[i]
  }

  // 优先随机选空闲轨道，无空闲则取最早就绪的
  let lane: number
  if (free.length > 0) {
    lane = free[Math.floor(Math.random() * free.length)]
  } else {
    lane = laneReady.indexOf(earliest)
  }

  item.top = LANE_START_PCT + lane * LANE_GAP_PCT
  item.laneIndex = lane

  const delay = Math.max(0, laneReady[lane] - now)
  const textWidth = item.text.length * 14 + 52
  const speed = (window.innerWidth + textWidth) / item.duration
  const clearMs = ((textWidth + 80) / speed) * 1000
  laneReady[lane] = Math.max(now, laneReady[lane]) + clearMs

  const doPush = () => { danmakuList.value.push(item) }
  if (delay > 0) { setTimeout(doPush, delay) } else { doPush() }
}

function getAvatarUrl(d: DanmakuItem): string | undefined {
  if (d.userId && d.userId === authStore.user?.id) {
    return authStore.user?.avatarUrl
  }
  if (d.userId && userAvatarMap.value[d.userId]) {
    return userAvatarMap.value[d.userId]
  }
  return d.avatarUrl
}

function onDanmakuAnimEnd(id: number) {
  const finished = danmakuList.value.find(d => d.id === id)
  danmakuList.value = danmakuList.value.filter(d => d.id !== id)
  if (finished) replayPool.push(finished)
}

let replayTimer: ReturnType<typeof setInterval> | null = null

function startReplayLoop() {
  replayTimer = setInterval(() => {
    if (replayPool.length === 0) return
    const idx = Math.floor(Math.random() * replayPool.length)
    const entry = replayPool.splice(idx, 1)[0]
    const d: DanmakuItem = {
      ...entry,
      id: ++danmakuId,
      paused: false,
      isReplay: true,
      animDelay: 0
    }
    launchOnLane(d)
  }, 500)
}

async function sendDanmaku() {
  if (!message.value.trim()) return

  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }

  const content = message.value.trim()
  const avatarUrl = authStore.user?.avatarUrl
  message.value = ''

  try {
    const result = await createGuestbook({
      content,
      nickname: authStore.user?.displayName || authStore.user?.username,
      avatarUrl
    })

    if (result.code === 0 && result.data) {
      launchOnLane({
        id: ++danmakuId,
        userId: authStore.user?.id,
        avatarUrl,
        text: content,
        top: 0,
        duration: calcDuration(content),
        laneIndex: -1,
        paused: false,
        isReplay: false,
        animDelay: 0
      })
    }
  } catch {
    // silent fail, danmaku was already optimistically removed from input
  }
}

async function loadHistory() {
  loading.value = true
  try {
    const result = await getGuestbookList({ page: 0, size: 100 })
    if (result.code === 0 && result.data) {
      const messages = result.data.content
      messages.forEach((msg, index) => {
        const d: DanmakuItem = {
          id: ++danmakuId,
          userId: msg.userId,
          avatarUrl: msg.avatarUrl,
          text: msg.content,
          top: 0,
          duration: calcDuration(msg.content),
          laneIndex: -1,
          paused: false,
          isReplay: true,
          animDelay: 0
        }
        setTimeout(() => launchOnLane(d), Math.random() * 3000 + index * 150)
      })

      const uniqueIds = [...new Set(messages.map(m => m.userId).filter(Boolean))]
      await Promise.all(uniqueIds.map(async uid => {
        if (!userAvatarMap.value[uid]) {
          try {
            const profile = await getUserProfile(uid)
            if (profile.code === 0 && profile.data?.avatarUrl) {
              userAvatarMap.value[uid] = profile.data.avatarUrl
            }
          } catch { /* skip */ }
        }
      }))
    }
  } catch {
    // silent fail
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  currentBackground.value = getRandomBackground()

  setTimeout(() => {
    resizeCanvas()
    animate()
    window.addEventListener('resize', resizeCanvas)
  }, 100)

  await loadHistory()
  startReplayLoop()
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (replayTimer) clearInterval(replayTimer)
  window.removeEventListener('resize', resizeCanvas)
})
</script>

<template>
  <div class="guestbook-container">
    <div class="page-mask"></div>
    <div class="background-layer" :style="{ backgroundImage: `url(${currentBackground})` }"></div>
    <canvas ref="meteorCanvas" class="meteor-canvas"></canvas>
    <Header />
    <div class="danmaku-container">
      <div
        v-for="danmaku in danmakuList"
        :key="danmaku.id"
        class="danmaku-item"
        :class="{ paused: danmaku.paused }"
        :style="{
          top: danmaku.top + '%',
          animationDuration: danmaku.duration + 's',
          animationDelay: danmaku.animDelay + 's'
        }"
        @animationend="onDanmakuAnimEnd(danmaku.id)"
        @mouseenter="danmaku.paused = true"
        @mouseleave="danmaku.paused = false"
      >
        <div class="danmaku-pill">
          <img
            v-if="getAvatarUrl(danmaku)"
            :src="getAvatarUrl(danmaku)"
            class="danmaku-avatar"
            alt=""
            @error="(e: Event) => console.warn('[弹幕] 头像图片加载失败:', (e.target as HTMLImageElement).src)"
          />
          <span v-else class="danmaku-avatar-placeholder">
            {{ danmaku.text.charAt(0) }}
          </span>
          <span class="danmaku-text">{{ danmaku.text }}</span>
        </div>
      </div>
    </div>

    <div class="guestbook-content">
      <h1 class="title">弹幕</h1>

      <div class="input-section">
        <div class="message-input-wrapper" :class="{ 'has-focus': isInputFocused }">
          <input
            v-model="message"
            type="text"
            placeholder="留下点什么吧~"
            class="message-input"
            :disabled="loading"
            @keyup.enter="sendDanmaku"
            @focus="isInputFocused = true"
          />
        </div>
        <div class="send-btn-wrapper" :class="{ visible: isInputFocused }">
          <button class="send-btn" @click="sendDanmaku">发射</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.guestbook-container {
  min-height: 100vh;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(26, 26, 46, 0.4);
    z-index: 1;
  }
}

.page-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 9999;
  animation: mask-fade 1s ease-out forwards;
}

@keyframes mask-fade {
  0%   { opacity: 1; visibility: visible; }
  99%  { opacity: 0; visibility: visible; }
  100% { opacity: 0; visibility: hidden; }
}

.background-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: -1;
}

.meteor-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.danmaku-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
  z-index: 11;
}

.danmaku-item {
  position: absolute;
  left: 0;
  white-space: nowrap;
  pointer-events: auto;
  will-change: transform;
  animation: danmaku-scroll linear forwards;
  animation-play-state: running;

  &.paused {
    z-index: 100;
    animation-play-state: paused;
    .danmaku-pill {
      background: rgba(0, 0, 0, 0.85);
    }
  }
}

@keyframes danmaku-scroll {
  from { transform: translateX(100vw); }
  to   { transform: translateX(-100%); }
}

.danmaku-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 14px 4px 4px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 20px;
}

.danmaku-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.danmaku-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.danmaku-text {
  color: #ffffff;
  font-size: 14px;
  line-height: 1.4;
}

.guestbook-content {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  min-height: 100vh;
  padding: 2rem;
  padding-top: 250px;
}

.title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 0 30px rgba(0, 212, 255, 0.4);
  margin-bottom: 1.5rem;
  text-align: center;
}

.input-section {
  display: flex;
  align-items: center;
  width: 400px;
  height: 36px;
  position: relative;
}

.message-input-wrapper {
  position: absolute;
  left: 0;
  right: 0;
  height: 100%;
  transition: right 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &.has-focus {
    right: calc(60px + 1rem);
  }
}

.message-input {
  width: 100%;
  height: 100%;
  line-height: 36px;
  padding: 0 0.875rem;
  font-size: 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.3s, box-shadow 0.3s, background 0.3s;

  &:focus {
    border-color: rgba(0, 212, 255, 0.6);
    box-shadow: 0 0 12px rgba(0, 212, 255, 0.2);
    background: rgba(255, 255, 255, 0.2);
  }

  &::placeholder {
    color: rgba(255, 255, 255, 0.5);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.send-btn-wrapper {
  position: absolute;
  right: 0;
  top: 0;
  width: 60px;
  height: 100%;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.25s ease;

  &.visible {
    opacity: 1;
    pointer-events: auto;
  }
}

.send-btn {
  width: 100%;
  height: 100%;
  padding: 0 1rem;
  font-size: 0.75rem;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.85);
  color: #333;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.95);
    border-color: rgba(255, 255, 255, 0.7);
  }

  &:active {
    transform: scale(0.98);
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 2rem;
  }

  .input-section {
    flex-direction: column;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
