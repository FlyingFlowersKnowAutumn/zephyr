import { ref, onMounted, watch } from 'vue'

export function useDarkMode() {
  const isDark = ref(false)

  function toggleDark() {
    isDark.value = !isDark.value
  }

  function applyDarkMode() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  function loadFromStorage() {
    const saved = localStorage.getItem('darkMode')
    if (saved !== null) {
      isDark.value = saved === 'true'
    } else {
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyDarkMode()
  }

  watch(isDark, (newVal) => {
    localStorage.setItem('darkMode', String(newVal))
    applyDarkMode()
  })

  onMounted(() => {
    loadFromStorage()
  })

  return {
    isDark,
    toggleDark
  }
}
