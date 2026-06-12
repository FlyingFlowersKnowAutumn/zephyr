<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/layout/Header.vue'

interface DanmakuItem {
  id: number
  text: string
  color: string
  top: number
  speed: number
}

const router = useRouter()
const message = ref('')
const danmakuList = ref<DanmakuItem[]>([])
const danmakuContainer = ref<HTMLElement | null>(null)
const isInputFocused = ref(false)
let danmakuId = 0
let animationFrameId: number | null = null

const backgroundImages = [
  '/Note/wallhaven-8gdg62_2560x1440.png',
  '/Note/wallhaven-8grwv1_2560x1440.png',
  '/Note/wallhaven-jevgyp_2560x1440.png',
  '/Note/wallhaven-lykqqq_2560x1440.png',
  '/Note/wallhaven-ml2y21_2560x1440.png',
  '/Note/wallhaven-qr3175_2560x1440.png',
  '/Note/wallhaven-w5lr6x_2560x1440.png',
  '/Note/wallhaven-yxpdpk_2560x1440.png',
  '/Note/wallhaven-zp5o8o_2560x1440.png'
]

const currentBackground = ref('')

function getRandomBackground() {
  const randomIndex = Math.floor(Math.random() * backgroundImages.length)
  return backgroundImages[randomIndex]
}

const colors = [
  '#ffffff', '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4',
  '#ffeaa7', '#dfe6e9', '#fd79a8', '#a29bfe', '#00b894'
]

const initialDanmakus = [
  '欢迎来到留言板！',
  '留下你的足迹吧~',
  'Hello World!',
  '这个博客很棒！',
  '技术分享很实用',
  '加油！继续更新',
  '路过留个言',
  '学到了很多',
  '感谢分享',
  '期待更多文章'
]

function generateDanmaku(text: string): DanmakuItem {
  return {
    id: ++danmakuId,
    text,
    color: colors[Math.floor(Math.random() * colors.length)],
    top: Math.random() * 70 + 10,
    speed: Math.random() * 10 + 15
  }
}

function sendDanmaku() {
  router.push('/login')
}

function animateDanmakus() {
  animationFrameId = requestAnimationFrame(animateDanmakus)
}

onMounted(() => {
  currentBackground.value = getRandomBackground()
  
  initialDanmakus.forEach((text, index) => {
    setTimeout(() => {
      const danmaku = generateDanmaku(text)
      danmakuList.value.push(danmaku)
      
      setTimeout(() => {
        danmakuList.value = danmakuList.value.filter(d => d.id !== danmaku.id)
      }, danmaku.speed * 100)
    }, index * 500)
  })
  
  animateDanmakus()
  
  setInterval(() => {
    if (danmakuList.value.length < 15) {
      const randomText = initialDanmakus[Math.floor(Math.random() * initialDanmakus.length)]
      const danmaku = generateDanmaku(randomText)
      danmakuList.value.push(danmaku)
      
      setTimeout(() => {
        danmakuList.value = danmakuList.value.filter(d => d.id !== danmaku.id)
      }, danmaku.speed * 100)
    }
  }, 2000)
})

onUnmounted(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
})
</script>

<template>
  <div class="guestbook-container" :style="{ backgroundImage: `url(${currentBackground})` }">
    <Header />
    <div class="danmaku-container" ref="danmakuContainer">
      <div
        v-for="danmaku in danmakuList"
        :key="danmaku.id"
        class="danmaku-item"
        :style="{
          color: danmaku.color,
          top: danmaku.top + '%',
          animationDuration: danmaku.speed + 's'
        }"
      >
        {{ danmaku.text }}
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
            @keyup.enter="sendDanmaku"
            @focus="isInputFocused = true"
            @blur="isInputFocused = false"
          />
        </div>
        <div class="send-btn-wrapper" :class="{ 'visible': isInputFocused }">
          <button class="send-btn" @click="sendDanmaku">发射</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.guestbook-container {
  min-height: 100vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
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

.danmaku-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
  z-index: 2;
}

.danmaku-item {
  position: absolute;
  right: -100%;
  white-space: nowrap;
  font-size: 16px;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  animation: danmaku-scroll linear forwards;
}

@keyframes danmaku-scroll {
  from {
    transform: translateX(0);
    right: -100%;
  }
  to {
    transform: translateX(-100vw);
    right: 100%;
  }
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
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.3);
    border-color: rgba(255, 255, 255, 0.5);
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
