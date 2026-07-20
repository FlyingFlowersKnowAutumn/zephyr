<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import SearchModal from '@/components/common/SearchModal.vue'

const { fetchUser } = useAuth()
const isSearchOpen = ref(false)

onMounted(() => {
  fetchUser()
})

function openSearch() {
  isSearchOpen.value = true
}

function closeSearch() {
  isSearchOpen.value = false
}
</script>

<template>
  <RouterView @open-search="openSearch" />
  <SearchModal :is-open="isSearchOpen" @close="closeSearch" />
</template>

<style lang="scss">
</style>
