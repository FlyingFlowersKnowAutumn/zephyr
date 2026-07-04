<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { createGuestbook, getGuestbookList } from '@/api/guestbook'
import { getUserProfile } from '@/api/profile'
import Header from '@/components/layout/Header.vue'
import gsap from 'gsap'

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
}

interface LaneState {
  occupiedUntil: number
}

const router = useRouter()
const authStore = useAuthStore()

const message = ref('')
const danmakuList = ref<DanmakuItem[]>([])
const isInputFocused = ref(false)
const loading = ref(false)
const userAvatarMap = ref<Record<string, string>>({})

let danmakuId = 0
const lanes: LaneState[] = Array.from({ length: LANE_COUNT }, () => ({
  occupiedUntil: 0
}))
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
  
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  
  initMeteors(canvas.width, canvas.height)
}

function getRandomBackground() {
  return backgroundImages[Math.floor(Math.random() * backgroundImages.length)]
}

function calcDuration(text: string): number {
  const BASE = 35
  const MIN = 12
  const FACTOR = -0.25
  return Math.max(MIN, BASE + text.length * FACTOR)
}

const tweenMap = new Map<number, gsap.core.Tween>()

const laneUseCount = new Array(LANE_COUNT).fill(0)

function startDanmakuAnimation(danmaku: DanmakuItem) {
  nextTick(() => {
    const el = document.querySelector(`[data-danmaku-id="${danmaku.id}"]`) as HTMLElement
    if (!el) return

    const group = laneUseCount[danmaku.laneIndex] % 3
    laneUseCount[danmaku.laneIndex]++
    const startOffset = group * 15 + Math.random() * 5
    const tween = gsap.fromTo(el,
      { left: `${100 + startOffset}%` },
      {
        left: '-2000px',
        duration: danmaku.duration,
        ease: 'none',
        onComplete: () => {
          tweenMap.delete(danmaku.id)
          onDanmakuEnd(danmaku.id, danmaku.laneIndex)
        }
      }
    )
    tweenMap.set(danmaku.id, tween)
  })
}

function acquireLane(duration: number): number | null {
  const now = Date.now()
  for (let i = 0; i < LANE_COUNT; i++) {
    if (lanes[i].occupiedUntil < now) {
      lanes[i].occupiedUntil = now + duration * 1000 + 600
      return i
    }
  }
  return null
}

function releaseLane(laneIndex: number) {
  lanes[laneIndex].occupiedUntil = 0
}

const replayPool: { item: Omit<DanmakuItem, 'id' | 'top' | 'laneIndex'>; originalId: number }[] = []
let newMessageQueue: DanmakuItem[] = []

function scheduleReplay(item: Omit<DanmakuItem, 'id' | 'top' | 'laneIndex'>, originalId: number) {
  setTimeout(() => {
    replayPool.push({ item, originalId })
    replayPool.sort((a, b) => b.item.text.length - a.item.text.length)
    drainReplayQueue()
  }, 2000)
}

function launchDanmakuInternal(item: DanmakuItem, isReplay: boolean) {
  const laneIndex = acquireLane(item.duration)
  if (laneIndex !== null) {
    item.top = LANE_START_PCT + laneIndex * LANE_GAP_PCT
    item.laneIndex = laneIndex
    danmakuList.value.push(item)
    startDanmakuAnimation(item)
  } else {
    if (isReplay) {
      replayPool.unshift({
        item: {
          userId: item.userId,
          avatarUrl: item.avatarUrl,
          text: item.text,
          duration: item.duration,
          paused: false
        },
        originalId: item.id
      })
    } else {
      newMessageQueue.push(item)
    }
  }
}

function launchDanmaku(item: DanmakuItem) {
  launchDanmakuInternal(item, false)
}

function drainNewMessageQueue() {
  if (newMessageQueue.length === 0) return
  newMessageQueue.sort((a, b) => b.text.length - a.text.length)
  const batch = [...newMessageQueue]
  newMessageQueue = []
  for (const d of batch) {
    launchDanmakuInternal(d, false)
  }
}

function drainReplayQueue() {
  if (replayPool.length === 0) return
  replayPool.sort((a, b) => b.item.text.length - a.item.text.length)
  while (replayPool.length > 0) {
    const entry = replayPool.shift()!
    const d: DanmakuItem = {
      id: ++danmakuId,
      ...entry.item,
      top: 0,
      laneIndex: -1
    }
    launchDanmakuInternal(d, true)
    if (d.laneIndex === -1) break
  }
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

function onDanmakuEnd(id: number, laneIndex: number) {
  const finished = danmakuList.value.find(d => d.id === id)
  danmakuList.value = danmakuList.value.filter(d => d.id !== id)
  releaseLane(laneIndex)

  if (finished) {
    scheduleReplay(
      {
        userId: finished.userId,
        avatarUrl: finished.avatarUrl,
        text: finished.text,
        duration: finished.duration,
        paused: false
      },
      id
    )
  }

  drainNewMessageQueue()
  drainReplayQueue()
}

function onDanmakuHover(id: number) {
  const tween = tweenMap.get(id)
  if (tween) tween.pause()
  const index = danmakuList.value.findIndex(d => d.id === id)
  if (index !== -1) {
    danmakuList.value[index] = { ...danmakuList.value[index], paused: true }
  }
}

function onDanmakuLeave(id: number) {
  const tween = tweenMap.get(id)
  if (tween) tween.resume()
  const index = danmakuList.value.findIndex(d => d.id === id)
  if (index !== -1) {
    danmakuList.value[index] = { ...danmakuList.value[index], paused: false }
  }
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
      launchDanmaku({
        id: ++danmakuId,
        userId: authStore.user?.id,
        avatarUrl,
        text: content,
        top: 0,
        duration: calcDuration(content),
        laneIndex: -1,
        paused: false
      })
      // 新消息优先: 清空消息队列后再尝试回放队列
      drainNewMessageQueue()
      if (newMessageQueue.length === 0) {
        drainReplayQueue()
      }
    }
  } catch {
    // silent fail, danmaku was already optimistically removed from input
  }
}

async function loadHistory() {
  loading.value = true
  try {
    const result = await getGuestbookList({ page: 0, size: 30 })
    if (result.code === 0 && result.data) {
      const messages = result.data.content
      messages.forEach(msg => {
        launchDanmaku({
          id: ++danmakuId,
          userId: msg.userId,
          avatarUrl: msg.avatarUrl,
          text: msg.content,
          top: 0,
          duration: calcDuration(msg.content),
          laneIndex: -1,
          paused: false
        })
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

  gsap.to('.page-mask', {
    opacity: 0,
    duration: 1,
    ease: 'power2.out',
    onComplete: () => {
      const mask = document.querySelector('.page-mask')
      if (mask) {
        mask.setAttribute('style', 'pointer-events: none; opacity: 0;')
      }
    }
  })

  setTimeout(() => {
    resizeCanvas()
    animate()
    window.addEventListener('resize', resizeCanvas)
  }, 100)

  await loadHistory()
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  window.removeEventListener('resize', resizeCanvas)
  tweenMap.forEach(t => t.kill())
  tweenMap.clear()
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
        :data-danmaku-id="danmaku.id"
        :style="{ top: danmaku.top + '%' }"
        @mouseenter="onDanmakuHover(danmaku.id)"
        @mouseleave="onDanmakuLeave(danmaku.id)"
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
  opacity: 1;
  pointer-events: none;
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
  left: 100%;
  white-space: nowrap;
  pointer-events: auto;
  will-change: left;

  &.paused {
    .danmaku-pill {
      background: rgba(0, 0, 0, 0.85);
    }
  }
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
