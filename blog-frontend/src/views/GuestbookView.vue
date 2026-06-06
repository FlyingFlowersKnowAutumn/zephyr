<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
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
let danmakuId = 0
let animationFrameId: number | null = null

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
  <div class="guestbook-container">
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
        <input
          v-model="message"
          type="text"
          placeholder="留下点什么吧~"
          class="message-input"
          @keyup.enter="sendDanmaku"
        />
        <button class="send-btn" @click="sendDanmaku">发射</button>
      </div>
      
      <div class="tips">
        <p>💡 提示：输入内容后按回车或点击发射按钮发送弹幕</p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.guestbook-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

.danmaku-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
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
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
}

.title {
  font-size: 3rem;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 0 40px rgba(0, 212, 255, 0.5);
  margin-bottom: 2rem;
  text-align: center;
}

.input-section {
  display: flex;
  gap: 1rem;
  width: 100%;
  max-width: 500px;
}

.message-input {
  flex: 1;
  padding: 0.875rem 1.25rem;
  font-size: 1rem;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  outline: none;
  transition: all 0.3s ease;
  
  &:focus {
    border-color: var(--color-accent);
    box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
  }
  
  &::placeholder {
    color: rgba(255, 255, 255, 0.5);
  }
}

.send-btn {
  padding: 0.875rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  color: white;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
  }
}

.tips {
  margin-top: 1.5rem;
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.875rem;
  text-align: center;
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
