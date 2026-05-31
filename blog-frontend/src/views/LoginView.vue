<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import gsap from 'gsap'
import { useGuestAuth } from '@/composables/useGuestAuth'

const router = useRouter()
const { guestLogin } = useGuestAuth()
const isLoading = ref(false)

async function handleGuestLogin() {
  isLoading.value = true
  try {
    await guestLogin()
    router.push('/')
  } finally {
    isLoading.value = false
  }
}

function goToLoginForm() {
  router.push('/login-form')
}

onMounted(() => {
  const particles = document.querySelectorAll('.particle')
  particles.forEach((particle, index) => {
    gsap.to(particle, {
      x: gsap.utils.random(-100, 100),
      y: gsap.utils.random(-100, 100),
      opacity: gsap.utils.random(0.3, 0.8),
      scale: gsap.utils.random(0.5, 1.5),
      duration: gsap.utils.random(3, 7),
      repeat: -1,
      yoyo: true,
      delay: index * 0.1
    })
  })

  gsap.from('.hero-content', {
    opacity: 0,
    y: 30,
    duration: 1,
    ease: 'power3.out'
  })

  gsap.from('.action-buttons button', {
    opacity: 0,
    y: 20,
    duration: 0.8,
    delay: 0.3,
    stagger: 0.15
  })
})
</script>

<template>
  <div class="login-container">
    <div class="particles-container">
      <div v-for="i in 50" :key="i" class="particle"></div>
    </div>
    
    <div class="login-content">
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="title">
            <span class="text-gradient">个人技术博客</span>
          </h1>
          <p class="subtitle">记录技术成长，分享编程心得</p>
          <div class="tech-stack">
            <span>Java</span>
            <span>Vue</span>
            <span>Spring Cloud</span>
            <span>PostgreSQL</span>
          </div>
        </div>
      </div>
      
      <div class="action-section">
        <div class="action-buttons">
          <button 
            class="btn btn-primary"
            @click="goToLoginForm"
          >
            <span>博主登录</span>
          </button>
          <button 
            class="btn btn-secondary"
            @click="handleGuestLogin"
            :disabled="isLoading"
          >
            <span v-if="isLoading">登录中...</span>
            <span v-else>游客一键登录</span>
          </button>
        </div>
        <p class="hint">游客登录无需注册，点击即可体验</p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1628 0%, #0f1d32 50%, #162337 100%);
  position: relative;
  overflow: hidden;
}

.particles-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: radial-gradient(circle, #00d4ff 0%, transparent 70%);
  border-radius: 50%;
  box-shadow: 0 0 10px #00d4ff, 0 0 20px #00d4ff;
}

.login-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 2rem;
}

.hero-section {
  text-align: center;
  margin-bottom: 4rem;
}

.title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 0 0 40px rgba(0, 212, 255, 0.5);
}

.subtitle {
  font-size: 1.25rem;
  color: var(--color-text-secondary);
  margin-bottom: 2rem;
}

.tech-stack {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  
  span {
    padding: 0.5rem 1rem;
    background: rgba(0, 212, 255, 0.1);
    border: 1px solid rgba(0, 212, 255, 0.2);
    border-radius: 20px;
    font-size: 0.875rem;
    color: var(--color-accent);
  }
}

.action-section {
  text-align: center;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn {
  padding: 1rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 200px;
  
  &-primary {
    background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
    color: white;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
    }
  }
  
  &-secondary {
    background: rgba(255, 255, 255, 0.1);
    color: var(--color-text-primary);
    border: 1px solid rgba(255, 255, 255, 0.2);
    
    &:hover {
      background: rgba(255, 255, 255, 0.15);
      border-color: rgba(0, 212, 255, 0.5);
    }
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none !important;
  }
}

.hint {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

@media (min-width: 768px) {
  .title {
    font-size: 4.5rem;
  }
  
  .action-buttons {
    flex-direction: row;
  }
  
  .btn {
    min-width: 180px;
  }
}
</style>
