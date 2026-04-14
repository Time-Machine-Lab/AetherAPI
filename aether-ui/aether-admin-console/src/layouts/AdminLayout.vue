<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import AppLocaleSwitch from '@/components/AppLocaleSwitch.vue'
import BrandMarkIcon from '@/components/icons/BrandMarkIcon.vue'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/stores/useAuthStore'
import { appConfig } from '@/app/app-config'

const authStore = useAuthStore()
const { user } = storeToRefs(authStore)
const { t } = useI18n()
const router = useRouter()

async function handleSignOut() {
  authStore.signOut()
  await router.push({ name: appConfig.signInRouteName })
}
</script>

<template>
  <div class="min-h-screen">
    <div class="mx-auto flex min-h-screen max-w-7xl flex-col px-4 sm:px-6 lg:px-8">
      <header
        class="sticky top-4 z-20 mt-4 flex items-center justify-between rounded-[1.75rem] bg-[rgb(255_255_255_/_0.72)] px-5 py-4 shadow-[0_24px_40px_-32px_rgba(25,28,30,0.3)] backdrop-blur-xl sm:px-6"
      >
        <RouterLink :to="{ name: 'admin-dashboard' }" class="flex items-center gap-3">
          <BrandMarkIcon class="h-10 w-10" />
          <div>
            <p class="text-sm font-semibold tracking-[0.28em] text-muted-foreground">AETHERAPI</p>
            <p class="text-base font-medium text-foreground/88">{{ t('app.subtitle') }}</p>
          </div>
        </RouterLink>

        <div class="flex flex-wrap items-center justify-end gap-2">
          <Button as-child variant="ghost" size="sm">
            <RouterLink :to="{ name: 'admin-dashboard' }">{{
              t('admin.nav.dashboard')
            }}</RouterLink>
          </Button>
          <Button v-if="!authStore.isAuthenticated" as-child size="sm">
            <RouterLink :to="{ name: 'admin-sign-in' }">{{ t('admin.nav.signIn') }}</RouterLink>
          </Button>
          <Button v-else variant="outline" size="sm" @click="handleSignOut">
            {{ t('admin.nav.signOut') }}
          </Button>
          <AppLocaleSwitch />
        </div>
      </header>

      <main class="flex-1 py-8 sm:py-10">
        <div
          v-if="authStore.isAuthenticated && user"
          class="relative mb-8 overflow-hidden rounded-[1.5rem] bg-secondary/70 px-5 py-4 text-sm text-muted-foreground shadow-[0_20px_34px_-28px_rgba(25,28,30,0.34)]"
        >
          <span class="absolute inset-y-4 left-0 w-1 rounded-full bg-primary" />
          {{ t('admin.shell.operator') }}:
          <span class="font-medium text-foreground">{{ user.displayName }}</span>
          / {{ user.role }}
        </div>
        <slot />
      </main>
    </div>
  </div>
</template>
