<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import {
  Activity,
  BookOpen,
  Bot,
  CreditCard,
  Grid2x2,
  KeyRound,
  LayoutDashboard,
  LogOut,
  Menu,
  ReceiptText,
  UserRound,
} from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import AppLocaleSwitch from '@/components/AppLocaleSwitch.vue'
import BrandMarkIcon from '@/components/icons/BrandMarkIcon.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { appConfig } from '@/app/app-config'
import {
  consoleNotices,
  consoleSidebarGroups,
  consoleTopUtilities,
  type ConsoleNavId,
} from '@/features/console/console-shell'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const { user } = storeToRefs(authStore)
const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const isSignInScreen = computed(() => route.name === appConfig.signInRouteName)

const iconMap = {
  overview: LayoutDashboard,
  marketplace: Grid2x2,
  agents: Bot,
  credentials: KeyRound,
  usage: Activity,
  orders: ReceiptText,
  billing: CreditCard,
  docs: BookOpen,
} satisfies Record<ConsoleNavId, unknown>

const activeNavId = computed<ConsoleNavId>(() => {
  if (route.name === 'console-home') {
    return 'marketplace'
  }

  const hashId = route.hash.replace('#', '') as ConsoleNavId

  if (hashId) {
    return hashId
  }

  return 'overview'
})

const pageTitle = computed(() => {
  const titleKey = typeof route.meta.titleKey === 'string' ? route.meta.titleKey : undefined
  return titleKey ? t(titleKey) : appConfig.appName
})

async function handleSignOut() {
  authStore.signOut()
  await router.push({ name: appConfig.signInRouteName })
}
</script>

<template>
  <div v-if="isSignInScreen" class="min-h-screen bg-background">
    <div class="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:px-8">
      <div class="mb-6 flex items-center gap-3">
        <BrandMarkIcon class="h-10 w-10" />
        <div>
          <p class="text-base font-semibold text-foreground">AetherAPI</p>
          <p class="text-sm text-muted-foreground">{{ t('app.subtitle') }}</p>
        </div>
      </div>
      <slot />
    </div>
  </div>

  <div v-else class="min-h-screen bg-background">
    <div class="grid min-h-screen lg:grid-cols-[248px_minmax(0,1fr)]">
      <aside class="border-r border-[rgb(34_34_34_/_0.06)] bg-white">
        <div class="flex h-18 items-center gap-3 border-b border-[rgb(34_34_34_/_0.06)] px-5">
          <div class="flex size-10 items-center justify-center rounded-full bg-secondary text-foreground">
            <Menu class="size-4" />
          </div>
          <RouterLink :to="{ name: 'console-home' }" class="flex items-center gap-3">
            <BrandMarkIcon class="h-9 w-9" />
            <div>
              <p class="text-sm font-semibold text-foreground">AetherAPI</p>
              <p class="text-xs text-muted-foreground">{{ t('app.subtitle') }}</p>
            </div>
          </RouterLink>
        </div>

        <div class="px-4 py-5">
          <div class="mb-6 rounded-[20px] border border-[rgb(34_34_34_/_0.04)] bg-white p-4 shadow-console">
            <div class="flex items-center gap-3">
              <div
                class="flex size-11 items-center justify-center rounded-full bg-secondary text-foreground"
              >
                <UserRound class="size-4" />
              </div>
              <div class="min-w-0">
                <p class="truncate text-sm font-medium text-foreground">
                  {{ user?.displayName ?? 'Console Operator' }}
                </p>
                <p class="truncate text-xs text-muted-foreground">
                  {{ user?.email ?? 'console@aetherapi.local' }}
                </p>
              </div>
            </div>
          </div>

          <div class="space-y-6">
            <section v-for="group in consoleSidebarGroups" :key="group.id">
              <p class="px-3 pb-2 text-[11px] font-semibold uppercase tracking-[0.24em] text-muted-foreground">
                {{ t(group.titleKey) }}
              </p>
              <div class="space-y-1.5">
                <RouterLink
                  v-for="item in group.items"
                  :key="item.id"
                  :to="{ name: item.routeName, hash: item.hash }"
                  class="flex items-center gap-3 rounded-[14px] px-3 py-3 text-sm transition-colors"
                  :class="
                    activeNavId === item.id
                      ? 'bg-[color-mix(in_srgb,var(--primary)_9%,white)] text-foreground shadow-console'
                      : 'text-muted-foreground hover:bg-secondary hover:text-foreground'
                  "
                >
                  <component :is="iconMap[item.id]" class="size-4 shrink-0" />
                  <span class="min-w-0 flex-1 truncate font-medium">{{ t(item.labelKey) }}</span>
                  <Badge v-if="item.badge" variant="outline">{{ item.badge }}</Badge>
                </RouterLink>
              </div>
            </section>
          </div>
        </div>
      </aside>

      <div class="min-w-0">
        <header class="sticky top-0 z-20 border-b border-[rgb(34_34_34_/_0.06)] bg-white/92 backdrop-blur-md">
          <div class="px-4 py-4 sm:px-6">
            <div class="flex flex-wrap items-center justify-between gap-4">
              <div class="min-w-0">
                <h1 class="text-lg font-semibold text-foreground">{{ pageTitle }}</h1>
                <div class="mt-1 hidden flex-wrap items-center gap-4 lg:flex">
                  <span
                    v-for="utility in consoleTopUtilities"
                    :key="utility.id"
                    class="text-sm text-muted-foreground"
                  >
                    {{ t(utility.labelKey) }}
                    <span v-if="utility.badge" class="ml-1 text-[11px] font-semibold text-primary">
                      {{ utility.badge }}
                    </span>
                  </span>
                </div>
              </div>

              <div class="flex flex-wrap items-center justify-end gap-2">
                <div class="hidden items-center lg:flex">
                  <div class="w-[320px] rounded-full bg-white shadow-console">
                    <Input
                      :default-value="''"
                      class="rounded-full border-transparent pl-5 pr-5 shadow-none focus-visible:bg-white"
                      :placeholder="t('console.nav.searchPlaceholder')"
                    />
                  </div>
                </div>
                <AppLocaleSwitch />
                <Button variant="outline" size="sm" @click="handleSignOut">
                  <LogOut class="size-4" />
                  <span>{{ t('console.nav.signOut') }}</span>
                </Button>
              </div>
            </div>

            <div class="mt-4 flex flex-wrap gap-3">
              <div
                v-for="notice in consoleNotices"
                :key="notice.id"
                class="inline-flex items-center rounded-full px-4 py-2 text-sm shadow-console"
                :class="
                  notice.tone === 'success'
                    ? 'bg-[color-mix(in_srgb,var(--primary)_9%,white)] text-foreground'
                    : 'bg-white text-muted-foreground'
                "
              >
                {{ t(notice.labelKey) }}
              </div>
            </div>
          </div>
        </header>

        <main class="px-4 py-5 sm:px-6 sm:py-6">
          <slot />
        </main>
      </div>
    </div>
  </div>
</template>
