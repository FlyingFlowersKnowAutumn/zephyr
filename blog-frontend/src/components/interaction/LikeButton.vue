<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import * as reactionApi from '@/api/reaction'

const props = defineProps<{ articleId: string }>()
const authStore = useAuthStore()

const liked = ref(false)
const likeCount = ref(0)
const animating = ref(false)

onMounted(async () => {
  const result = await reactionApi.getReactionStatus(props.articleId)
  if (result.code === 0 && result.data) {
    liked.value = result.data.liked
    likeCount.value = result.data.likeCount
  }
})

async function toggle() {
  if (!authStore.isLoggedIn) return
  animating.value = true
  const result = await reactionApi.toggleReaction(props.articleId)
  if (result.code === 0 && result.data) {
    liked.value = result.data.liked
    likeCount.value = result.data.likeCount
  }
  setTimeout(() => { animating.value = false }, 400)
}
</script>

<template>
  <button
    class="like-button"
    :class="{ liked, animating }"
    :disabled="!authStore.isLoggedIn"
    @click="toggle"
  >
    <svg class="heart-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
    </svg>
    <span class="like-count">{{ likeCount }}</span>
  </button>
</template>

<style lang="scss" scoped>
.like-button {
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
    border-color: #ef4444;
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &.liked {
    border-color: #ef4444;
    background: rgba(239, 68, 68, 0.15);
    color: #ef4444;

    .heart-icon {
      fill: #ef4444;
      stroke: #ef4444;
    }
  }

  &.animating {
    animation: likePop 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }
}

.heart-icon {
  width: 18px;
  height: 18px;
  stroke: currentColor;
  fill: transparent;
  transition: fill 0.3s ease;
}

.like-count {
  font-weight: 500;
  font-size: 0.875rem;
}

@keyframes likePop {
  0% {
    transform: scale(1);
  }
  30% {
    transform: scale(1.3);
  }
  50% {
    transform: scale(0.9);
  }
  70% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}
</style>
