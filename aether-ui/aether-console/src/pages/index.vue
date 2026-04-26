<script setup lang="ts">
import { Search, MousePointerClick } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { getAiCapabilityLabels } from '@/features/catalog/catalog-helpers'
import { useCatalogDiscovery } from '@/composables/useCatalogDiscovery'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'

const { t } = useI18n()
const {
  keyword,
  assets,
  listError,
  listLoading,
  selectedAsset,
  detail,
  detailLoading,
  detailError,
  loadList,
  selectAsset,
} = useCatalogDiscovery()

loadList()
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
    <section class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <p class="console-kicker">{{ t('console.home.kicker') }}</p>
        <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
          {{ t('console.home.title') }}
        </h2>
        <p class="mt-3 text-sm leading-6 text-muted-foreground">
          {{ t('console.home.description') }}
        </p>
      </div>
      <div class="relative w-full max-w-sm">
        <div class="relative">
          <Search
            class="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
          />
          <Input
            v-model="keyword"
            class="rounded-full border-[rgb(34_34_34_/_0.06)] pl-10 pr-4 shadow-none focus-visible:border-primary focus-visible:shadow-console"
            :placeholder="t('console.home.searchPlaceholder')"
          />
        </div>
      </div>
    </section>

    <section class="grid items-start gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
      <!-- Asset list -->
      <div>
        <div v-if="listLoading" class="py-16 text-center text-sm text-muted-foreground">
          {{ t('console.home.loading') }}
        </div>
        <div v-else-if="listError" class="py-16 text-center text-sm text-destructive">
          {{ t('console.home.listError') }}
        </div>
        <div
          v-else-if="assets.length === 0"
          class="py-16 text-center text-sm text-muted-foreground"
        >
          {{ t('console.home.empty') }}
        </div>
        <div v-else class="grid min-h-[200px] gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <Card
            v-for="asset in assets"
            :key="asset.apiCode"
            class="cursor-pointer transition-[box-shadow,transform,border-color] duration-200 hover:-translate-y-px hover:shadow-console-hover"
            :class="
              selectedAsset?.apiCode === asset.apiCode
                ? 'ring-2 ring-primary/40 border-primary/25 shadow-console-hover'
                : ''
            "
            @click="selectAsset(asset)"
          >
            <CardContent class="p-5">
              <div class="flex items-start justify-between gap-2">
                <p class="truncate text-sm font-semibold text-foreground">
                  {{ asset.displayName }}
                </p>
                <Badge
                  :variant="asset.assetType === 'AI_API' ? 'type-ai' : 'type-api'"
                  class="shrink-0 text-[11px]"
                >
                  {{ asset.assetType === 'AI_API' ? 'AI' : 'API' }}
                </Badge>
              </div>
              <p class="mt-1 text-xs text-muted-foreground">{{ asset.apiCode }}</p>
              <p v-if="asset.publisherDisplayName" class="mt-2 text-xs text-muted-foreground">
                {{ t('console.home.publisher') }} {{ asset.publisherDisplayName }}
              </p>
              <p v-if="asset.publishedAt" class="mt-1 text-xs text-muted-foreground">
                {{ t('console.home.publishedAt') }} {{ asset.publishedAt }}
              </p>
              <div v-if="asset.categoryName" class="mt-3 flex flex-wrap items-center gap-2">
                <span
                  class="rounded-[8px] border border-[rgb(34_34_34_/_0.06)] bg-secondary px-2 py-0.5 text-[11px] text-muted-foreground"
                >
                  {{ asset.categoryName }}
                </span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <!-- Detail panel -->
      <div>
        <Card class="sticky top-24">
          <CardHeader>
            <CardTitle class="text-base">{{ t('console.home.detailTitle') }}</CardTitle>
          </CardHeader>
          <CardContent>
            <Transition name="fade" mode="out-in">
              <div
                v-if="!selectedAsset"
                key="empty"
                class="flex flex-col items-center gap-3 py-12 text-center"
              >
                <MousePointerClick class="size-8 text-muted-foreground/40" />
                <p class="text-sm text-muted-foreground">{{ t('console.home.detailEmpty') }}</p>
              </div>
              <div
                v-else-if="detailLoading"
                key="loading"
                class="py-10 text-center text-sm text-muted-foreground"
              >
                {{ t('console.home.loading') }}
              </div>
              <div
                v-else-if="detailError"
                key="error"
                class="py-10 text-center text-sm text-destructive"
              >
                {{ t('console.home.detailError') }}
              </div>
              <div v-else-if="detail" :key="detail.apiCode" class="space-y-5 text-sm">
                <div class="flex items-start justify-between gap-2">
                  <div class="min-w-0">
                    <p class="font-semibold text-foreground">{{ detail.displayName }}</p>
                    <p class="mt-1 text-xs text-muted-foreground">{{ detail.apiCode }}</p>
                  </div>
                  <Badge
                    :variant="detail.assetType === 'AI_API' ? 'type-ai' : 'type-api'"
                    class="shrink-0 text-[11px]"
                  >
                    {{ detail.assetType === 'AI_API' ? 'AI' : 'API' }}
                  </Badge>
                </div>
                <p v-if="detail.description" class="leading-6 text-muted-foreground">
                  {{ detail.description }}
                </p>
                <div
                  v-if="detail.publisherDisplayName || detail.publishedAt"
                  class="rounded-[14px] bg-secondary px-4 py-3 text-xs text-muted-foreground"
                >
                  <p v-if="detail.publisherDisplayName">
                    {{ t('console.home.publisher') }} {{ detail.publisherDisplayName }}
                  </p>
                  <p v-if="detail.publishedAt" class="mt-1">
                    {{ t('console.home.publishedAt') }} {{ detail.publishedAt }}
                  </p>
                </div>
                <div class="grid grid-cols-2 gap-3">
                  <div class="rounded-[14px] bg-secondary px-4 py-3">
                    <p class="text-xs text-muted-foreground">{{ t('console.home.authScheme') }}</p>
                    <p class="mt-1 font-medium text-foreground">
                      {{ detail.authScheme ?? t('console.home.noAuth') }}
                    </p>
                  </div>
                  <div v-if="detail.requestMethod" class="rounded-[14px] bg-secondary px-4 py-3">
                    <p class="text-xs text-muted-foreground">
                      {{ t('console.home.requestMethod') }}
                    </p>
                    <p
                      class="mt-1 font-mono font-bold"
                      :class="{
                        'text-[var(--palette-text-legal)]': detail.requestMethod === 'GET',
                        'text-primary': ['POST', 'PUT', 'PATCH'].includes(detail.requestMethod),
                        'text-destructive': detail.requestMethod === 'DELETE',
                      }"
                    >
                      {{ detail.requestMethod }}
                    </p>
                  </div>
                </div>
                <div v-if="detail.assetType === 'AI_API' && detail.aiProfile" class="space-y-2">
                  <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    {{ t('console.home.aiCapability') }}
                  </p>
                  <div class="rounded-[14px] bg-secondary px-4 py-3 space-y-1">
                    <p class="text-foreground">
                      {{ detail.aiProfile.provider }} / {{ detail.aiProfile.model }}
                    </p>
                    <div class="flex flex-wrap gap-2 pt-1">
                      <span
                        v-for="label in getAiCapabilityLabels(detail.aiProfile)"
                        :key="label"
                        class="rounded-full bg-white px-2.5 py-1 text-[11px] font-medium text-[var(--chart-3)] shadow-console"
                      >
                        {{ label }}
                      </span>
                    </div>
                  </div>
                </div>
                <div v-if="detail.requestTemplate">
                  <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-primary/70">
                    {{ t('console.home.requestTemplate') }}
                  </p>
                  <pre
                    class="overflow-x-auto rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-[#fafafa] px-4 py-3 text-xs leading-5 text-foreground"
                    >{{ detail.requestTemplate }}</pre
                  >
                </div>
                <div v-if="detail.exampleSnapshot?.requestExample">
                  <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-primary/70">
                    {{ t('console.home.requestExample') }}
                  </p>
                  <pre
                    class="overflow-x-auto rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-[#fafafa] px-4 py-3 text-xs leading-5 text-foreground"
                    >{{ detail.exampleSnapshot.requestExample }}</pre
                  >
                </div>
                <div v-if="detail.exampleSnapshot?.responseExample">
                  <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-primary/70">
                    {{ t('console.home.responseExample') }}
                  </p>
                  <pre
                    class="overflow-x-auto rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-[#fafafa] px-4 py-3 text-xs leading-5 text-foreground"
                    >{{ detail.exampleSnapshot.responseExample }}</pre
                  >
                </div>
              </div>
            </Transition>
          </CardContent>
        </Card>
      </div>
    </section>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
