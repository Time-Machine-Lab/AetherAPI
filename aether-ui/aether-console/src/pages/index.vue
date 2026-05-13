<script setup lang="ts">
import { computed, watch } from 'vue'
import {
  CalendarClock,
  Download,
  Folder,
  KeyRound,
  Loader2,
  MousePointerClick,
  Play,
  Search,
  UserRound,
} from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { getAiCapabilityLabels } from '@/features/catalog/catalog-helpers'
import { useCatalogDiscovery } from '@/composables/useCatalogDiscovery'
import { useCatalogDocExport } from '@/composables/useCatalogDocExport'
import { useApiSubscriptionStatus } from '@/composables/useApiSubscriptionStatus'
import type { CatalogDocLabels } from '@/features/catalog/catalog-doc-export'
import { buildAsyncTaskResponseStructure } from '@/features/catalog/async-task-response-structure'
import type { ApiSubscriptionAccessStatus } from '@/api/subscription/subscription.types'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import MetaItem from '@/components/console/MetaItem.vue'
import CopyableField from '@/components/console/CopyableField.vue'
import CodeBlock from '@/components/console/CodeBlock.vue'
import DisplayTag from '@/components/console/DisplayTag.vue'
import MethodTag from '@/components/console/MethodTag.vue'
import FieldGroup from '@/components/console/FieldGroup.vue'
import StateBlock from '@/components/console/StateBlock.vue'
import { buildUnifiedAccessAddress, buildUnifiedAccessTaskAddress } from '@/utils/platform-url'
import { assetTypeTone } from '@/utils/visual-system'

const { t } = useI18n()
const router = useRouter()
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
const {
  status: subscriptionStatus,
  statusLoading: subscriptionStatusLoading,
  statusError: subscriptionStatusError,
  actionLoading: subscriptionActionLoading,
  actionError: subscriptionActionError,
  canSubscribe,
  canCancel,
  loadStatus: loadSubscriptionStatus,
  subscribe: subscribeToApi,
  cancel: cancelSubscription,
  resetStatus: resetSubscriptionStatus,
} = useApiSubscriptionStatus()
const catalogDocLabels = computed<CatalogDocLabels>(() => ({
  exportTitle: t('console.home.docExport.markdown.exportTitle'),
  generatedAt: t('console.home.docExport.markdown.generatedAt'),
  exportSummary: t('console.home.docExport.markdown.exportSummary'),
  successCount: t('console.home.docExport.markdown.successCount'),
  failedCount: t('console.home.docExport.markdown.failedCount'),
  failedItems: t('console.home.docExport.markdown.failedItems'),
  basicInfo: t('console.home.docExport.markdown.basicInfo'),
  apiCode: t('console.home.docExport.markdown.apiCode'),
  displayName: t('console.home.docExport.markdown.displayName'),
  assetType: t('console.home.docExport.markdown.assetType'),
  category: t('console.home.docExport.markdown.category'),
  publisher: t('console.home.docExport.markdown.publisher'),
  publishedAt: t('console.home.docExport.markdown.publishedAt'),
  platformUnifiedAccessUrl: t('console.home.docExport.markdown.platformUnifiedAccessUrl'),
  description: t('console.home.docExport.markdown.description'),
  request: t('console.home.docExport.markdown.request'),
  requestMethod: t('console.home.docExport.markdown.requestMethod'),
  authScheme: t('console.home.docExport.markdown.authScheme'),
  requestTemplate: t('console.home.docExport.markdown.requestTemplate'),
  requestExample: t('console.home.docExport.markdown.requestExample'),
  responseExample: t('console.home.docExport.markdown.responseExample'),
  asyncTaskQuery: t('console.home.docExport.markdown.asyncTaskQuery'),
  asyncTaskQueryEndpoint: t('console.home.docExport.markdown.asyncTaskQueryEndpoint'),
  asyncTaskAuthMode: t('console.home.docExport.markdown.asyncTaskAuthMode'),
  asyncTaskStatusPath: t('console.home.docExport.markdown.asyncTaskStatusPath'),
  asyncTaskResultPath: t('console.home.docExport.markdown.asyncTaskResultPath'),
  asyncTaskErrorPath: t('console.home.docExport.markdown.asyncTaskErrorPath'),
  asyncTaskResponseStructure: t('console.home.docExport.markdown.asyncTaskResponseStructure'),
  aiCapability: t('console.home.docExport.markdown.aiCapability'),
  provider: t('console.home.docExport.markdown.provider'),
  model: t('console.home.docExport.markdown.model'),
  streaming: t('console.home.docExport.markdown.streaming'),
  tags: t('console.home.docExport.markdown.tags'),
  unavailable: t('console.home.docExport.markdown.unavailable'),
  yes: t('console.home.docExport.markdown.yes'),
  no: t('console.home.docExport.markdown.no'),
  detailLoadFailed: t('console.home.docExport.markdown.detailLoadFailed'),
}))
const {
  selectedCount: exportSelectedCount,
  exporting: docExporting,
  exportFeedback,
  lastFailureCount,
  toggleExportSelection,
  clearExportSelection,
  isSelectedForExport,
  exportCurrentDetail,
  exportSelectedDocs,
} = useCatalogDocExport({
  getLabels: () => catalogDocLabels.value,
})

