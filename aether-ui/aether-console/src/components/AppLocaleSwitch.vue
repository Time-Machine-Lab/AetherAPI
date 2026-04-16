<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { Button } from '@/components/ui/button'
import { useAppShellStore } from '@/stores/useAppShellStore'
import type { SupportedLocale } from '@/utils/env'

const appShellStore = useAppShellStore()
const { locale } = storeToRefs(appShellStore)
const { t } = useI18n()

const locales: SupportedLocale[] = ['zh-CN', 'en-US']

function labelFor(targetLocale: SupportedLocale) {
  return targetLocale === 'zh-CN' ? t('common.locale.zhCn') : t('common.locale.enUs')
}
</script>

<template>
  <div class="inline-flex items-center gap-1 rounded-full bg-white p-1 shadow-console">
    <Button
      v-for="targetLocale in locales"
      :key="targetLocale"
      :variant="locale === targetLocale ? 'secondary' : 'ghost'"
      size="xs"
      class="rounded-full px-3"
      @click="appShellStore.setLocalePreference(targetLocale)"
    >
      {{ labelFor(targetLocale) }}
    </Button>
  </div>
</template>
