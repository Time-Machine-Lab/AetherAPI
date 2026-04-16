<script setup lang="ts">
import { ref, watch } from 'vue'
import { Search } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { getDiscoveryAssetDetail, listDiscoveryAssets } from '@/api/catalog/discovery.api'
import type { DiscoveryAsset, DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import { getAiCapabilityLabels, pushRecentAsset } from '@/features/catalog/catalog-helpers'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'

const { t } = useI18n()

const keyword = ref('')
const assets = ref<DiscoveryAsset[]>([])
const listError = ref(false)
const listLoading = ref(false)

const selectedAsset = ref<DiscoveryAsset | null>(null)
const detail = ref<DiscoveryAssetDetail | null>(null)
const detailLoading = ref(false)
const detailError = ref(false)

async function loadList() {
  listLoading.value = true
  listError.value = false
  try {
    const result = await listDiscoveryAssets({ keyword: keyword.value || undefined })
    assets.value = result.items
  } catch {
    listError.value = true
  } finally {
    listLoading.value = false
  }
}

async function selectAsset(asset: DiscoveryAsset) {
  selectedAsset.value = asset
  detail.value = null
  detailError.value = false
  detailLoading.value = true
  pushRecentAsset(asset)
  try {
    detail.value = await getDiscoveryAssetDetail(asset.apiCode)
  } catch {
    detailError.value = true
  } finally {
    detailLoading.value = false
  }
}

let debounceTimer: ReturnType<typeof setTimeout>
watch(keyword, () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(loadList, 300)
})

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
  <div class="space-y-5">
    <section class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <p class="console-kicker">{{ t('console.home.kicker') }}</p>
        <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
          {{ t('console.home.title') }}
        </h2>
        <p class="mt-3 text-sm leading-6 text-muted-foreground">{{ t('console.home.description') }}</p>
      </div>
      <div class="w-full max-w-sm rounded-full bg-white px-2 py-2 shadow-console">
        <div class="relative">
          <Search class="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            v-model="keyword"
            class="rounded-full border-transparent pl-10 pr-4 shadow-none focus-visible:bg-white"
            :placeholder="t('console.home.searchPlaceholder')"
          />
        </div>
      </div>
    </section>

    <section class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
      <!-- Asset list -->
      <div>
        <div v-if="listLoading" class="py-16 text-center text-sm text-muted-foreground">
          {{ t('console.home.loading') }}
        </div>
        <div v-else-if="listError" class="py-16 text-center text-sm text-destructive">
          {{ t('console.home.listError') }}
        </div>
        <div v-else-if="assets.length === 0" class="py-16 text-center text-sm text-muted-foreground">
          {{ t('console.home.empty') }}
        </div>
        <div v-else class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <Card
            v-for="asset in assets"
            :key="asset.apiCode"
            class="cursor-pointer transition-shadow hover:shadow-console"
            :class="selectedAsset?.apiCode === asset.apiCode ? 'ring-2 ring-primary/30' : ''"
            @click="selectAsset(asset)"
          >
            <CardContent class="p-5">
              <div class="flex items-start justify-between gap-2">
                <p class="truncate text-sm font-semibold text-foreground">{{ asset.displayName }}</p>
                <Badge variant="outline" class="shrink-0 text-[11px]">
                  {{ asset.assetType === 'AI_API' ? 'AI' : 'API' }}
                </Badge>
              </div>
              <p class="mt-1 text-xs text-muted-foreground">{{ asset.apiCode }}</p>
              <p v-if="asset.categoryName" class="mt-3 text-xs text-muted-foreground">
                {{ asset.categoryName }}
              </p>
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
            <div v-if="!selectedAsset" class="py-10 text-center text-sm text-muted-foreground">
              {{ t('console.home.detailEmpty') }}
            </div>
            <div v-else-if="detailLoading" class="py-10 text-center text-sm text-muted-foreground">
              {{ t('console.home.loading') }}
            </div>
            <div v-else-if="detailError" class="py-10 text-center text-sm text-destructive">
              {{ t('console.home.detailError') }}
            </div>
            <div v-else-if="detail" class="space-y-4 text-sm">
              <div>
                <p class="font-semibold text-foreground">{{ detail.displayName }}</p>
                <p class="mt-1 text-xs text-muted-foreground">{{ detail.apiCode }}</p>
              </div>
              <p v-if="detail.description" class="leading-6 text-muted-foreground">
                {{ detail.description }}
              </p>
              <div class="rounded-[14px] bg-secondary px-4 py-3">
                <p class="text-xs text-muted-foreground">{{ t('console.home.authScheme') }}</p>
                <p class="mt-1 font-medium text-foreground">{{ detail.authScheme ?? t('console.home.noAuth') }}</p>
              </div>
              <div v-if="detail.methods?.length" class="flex flex-wrap gap-2">
                <span
                  v-for="method in detail.methods"
                  :key="method"
                  class="rounded-full bg-secondary px-2.5 py-1 text-[11px] font-medium text-foreground"
                >
                  {{ method }}
                </span>
              </div>
              <div v-if="detail.assetType === 'AI_API' && detail.aiProfile" class="space-y-2">
                <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  {{ t('console.home.aiCapability') }}
                </p>
                <div class="rounded-[14px] bg-secondary px-4 py-3 space-y-1">
                  <p class="text-foreground">{{ detail.aiProfile.provider }} / {{ detail.aiProfile.model }}</p>
                  <div class="flex flex-wrap gap-2 pt-1">
                    <span
                      v-for="label in getAiCapabilityLabels(detail.aiProfile)"
                      :key="label"
                      class="rounded-full bg-white px-2.5 py-1 text-[11px] shadow-console"
                    >
                      {{ label }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-if="detail.exampleSnapshot">
                <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  {{ t('console.home.exampleSnapshot') }}
                </p>
                <pre class="overflow-x-auto rounded-[14px] bg-secondary px-4 py-3 text-xs text-foreground">{{ detail.exampleSnapshot }}</pre>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </section>
  </div>
</template>