const exportFeedbackMessage = computed(() => {
  if (exportFeedback.value === 'success') return t('console.home.docExport.success')
  if (exportFeedback.value === 'partial') {
    return t('console.home.docExport.partialSuccess', { count: lastFailureCount.value })
  }
  if (exportFeedback.value === 'failed') return t('console.home.docExport.failed')
  return ''
})

const exportFeedbackClass = computed(() =>
  exportFeedback.value === 'success' || exportFeedback.value === 'partial'
    ? 'text-primary'
    : 'text-destructive',
)
const asyncTaskResponseStructure = computed(() => {
  if (!detail.value?.asyncTaskConfig?.enabled) {
    return undefined
  }

  return buildAsyncTaskResponseStructure(detail.value.asyncTaskConfig, {
    status: t('console.home.asyncTaskStatusValue'),
    result: t('console.home.asyncTaskResultValue'),
    error: t('console.home.asyncTaskErrorValue'),
  })
})

loadList()

function openPlayground(apiCode: string) {
  router.push({
    name: 'console-playground',
    query: {
      apiCode,
      subscriptionStatus: subscriptionStatus.value?.accessStatus,
    },
  })
}

function subscriptionStatusLabel(status?: ApiSubscriptionAccessStatus) {
  if (status === 'SUBSCRIBED') return t('console.home.subscriptionSubscribed')
  if (status === 'OWNER') return t('console.home.subscriptionOwner')
  return t('console.home.subscriptionNotSubscribed')
}

function subscriptionStatusTone(status?: ApiSubscriptionAccessStatus) {
  if (status === 'SUBSCRIBED' || status === 'OWNER') return 'success'
  return 'neutral'
}

async function handleSubscribe() {
  if (!detail.value) return
  await subscribeToApi(detail.value.apiCode)
}

async function handleCancelSubscription() {
  await cancelSubscription(subscriptionStatus.value?.subscriptionId)
}

