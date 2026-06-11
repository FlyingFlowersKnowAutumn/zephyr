<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const emit = defineEmits<{
  (e: 'open-search'): void
}>()

const isScrolled = ref(false)
const lastScrollY = ref(0)
const isVisible = ref(true)
const authStore = useAuthStore()
const router = useRouter()
const showUserMenu = ref(false)

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

function handleScroll() {
  const currentScrollY = window.scrollY
  
  isScrolled.value = currentScrollY > 50
  
  if (currentScrollY > lastScrollY.value && currentScrollY > 100) {
    // 向下滚动，隐藏导航栏
    isVisible.value = false
  } else {
    // 向上滚动，显示导航栏
    isVisible.value = true
  }
  
  lastScrollY.value = currentScrollY
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

function handleSearch() {
  emit('open-search')
}
</script>

<template>
  <header class="header" :class="{ scrolled: isScrolled, hidden: !isVisible }">
    <div class="header-inner">
      <div class="left-section">
        <div class="logo">
          <RouterLink to="/">Linzy 知识库</RouterLink>
        </div>
      </div>
      
      <div class="right-section">
        <nav class="nav">
          <div class="nav-item">
            <div class="search-icon" @click="handleSearch">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <span>搜索</span>
              <span class="search-underline"></span>
            </div>
          </div>
          <div class="nav-item">
            <RouterLink to="/">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" style="margin-right: 4px;">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              </svg>
              <span class="link-text">首页</span>
              <span class="underline"></span>
            </RouterLink>
          </div>
          <div class="nav-item">
            <RouterLink to="/archive">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" style="margin-right: 4px;">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              <span class="link-text">归档</span>
              <span class="underline"></span>
            </RouterLink>
          </div>
          <div class="nav-item">
            <RouterLink to="/categories">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" style="margin-right: 4px;">
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
              </svg>
              <span class="link-text">分类</span>
              <span class="underline"></span>
            </RouterLink>
          </div>
          <div class="nav-item">
            <RouterLink to="/tags">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" style="margin-right: 4px;">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                <line x1="7" y1="7" x2="7.01" y2="7"/>
              </svg>
              <span class="link-text">标签</span>
              <span class="underline"></span>
            </RouterLink>
          </div>
          <div class="nav-item">
            <RouterLink to="/guestbook">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" style="margin-right: 4px;">
                <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"/>
              </svg>
              <span class="link-text">留言</span>
              <span class="underline"></span>
            </RouterLink>
          </div>
        </nav>
        
        <div class="auth-section">
          <RouterLink v-if="!authStore.isLoggedIn" to="/login" class="login-btn">
            登录
          </RouterLink>
          <div v-else class="user-menu-wrapper" @mouseenter="showUserMenu = true" @mouseleave="showUserMenu = false">
            <button class="user-avatar-btn">
              <img v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl" alt="avatar" />
              <span v-else class="avatar-placeholder">{{ authStore.user?.username?.charAt(0) || 'U' }}</span>
            </button>
            <div v-show="showUserMenu" class="user-dropdown">
              <div class="dropdown-header">
                <img v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl" alt="avatar" />
                <div class="user-info">
                  <span class="username">{{ authStore.user?.displayName || authStore.user?.username || '用户' }}</span>
                  <span class="user-level">Lv.1</span>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-menu">
                <RouterLink to="/profile" class="menu-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  <span>个人中心</span>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                  </svg>
                </RouterLink>
                <div class="menu-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M8 21l4-8 4 8M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7"/>
                    <polyline points="7.5 4.27 9 12 15 12 16.5 4.27"/>
                    <line x1="12" y1="22" x2="12" y2="12"/>
                  </svg>
                  <span>会员中心</span>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                  </svg>
                </div>
                <div class="menu-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <line x1="3" y1="9" x2="21" y2="9"/>
                    <line x1="9" y1="21" x2="9" y2="9"/>
                  </svg>
                  <span>订单管理</span>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                  </svg>
                </div>
                <div class="menu-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 20h9"/>
                    <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
                  </svg>
                  <span>工单管理</span>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                  </svg>
                </div>
                <div class="dropdown-divider"></div>
                <button class="menu-item logout" @click="handleLogout">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  <span>退出</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style lang="scss" scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: transparent;
  border-bottom: 1px solid transparent;
  transition: transform 0.4s ease, background 0.3s ease;
  margin: 0;
  width: 100%;
  transform: translateY(0);

  &.hidden {
    transform: translateY(-100%);
  }

  &.scrolled {
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 30px rgba(0, 0, 0, 0.08);
  }
}

