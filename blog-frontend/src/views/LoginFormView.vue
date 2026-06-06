<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import type { LoginDTO, RegisterDTO } from '@/types'
import Header from '@/components/layout/Header.vue'

const router = useRouter()
const { login, register } = useAuth()

const isLogin = ref(true)
const isLoading = ref(false)
const error = ref('')

const loginForm = ref<LoginDTO>({
  username: '',
  password: ''
})

const registerForm = ref<RegisterDTO>({
  username: '',
  email: '',
  password: ''
})

async function handleSubmit() {
  error.value = ''
  isLoading.value = true
  
  try {
    if (isLogin.value) {
      await login(loginForm.value)
      router.push('/')
    } else {
      await register(registerForm.value)
      isLogin.value = true
    }
  } catch (e) {
    error.value = '操作失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

function goBack() {
  router.push('/login')
}
</script>

<template>
  <div class="form-container">
    <Header />
    <div class="form-card">
      <button class="back-btn" @click="goBack">
        ← 返回
      </button>
      
      <h2 class="form-title">
        {{ isLogin ? '博主登录' : '注册账号' }}
      </h2>
      
      <form @submit.prevent="handleSubmit" class="form">
        <div v-if="!isLogin" class="form-group">
          <label>邮箱</label>
          <input 
            v-model="registerForm.email"
            type="email" 
            placeholder="请输入邮箱"
            required
          />
        </div>
        
        <div class="form-group">
          <label>{{ isLogin ? '用户名' : '用户名（3-50字符）' }}</label>
          <input 
            v-if="isLogin"
            v-model="loginForm.username"
            type="text" 
            placeholder="请输入用户名"
            required
          />
          <input 
            v-else
            v-model="registerForm.username"
            type="text" 
            placeholder="请输入用户名"
            required
          />
        </div>
        
        <div class="form-group">
          <label>{{ isLogin ? '密码' : '密码（至少8位）' }}</label>
          <input 
            v-if="isLogin"
            v-model="loginForm.password"
            type="password" 
            placeholder="请输入密码"
            required
          />
          <input 
            v-else
            v-model="registerForm.password"
            type="password" 
            placeholder="请输入密码"
            required
          />
        </div>
        
        <button type="submit" class="submit-btn" :disabled="isLoading">
          {{ isLoading ? '处理中...' : (isLogin ? '登录' : '注册') }}
        </button>
        
        <p v-if="error" class="error-message">{{ error }}</p>
      </form>
      
      <p class="switch-text">
        {{ isLogin ? '还没有账号？' : '已有账号？' }}
        <button class="switch-btn" @click="isLogin = !isLogin">
          {{ isLogin ? '立即注册' : '立即登录' }}
        </button>
      </p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.form-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1628 0%, #0f1d32 50%, #162337 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
}

.form-card {
  background: rgba(22, 35, 55, 0.8);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 2rem;
  width: 100%;
  max-width: 400px;
  backdrop-filter: blur(10px);
}

.back-btn {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  transition: color 0.3s;
  
  &:hover {
    color: var(--color-accent);
  }
}

.form-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  text-align: center;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  
  label {
    font-size: 0.875rem;
    color: var(--color-text-secondary);
  }
  
  input {
    padding: 0.75rem 1rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--color-border);
    border-radius: 8px;
    color: var(--color-text-primary);
    font-size: 1rem;
    transition: border-color 0.3s;
    
    &:focus {
      outline: none;
      border-color: var(--color-accent);
    }
    
    &::placeholder {
      color: var(--color-text-secondary);
    }
  }
}

.submit-btn {
  padding: 0.875rem;
  background: linear-gradient(135deg, #00d4ff 0%, #a855f7 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.4);
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  text-align: center;
  margin-top: 0.5rem;
}

.switch-text {
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  margin-top: 1.5rem;
}

.switch-btn {
  background: none;
  border: none;
  color: var(--color-accent);
  cursor: pointer;
  font-size: 0.875rem;
  
  &:hover {
    text-decoration: underline;
  }
}
</style>
