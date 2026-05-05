<script setup lang="ts">
import { watch } from 'vue'
import {
  CalendarClock,
  Folder,
  KeyRound,
  MousePointerClick,
  Play,
  Search,
  UserRound,
} from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { getAiCapabilityLabels } from '@/features/catalog/catalog-helpers'
import { useCatalogDiscovery } from '@/composables/useCatalogDiscovery'
import { useApiSubscriptionStatus } from '@/composables/useApiSubscriptionStatus'
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
import { buildUnifiedAccessAddress } from '@/utils/platform-url'
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
        <StateBlock v-if="listLoading" tone="loading" :title="t('console.home.loading')" />
        <StateBlock v-else-if="listError" tone="error" :title="t('console.home.listError')" />
        <StateBlock v-else-if="assets.length === 0" tone="empty" :title="t('console.home.empty')" />
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
                <DisplayTag
                  :tone="assetTypeTone(asset.assetType)"
                  :label="asset.assetType === 'AI_API' ? 'AI' : 'API'"
                />
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
                  <Button size="sm" @click="openPlayground(detail.apiCode)">
                    <Play class="size-4" />
                    {{ t('console.home.tryInPlayground') }}
                  </Button>
                </div>
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