.header-inner {
  width: 100%;
  margin: 0;
  padding: 0 2rem;
  height: 55px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
}

.left-section {
  flex-shrink: 0;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex: 1;
  justify-content: flex-end;
}

.auth-section {
  display: flex;
  align-items: center;
}

.login-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.3);
  color: white;
  font-size: 0.85rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: all 0.3s ease;
  box-shadow: none;
  backdrop-filter: blur(8px);
  
  &:hover {
    transform: scale(1.08);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
    text-decoration: none;
    color: white;
  }
}

.user-menu-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.user-avatar-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8fab 100%);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 107, 157, 0.4);
  overflow: hidden;
  
  &:hover {
    transform: scale(1.08);
    box-shadow: 0 6px 20px rgba(255, 107, 157, 0.5);
  }
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .avatar-placeholder {
    color: white;
    font-size: 1rem;
    font-weight: 600;
  }
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1000;
  animation: dropdownFadeIn 0.2s ease;
  
  &::before {
    content: '';
    position: absolute;
    top: -8px;
    right: 14px;
    width: 0;
    height: 0;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    border-bottom: 8px solid white;
  }
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #fef3f8 0%, #fce7f3 100%);
  
  img {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid white;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  }
  
  .user-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  
  .username {
    font-size: 1rem;
    font-weight: 600;
    color: #1a1a1a;
  }
  
  .user-level {
    font-size: 0.75rem;
    color: var(--color-accent);
    background: rgba(0, 212, 255, 0.1);
    padding: 2px 8px;
    border-radius: 10px;
    align-self: flex-start;
  }
}

.dropdown-divider {
  height: 1px;
  background: #f0f0f0;
}

.dropdown-menu {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: #666;
  font-size: 0.875rem;
  cursor: pointer;
  transition: background 0.2s ease;
  text-decoration: none;
  
  &:hover {
    background: #f8f9fa;
    color: #1a1a1a;
  }
  
  &.logout:hover {
    background: #fff5f5;
    color: #ef4444;
  }
  
  svg:last-child {
    margin-left: auto;
    opacity: 0.5;
  }
}

.logo {
  font-size: 1.1rem;
  font-weight: 700;
  z-index: 10;
  white-space: nowrap;

  a {
    color: white;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
    transition: color var(--transition-default);

    &:hover {
      text-decoration: none;
      opacity: 0.9;
    }
  }
}

.nav {
  display: flex;
  gap: 1.4rem;
  align-items: center;

  .nav-item {
    .search-icon {
      color: white;
      display: flex;
      align-items: center;
      gap: 4px;
      cursor: pointer;
      font-size: 0.85rem;
      font-weight: 500;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
      transition: color var(--transition-default);
      position: relative;
      padding-bottom: 2px;

      &:hover {
        color: var(--color-accent);
      }

      .search-underline {
        position: absolute;
        bottom: -2px;
        left: 0;
        width: 0;
        height: 2px;
        background: var(--color-accent);
        transition: width 0.3s ease;
      }

      &:hover .search-underline {
        width: 100%;
      }
    }

    a {
      color: white;
      font-size: 0.85rem;
      font-weight: 500;
      display: flex;
      align-items: center;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
      transition: color var(--transition-default);
      position: relative;
      padding-bottom: 2px;

      &:hover {
        color: var(--color-accent);
        text-decoration: none;
      }

      .link-text {
        position: relative;
      }

      .underline {
        position: absolute;
        bottom: -2px;
        left: 0;
        width: 0;
        height: 2px;
        background: var(--color-accent);
        transition: width 0.3s ease;
      }

      &:hover .underline {
        width: 100%;
      }
    }
  }
}

.header.scrolled {
  .logo a {
    color: #1a1a1a;
    text-shadow: none;
  }

  .nav .nav-item .search-icon {
    color: #1a1a1a;
    text-shadow: none;
    
    &:hover {
      color: var(--color-accent);
    }
  }

  .nav .nav-item a {
    color: #1a1a1a;
    text-shadow: none;
    
    &:hover {
      color: var(--color-accent);
    }
  }
}

@media (max-width: 1024px) {
  .header-inner {
    padding: 0 2rem;
  }
  
  .nav-item span {
    display: none;
  }
  
  .nav-item svg {
    margin-right: 0 !important;
  }
}

@media (max-width: 768px) {
  .header-inner {
    padding: 0 1rem;
    gap: 1rem;
  }

  .nav {
    gap: 0.8rem;
  }

  .search-icon span {
    display: none;
  }
}
</style>
