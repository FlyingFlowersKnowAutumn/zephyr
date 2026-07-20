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

const isLoginMode = ref(true)
const isPasswordlessMode = ref(false)
const showResetModal = ref(false)

const username = ref('')
const password = ref('')
const email = ref('')
const captcha = ref('')
const resetEmail = ref('')
const resetCaptcha = ref('')
const resetNewPassword = ref('')

const captchaBtnText = ref('验证码')
const resetCaptchaBtnText = ref('验证码')
const isCaptchaDisabled = ref(false)
const isResetCaptchaDisabled = ref(false)
const isLoading = ref(false)

onBeforeMount(() => {
  if (authStore.isLoggedIn) {
    router.push('/')
  }
})

async function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    ElMessage.warning('请填写邮箱/用户名和密码')
    return
  }
  isLoading.value = true
  try {
    await authStore.login({
      username: username.value,
      password: password.value
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    isLoading.value = false
  }
}

async function handlePasswordlessLogin() {
  if (!email.value.trim() || !captcha.value.trim()) {
    ElMessage.warning('请填写邮箱和验证码')
    return
  }
  isLoading.value = true
  try {
    await authStore.guestLogin({
      email: email.value,
      code: captcha.value
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    isLoading.value = false
  }
}

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
    isLoginMode.value = true
    isPasswordlessMode.value = false
    username.value = ''
    password.value = ''
    email.value = ''
    captcha.value = ''
  } catch (error: any) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    isLoading.value = false
  }
}

async function handleResetPassword() {
  if (!resetEmail.value.trim() || !resetCaptcha.value.trim() || !resetNewPassword.value.trim()) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  
  isLoading.value = true
  try {
    await authApi.updatePassword({
      email: resetEmail.value,
      code: resetCaptcha.value,
      newPassword: resetNewPassword.value
    })
    ElMessage.success('密码修改成功')
    showResetModal.value = false
    resetEmail.value = ''
    resetCaptcha.value = ''
    resetNewPassword.value = ''
  } catch (error: any) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    isLoading.value = false
  }
}

async function getCaptcha() {
  const targetEmail = isPasswordlessMode.value ? email.value : (isLoginMode.value ? email.value : email.value)
  if (!targetEmail.trim()) {
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
    const type = isLoginMode.value && !isPasswordlessMode.value ? 'FORGOT_PASSWORD' : 'REGISTER'
    await authApi.sendCode({ email: targetEmail, type })
    ElMessage.success('验证码发送成功')
  } catch (error: any) {
    clearInterval(timer)
    isCaptchaDisabled.value = false
    captchaBtnText.value = '验证码'
    ElMessage.error(error.response?.data?.message || '验证码发送失败')
  }
}

async function getResetCaptcha() {
  if (!resetEmail.value.trim()) {
    ElMessage.warning('请先填写邮箱')
    return
  }

  isResetCaptchaDisabled.value = true
  resetCaptchaBtnText.value = '60s'

  // 倒计时先启动，不等 API 返回
  let count = 60
  const timer = setInterval(() => {
    count--
    resetCaptchaBtnText.value = `${count}s`

    if (count <= 0) {
      clearInterval(timer)
      isResetCaptchaDisabled.value = false
      resetCaptchaBtnText.value = '验证码'
    }
  }, 1000)

  try {
    await authApi.sendCode({ email: resetEmail.value, type: 'FORGOT_PASSWORD' })
    ElMessage.success('验证码发送成功')
  } catch (error: any) {
    clearInterval(timer)
    isResetCaptchaDisabled.value = false
    resetCaptchaBtnText.value = '验证码'
    ElMessage.error(error.response?.data?.message || '验证码发送失败')
  }
}

function toggleMode() {
  isLoginMode.value = !isLoginMode.value
  isPasswordlessMode.value = false
}

