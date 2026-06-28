<script setup lang="ts">
import { ref, onMounted, onBeforeMount, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

import gsap from 'gsap'
import Header from '@/components/layout/Header.vue'
import * as authApi from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const animationPlayed = ref(false)

const username = ref('')
const password = ref('')
const email = ref('')
const captcha = ref('')
const captchaBtnText = ref('验证码')
const isCaptchaDisabled = ref(false)
const isLoading = ref(false)

onBeforeMount(() => {
  if (authStore.isLoggedIn) {
    router.push('/')
  }
})

async function handleRegister() {
  if (!username.value.trim() || !password.value.trim() || !email.value.trim() || !captcha.value.trim()) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  
  isLoading.value = true
  try {
    await authStore.register({
      username: username.value,
      email: email.value,
      password: password.value,
      code: captcha.value
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    isLoading.value = false
  }
}

async function getCaptcha() {
  if (!email.value.trim()) {
    ElMessage.warning('请先填写邮箱')
    return
  }
  
  isCaptchaDisabled.value = true
  captchaBtnText.value = '60s'

  // 倒计时先启动，不等 API 返回
  let count = 60
  const timer = setInterval(() => {
    count--
    captchaBtnText.value = `${count}s`

    if (count <= 0) {
      clearInterval(timer)
      isCaptchaDisabled.value = false
      captchaBtnText.value = '验证码'
    }
  }, 1000)

  try {
    await authApi.sendCode({ email: email.value, type: 'REGISTER' })
    ElMessage.success('验证码发送成功')
  } catch (error: any) {
    clearInterval(timer)
    isCaptchaDisabled.value = false
    captchaBtnText.value = '验证码'
    ElMessage.error(error.response?.data?.message || '验证码发送失败')
  }
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
  if (animationPlayed.value) return
  animationPlayed.value = true
  
  gsap.fromTo('.login-card', { opacity: 0, x: 50 }, { opacity: 1, x: 0, duration: 0.6, ease: 'power3.out' })
  gsap.fromTo('.left-section', { opacity: 0, x: -50 }, { opacity: 1, x: 0, duration: 0.6, delay: 0.2, ease: 'power3.out' })
  
  initMeteors()
  window.addEventListener('resize', initMeteors)
})

onUnmounted(() => {
  window.removeEventListener('resize', initMeteors)
})
</script>

<template>
  <div class="login-container">
    <Header @open-search="$emit('open-search')" />
    
    <div class="meteors-container">
      <div v-for="i in 40" :key="i" class="meteor"></div>
    </div>
    
    <div class="login-wrapper">
      <div class="left-section">
        <div class="image-container">
          <img src="/blog.png" alt="背景图" />
        </div>
      </div>
      
      <div class="right-section">
        <div class="login-card">
          <h2 class="login-title">注册</h2>
          
          <div class="links-row">
            <a href="/login" class="link">已有账号？立即登录 &gt;</a>
          </div>
          
          <form class="login-form" @submit.prevent="handleRegister">
            <div class="form-group">
              <input
                v-model="username"
                type="text"
                placeholder="用户名"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <input
                v-model="password"
                type="password"
                placeholder="密码"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <input
                v-model="email"
                type="email"
                placeholder="邮箱"
                class="form-input"
              />
            </div>
            
            <div class="form-group captcha-group">
              <input
                v-model="captcha"
                type="text"
                placeholder="验证码"
                class="form-input captcha-input"
              />
              <button type="button" class="captcha-btn" :disabled="isCaptchaDisabled" @click="getCaptcha">
                {{ captchaBtnText }}
              </button>
            </div>
            
            <button type="submit" class="login-btn register-btn" :disabled="isLoading">
              <svg v-if="isLoading" class="loading-icon" width="18" height="18" viewBox="0 0 24 24" fill="none">
                <circle class="spin" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <span>{{ isLoading ? '注册中...' : '注册' }}</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background-image: url('/dl.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  overflow: hidden;
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
  z-index: 1;
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

.login-wrapper {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 80px 2rem 2rem;
  gap: 0;
}

.left-section {
  flex: 1;
  max-width: 600px;
  z-index: 1;
}

.image-container {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  
  img {
    width: 100%;
    height: 550px;
    object-fit: cover;
  }
}

.right-section {
  flex: 1;
  max-width: 500px;
  margin-left: -100px;
  z-index: 2;
}

.login-card {
  width: 430px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.5) 0%, rgba(255, 248, 240, 0.5) 100%);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 2rem;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.1);
}

.login-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 1rem;
  position: relative;
  display: inline-block;
  padding-bottom: 4px;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 66%;
    height: 3px;
    background: linear-gradient(90deg, var(--color-accent), #ff8fab);
    transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  }
  
  &:hover::after {
    width: 100%;
  }
}

.links-row {
  margin-bottom: 0.5rem;
  
  .link {
    font-size: 0.875rem;
    color: #666;
    text-decoration: none;
    
    &:hover {
      color: var(--color-accent);
    }
  }
}

.login-form {
  margin-top: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
  position: relative;
}

.form-input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: none;
  border-bottom: 2px solid transparent;
  background: transparent;
  font-size: 0.9rem;
  color: #333;
  outline: none;
  position: relative;
  z-index: 1;
  
  &::placeholder {
    color: #999;
  }
}

.captcha-group {
  display: flex;
  gap: 0.75rem;
  
  .captcha-input {
    flex: 1;
  }
  
  .captcha-btn {
    padding: 0.875rem 1rem;
    background: transparent;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 0.875rem;
    color: #666;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover:not(:disabled) {
      border-color: var(--color-accent);
      color: var(--color-accent);
    }
    
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.form-group::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: #e0e0e0;
  z-index: 0;
}

.form-group::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--color-accent), #ff8fab);
  transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  z-index: 1;
}

.form-group:hover::after {
  width: 100%;
}

.form-group:focus-within::after {
  width: 100%;
}

.links-row {
  margin-bottom: 0.5rem;
  
  .link {
    font-size: 0.875rem;
    color: #666;
    text-decoration: none;
    transition: color 0.3s;
    
    &:hover {
      color: var(--color-accent);
    }
  }
}

.login-btn {
  width: 200px;
  margin: 0 auto;
  padding: 0.6rem;
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.register-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  
  &:hover:not(:disabled) {
    box-shadow: 0 10px 25px rgba(16, 185, 129, 0.4);
  }
}

.loading-icon {
  .spin {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 900px) {
  .login-wrapper {
    flex-direction: column;
    gap: 1rem;
  }
  
  .left-section,
  .right-section {
    max-width: 100%;
    width: 100%;
    margin-left: 0;
  }
  
  .image-container img {
    height: 250px;
  }
}

@media (max-width: 500px) {
  .login-card {
    padding: 1.5rem;
  }
  
  .nav {
    gap: 1rem;
  }
  
  .header-inner {
    padding: 0.75rem 1rem;
  }
}
</style>