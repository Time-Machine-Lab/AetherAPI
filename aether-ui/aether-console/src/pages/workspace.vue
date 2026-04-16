<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui/button'
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { appConfig } from '@/app/app-config'
import { useAppInfo } from '@/composables/useAppInfo'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const router = useRouter()
const { env } = useAppInfo()
const { t } = useI18n()
const cards = ['app', 'auth', 'next'] as const

async function handleSignOut() {
  authStore.signOut()
  await router.push({ name: appConfig.signInRouteName })
}
</script>

<route lang="json5">
{
  name: 'portal-workspace',
  meta: {
    layout: 'PortalLayout',
    titleKey: 'portal.workspace.metaTitle',
    requiresAuth: true,
  },
}
</route>

<template>
  <div class="space-y-8">
    <section class="grid gap-5 lg:grid-cols-[minmax(0,1.5fr)_minmax(280px,0.9fr)]">
      <div class="rounded-[2rem] bg-card/95 p-8 shadow-[0_30px_60px_-40px_rgba(28,39,56,0.3)] sm:p-10">
        <p class="text-sm font-semibold uppercase tracking-[0.28em] text-muted-foreground">
          BUILDER WORKSPACE
        </p>
        <h1 class="mt-4 max-w-3xl text-5xl font-semibold tracking-tight text-foreground sm:text-6xl">
          {{ t('portal.workspace.title') }}
        </h1>
        <p class="mt-5 max-w-2xl text-lg leading-8 text-muted-foreground">
          {{ t('portal.workspace.description') }}
        </p>
        <div class="mt-8 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
          <span class="rounded-full bg-secondary px-3 py-1.5 text-foreground/80">{{ env.appId }}</span>
          <span class="rounded-full bg-secondary px-3 py-1.5 text-foreground/80">
            {{ env.apiBaseUrl }}
          </span>
        </div>
      </div>

      <div class="rounded-[2rem] bg-secondary/78 p-6 shadow-[0_24px_44px_-36px_rgba(28,39,56,0.3)] sm:p-8">
        <p class="text-sm font-semibold uppercase tracking-[0.24em] text-muted-foreground">
          Focus
        </p>
        <p class="mt-6 text-6xl font-semibold tracking-tight text-foreground">03</p>
        <p class="mt-3 max-w-xs text-sm leading-7 text-muted-foreground">
          {{ t('portal.workspace.description') }}
        </p>
      </div>
    </section>

    <div class="grid gap-5 lg:grid-cols-3">
      <Card v-for="cardKey in cards" :key="cardKey" class="relative bg-card/95">
        <CardHeader class="pl-7">
          <span class="absolute inset-y-6 left-0 w-1 rounded-full bg-primary/80" />
          <CardTitle>{{ t(`portal.workspace.cards.${cardKey}.title`) }}</CardTitle>
          <CardDescription>{{
            t(`portal.workspace.cards.${cardKey}.description`)
          }}</CardDescription>
        </CardHeader>
      </Card>
    </div>

    <div class="flex justify-start">
      <Button variant="outline" @click="handleSignOut">
        {{ t('portal.workspace.actions.signOut') }}
      </Button>
    </div>
  </div>
</template>