function togglePasswordlessMode() {
  isPasswordlessMode.value = !isPasswordlessMode.value
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
          <img src="/2.png" alt="背景图" />
        </div>
      </div>
      
      <div class="right-section">
        <div class="login-card">
          <h2 class="login-title">{{ isLoginMode ? (isPasswordlessMode ? '登录' : '登录') : '注册' }}</h2>
          
          <div v-if="isLoginMode && !isPasswordlessMode" class="links-row">
            <a href="#" class="link" @click.prevent="toggleMode">
              没有账号？立即注册 &gt;
            </a>
          </div>
          
          <div v-else-if="isLoginMode && isPasswordlessMode" class="links-row">
            <a href="#" class="link" @click.prevent="toggleMode">
              没有账号？立即注册 &gt;
            </a>
          </div>
          
          <div v-else class="links-row">
            <a href="#" class="link" @click.prevent="toggleMode">
              已有账号？立即登录 &gt;
            </a>
          </div>
          
          <form v-if="isLoginMode && !isPasswordlessMode" class="login-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="form-label">邮箱/用户名</label>
              <input
                v-model="username"
                type="text"
                placeholder="邮箱或用户名"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">密码</label>
              <input
                v-model="password"
                type="password"
                placeholder="密码"
                class="form-input"
              />
            </div>
            
            <div class="form-links">
              <a href="#" class="form-link" @click.prevent="showResetModal = true">修改密码</a>
              <a href="#" class="form-link" @click.prevent="togglePasswordlessMode">免密登录</a>
            </div>
            
            <button type="submit" class="login-btn" :disabled="isLoading">
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
          </form>
          
          <form v-else-if="isLoginMode && isPasswordlessMode" class="login-form" @submit.prevent="handlePasswordlessLogin">
            <div class="form-group">
              <label class="form-label">邮箱</label>
              <input
                v-model="email"
                type="email"
                placeholder="邮箱"
                class="form-input"
              />
            </div>
            
            <div class="form-group captcha-group">
              <div class="captcha-input-wrapper">
                <label class="form-label">验证码</label>
                <input
                  v-model="captcha"
                  type="text"
                  placeholder="验证码"
                  class="form-input"
                />
              </div>
              <button type="button" class="captcha-btn" :disabled="isCaptchaDisabled" @click="getCaptcha">
                {{ captchaBtnText }}
              </button>
            </div>
            
            <div class="form-links">
              <a href="#" class="form-link" @click.prevent="togglePasswordlessMode">账号密码登录</a>
            </div>
            
            <button type="submit" class="login-btn" :disabled="isLoading">
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
          </form>
          
          <form v-else class="login-form" @submit.prevent="handleRegister">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input
                v-model="username"
                type="text"
                placeholder="用户名"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">密码</label>
              <input
                v-model="password"
                type="password"
                placeholder="密码"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">邮箱</label>
              <input
                v-model="email"
                type="email"
                placeholder="邮箱"
                class="form-input"
              />
            </div>
            
            <div class="form-group captcha-group">
              <div class="captcha-input-wrapper">
                <label class="form-label">验证码</label>
                <input
                  v-model="captcha"
                  type="text"
                  placeholder="验证码"
                  class="form-input"
                />
              </div>
              <button type="button" class="captcha-btn" :disabled="isCaptchaDisabled" @click="getCaptcha">
                {{ captchaBtnText }}
              </button>
            </div>
            
            <button type="submit" class="login-btn register-btn" :disabled="isLoading">
              {{ isLoading ? '注册中...' : '注册' }}
            </button>
          </form>
        </div>
      </div>
    </div>
    
    <div v-if="showResetModal" class="modal-overlay" @click.self="showResetModal = false">
      <div class="modal-content">
        <button class="modal-close" @click="showResetModal = false">&times;</button>
        <h3 class="modal-title">修改密码</h3>
        
        <form class="reset-form" @submit.prevent="handleResetPassword">
          <div class="form-group">
            <label class="form-label">邮箱:</label>
            <input
              v-model="resetEmail"
              type="email"
              placeholder="邮箱"
              class="form-input"
            />
          </div>
          
          <div class="form-group captcha-group">
            <div class="captcha-input-wrapper">
              <label class="form-label">验证码:</label>
              <input
                v-model="resetCaptcha"
                type="text"
                placeholder="验证码"
                class="form-input"
              />
            </div>
            <button type="button" class="captcha-btn" :disabled="isResetCaptchaDisabled" @click="getResetCaptcha">
              {{ resetCaptchaBtnText }}
            </button>
          </div>
          
          <div class="form-group">
            <label class="form-label">新密码:</label>
            <input
              v-model="resetNewPassword"
              type="password"
              placeholder="新密码"
              class="form-input"
            />
          </div>
          
          <div class="modal-actions">
            <button type="submit" class="modal-btn modal-btn-submit full-width" :disabled="isLoading">提交</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background-image: url('/1.png');
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
  width: 380px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.7) 0%, rgba(255, 248, 240, 0.7) 100%);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.login-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
  text-align: center;
  position: relative;
  display: inline-block;
  padding-bottom: 4px;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #00d4ff, #a855f7);
    border-radius: 2px;
  }
}

