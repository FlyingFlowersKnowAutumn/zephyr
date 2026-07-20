<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  current: number
  total: number
  size?: number
}>(), {
  size: 10
})

const emit = defineEmits<{
  change: [page: number]
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

const pages = computed(() => {
  const p: (number | string)[] = []
  const tp = totalPages.value
  const cur = props.current

  if (tp <= 7) {
    for (let i = 1; i <= tp; i++) p.push(i)
    return p
  }

  p.push(1)
  if (cur > 3) p.push('...')

  const start = Math.max(2, cur - 1)
  const end = Math.min(tp - 1, cur + 1)
  for (let i = start; i <= end; i++) p.push(i)

  if (cur < tp - 2) p.push('...')
  p.push(tp)

  return p
})
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button
      v-if="current > 1"
      class="page-arrow"
      @click="emit('change', current - 1)"
      aria-label="上一页"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="15 18 9 12 15 6"/>
      </svg>
    </button>

    <div class="page-numbers">
      <template v-for="p in pages" :key="p">
        <span v-if="p === '...'" class="page-dots">...</span>
        <button
          v-else
          class="page-num"
          :class="{ active: p === current }"
          @click="emit('change', (p as number))"
        >
          {{ p }}
        </button>
      </template>
    </div>

    <button
      v-if="current < totalPages"
      class="page-arrow"
      @click="emit('change', current + 1)"
      aria-label="下一页"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="9 18 15 12 9 6"/>
      </svg>
    </button>
  </div>
</template>

<style lang="scss" scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 2.5rem 0 1.5rem;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.page-num {
  min-width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 0.25rem;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: var(--color-hover-bg);
    color: var(--color-text-primary);
  }

  &.active {
    background: var(--color-accent);
    color: #fff;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(52, 152, 219, 0.3);
  }
}

.page-dots {
  min-width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  letter-spacing: 0.1em;
  user-select: none;
}

.page-arrow {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: 50%;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    border-color: var(--color-accent);
    color: var(--color-accent);
    background: var(--color-hover-bg);
    transform: scale(1.05);
  }
}
</style>
