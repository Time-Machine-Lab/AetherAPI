<script setup lang="ts">
import { computed, ref } from 'vue'
import { ArrowDownUp, Download, Heart, Search, SlidersHorizontal } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import heroImage from '@/assets/hero.png'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import {
  type ConsoleCardFlag,
  consoleMarketplaceCards,
  consoleMarketplaceFilters,
  consoleMarketplaceMetrics,
} from '@/features/console/console-shell'

const { t } = useI18n()

const keyword = ref('')
const activeFilter = ref('all')

const filteredCards = computed(() => {
  const query = keyword.value.trim().toLowerCase()

  return consoleMarketplaceCards.filter((card) => {
    const matchesQuery =
      query.length === 0 ||
      `${card.name} ${card.vendor} ${card.description}`.toLowerCase().includes(query)

    const matchesFilter =
      activeFilter.value === 'all' ||
      card.flags.includes(activeFilter.value as ConsoleCardFlag)

    return matchesQuery && matchesFilter
  })
})
</script>

<route lang="json5">
{
  name: 'console-home',
  meta: {
    layout: 'ConsoleLayout',
    titleKey: 'console.home.metaTitle',
    requiresAuth: false,
  },
}
</route>

<template>
  <div class="space-y-6">
    <section class="flex flex-col gap-4">
      <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p class="console-kicker">{{ t('console.home.title') }}</p>
          <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
            {{ t('console.home.title') }}
          </h2>
          <p class="mt-3 text-sm leading-6 text-muted-foreground">
            {{ t('console.home.description') }}
          </p>
        </div>
        <div class="flex flex-wrap gap-2">
          <span
            v-for="filter in consoleMarketplaceFilters"
            :key="filter.id"
            class="inline-flex cursor-pointer items-center rounded-full border px-4 py-2 text-sm font-medium transition-colors"
            :class="
              filter.id === activeFilter
                ? 'border-[rgb(34_34_34_/_0.08)] bg-white text-foreground shadow-console'
                : 'border-transparent bg-secondary text-foreground hover:bg-white hover:shadow-console'
            "
            @click="activeFilter = filter.id"
          >
            {{ t(filter.labelKey) }}
          </span>
        </div>
      </div>

      <div
        class="rounded-[20px] border border-[rgb(255_56_92_/_0.08)] bg-[color-mix(in_srgb,var(--primary)_5%,white)] px-5 py-4 text-sm text-muted-foreground"
      >
        {{ t('console.home.banner') }}
      </div>
    </section>

    <section class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_auto_auto_auto] xl:items-center">
      <div class="rounded-full bg-white px-2 py-2 shadow-console">
        <div class="relative">
          <Search class="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            v-model="keyword"
            class="rounded-full border-transparent pl-10 pr-4 shadow-none focus-visible:bg-white"
            :placeholder="t('console.home.searchPlaceholder')"
          />
        </div>
      </div>
      <Button variant="outline" size="sm">
        <SlidersHorizontal class="size-4" />
        <span>{{ t('console.home.toolbarExpand') }}</span>
      </Button>
      <Button variant="outline" size="sm">
        <ArrowDownUp class="size-4" />
        <span>{{ t('console.home.toolbarSort') }}</span>
      </Button>
      <Button variant="outline" size="sm">
        <Download class="size-4" />
        <span>{{ t('console.home.toolbarExport') }}</span>
      </Button>
    </section>

    <section class="grid gap-4 md:grid-cols-3">
      <Card v-for="metric in consoleMarketplaceMetrics" :key="metric.id">
        <CardContent class="p-5">
          <p class="text-sm text-muted-foreground">{{ t(metric.labelKey) }}</p>
          <p class="console-display mt-3 text-[2rem] font-bold text-foreground">{{ metric.value }}</p>
          <p class="mt-3 text-xs leading-5 text-muted-foreground">{{ t(metric.hintKey) }}</p>
        </CardContent>
      </Card>
    </section>

    <section class="grid gap-5 xl:grid-cols-5">
      <Card class="overflow-hidden xl:col-span-2">
        <div class="grid h-full gap-0 md:grid-cols-[minmax(0,1fr)_210px]">
          <CardContent class="flex flex-col justify-between p-6">
            <div>
              <Badge variant="outline">{{ t('console.home.promoBadge') }}</Badge>
              <h3 class="console-display mt-5 max-w-xl text-[2rem] font-bold text-foreground">
                {{ t('console.home.promoTitle') }}
              </h3>
              <p class="mt-4 max-w-xl text-sm leading-7 text-muted-foreground">
                {{ t('console.home.promoDescription') }}
              </p>
            </div>
            <div class="mt-6">
              <Button>{{ t('console.home.promoAction') }}</Button>
            </div>
          </CardContent>

          <div class="relative min-h-[220px]">
            <img :src="heroImage" alt="" class="size-full object-cover" />
            <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(34,34,34,0.03),rgba(34,34,34,0.28))]" />
          </div>
        </div>
      </Card>

      <Card
        v-for="card in filteredCards"
        :key="card.id"
        class="overflow-hidden"
      >
        <div class="relative aspect-[16/10]">
          <img :src="heroImage" alt="" class="size-full object-cover" />
          <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(34,34,34,0.03),rgba(34,34,34,0.1))]" />
          <div class="absolute right-3 top-3">
            <button
              type="button"
              class="flex size-9 items-center justify-center rounded-full bg-white/92 text-foreground shadow-console"
            >
              <Heart class="size-4" />
            </button>
          </div>
          <div class="absolute left-3 top-3">
            <Badge variant="outline">{{ card.status }}</Badge>
          </div>
        </div>

        <CardContent class="flex h-full flex-col p-5">
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="truncate text-sm font-semibold text-foreground">{{ card.name }}</p>
              <p class="mt-1 text-xs text-muted-foreground">{{ card.vendor }}</p>
            </div>
          </div>

          <div class="mt-3 flex flex-wrap gap-2">
            <span
              v-for="tag in card.tags"
              :key="tag"
              class="rounded-full bg-secondary px-2.5 py-1 text-[11px] text-muted-foreground"
            >
              {{ tag }}
            </span>
          </div>

          <p class="mt-4 line-clamp-4 text-sm leading-6 text-muted-foreground">
            {{ card.description }}
          </p>

          <div class="mt-auto grid grid-cols-2 gap-3 pt-5 text-xs">
            <div class="rounded-[14px] bg-secondary px-3 py-3">
              <p class="text-muted-foreground">{{ t('console.home.inputPrice') }}</p>
              <p class="mt-1 font-semibold text-foreground">{{ card.priceInput }}</p>
            </div>
            <div class="rounded-[14px] bg-secondary px-3 py-3">
              <p class="text-muted-foreground">{{ t('console.home.outputPrice') }}</p>
              <p class="mt-1 font-semibold text-foreground">{{ card.priceOutput }}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </section>
  </div>
</template>
