<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { appConfig } from '@/app/app-config'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const { t } = useI18n()

const sections = ['onboarding', 'governance'] as const
</script>

<route lang="json5">
{
  name: 'portal-home',
  meta: {
    layout: 'PortalLayout',
    titleKey: 'portal.home.metaTitle',
    requiresAuth: false,
  },
}
</route>

<template>
  <div class="space-y-10">
    <section class="grid gap-5 lg:grid-cols-[minmax(0,1.55fr)_minmax(280px,0.95fr)]">
      <div class="rounded-[2rem] bg-card/95 p-8 shadow-[0_30px_60px_-40px_rgba(28,39,56,0.3)] sm:p-10">
        <Badge variant="outline">{{ t('portal.home.badge') }}</Badge>
        <div class="mt-5 max-w-3xl space-y-4">
          <h1 class="text-5xl font-semibold tracking-tight text-foreground md:text-6xl">
            {{ t('portal.home.headline') }}
          </h1>
          <p class="text-lg leading-8 text-muted-foreground">
            {{ t('portal.home.description') }}
          </p>
        </div>
        <div class="mt-8 flex flex-wrap gap-3">
          <Button as-child>
            <RouterLink :to="{ name: appConfig.signInRouteName }">
              {{ t('portal.home.primaryAction') }}
            </RouterLink>
          </Button>
          <Button as-child variant="outline">
            <RouterLink :to="{ name: appConfig.protectedHomeRouteName }">
              {{ t('portal.home.secondaryAction') }}
            </RouterLink>
          </Button>
        </div>
      </div>

      <div class="rounded-[2rem] bg-secondary/78 p-6 shadow-[0_24px_44px_-36px_rgba(28,39,56,0.3)] sm:p-8">
        <p class="text-sm font-semibold uppercase tracking-[0.24em] text-muted-foreground">
          Insight
        </p>
        <p class="mt-6 text-6xl font-semibold tracking-tight text-foreground">02</p>
        <p class="mt-3 max-w-xs text-sm leading-7 text-muted-foreground">
          {{ t('portal.home.description') }}
        </p>
      </div>
    </section>

    <div class="grid gap-5 lg:grid-cols-2">
      <Card v-for="sectionKey in sections" :key="sectionKey" class="relative bg-card/95">
        <CardHeader class="pl-7">
          <span class="absolute inset-y-6 left-0 w-1 rounded-full bg-primary/80" />
          <CardTitle>{{ t(`portal.home.sections.${sectionKey}.title`) }}</CardTitle>
          <CardDescription>{{
            t(`portal.home.sections.${sectionKey}.description`)
          }}</CardDescription>
        </CardHeader>
      </Card>
    </div>

    <Card v-if="authStore.isAuthenticated" class="relative bg-card/95">
      <CardHeader class="pl-7">
        <span class="absolute inset-y-6 left-0 w-1 rounded-full bg-primary/80" />
        <CardTitle>{{ t('portal.workspace.title') }}</CardTitle>
        <CardDescription>{{ t('portal.workspace.description') }}</CardDescription>
      </CardHeader>
    </Card>
  </div>
</template>
