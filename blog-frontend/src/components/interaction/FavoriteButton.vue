<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import * as favoriteApi from '@/api/favorite'

const props = defineProps<{ articleId: string }>()
const authStore = useAuthStore()

const favorited = ref(false)
const favoriteCount = ref(0)
const animating = ref(false)

onMounted(async () => {
  const result = await favoriteApi.getFavoriteStatus(props.articleId)
  if (result.code === 0 && result.data) {
    favorited.value = result.data.favorited
    favoriteCount.value = result.data.favoriteCount
  }
})

async function toggle() {
  if (!authStore.isLoggedIn) return
  animating.value = true
  const result = await favoriteApi.toggleFavorite(props.articleId)
  if (result.code === 0 && result.data) {
    favorited.value = result.data.favorited
    favoriteCount.value = result.data.favoriteCount
  }
  setTimeout(() => { animating.value = false }, 400)
}
</script>

<template>
  <button
    class="favorite-button"
    :class="{ favorited, animating }"
    :disabled="!authStore.isLoggedIn"
    @click="toggle"
  >
    <svg class="star-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
    </svg>
    <span class="favorite-count">{{ favoriteCount }}</span>
  </button>
</template>

<style lang="scss" scoped>
.favorite-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.6rem 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: 24px;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover:not(:disabled) {
    border-color: #f59e0b;
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &.favorited {
    border-color: #f59e0b;
    background: rgba(245, 158, 11, 0.15);
    color: #f59e0b;

    .star-icon {
      fill: #f59e0b;
      stroke: #f59e0b;
    }
  }

  &.animating {
    animation: starPop 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }
}

.star-icon {
  width: 18px;
  height: 18px;
  stroke: currentColor;
  fill: transparent;
  transition: fill 0.3s ease;
}

.favorite-count {
  font-weight: 500;
  font-size: 0.875rem;
}

@keyframes starPop {
  0% {
    transform: scale(1) rotate(0deg);
  }
  30% {
    transform: scale(1.3) rotate(15deg);
  }
  50% {
    transform: scale(0.9) rotate(-10deg);
  }
  70% {
    transform: scale(1.1) rotate(5deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
  }
}
</style>
