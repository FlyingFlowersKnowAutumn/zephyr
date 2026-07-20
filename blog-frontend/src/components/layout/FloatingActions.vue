<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useDarkMode } from '@/composables/useDarkMode'

const { isDark, toggleDark } = useDarkMode()
const visible = ref(false)

function onScroll() {
  visible.value = window.scrollY > window.innerHeight
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function setLight() {
  if (isDark.value) toggleDark()
}

function setDark() {
  if (!isDark.value) toggleDark()
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <div class="floating-actions" :class="{ visible }">
    <div class="theme-picker">
      <button
        class="action-btn"
        :class="{ active: !isDark }"
        @click="setLight"
        title="亮色模式"
      >
        <svg class="icon sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
          <circle cx="12" cy="12" r="4.5"/>
          <g class="rays">
            <line x1="12" y1="1.5" x2="12" y2="4"/>
            <line x1="12" y1="20" x2="12" y2="22.5"/>
            <line x1="4.6" y1="4.6" x2="6.3" y2="6.3"/>
            <line x1="17.7" y1="17.7" x2="19.4" y2="19.4"/>
            <line x1="1.5" y1="12" x2="4" y2="12"/>
            <line x1="20" y1="12" x2="22.5" y2="12"/>
            <line x1="4.6" y1="19.4" x2="6.3" y2="17.7"/>
            <line x1="17.7" y1="6.3" x2="19.4" y2="4.6"/>
          </g>
        </svg>
      </button>
      <button
        class="action-btn"
        :class="{ active: isDark }"
        @click="setDark"
        title="暗色模式"
      >
        <svg class="icon moon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/>
          <g class="stars">
            <circle cx="17.5" cy="3" r="0.8" fill="currentColor" stroke="none"/>
          </g>
        </svg>
      </button>
    </div>
    <span class="divider"></span>
    <button class="action-btn top-btn" @click="scrollToTop" title="回到顶部">
      <svg class="icon arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="12" y1="19" x2="12" y2="5"/>
        <polyline points="6 11 12 5 18 11"/>
      </svg>
    </button>
  </div>
</template>

<style lang="scss" scoped>
.floating-actions {
  position: fixed;
  right: 1.5rem;
  bottom: 2.5rem;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 28px;
  padding: 0.35rem;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.02);
  opacity: 0;
  transform: translateY(16px) scale(0.9);
  pointer-events: none;
  transition: opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1), transform 0.35s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s ease;

  &.visible {
    opacity: 1;
    transform: translateY(0) scale(1);
    pointer-events: auto;
  }

  &:hover {
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.04);
    transform: translateY(-2px);
  }
}

.theme-picker {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.1rem;
}

.action-btn {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 50%;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.25s ease;

  &:hover {
    color: var(--color-accent);
    background: var(--color-hover-bg);
  }

  &.active {
    color: var(--color-accent);
    background: var(--color-hover-bg);
  }
}

.icon {
  width: 18px;
  height: 18px;
  transition: transform 0.4s ease;
}

.sun .rays {
  transform-origin: center;
  transition: transform 0.6s ease;
}
.action-btn.active .sun .rays,
.action-btn:hover .sun .rays {
  transform: rotate(90deg);
}

.moon {
  transition: transform 0.4s ease;
}
.action-btn:hover .moon {
  transform: rotate(-20deg);
}

.moon .stars {
  opacity: 0;
  transition: opacity 0.3s ease;
}
.action-btn.active .moon .stars {
  opacity: 1;
}

.divider {
  width: 20px;
  height: 1px;
  background: var(--color-border);
  margin: 0.15rem 0;
}
</style>
