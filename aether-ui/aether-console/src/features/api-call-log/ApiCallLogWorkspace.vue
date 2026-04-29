<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useApiCallLogWorkspace } from '@/composables/useApiCallLogWorkspace'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import CodeBlock from '@/components/console/CodeBlock.vue'
import DisplayTag from '@/components/console/DisplayTag.vue'
import MethodTag from '@/components/console/MethodTag.vue'
import DataListRow from '@/components/console/DataListRow.vue'
import FieldGroup from '@/components/console/FieldGroup.vue'
import StateBlock from '@/components/console/StateBlock.vue'
import MetaItem from '@/components/console/MetaItem.vue'
import { contractLimitedDiagnosticFieldKeys } from '@/features/api-call-log/api-call-log-diagnostics'
import { AlertTriangle, CheckCircle2, Clock3, KeyRound, Network } from 'lucide-vue-next'
import { successTone } from '@/utils/visual-system'

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

function diagnosticUnavailableFields() {
  return contractLimitedDiagnosticFieldKeys.map((key) => t(key))
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
            <DisplayTag
              tone="neutral"
              :label="
                t('console.apiCallLogs.listSummary', {
                  from: showingFrom,
                  to: showingTo,
                  total: listTotal,
                })
              "
            />
          </div>
        </CardHeader>
        <CardContent class="space-y-3">
          <StateBlock v-if="listLoading" tone="loading" :title="t('console.workspace.loading')" />
          <StateBlock
            v-else-if="listError"
            tone="error"
            :title="t('console.apiCallLogs.listError')"
          />
          <StateBlock
            v-else-if="listItems.length === 0"
            tone="empty"
            :title="t('console.apiCallLogs.listEmpty')"
          />
          <div v-else class="space-y-2">
            <DataListRow
              v-for="item in listItems"
              :key="item.logId"
              as="button"
              :selected="selectedLogId === item.logId"
              @click="handleSelectLog(item)"
            >
              <template #title>
                <p class="truncate text-sm font-semibold text-foreground">
                  {{ formatValue(item.targetApiName) }}
                </p>
              </template>
              <template #description>
                <p class="text-xs text-muted-foreground">{{ item.targetApiCode }}</p>
              </template>
              <template #meta>
                <MetaItem :icon="Clock3" :value="formatDateTime(item.invocationTime)" />
                <MetaItem :icon="Clock3" :value="`${item.durationMs} ms`" />
              </template>
              <template #tags>
                <DisplayTag
                  :tone="successTone(item.success)"
                  :label="
                    item.success
                      ? t('console.apiCallLogs.success')
                      : t('console.apiCallLogs.failed')
                  "
                />
                <MethodTag :method="item.requestMethod" />
                <DisplayTag tone="neutral" :label="item.resultType" />
                <DisplayTag v-if="item.httpStatusCode" tone="info" :label="item.httpStatusCode" />
              </template>
            </DataListRow>
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
          <StateBlock
            v-if="detailLoading"
            tone="loading"
            :title="t('console.apiCallLogs.detailLoading')"
          />
          <StateBlock
            v-else-if="detailError"
            tone="error"
            :title="t('console.apiCallLogs.detailError')"
          />
          <StateBlock
            v-else-if="!selectedLogDetail"
            tone="empty"
            :title="t('console.apiCallLogs.detailEmpty')"
          />
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

            <FieldGroup :title="t('console.apiCallLogs.groups.basic')">
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
                  <dd class="mt-1">
                    <MethodTag :method="selectedLogDetail.requestMethod" />
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
                  <dd class="mt-1">
                    <DisplayTag tone="neutral" :label="formatValue(selectedLogDetail.resultType)" />
                  </dd>
                </div>
                <div>
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.apiCallLogs.fields.httpStatusCode') }}
                  </dt>
                  <dd class="mt-1">
                    <DisplayTag
                      tone="info"
                      :label="formatValue(selectedLogDetail.httpStatusCode)"
                    />
                  </dd>
                </div>
                <div>
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.apiCallLogs.fields.accessChannel') }}
                  </dt>
                  <dd class="mt-1">
                    <DisplayTag
                      :icon="Network"
                      tone="info"
                      :label="formatValue(selectedLogDetail.accessChannel)"
                    />
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
                  <dd class="mt-1">
                    <DisplayTag
                      :icon="KeyRound"
                      tone="neutral"
                      :label="formatValue(selectedLogDetail.credentialStatus)"
                    />
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
            </FieldGroup>

            <FieldGroup :title="t('console.apiCallLogs.groups.error')">
              <div class="text-sm">
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
            </FieldGroup>

            <FieldGroup :title="t('console.apiCallLogs.groups.ai')">
              <div class="text-sm">
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
                  <CodeBlock
                    v-if="selectedLogDetail.aiExtension.usageSnapshot"
                    class="mt-3"
                    :label="t('console.apiCallLogs.fields.usageSnapshot')"
                    :value="selectedLogDetail.aiExtension.usageSnapshot"
                  />
                </template>
                <p v-else class="text-muted-foreground">
                  {{ t('console.apiCallLogs.noAiExtension') }}
                </p>
              </div>
            </FieldGroup>

            <FieldGroup
              :title="t('console.apiCallLogs.groups.contract')"
              :description="t('console.apiCallLogs.diagnosticDescription')"
            >
              <div
                class="rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/40 p-4 text-sm"
              >
                <div class="flex flex-wrap gap-2">
                  <DisplayTag
                    v-for="field in diagnosticUnavailableFields()"
                    :key="field"
                    tone="neutral"
                    :label="`${field} · ${t('console.shared.unavailable')}`"
                  />
                </div>
                <p class="mt-3 text-xs leading-5 text-muted-foreground">
                  {{ t('console.apiCallLogs.contractLimitedHint') }}
                </p>
              </div>
            </FieldGroup>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
