<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AppLocaleSwitch from '@/components/AppLocaleSwitch.vue'
import BrandMarkIcon from '@/components/icons/BrandMarkIcon.vue'
import { Button } from '@/components/ui/button'

const { t } = useI18n()
</script>

<template>
  <div class="editorial-shell min-h-screen">
    <div class="mx-auto flex min-h-screen max-w-7xl flex-col px-4 sm:px-6 lg:px-8">
      <header
        class="surface-glass ghost-outline sticky top-4 z-20 mt-4 flex items-center justify-between rounded-[1.5rem] px-5 py-4"
      >
        <a class="flex items-center gap-3" href="#top">
          <BrandMarkIcon class="h-10 w-10" />
          <div>
            <p class="text-[0.7rem] font-semibold tracking-[0.28em] text-[color:var(--primary-fixed-dim)]">AETHERAPI</p>
            <p class="text-base font-medium text-foreground">{{ t('app.subtitle') }}</p>
          </div>
        </a>

        <div class="hidden items-center gap-2 lg:flex">
          <Button as-child variant="ghost" size="sm">
            <a href="#platform">{{ t('marketing.nav.platform') }}</a>
          </Button>
          <Button as-child variant="ghost" size="sm">
            <a href="#standards">{{ t('marketing.nav.standards') }}</a>
          </Button>
          <Button as-child variant="ghost" size="sm">
            <a href="#summary">{{ t('marketing.nav.summary') }}</a>
          </Button>
        </div>

        <AppLocaleSwitch />
      </header>

      <main id="top" class="flex-1">
        <slot />
      </main>

      <footer class="py-8 text-sm text-muted-foreground">
        {{ t('marketing.footer') }}
      </footer>
    </div>
  </div>
</template>
