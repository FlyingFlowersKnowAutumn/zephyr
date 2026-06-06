<script setup lang="ts">
import { useDarkMode } from '@/composables/useDarkMode'

const { isDark, toggleDark } = useDarkMode()
</script>

<template>
  <button class="dark-toggle" @click="toggleDark()" :title="isDark ? '切换到亮色模式' : '切换到暗色模式'">
    <span v-if="isDark" class="toggle-icon">☀️</span>
    <span v-else class="toggle-icon">🌙</span>
  </button>
</template>

<style lang="scss" scoped>
.dark-toggle {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }
}

.toggle-icon {
  line-height: 1;
}
</style>
