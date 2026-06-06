<script setup lang="ts">
import { computed } from 'vue'
import { useHead, useSeoMeta } from '@unhead/vue'

const props = withDefaults(defineProps<{
  title?: string
  description?: string
  image?: string
  url?: string
  type?: string
}>(), {
  title: '个人技术博客',
  description: '记录技术成长，分享编程心得',
  image: '/logo.png',
  url: '',
  type: 'website'
})

const siteName = '个人技术博客'
const fullTitle = computed(() =>
  props.title === siteName ? siteName : `${props.title} | ${siteName}`
)

useHead({
  title: fullTitle.value,
  titleTemplate: undefined
})

useSeoMeta({
  description: props.description,
  ogTitle: fullTitle.value,
  ogDescription: props.description,
  ogImage: props.image,
  ogUrl: props.url,
  ogType: props.type as 'website' | 'article',
  ogSiteName: siteName,
  twitterCard: 'summary_large_image',
  twitterTitle: fullTitle.value,
  twitterDescription: props.description,
  twitterImage: props.image
})
</script>

<template>
  <slot />
</template>
