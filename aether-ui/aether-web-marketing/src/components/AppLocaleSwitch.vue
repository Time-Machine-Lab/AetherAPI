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
  <div class="flex items-center gap-2">
    <Button
      v-for="targetLocale in locales"
      :key="targetLocale"
      :variant="locale === targetLocale ? 'default' : 'outline'"
      size="sm"
      @click="appShellStore.setLocalePreference(targetLocale)"
    >
      {{ labelFor(targetLocale) }}
    </Button>
  </div>
</template>
