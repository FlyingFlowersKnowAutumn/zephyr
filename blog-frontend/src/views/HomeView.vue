<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { user, isLoggedIn, logout, fetchUser } = useAuth()

onMounted(() => {
  fetchUser()
})
</script>

<template>
  <div class="home-container">
    <header class="header">
      <div class="logo">
        <span class="text-gradient">技术博客</span>
      </div>
      <nav class="nav">
        <a href="/" class="nav-link active">首页</a>
        <a href="/write" class="nav-link">写文章</a>
        <a href="/profile" class="nav-link">个人中心</a>
      </nav>
      <div class="auth-section">
        <span v-if="user" class="user-info">
          {{ user.isGuest ? user.displayName : user.username }}
        </span>
        <button v-if="isLoggedIn" class="logout-btn" @click="logout">
          退出
        </button>
        <a v-else href="/login" class="login-link">登录</a>
      </div>
    </header>
    
    <main class="main-content">
      <section class="hero">
        <h1>欢迎来到我的技术博客</h1>
        <p>记录技术成长，分享编程心得</p>
      </section>
      
      <section class="articles-section">
        <h2>最新文章</h2>
        <div class="article-list">
          <div class="article-card">
            <h3>第一篇文章</h3>
            <p>这是文章摘要...</p>
            <span class="date">2024-01-01</span>
          </div>
          <div class="article-card">
            <h3>第二篇文章</h3>
            <p>这是文章摘要...</p>
            <span class="date">2024-01-02</span>
          </div>
          <div class="article-card">
            <h3>第三篇文章</h3>
            <p>这是文章摘要...</p>
            <span class="date">2024-01-03</span>
          </div>
        </div>
      </section>
    </main>
    
    <footer class="footer">
      <p>© 2024 个人技术博客</p>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
  background: var(--color-bg-primary);
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(22, 35, 55, 0.8);
  border-bottom: 1px solid var(--color-border);
}

.logo {
  font-size: 1.25rem;
  font-weight: 700;
}

.nav {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color 0.3s;
  
  &:hover, &.active {
    color: var(--color-accent);
  }
}

.auth-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

.logout-btn {
  padding: 0.5rem 1rem;
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid var(--color-error);
  border-radius: 6px;
  color: var(--color-error);
  cursor: pointer;
  font-size: 0.875rem;
  transition: background 0.3s;
  
  &:hover {
    background: rgba(239, 68, 68, 0.3);
  }
}

.login-link {
  padding: 0.5rem 1rem;
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid var(--color-accent);
  border-radius: 6px;
  color: var(--color-accent);
  text-decoration: none;
  font-size: 0.875rem;
  transition: background 0.3s;
  
  &:hover {
    background: rgba(0, 212, 255, 0.3);
  }
}

.main-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.hero {
  text-align: center;
  margin-bottom: 3rem;
  
  h1 {
    font-size: 2.5rem;
    margin-bottom: 1rem;
  }
  
  p {
    color: var(--color-text-secondary);
  }
}

.articles-section {
  h2 {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
  }
}

.article-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.article-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
  transition: transform 0.3s, box-shadow 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-card);
  }
  
  h3 {
    font-size: 1.125rem;
    margin-bottom: 0.5rem;
    color: var(--color-text-primary);
  }
  
  p {
    color: var(--color-text-secondary);
    font-size: 0.875rem;
    margin-bottom: 1rem;
  }
  
  .date {
    font-size: 0.75rem;
    color: var(--color-text-secondary);
  }
}

.footer {
  text-align: center;
  padding: 1.5rem;
  border-top: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .nav {
    gap: 1rem;
  }
  
  .article-list {
    grid-template-columns: 1fr;
  }
}
</style>
