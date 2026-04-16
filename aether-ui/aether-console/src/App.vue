<script setup lang="ts">
import { computed, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { appConfig } from '@/app/app-config'
import { fallbackLayout, layoutRegistry } from '@/app/layout-registry'

const route = useRoute()
const { t } = useI18n()

const currentLayout = computed(
  () => layoutRegistry[route.meta.layout ?? appConfig.defaultLayout] ?? fallbackLayout,
)

watchEffect(() => {
  const titleKey = route.meta.titleKey
  document.title = titleKey ? `${t(titleKey)} | ${appConfig.appName}` : appConfig.appName
})
</script>

<template>
  <component :is="currentLayout">
    <RouterView />
  </component>
</template>
