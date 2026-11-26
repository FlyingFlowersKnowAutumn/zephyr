<script setup lang="ts">
import { useRouter } from 'vue-router'
import SeoHead from '@/components/seo/SeoHead.vue'
import LayoutWrapper from '@/components/layout/LayoutWrapper.vue'

const router = useRouter()

function goHome() {
  router.push('/')
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<template>
  <div class="not-found-page">
    <SeoHead title="页面不存在" description="你访问的页面不存在或已被删除。" />
    <LayoutWrapper :show-sidebar="false" :show-hero="false">
      <div class="not-found-card">
        <div class="code">404</div>
        <h2 class="title">这个页面走丢了</h2>
        <p class="desc">地址可能输错了，或者这篇文章已经被删除。</p>
        <div class="actions">
          <button class="btn-primary" @click="goHome">回到首页</button>
          <button class="btn-ghost" @click="goBack">返回上一页</button>
        </div>
      </div>
    </LayoutWrapper>
  </div>
</template>

<style lang="scss" scoped>
.not-found-page {
  min-height: 100vh;
  /* 没有大图 hero，靠顶部留白避开固定顶栏 */
  padding: 7rem 1.5rem 4rem;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.not-found-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg, 14px);
  box-shadow: var(--shadow-card);
  padding: 4rem 2rem;
  text-align: center;
  max-width: 560px;
  width: 100%;
}

.code {
  font-size: 4.5rem;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 0.06em;
  background: linear-gradient(135deg, var(--color-accent) 0%, #7c5cff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.title {
  margin: 1.2rem 0 0.6rem;
  font-size: 1.5rem;
  color: var(--color-text-primary);
}

.desc {
  margin: 0 0 2rem;
  color: var(--color-text-secondary);
  font-size: 0.95rem;
}

.actions {
  display: flex;
  gap: 0.9rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary,
.btn-ghost {
  padding: 0.65rem 1.6rem;
  border-radius: 999px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary {
  border: none;
  background: var(--color-accent);
  color: #fff;
}

.btn-primary:hover {
  filter: brightness(1.08);
  transform: translateY(-1px);
}

.btn-ghost {
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-secondary);
}

.btn-ghost:hover {
  border-color: var(--color-accent-soft);
  color: var(--color-accent);
}
</style>