watch(
  () => detail.value?.apiCode,
  async (apiCode) => {
    if (apiCode) {
      await loadSubscriptionStatus(apiCode)
      return
    }
    resetSubscriptionStatus()
  },
)
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
        <div
          v-if="exportSelectedCount > 0"
          class="mb-4 flex flex-col gap-3 rounded-[14px] border border-primary/15 bg-[color-mix(in_srgb,var(--primary)_6%,white)] px-4 py-3 sm:flex-row sm:items-center sm:justify-between"
        >
          <div>
            <p class="text-sm font-semibold text-foreground">
              {{ t('console.home.docExport.selectedCount', { count: exportSelectedCount }) }}
            </p>
            <p v-if="exportFeedbackMessage" class="mt-1 text-xs" :class="exportFeedbackClass">
              {{ exportFeedbackMessage }}
            </p>
          </div>
          <div class="flex flex-wrap gap-2">
            <Button
              type="button"
              size="xs"
              variant="ghost"
              :disabled="docExporting"
              @click="clearExportSelection"
            >
              {{ t('console.home.docExport.clearSelection') }}
            </Button>
            <Button type="button" size="xs" :disabled="docExporting" @click="exportSelectedDocs">
              <Loader2 v-if="docExporting" class="size-3.5 animate-spin" />
              <Download v-else class="size-3.5" />
              {{
                docExporting
                  ? t('console.home.docExport.exporting')
                  : t('console.home.docExport.exportSelected')
              }}
            </Button>
          </div>
        </div>
        <StateBlock v-if="listLoading" tone="loading" :title="t('console.home.loading')" />
        <StateBlock v-else-if="listError" tone="error" :title="t('console.home.listError')" />
        <StateBlock v-else-if="assets.length === 0" tone="empty" :title="t('console.home.empty')" />
        <div v-else class="grid min-h-[200px] gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <Card
            v-for="asset in assets"
            :key="asset.apiCode"
            class="cursor-pointer transition-[box-shadow,transform,border-color] duration-200 hover:-translate-y-px hover:shadow-console-hover"
            :class="[
              selectedAsset?.apiCode === asset.apiCode
                ? 'ring-2 ring-primary/40 border-primary/25 shadow-console-hover'
                : '',
              isSelectedForExport(asset.apiCode) ? 'border-primary/40 bg-primary/5' : '',
            ]"
            @click="selectAsset(asset)"
          >
            <CardContent class="p-5">
              <div class="flex items-start justify-between gap-2">
                <p class="truncate text-sm font-semibold text-foreground">
                  {{ asset.displayName }}
                </p>
                <div class="flex shrink-0 items-center gap-2">
                  <input
                    type="checkbox"
                    class="size-4 rounded border-[rgb(34_34_34_/_0.18)] text-primary accent-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/20"
                    :checked="isSelectedForExport(asset.apiCode)"
                    :disabled="docExporting"
                    :aria-label="
                      t('console.home.docExport.selectLabel', { name: asset.displayName })
                    "
                    @click.stop
                    @change.stop="toggleExportSelection(asset)"
                  />
                  <DisplayTag
                    :tone="assetTypeTone(asset.assetType)"
                    :label="asset.assetType === 'AI_API' ? 'AI' : 'API'"
                  />
                </div>
              </div>
              <p class="mt-1 text-xs text-muted-foreground">{{ asset.apiCode }}</p>
              <div class="mt-3 flex flex-wrap gap-1.5">
                <MetaItem :icon="UserRound" :value="asset.publisherDisplayName" />
                <MetaItem :icon="CalendarClock" :value="asset.publishedAt" />
                <MetaItem :icon="Folder" :value="asset.categoryName" />
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
              <div v-if="!selectedAsset" key="empty">
                <StateBlock
                  :icon="MousePointerClick"
                  tone="empty"
                  :title="t('console.home.detailEmpty')"
                />
              </div>
              <StateBlock
                v-else-if="detailLoading"
                key="loading"
                tone="loading"
                :title="t('console.home.loading')"
              />
              <StateBlock
                v-else-if="detailError"
                key="error"
                tone="error"
                :title="t('console.home.detailError')"
              />
              <div v-else-if="detail" :key="detail.apiCode" class="space-y-5 text-sm">
                <div class="flex items-start justify-between gap-2">
                  <div class="min-w-0">
                    <p class="font-semibold text-foreground">{{ detail.displayName }}</p>
                    <p class="mt-1 text-xs text-muted-foreground">{{ detail.apiCode }}</p>
                  </div>
                  <DisplayTag
                    :tone="assetTypeTone(detail.assetType)"
                    :label="detail.assetType === 'AI_API' ? 'AI' : 'API'"
                  />
                </div>
                <p v-if="detail.description" class="leading-6 text-muted-foreground">
                  {{ detail.description }}
                </p>
                <div
                  v-if="detail.publisherDisplayName || detail.publishedAt"
                  class="flex flex-wrap gap-1.5 rounded-[14px] bg-secondary px-4 py-3"
                >
                  <MetaItem
                    :icon="UserRound"
                    :label="t('console.home.publisher')"
                    :value="detail.publisherDisplayName"
                  />
                  <MetaItem
                    :icon="CalendarClock"
                    :label="t('console.home.publishedAt')"
                    :value="detail.publishedAt"
                  />
                  <MetaItem
                    :icon="Folder"
                    :label="t('console.home.category')"
                    :value="detail.categoryName"
                  />
                </div>
                <CopyableField
                  :label="t('console.shared.platformCallAddress')"
                  :hint="t('console.shared.platformCallAddressHint')"
                  :value="buildUnifiedAccessAddress(detail.apiCode)"
                />
                <div
                  class="space-y-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white px-4 py-3"
                >
                  <div class="flex flex-wrap items-center justify-between gap-2">
                    <div class="min-w-0">
                      <p
                        class="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground"
                      >
                        {{ t('console.navigation.apiSubscriptions') }}
                      </p>
                      <p class="mt-1 text-xs leading-5 text-muted-foreground">
                        {{ t('console.home.subscriptionGuidance') }}
                      </p>
                    </div>
                    <DisplayTag
                      v-if="subscriptionStatus && !subscriptionStatusLoading"
                      :tone="subscriptionStatusTone(subscriptionStatus.accessStatus)"
                      :label="subscriptionStatusLabel(subscriptionStatus.accessStatus)"
                    />
                  </div>
                  <StateBlock
                    v-if="subscriptionStatusLoading"
                    tone="loading"
                    :title="t('console.home.subscriptionLoading')"
                  />
                  <StateBlock
                    v-else-if="subscriptionStatusError"
                    tone="error"
                    :title="t('console.home.subscriptionStatusError')"
                  />
                  <p v-if="subscriptionActionError" class="text-xs text-destructive">
                    {{ t('console.home.subscriptionActionError') }}
                  </p>
                  <div
                    v-if="subscriptionStatus && !subscriptionStatusLoading"
                    class="flex flex-wrap gap-2"
                  >
                    <Button
                      v-if="canSubscribe"
                      size="sm"
                      :disabled="subscriptionActionLoading"
                      @click="handleSubscribe"
                    >
                      <KeyRound class="size-4" />
                      {{ t('console.home.subscribe') }}
                    </Button>
                    <Button
                      v-if="canCancel"
                      size="sm"
                      variant="outline"
                      :disabled="subscriptionActionLoading"
                      @click="handleCancelSubscription"
                    >
                      {{ t('console.home.cancelSubscription') }}
                    </Button>
                  </div>
                </div>
                <div class="flex flex-wrap gap-2">
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    :disabled="docExporting"
                    @click="exportCurrentDetail(detail)"
                  >
                    <Loader2 v-if="docExporting" class="size-4 animate-spin" />
                    <Download v-else class="size-4" />
                    {{
                      docExporting
                        ? t('console.home.docExport.exporting')
                        : t('console.home.docExport.exportCurrent')
                    }}
                  </Button>
                  <Button size="sm" @click="openPlayground(detail.apiCode)">
                    <Play class="size-4" />
                    {{ t('console.home.tryInPlayground') }}
                  </Button>
                </div>
                <p v-if="exportFeedbackMessage" class="text-xs" :class="exportFeedbackClass">
                  {{ exportFeedbackMessage }}
                </p>
                <FieldGroup :title="t('console.home.detailTitle')">
                  <div class="grid grid-cols-2 gap-3">
                    <MetaItem
                      :icon="KeyRound"
                      :label="t('console.home.authScheme')"
                      :value="detail.authScheme ?? t('console.home.noAuth')"
                    />
                    <MethodTag :method="detail.requestMethod" />
                  </div>
                </FieldGroup>
                <FieldGroup
                  v-if="detail.asyncTaskConfig?.enabled"
                  :title="t('console.home.asyncTaskQuery')"
                  :description="t('console.home.asyncTaskQueryDescription')"
                >
                  <CopyableField
                    :label="t('console.home.asyncTaskQueryEndpoint')"
                    :value="buildUnifiedAccessTaskAddress(detail.apiCode)"
                  />
                  <div class="flex flex-wrap gap-2">
                    <MetaItem
                      :label="t('console.home.requestMethod')"
                      :value="detail.asyncTaskConfig.queryMethod"
                    />
                    <MetaItem
                      :label="t('console.home.asyncTaskAuthMode')"
                      :value="detail.asyncTaskConfig.authMode"
                    />
                    <MetaItem
                      :label="t('console.home.authScheme')"
                      :value="detail.asyncTaskConfig.authScheme"
                    />
                    <MetaItem
                      :label="t('console.home.asyncTaskStatusPath')"
                      :value="detail.asyncTaskConfig.statusPath"
                    />
                    <MetaItem
                      :label="t('console.home.asyncTaskResultPath')"
                      :value="detail.asyncTaskConfig.resultPath"
                    />
                    <MetaItem
                      :label="t('console.home.asyncTaskErrorPath')"
                      :value="detail.asyncTaskConfig.errorPath"
                    />
                  </div>
                  <CodeBlock
                    v-if="asyncTaskResponseStructure"
                    :label="t('console.home.asyncTaskResponseStructure')"
                    :value="asyncTaskResponseStructure"
                  />
                </FieldGroup>
                <div v-if="detail.assetType === 'AI_API' && detail.aiProfile" class="space-y-2">
                  <FieldGroup :title="t('console.home.aiCapability')">
                    <p class="text-foreground">
                      {{ detail.aiProfile.provider }} / {{ detail.aiProfile.model }}
                    </p>
                    <div class="flex flex-wrap gap-2 pt-1">
                      <DisplayTag
                        v-for="label in getAiCapabilityLabels(detail.aiProfile)"
                        :key="label"
                        tone="ai"
                        :label="label"
                      />
                    </div>
                  </FieldGroup>
                </div>
                <div v-if="detail.requestTemplate">
                  <CodeBlock
                    :label="t('console.home.requestTemplate')"
                    :value="detail.requestTemplate"
                  />
                </div>
                <div v-if="detail.exampleSnapshot?.requestExample">
                  <CodeBlock
                    :label="t('console.home.requestExample')"
                    :value="detail.exampleSnapshot.requestExample"
                  />
                </div>
                <div v-if="detail.exampleSnapshot?.responseExample">
                  <CodeBlock
                    :label="t('console.home.responseExample')"
                    :value="detail.exampleSnapshot.responseExample"
                  />
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
