<script setup lang="ts">
import { ref } from 'vue'
import type { RouteRecordName } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import heroImage from '@/assets/hero.png'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { appConfig } from '@/app/app-config'
import { consoleWorkspacePanels } from '@/features/console/console-shell'
import { useConsoleAuth } from '@/composables/useConsoleAuth'
import type { NormalizedHttpError } from '@/api/http'

const { signIn } = useConsoleAuth()
const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loginName = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorCode = ref<string | null>(null)

async function handleSignIn() {
  isSubmitting.value = true
  errorCode.value = null

  try {
    await signIn(loginName.value, password.value)

    const redirectName =
      typeof route.query.redirectName === 'string'
        ? (route.query.redirectName as RouteRecordName)
        : (appConfig.protectedHomeRouteName as RouteRecordName)

    await router.push({ name: redirectName })
  } catch (err) {
    const httpError = err as NormalizedHttpError
    errorCode.value = httpError.code ?? 'UNKNOWN'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<route lang="json5">
{
  name: 'console-sign-in',
  meta: {
    layout: 'ConsoleLayout',
    titleKey: 'console.signIn.metaTitle',
    guestOnly: true,
    requiresAuth: false,
  },
}
</route>

<template>
  <div class="grid gap-6 2xl:grid-cols-[minmax(0,1.15fr)_minmax(380px,0.85fr)]">
    <Card class="overflow-hidden">
      <div class="grid gap-0 lg:grid-cols-[minmax(0,1fr)_minmax(300px,0.92fr)]">
        <CardContent class="p-6 sm:p-8 lg:p-10">
          <Badge variant="outline">{{ t('console.signIn.eyebrow') }}</Badge>
          <p class="console-kicker mt-6">{{ t('console.signIn.eyebrow') }}</p>
          <h1 class="console-display mt-4 max-w-3xl text-5xl font-semibold leading-[1.05] md:text-6xl">
            {{ t('console.signIn.title') }}
          </h1>
          <p class="mt-5 max-w-2xl text-base leading-8 text-muted-foreground sm:text-lg">
            {{ t('console.signIn.description') }}
          </p>

          <div class="mt-8 rounded-[24px] bg-secondary px-5 py-5">
            <p class="text-base font-medium text-foreground">{{ t('console.signIn.helperTitle') }}</p>
            <p class="mt-2 text-sm leading-6 text-muted-foreground">
              {{ t('console.signIn.helperDescription') }}
            </p>
            <div class="mt-5 space-y-3">
              <div
                v-for="item in consoleWorkspacePanels.slice(0, 3)"
                :key="item.id"
                class="rounded-[18px] bg-white px-4 py-4 shadow-console"
              >
                <p class="text-sm font-medium text-foreground">{{ t(item.titleKey) }}</p>
                <p class="mt-2 text-sm leading-6 text-muted-foreground">
                  {{ t(item.descriptionKey) }}
                </p>
              </div>
            </div>
          </div>
        </CardContent>

        <div class="relative min-h-[320px] border-t border-border lg:border-t-0 lg:border-l">
          <img :src="heroImage" alt="Console preview" class="absolute inset-0 size-full object-cover" />
          <div
            class="absolute inset-0 bg-[linear-gradient(180deg,rgba(34,34,34,0.08),rgba(34,34,34,0.64))]"
          />
          <div class="relative flex h-full items-end p-6 text-white sm:p-8">
            <p class="max-w-sm text-sm leading-6 text-white/84">{{ t('console.signIn.note') }}</p>
          </div>
        </div>
      </div>
    </Card>

    <Card class="self-start">
      <CardHeader class="space-y-3">
        <CardTitle class="text-3xl">{{ t('console.signIn.formTitle') }}</CardTitle>
        <CardDescription>{{ t('console.signIn.formDescription') }}</CardDescription>
      </CardHeader>
      <CardContent>
        <form class="space-y-5" @submit.prevent="handleSignIn">
          <div class="space-y-2">
            <Label for="login-name">{{ t('console.signIn.loginNameLabel') }}</Label>
            <Input id="login-name" v-model="loginName" autocomplete="username" :disabled="isSubmitting" />
          </div>
          <div class="space-y-2">
            <Label for="password">{{ t('console.signIn.passwordLabel') }}</Label>
            <Input id="password" v-model="password" type="password" autocomplete="current-password" :disabled="isSubmitting" />
          </div>
          <p
            v-if="errorCode"
            class="rounded-[18px] bg-destructive/10 px-4 py-3 text-sm leading-6 text-destructive"
          >
            {{ t(`console.signIn.errors.${errorCode}`, t('console.signIn.errors.UNKNOWN')) }}
          </p>
          <Button class="w-full" type="submit" :disabled="isSubmitting || !loginName || !password">
            {{ isSubmitting ? t('console.signIn.submitting') : t('console.signIn.submit') }}
          </Button>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
