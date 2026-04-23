<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useApiCallLogWorkspace } from '@/composables/useApiCallLogWorkspace'
import type { ApiCallLogItem } from '@/api/api-call-log/api-call-log.types'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { AlertTriangle, CheckCircle2, Clock3, Loader2 } from 'lucide-vue-next'

const { t } = useI18n()

const {
  listLoading,
  listError,
  listItems,
  listTotal,
  listPage,
  targetApiCodeFilter,
  invocationStartAt,
  invocationEndAt,
  filterError,
  selectedLogId,
  selectedLogDetail,
  detailLoading,
  detailError,
  pageCount,
  showingFrom,
  showingTo,
  handleSelectLog,
  handleSearch,
  handleReset,
  handlePrevPage,
  handleNextPage,
} = useApiCallLogWorkspace({ t })

function formatDateTime(iso: string | null): string {
  if (!iso) {
    return '—'
  }

  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}

function formatValue(value: string | number | boolean | null | undefined): string {
  if (value === null || value === undefined || value === '') {
    return '—'
  }
  return String(value)
}

function statusBadgeVariant(log: ApiCallLogItem) {
  if (log.success) {
    return 'status-enabled' as const
  }
  return 'destructive' as const
}
</script>

<template>
  <div class="space-y-6">
    <section>
      <p class="console-kicker">{{ t('console.navigation.apiCallLogs') }}</p>
      <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
        {{ t('console.apiCallLogs.title') }}
      </h2>
      <p class="mt-3 text-sm leading-6 text-muted-foreground">
        {{ t('console.apiCallLogs.description') }}
      </p>
    </section>

    <Card>
      <CardHeader>
        <CardTitle>{{ t('console.apiCallLogs.filterTitle') }}</CardTitle>
        <CardDescription>{{ t('console.apiCallLogs.filterDescription') }}</CardDescription>
      </CardHeader>
      <CardContent class="space-y-3">
        <div class="grid gap-3 lg:grid-cols-[minmax(0,1fr)_220px_220px]">
          <Input
            v-model="targetApiCodeFilter"
            :placeholder="t('console.apiCallLogs.filterTargetApiPlaceholder')"
          />
          <Input v-model="invocationStartAt" type="datetime-local" />
          <Input v-model="invocationEndAt" type="datetime-local" />
        </div>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <p v-if="filterError" class="text-sm text-destructive">{{ filterError }}</p>
          <div class="ml-auto flex flex-wrap gap-2">
            <Button size="sm" variant="outline" @click="handleReset">{{
              t('console.apiCallLogs.reset')
            }}</Button>
            <Button size="sm" :disabled="listLoading" @click="handleSearch">{{
              t('console.apiCallLogs.search')
            }}</Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <div class="grid gap-5 xl:grid-cols-[1fr_420px]">
      <Card>
        <CardHeader>
          <div class="flex items-center justify-between gap-3">
            <div>
              <CardTitle>{{ t('console.apiCallLogs.listTitle') }}</CardTitle>
              <CardDescription>{{ t('console.apiCallLogs.listDescription') }}</CardDescription>
            </div>
            <Badge variant="outline">
              {{
                t('console.apiCallLogs.listSummary', {
                  from: showingFrom,
                  to: showingTo,
                  total: listTotal,
                })
              }}
            </Badge>
          </div>
        </CardHeader>
        <CardContent class="space-y-3">
          <div
            v-if="listLoading"
            class="flex items-center justify-center gap-2 py-9 text-sm text-muted-foreground"
          >
            <Loader2 class="size-4 animate-spin" />
            <span>{{ t('console.workspace.loading') }}</span>
          </div>
          <div
            v-else-if="listError"
            class="flex items-center justify-center gap-2 py-9 text-sm text-destructive"
          >
            <AlertTriangle class="size-4" />
            <span>{{ t('console.apiCallLogs.listError') }}</span>
          </div>
          <div
            v-else-if="listItems.length === 0"
            class="py-9 text-center text-sm text-muted-foreground"
          >
            {{ t('console.apiCallLogs.listEmpty') }}
          </div>
          <div v-else class="space-y-2">
            <button
              v-for="item in listItems"
              :key="item.logId"
              type="button"
              class="w-full rounded-[14px] border bg-white px-4 py-3 text-left shadow-console transition-[box-shadow,transform] duration-200 hover:-translate-y-px hover:shadow-console-hover"
              :class="
                selectedLogId === item.logId ? 'border-primary/35' : 'border-[rgb(34_34_34_/_0.06)]'
              "
              @click="handleSelectLog(item)"
            >
              <div class="flex flex-wrap items-start justify-between gap-3">
                <div class="min-w-0 space-y-1">
                  <p class="truncate text-sm font-semibold text-foreground">
                    {{ formatValue(item.targetApiName) }}
                  </p>
                  <p class="text-xs text-muted-foreground">
                    {{ item.targetApiCode }} · {{ item.requestMethod }}
                  </p>
                  <p class="text-xs text-muted-foreground">
                    {{ formatDateTime(item.invocationTime) }}
                  </p>
                </div>
                <div class="flex flex-wrap items-center gap-2">
                  <Badge :variant="statusBadgeVariant(item)">
                    {{
                      item.success
                        ? t('console.apiCallLogs.success')
                        : t('console.apiCallLogs.failed')
                    }}
                  </Badge>
                  <Badge variant="outline">{{ item.resultType }}</Badge>
                  <Badge variant="outline">
                    <Clock3 class="mr-1 size-3" />
                    {{ item.durationMs }} ms
                  </Badge>
                </div>
              </div>
            </button>
          </div>

          <div class="flex items-center justify-end gap-2 border-t border-border/70 pt-3">
            <Button
              size="xs"
              variant="outline"
              :disabled="listLoading || listPage <= 1"
              @click="handlePrevPage"
            >
              {{ t('console.apiCallLogs.prevPage') }}
            </Button>
            <span class="text-xs text-muted-foreground">
              {{ t('console.apiCallLogs.pageLabel', { page: listPage, totalPages: pageCount }) }}
            </span>
            <Button
              size="xs"
              variant="outline"
              :disabled="listLoading || listPage >= pageCount"
              @click="handleNextPage"
            >
              {{ t('console.apiCallLogs.nextPage') }}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{{ t('console.apiCallLogs.detailTitle') }}</CardTitle>
          <CardDescription>{{ t('console.apiCallLogs.detailDescription') }}</CardDescription>
        </CardHeader>
        <CardContent>
          <div
            v-if="detailLoading"
            class="flex items-center gap-2 py-2 text-sm text-muted-foreground"
          >
            <Loader2 class="size-4 animate-spin" />
            <span>{{ t('console.apiCallLogs.detailLoading') }}</span>
          </div>
          <div
            v-else-if="detailError"
            class="rounded-[14px] border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive"
          >
            {{ t('console.apiCallLogs.detailError') }}
          </div>
          <div v-else-if="!selectedLogDetail" class="py-2 text-sm text-muted-foreground">
            {{ t('console.apiCallLogs.detailEmpty') }}
          </div>
          <div v-else class="space-y-4">
            <div class="rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/40 p-4">
              <div class="flex items-center gap-2 text-sm font-semibold text-foreground">
                <CheckCircle2 v-if="selectedLogDetail.success" class="size-4 text-primary" />
                <AlertTriangle v-else class="size-4 text-destructive" />
                <span>
                  {{
                    selectedLogDetail.success
                      ? t('console.apiCallLogs.success')
                      : t('console.apiCallLogs.failed')
                  }}
                </span>
              </div>
              <p class="mt-2 break-all text-xs text-muted-foreground">
                {{ t('console.apiCallLogs.logIdLabel') }}: {{ selectedLogDetail.logId }}
              </p>
            </div>

            <dl class="grid gap-3 text-sm sm:grid-cols-2">
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.targetApiCode') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatValue(selectedLogDetail.targetApiCode) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.targetApiName') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatValue(selectedLogDetail.targetApiName) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.requestMethod') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatValue(selectedLogDetail.requestMethod) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.invocationTime') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatDateTime(selectedLogDetail.invocationTime) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.durationMs') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ selectedLogDetail.durationMs }} ms
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.resultType') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatValue(selectedLogDetail.resultType) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.httpStatusCode') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatValue(selectedLogDetail.httpStatusCode) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.accessChannel') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatValue(selectedLogDetail.accessChannel) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.credentialCode') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatValue(selectedLogDetail.credentialCode) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.credentialStatus') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatValue(selectedLogDetail.credentialStatus) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.createdAt') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatDateTime(selectedLogDetail.createdAt) }}
                </dd>
              </div>
              <div>
                <dt class="text-xs text-muted-foreground">
                  {{ t('console.apiCallLogs.fields.updatedAt') }}
                </dt>
                <dd class="mt-1 font-medium text-foreground">
                  {{ formatDateTime(selectedLogDetail.updatedAt) }}
                </dd>
              </div>
            </dl>

            <div class="space-y-3">
              <h3 class="text-sm font-semibold text-foreground">
                {{ t('console.apiCallLogs.errorTitle') }}
              </h3>
              <div class="rounded-[14px] border border-[rgb(34_34_34_/_0.06)] p-3 text-sm">
                <template v-if="selectedLogDetail.error">
                  <p>
                    <span class="text-muted-foreground"
                      >{{ t('console.apiCallLogs.fields.errorCode') }}:</span
                    >
                    {{ formatValue(selectedLogDetail.error.errorCode) }}
                  </p>
                  <p>
                    <span class="text-muted-foreground"
                      >{{ t('console.apiCallLogs.fields.errorType') }}:</span
                    >
                    {{ formatValue(selectedLogDetail.error.errorType) }}
                  </p>
                  <p>
                    <span class="text-muted-foreground"
                      >{{ t('console.apiCallLogs.fields.errorSummary') }}:</span
                    >
                    {{ formatValue(selectedLogDetail.error.errorSummary) }}
                  </p>
                </template>
                <p v-else class="text-muted-foreground">{{ t('console.apiCallLogs.noError') }}</p>
              </div>
            </div>

            <div class="space-y-3">
              <h3 class="text-sm font-semibold text-foreground">
                {{ t('console.apiCallLogs.aiExtensionTitle') }}
              </h3>
              <div class="rounded-[14px] border border-[rgb(34_34_34_/_0.06)] p-3 text-sm">
                <template v-if="selectedLogDetail.aiExtension">
                  <p>
                    <span class="text-muted-foreground"
                      >{{ t('console.apiCallLogs.fields.provider') }}:</span
                    >
                    {{ formatValue(selectedLogDetail.aiExtension.provider) }}
                  </p>
                  <p>
                    <span class="text-muted-foreground"
                      >{{ t('console.apiCallLogs.fields.model') }}:</span
                    >
                    {{ formatValue(selectedLogDetail.aiExtension.model) }}
                  </p>
                  <p>
                    <span class="text-muted-foreground"
                      >{{ t('console.apiCallLogs.fields.streaming') }}:</span
                    >
                    {{ formatValue(selectedLogDetail.aiExtension.streaming) }}
                  </p>
                  <p class="break-all">
                    <span class="text-muted-foreground"
                      >{{ t('console.apiCallLogs.fields.usageSnapshot') }}:</span
                    >
                    {{ formatValue(selectedLogDetail.aiExtension.usageSnapshot) }}
                  </p>
                </template>
                <p v-else class="text-muted-foreground">
                  {{ t('console.apiCallLogs.noAiExtension') }}
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
