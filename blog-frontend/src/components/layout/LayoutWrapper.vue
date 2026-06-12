<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import Header from './Header.vue'
import Sidebar from './Sidebar.vue'
import gsap from 'gsap'

interface Props {
  showSidebar?: boolean
  showHero?: boolean
  heroTitle?: string
  heroSubtitle?: string
  heroImage?: string
  heroSize?: 'full' | 'small'
}

withDefaults(defineProps<Props>(), {
  showSidebar: true,
  showHero: true,
  heroTitle: '微风日志',
  heroSubtitle: '微风书写我的心事',
  heroImage: '/blog.png',
  heroSize: 'full'
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

function initMeteors() {
  const meteors = document.querySelectorAll('.meteor')
  meteors.forEach((m, index) => {
    gsap.killTweensOf(m)

    const startFromTop = Math.random() > 0.5
    let startX, startY, endX, endY

    if (startFromTop) {
      startX = Math.random() * (window.innerWidth + 200) - 100
      startY = -200
      const distance = window.innerHeight + 300
      endX = startX - distance
      endY = startY + distance
    } else {
      startX = window.innerWidth + 200
      startY = Math.random() * (window.innerHeight + 200) - 100
      const distance = window.innerWidth + 300
      endX = startX - distance
      endY = startY + distance
    }

    gsap.set(m, {
      x: startX,
      y: startY,
      rotation: 315,
      opacity: 0
    })
    gsap.to(m, {
      x: endX,
      y: endY,
      opacity: 0.7,
      duration: 5 + Math.random() * 4,
      delay: index * 0.15,
      repeat: -1,
      ease: 'linear'
    })
  })
}

onMounted(() => {
  startTyping()
  initMeteors()

  window.addEventListener('resize', initMeteors)
})

onUnmounted(() => {
  if (typingInterval) clearInterval(typingInterval)
  window.removeEventListener('resize', initMeteors)
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

    <div v-if="showHero" class="hero-section" :class="{ 'hero-small': heroSize === 'small' }">
      <div class="hero-bg" :style="{ backgroundImage: `url(${heroImage})` }"></div>

      <div class="meteors-container">
        <div v-for="i in 40" :key="i" class="meteor"></div>
      </div>

      <div class="hero-content">
        <h1 class="hero-title">{{ heroTitle }}</h1>
        <p v-if="heroSubtitle && heroSize === 'full'" class="hero-subtitle">{{ currentSubtitle }}<span class="cursor"></span></p>
        <div v-if="heroSize === 'full'" class="scroll-indicator" @click="scrollDown">
          <div class="scroll-arrow"></div>
        </div>
      </div>
    </div>

    <div class="main-wrapper">
      <div class="main-inner">
        <Sidebar v-if="showSidebar" />
        <main class="main-content">
          <slot></slot>
        </main>
      </div>
    </div>

    <footer class="footer">
      <div class="footer-inner">
        <p>&copy; {{ new Date().getFullYear() }} 微风日志. All rights reserved.</p>
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
    background-position: top center;
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

.meteors-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.meteor {
  position: absolute;
  width: 150px;
  height: 2px;
  background: linear-gradient(315deg, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.9));
  border-radius: 50%;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.7);
  opacity: 0.7;
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
  padding: 3rem 2rem;
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
