<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { BadgeCheck, CalendarClock, UserRound, XCircle } from 'lucide-vue-next'
import {
  canCancelSubscription,
  useApiSubscriptionWorkspace,
} from '@/composables/useApiSubscriptionWorkspace'
import type {
  ApiSubscription,
  ApiSubscriptionRecordStatus,
} from '@/api/subscription/subscription.types'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import DataListRow from '@/components/console/DataListRow.vue'
import DisplayTag from '@/components/console/DisplayTag.vue'
import MetaItem from '@/components/console/MetaItem.vue'
import StateBlock from '@/components/console/StateBlock.vue'
import type { DisplayTone } from '@/utils/visual-system'

const { t } = useI18n()

const {
  items,
  page,
  total,
  loading,
  error,
  actionError,
  cancellingId,
  totalPages,
  loadSubscriptions,
  cancelSubscription,
} = useApiSubscriptionWorkspace()

onMounted(() => {
  loadSubscriptions(1)
})

function formatDateTime(value?: string | null) {
  if (!value) return ''
  try {
    return new Date(value).toLocaleString()
  } catch {
    return value
  }
}

function statusLabel(status: ApiSubscriptionRecordStatus) {
  if (status === 'ACTIVE') return t('console.subscriptions.statusActive')
  if (status === 'CANCELLED') return t('console.subscriptions.statusCancelled')
  return t('console.subscriptions.statusOwner')
}

function statusTone(status: ApiSubscriptionRecordStatus): DisplayTone {
  if (status === 'ACTIVE') return 'success'
  if (status === 'CANCELLED') return 'neutral'
  return 'info'
}

function rowDescription(subscription: ApiSubscription) {
  return subscription.assetName || subscription.apiCode
}

async function confirmCancel(subscription: ApiSubscription) {
  if (!canCancelSubscription(subscription)) return
  if (!window.confirm(t('console.subscriptions.cancelConfirm'))) return
  await cancelSubscription(subscription)
}
</script>

<template>
  <div class="space-y-6">
    <section>
      <p class="console-kicker">{{ t('console.navigation.apiSubscriptions') }}</p>
      <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
        {{ t('console.subscriptions.title') }}
      </h2>
      <p class="mt-3 text-sm leading-6 text-muted-foreground">
        {{ t('console.subscriptions.description') }}
      </p>
    </section>

    <Card id="api-subscriptions" class="scroll-mt-24">
      <CardHeader>
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div>
            <CardTitle>{{ t('console.subscriptions.listTitle') }}</CardTitle>
            <CardDescription>{{ t('console.subscriptions.listDescription') }}</CardDescription>
          </div>
          <Button size="sm" variant="outline" :disabled="loading" @click="loadSubscriptions(1)">
            {{ t('console.subscriptions.refresh') }}
          </Button>
        </div>
      </CardHeader>
      <CardContent class="space-y-4">
        <StateBlock v-if="loading" tone="loading" :title="t('console.subscriptions.loading')" />
        <StateBlock v-else-if="error" tone="error" :title="t('console.subscriptions.listError')" />
        <StateBlock
          v-else-if="items.length === 0"
          tone="empty"
          :title="t('console.subscriptions.listEmpty')"
          :description="t('console.subscriptions.listEmptyDescription')"
        />
        <div v-else class="space-y-2">
          <DataListRow
            v-for="subscription in items"
            :key="subscription.subscriptionId ?? subscription.apiCode"
          >
            <template #title>
              <p class="truncate text-sm font-medium text-foreground">
                {{ rowDescription(subscription) }}
              </p>
            </template>
            <template #description>
              <p class="text-xs text-muted-foreground">{{ subscription.apiCode }}</p>
            </template>
            <template #meta>
              <MetaItem
                :icon="UserRound"
                :label="t('console.subscriptions.owner')"
                :value="subscription.assetOwnerUserId"
              />
              <MetaItem
                :icon="CalendarClock"
                :label="t('console.subscriptions.createdAt')"
                :value="formatDateTime(subscription.createdAt)"
              />
              <MetaItem
                :icon="CalendarClock"
                :label="t('console.subscriptions.cancelledAt')"
                :value="formatDateTime(subscription.cancelledAt)"
              />
            </template>
            <template #tags>
              <DisplayTag
                :icon="subscription.subscriptionStatus === 'ACTIVE' ? BadgeCheck : undefined"
                :tone="statusTone(subscription.subscriptionStatus)"
                :label="statusLabel(subscription.subscriptionStatus)"
              />
            </template>
            <template #actions>
              <Button
                size="xs"
                variant="outline"
                :disabled="
                  !canCancelSubscription(subscription) ||
                  cancellingId === subscription.subscriptionId
                "
                @click="confirmCancel(subscription)"
              >
                <XCircle class="size-3.5" />
                {{ t('console.subscriptions.cancel') }}
              </Button>
            </template>
          </DataListRow>

          <p v-if="actionError" class="text-sm text-destructive">{{ actionError }}</p>

          <div class="flex items-center justify-between pt-2 text-xs text-muted-foreground">
            <span>
              {{
                t('console.subscriptions.pageSummary', {
                  page,
                  totalPages,
                  total,
                })
              }}
            </span>
            <div class="flex gap-1">
              <Button
                size="xs"
                variant="outline"
                :disabled="page <= 1 || loading"
                @click="loadSubscriptions(page - 1)"
              >
                {{ t('console.subscriptions.prev') }}
              </Button>
              <Button
                size="xs"
                variant="outline"
                :disabled="page >= totalPages || loading"
                @click="loadSubscriptions(page + 1)"
              >
                {{ t('console.subscriptions.next') }}
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
