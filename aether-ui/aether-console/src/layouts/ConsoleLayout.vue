<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import {
  Activity,
  BookOpen,
  ChevronLeft,
  ChevronRight,
  CreditCard,
  Info,
  KeyRound,
  LayoutList,
  LogOut,
  Package,
  Play,
  Rocket,
  ScrollText,
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
  normalizeConsoleWorkspaceNavId,
  type ConsoleNavId,
} from '@/features/console/console-shell'
import { useAuthStore } from '@/stores/useAuthStore'
import { useConsoleAuth } from '@/composables/useConsoleAuth'

const authStore = useAuthStore()
const { currentUser } = storeToRefs(authStore)
const { signOut } = useConsoleAuth()
const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const sidebarCollapsed = ref(false)

const isSignInScreen = computed(() => route.name === appConfig.signInRouteName)

const iconMap = {
  'catalog-browse': LayoutList,
  'catalog-manage': Package,
  'category-manage': Package,
  'unified-access-playground': Play,
  credentials: KeyRound,
  'api-call-logs': ScrollText,
  usage: Activity,
  orders: ReceiptText,
  billing: CreditCard,
  docs: BookOpen,
} satisfies Record<ConsoleNavId, unknown>

const activeNavId = computed<ConsoleNavId>(() => {
  if (route.name === 'console-home') {
    return 'catalog-browse'
  }

  if (route.name === 'console-playground') {
    return 'unified-access-playground'
  }

  if (route.name === 'console-workspace') {
    return normalizeConsoleWorkspaceNavId(route.hash)
  }

  return 'catalog-manage'
})

const pageTitle = computed(() => {
  const titleKey = typeof route.meta.titleKey === 'string' ? route.meta.titleKey : undefined
  return titleKey ? t(titleKey) : appConfig.appName
})

async function handleSignOut() {
  signOut()
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
    <div
      class="grid min-h-screen transition-[grid-template-columns] duration-200"
      :class="
        sidebarCollapsed
          ? 'lg:grid-cols-[64px_minmax(0,1fr)]'
          : 'lg:grid-cols-[248px_minmax(0,1fr)]'
      "
    >
      <aside
        class="sticky top-0 flex h-screen flex-col overflow-y-auto border-r border-[rgb(34_34_34_/_0.06)] bg-white"
      >
        <div
          class="flex h-18 shrink-0 items-center gap-3 border-b border-[rgb(34_34_34_/_0.06)] px-3"
        >
          <button
            type="button"
            class="flex size-10 shrink-0 items-center justify-center rounded-full bg-secondary text-foreground transition-colors hover:bg-[rgb(34_34_34_/_0.08)]"
            @click="sidebarCollapsed = !sidebarCollapsed"
          >
            <ChevronLeft v-if="!sidebarCollapsed" class="size-4" />
            <ChevronRight v-else class="size-4" />
          </button>
          <RouterLink
            v-if="!sidebarCollapsed"
            :to="{ name: 'console-home' }"
            class="flex items-center gap-3 overflow-hidden"
          >
            <BrandMarkIcon class="h-9 w-9 shrink-0" />
            <div class="min-w-0">
              <p class="text-sm font-semibold text-foreground">AetherAPI</p>
              <p class="text-xs text-muted-foreground">{{ t('app.subtitle') }}</p>
            </div>
          </RouterLink>
        </div>

        <div class="flex-1 overflow-y-auto px-3 py-5">
          <div
            v-if="!sidebarCollapsed"
            class="mb-6 rounded-[20px] border border-[rgb(34_34_34_/_0.04)] bg-white p-4 shadow-console"
          >
            <div class="flex items-center gap-3">
              <div
                class="flex size-11 shrink-0 items-center justify-center rounded-full bg-secondary text-foreground"
              >
                <UserRound class="size-4" />
              </div>
              <div class="min-w-0">
                <p class="truncate text-sm font-medium text-foreground">
                  {{ currentUser?.displayName ?? 'Console Operator' }}
                </p>
                <p class="truncate text-xs text-muted-foreground">
                  {{ currentUser?.email ?? 'console@aetherapi.local' }}
                </p>
              </div>
            </div>
          </div>
          <div v-else class="mb-6 flex justify-center">
            <div
              class="flex size-11 items-center justify-center rounded-full bg-secondary text-foreground"
            >
              <UserRound class="size-4" />
            </div>
          </div>

          <div class="space-y-6">
            <section v-for="group in consoleSidebarGroups" :key="group.id">
              <p
                v-if="!sidebarCollapsed"
                class="px-3 pb-2 text-[11px] font-semibold uppercase tracking-[0.24em] text-muted-foreground"
              >
                {{ t(group.titleKey) }}
              </p>
              <div class="space-y-1.5">
                <RouterLink
                  v-for="item in group.items"
                  :key="item.id"
                  :to="{ name: item.routeName, hash: item.hash }"
                  class="flex items-center rounded-[14px] px-3 py-3 text-sm transition-colors"
                  :class="[
                    sidebarCollapsed ? 'justify-center' : 'gap-3',
                    activeNavId === item.id
                      ? 'bg-[color-mix(in_srgb,var(--primary)_9%,white)] text-foreground shadow-console'
                      : 'text-muted-foreground hover:bg-secondary hover:text-foreground',
                  ]"
                  :title="sidebarCollapsed ? t(item.labelKey) : undefined"
                >
                  <component :is="iconMap[item.id]" class="size-4 shrink-0" />
                  <span v-if="!sidebarCollapsed" class="min-w-0 flex-1 truncate font-medium">{{
                    t(item.labelKey)
                  }}</span>
                  <Badge v-if="!sidebarCollapsed && item.badge" variant="outline">{{
                    item.badge
                  }}</Badge>
                </RouterLink>
              </div>
            </section>
          </div>
        </div>
      </aside>

      <div class="min-w-0">
        <header
          class="sticky top-0 z-20 border-b border-[rgb(34_34_34_/_0.06)] bg-white/92 backdrop-blur-md"
        >
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
                  <div class="w-[280px] rounded-full bg-secondary">
                    <Input
                      :default-value="''"
                      class="rounded-full border-transparent bg-transparent pl-5 pr-5 shadow-none focus-visible:ring-1 focus-visible:ring-ring/10 focus-visible:bg-transparent"
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

            <div v-if="consoleNotices.length" class="mt-4 flex flex-col gap-3">
              <div
                v-for="notice in consoleNotices"
                :key="notice.id"
                class="flex items-center gap-3 rounded-[14px] px-4 py-3 text-sm font-medium text-foreground"
                :class="
                  notice.tone === 'success'
                    ? 'bg-[color-mix(in_srgb,var(--primary)_6%,white)] border-b-2 border-primary'
                    : 'bg-[color-mix(in_srgb,var(--palette-text-legal)_6%,white)] border-b-2 border-[var(--palette-text-legal)]'
                "
              >
                <component
                  :is="notice.tone === 'success' ? Rocket : Info"
                  class="size-4 shrink-0"
                  :class="
                    notice.tone === 'success' ? 'text-primary' : 'text-[var(--palette-text-legal)]'
                  "
                />
                <span>{{ t(notice.labelKey) }}</span>
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
