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
  setTimeout(() => { animating.value = false }, 300)
}
</script>

<template>
  <button
    class="like-button"
    :class="{ liked, animating }"
    :disabled="!authStore.isLoggedIn"
    @click="toggle"
  >
    <span class="heart-icon">{{ liked ? '❤️' : '🤍' }}</span>
    <span class="like-count">{{ likeCount }}</span>
  </button>
</template>

<style lang="scss" scoped>
.like-button {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s;

  &:hover:not(:disabled) {
    border-color: #ef4444;
    background: rgba(239, 68, 68, 0.1);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &.liked {
    border-color: #ef4444;
    background: rgba(239, 68, 68, 0.15);
    color: #ef4444;
  }

  &.animating {
    transform: scale(1.2);
  }
}

.heart-icon {
  font-size: 1rem;
}

.like-count {
  font-weight: 500;
}
</style>
