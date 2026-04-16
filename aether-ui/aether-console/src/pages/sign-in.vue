<script setup lang="ts">
import { ref } from 'vue'
import type { RouteRecordName } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { appConfig } from '@/app/app-config'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const displayName = ref('Portal Operator')
const email = ref('builder@aetherapi.local')
const isSubmitting = ref(false)

async function handleSignIn() {
  isSubmitting.value = true

  authStore.signIn({
    displayName: displayName.value,
    email: email.value,
    role: 'developer',
  })

  const redirectName =
    typeof route.query.redirectName === 'string'
      ? (route.query.redirectName as RouteRecordName)
      : (appConfig.protectedHomeRouteName as RouteRecordName)

  await router.push({ name: redirectName })
  isSubmitting.value = false
}
</script>

<route lang="json5">
{
  name: 'portal-sign-in',
  meta: {
    layout: 'PortalLayout',
    titleKey: 'portal.signIn.metaTitle',
    guestOnly: true,
    requiresAuth: false,
  },
}
</route>

<template>
  <div class="grid items-start gap-6 lg:grid-cols-[minmax(0,1.1fr)_minmax(360px,0.9fr)]">
    <section class="rounded-[2rem] bg-secondary/70 p-8 sm:p-10">
      <p class="text-sm font-semibold uppercase tracking-[0.28em] text-muted-foreground">
        {{ t('portal.signIn.eyebrow') }}
      </p>
      <h1 class="mt-4 max-w-xl text-5xl font-semibold tracking-tight text-foreground sm:text-6xl">
        {{ t('portal.signIn.title') }}
      </h1>
      <p class="mt-5 max-w-lg text-lg leading-8 text-muted-foreground">
        {{ t('portal.signIn.description') }}
      </p>
    </section>

    <Card class="bg-card/95">
      <CardHeader class="space-y-2">
        <CardTitle class="text-3xl">{{ t('portal.signIn.title') }}</CardTitle>
        <CardDescription>{{ t('portal.signIn.description') }}</CardDescription>
      </CardHeader>
      <CardContent>
        <form class="space-y-5" @submit.prevent="handleSignIn">
          <div class="space-y-2">
            <Label for="display-name">{{ t('portal.signIn.nameLabel') }}</Label>
            <Input id="display-name" v-model="displayName" />
          </div>
          <div class="space-y-2">
            <Label for="email">{{ t('portal.signIn.emailLabel') }}</Label>
            <Input id="email" v-model="email" type="email" />
          </div>
          <Button class="w-full" type="submit" :disabled="isSubmitting">
            {{ t('portal.signIn.submit') }}
          </Button>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