.links-row {
  text-align: center;
  margin-bottom: 1rem;
  
  .link {
    font-size: 0.75rem;
    color: #666;
    text-decoration: none;
    
    &:hover {
      color: #00d4ff;
    }
  }
}

.login-form {
  margin-top: 0.5rem;
}

.form-group {
  margin-bottom: 0.875rem;
  position: relative;
}

.form-label {
  font-size: 0.7rem;
  color: #666;
  margin-bottom: 0.375rem;
  display: block;
}

.form-input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.8);
  font-size: 0.8rem;
  color: #333;
  outline: none;
  transition: all 0.2s;
  
  &::placeholder {
    color: #999;
  }
  
  &:focus {
    border-color: #00d4ff;
    box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
  }
}

.captcha-group {
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
  
  .captcha-input-wrapper {
    flex: 1;
  }
}

.captcha-btn {
  padding: 0.375rem 0.875rem;
  background: linear-gradient(135deg, #87ceeb 0%, #6ba3e0 100%);
  border: none;
  border-radius: 6px;
  font-size: 0.7rem;
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  height: fit-content;
  
  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #7bc7e8 0%, #5b98d4 100%);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.form-links {
  display: flex;
  justify-content: flex-end;
  gap: 1.25rem;
  margin-bottom: 1.25rem;
  
  .form-link {
    font-size: 0.7rem;
    color: #666;
    text-decoration: none;
    transition: color 0.2s;
    
    &:hover {
      color: #00d4ff;
    }
  }
}

.login-btn {
  width: 100%;
  margin: 0 auto;
  padding: 0.5rem;
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.375rem;
  
  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(0, 212, 255, 0.3);
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.register-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  
  &:hover:not(:disabled) {
    box-shadow: 0 6px 20px rgba(16, 185, 129, 0.3);
  }
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
  z-index: 100;
  backdrop-filter: blur(5px);
}

.modal-content {
  width: 350px;
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  position: relative;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
}

.modal-close {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  font-size: 1.25rem;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    color: #333;
  }
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 1.25rem;
}

.reset-form {
  .form-group {
    margin-bottom: 0.75rem;
  }
  
  .form-label {
    font-size: 0.65rem;
    color: #666;
    margin-bottom: 0.25rem;
  }
  
  .form-input {
    padding: 0.4375rem 0.625rem;
    font-size: 0.75rem;
  }
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.25rem;
}

.modal-btn {
  flex: 1;
  padding: 0.4375rem;
  border: none;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-btn-cancel {
  background: #333;
  color: white;
  
  &:hover {
    background: #444;
  }
}

.modal-btn-submit {
  background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
  color: white;
  
  &:hover:not(:disabled) {
    box-shadow: 0 4px 15px rgba(155, 89, 182, 0.3);
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
  
  &.full-width {
    flex: none;
    width: 100%;
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
    width: 100%;
    padding: 1.25rem;
  }
  
  .modal-content {
    width: 90%;
    padding: 1.25rem;
  }
}
</style>