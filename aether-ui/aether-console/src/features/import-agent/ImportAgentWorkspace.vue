<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import {
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Loader2,
  Paperclip,
  Play,
  RefreshCw,
  RotateCcw,
  Send,
  X,
} from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/useAuthStore'
import { useImportAgentWorkspace } from '@/composables/useImportAgentWorkspace'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import DisplayTag from '@/components/console/DisplayTag.vue'
import FieldLabel from '@/components/console/FieldLabel.vue'
import StateBlock from '@/components/console/StateBlock.vue'
import type { ImportAgentRunStatus, ImportAgentSessionStatus } from '@/api/import-agent/import-agent.types'

const { t } = useI18n()
const authStore = useAuthStore()
const { currentUser } = storeToRefs(authStore)

const workspace = useImportAgentWorkspace({ t })

const {
  documentSource,
  documentSummary,
  publisherDisplayName,
  messageDraft,
  draftAttachments,
  activeSession,
  activeRun,
  currentPlan,
  restoring,
  creating,
  refreshingSession,
  appending,
  confirming,
  startingRun,
  refreshingRun,
  attachingFiles,
  sessionError,
  turnError,
  runError,
  attachmentError,
  hasActiveSession,
  canSendMessage,
  canConfirmPlan,
  canStartRun,
  refreshSession,
  restoreActiveSession,
  sendMessage,
  confirmPlan,
  startRun,
  refreshRun,
  addDraftFiles,
  removeDraftAttachment,
  resetDraft,
} = workspace

const fileInputRef = ref<HTMLInputElement | null>(null)
const showContextPanel = ref(false)

const isPlanConfirmed = computed(
  () => activeSession.value?.confirmedPlanVersion === currentPlan.value?.version,
)
const composerError = computed(() => (hasActiveSession.value ? turnError.value : sessionError.value))
const sendButtonBusy = computed(() => (hasActiveSession.value ? appending.value : creating.value))
const sendButtonLabel = computed(() => {
  if (hasActiveSession.value) {
    return appending.value ? t('console.importAgent.sendingFollowup') : t('console.importAgent.sendFollowup')
  }
  return creating.value ? t('console.importAgent.sendingFirstMessage') : t('console.importAgent.sendFirstMessage')
})
const composerPlaceholder = computed(() => {
  if (hasActiveSession.value) {
    return t('console.importAgent.turnMessagePlaceholder')
  }
  return t('console.importAgent.importIntentPlaceholder')
})
const attachedFileAccept =
  '.txt,.md,.markdown,.json,.yaml,.yml,.csv,.http,.xml,.log,.js,.ts,.mjs,.cjs,text/*,application/json,application/xml,text/xml,text/csv'

function sessionStatusLabel(status: ImportAgentSessionStatus) {
  return t(`console.importAgent.sessionStatus.${status}`)
}

function runStatusLabel(status: ImportAgentRunStatus) {
  return t(`console.importAgent.runStatus.${status}`)
}

function sessionStatusTone(status: ImportAgentSessionStatus) {
  if (status === 'FAILED') return 'danger'
  if (status === 'COMPLETED' || status === 'CONFIRMED') return 'success'
  if (status === 'EXECUTING') return 'info'
  return 'neutral'
}

function runStatusTone(status: ImportAgentRunStatus) {
  if (status === 'FAILED') return 'danger'
  if (status === 'SUCCEEDED') return 'success'
  if (status === 'PARTIALLY_FAILED') return 'warning'
  return 'info'
}

function stepStatusTone(status: 'SUCCEEDED' | 'FAILED') {
  return status === 'SUCCEEDED' ? 'success' : 'danger'
}

function formatDateTime(value?: string | null) {
  if (!value) {
    return '—'
  }

  try {
    return new Date(value).toLocaleString()
  } catch {
    return value
  }
}

function formatFileSize(size: number) {
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function openFilePicker() {
  fileInputRef.value?.click()
}

async function handleFileSelection(event: Event) {
  const input = event.target as HTMLInputElement
  const files = input.files ? Array.from(input.files) : []
  if (files.length > 0) {
    await addDraftFiles(files)
  }
  input.value = ''
}

function handleStartFreshSession() {
  resetDraft(true)
  showContextPanel.value = false
}

onMounted(() => {
  if (!publisherDisplayName.value && currentUser.value?.displayName) {
    publisherDisplayName.value = currentUser.value.displayName
  }
  void restoreActiveSession()
})

watch(
  () => currentUser.value?.userId,
  (userId, previousUserId) => {
    if (!userId || userId === previousUserId) {
      return
    }
    if (!publisherDisplayName.value && currentUser.value?.displayName) {
      publisherDisplayName.value = currentUser.value.displayName
    }
    void restoreActiveSession()
  },
)
</script>

<template>
  <div class="space-y-6">
    <section>
      <p class="console-kicker">{{ t('console.navigation.importAgent') }}</p>
      <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
        {{ t('console.importAgent.title') }}
      </h2>
      <p class="mt-3 text-sm leading-6 text-muted-foreground">
        {{ t('console.importAgent.description') }}
      </p>
    </section>

    <StateBlock
      v-if="restoring"
      tone="loading"
      :title="t('console.importAgent.restoreLoadingTitle')"
      :description="t('console.importAgent.restoreLoadingDescription')"
    />

    <div class="grid gap-5 xl:grid-cols-[minmax(0,1.38fr)_minmax(300px,0.78fr)]">
      <div>
        <Card class="overflow-hidden">
          <CardHeader>
            <div class="flex items-start justify-between gap-3">
              <div>
                <CardTitle>{{ t('console.importAgent.conversationTitle') }}</CardTitle>
                <CardDescription>{{ t('console.importAgent.conversationDescription') }}</CardDescription>
              </div>
              <Button variant="outline" size="sm" @click="handleStartFreshSession">
                <RotateCcw class="mr-2 size-4" />
                {{ t('console.importAgent.startFresh') }}
              </Button>
            </div>
          </CardHeader>
          <CardContent class="space-y-5">
            <div v-if="hasActiveSession" class="flex flex-wrap items-center gap-2">
              <DisplayTag
                :tone="sessionStatusTone(activeSession!.status)"
                :label="sessionStatusLabel(activeSession!.status)"
              />
              <DisplayTag tone="neutral" :label="`#${activeSession!.sessionId}`" />
              <DisplayTag
                v-if="currentPlan"
                tone="info"
                :label="t('console.importAgent.planVersionLabel', { version: currentPlan.version })"
              />
            </div>

            <StateBlock
              v-if="!activeSession"
              tone="empty"
              :title="t('console.importAgent.emptyTitle')"
              :description="t('console.importAgent.emptyDescription')"
            />

            <div class="space-y-4 rounded-[20px] border border-[rgb(34_34_34_/_0.06)] bg-[rgb(255_255_255_/_0.88)] p-4 shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.04)_0px_2px_6px,rgba(0,0,0,0.08)_0px_4px_10px]">
              <div v-if="!activeSession" class="flex justify-start">
                <div class="max-w-[88%] rounded-[20px] bg-secondary/60 px-4 py-3 text-sm leading-6 text-foreground">
                  {{ t('console.importAgent.conversationStarterHint') }}
                </div>
              </div>

              <div v-for="turn in activeSession?.turns ?? []" :key="turn.turnId" class="flex" :class="turn.actorType === 'USER' ? 'justify-end' : 'justify-start'">
                <div
                  class="max-w-[88%] rounded-[20px] px-4 py-3"
                  :class="turn.actorType === 'USER'
                    ? 'bg-[rgb(255_56_92)] text-white shadow-[rgba(255,56,92,0.18)_0px_6px_18px]'
                    : 'border border-[rgb(34_34_34_/_0.06)] bg-secondary/50 text-foreground'"
                >
                  <div class="mb-2 flex flex-wrap items-center gap-2 text-xs" :class="turn.actorType === 'USER' ? 'text-white/80' : 'text-muted-foreground'">
                    <DisplayTag
                      :tone="turn.actorType === 'USER' ? 'neutral' : 'info'"
                      :label="t(`console.importAgent.actor.${turn.actorType}`)"
                    />
                    <span>{{ formatDateTime(turn.createdAt) }}</span>
                    <span v-if="turn.planVersion">
                      {{ t('console.importAgent.turnPlanVersionLabel', { version: turn.planVersion }) }}
                    </span>
                  </div>
                  <p class="whitespace-pre-wrap text-sm leading-6">{{ turn.message }}</p>
                </div>
              </div>

              <div v-if="currentPlan" class="flex justify-start">
                <div class="w-full max-w-[92%] rounded-[22px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/45 px-4 py-4 text-foreground">
                  <div class="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <p class="text-sm font-semibold text-foreground">
                        {{ t('console.importAgent.planTitle') }}
                      </p>
                      <p class="mt-1 text-xs text-muted-foreground">
                        {{ t('console.importAgent.planDescription') }}
                      </p>
                    </div>
                    <div class="flex flex-wrap items-center gap-2">
                      <DisplayTag
                        :tone="currentPlan.executable ? 'success' : 'warning'"
                        :label="
                          currentPlan.executable
                            ? t('console.importAgent.planExecutable')
                            : t('console.importAgent.planNeedsClarification')
                        "
                      />
                      <DisplayTag
                        :tone="isPlanConfirmed ? 'success' : 'neutral'"
                        :label="
                          isPlanConfirmed
                            ? t('console.importAgent.planConfirmed')
                            : t('console.importAgent.planUnconfirmed')
                        "
                      />
                      <DisplayTag
                        tone="info"
                        :label="t('console.importAgent.planVersionLabel', { version: currentPlan.version })"
                      />
                    </div>
                  </div>

                  <div class="mt-4 rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-white/80 p-4">
                    <p class="text-sm leading-6 text-foreground">{{ currentPlan.summary }}</p>
                  </div>

                  <div v-if="currentPlan.clarificationQuestions.length" class="mt-4 space-y-2">
                    <p class="text-sm font-semibold text-foreground">
                      {{ t('console.importAgent.clarificationTitle') }}
                    </p>
                    <ul class="space-y-2 text-sm text-muted-foreground">
                      <li
                        v-for="question in currentPlan.clarificationQuestions"
                        :key="question"
                        class="rounded-[12px] border border-dashed border-border/80 bg-white/65 px-3 py-2"
                      >
                        {{ question }}
                      </li>
                    </ul>
                  </div>

                  <div v-if="currentPlan.categoryPlans.length" class="mt-4 space-y-2">
                    <p class="text-sm font-semibold text-foreground">
                      {{ t('console.importAgent.categoryPlansTitle') }}
                    </p>
                    <div class="space-y-2">
                      <div
                        v-for="categoryPlan in currentPlan.categoryPlans"
                        :key="`${categoryPlan.categoryCode}:${categoryPlan.action}`"
                        class="rounded-[12px] border border-[rgb(34_34_34_/_0.06)] bg-white/70 px-3 py-3"
                      >
                        <div class="flex flex-wrap items-center gap-2">
                          <span class="text-sm font-medium text-foreground">{{ categoryPlan.categoryName }}</span>
                          <DisplayTag tone="neutral" :label="categoryPlan.categoryCode" />
                          <DisplayTag
                            tone="info"
                            :label="t(`console.importAgent.categoryAction.${categoryPlan.action}`)"
                          />
                        </div>
                      </div>
                    </div>
                  </div>

                  <div v-if="currentPlan.assetPlans.length" class="mt-4 space-y-2">
                    <p class="text-sm font-semibold text-foreground">
                      {{ t('console.importAgent.assetPlansTitle') }}
                    </p>
                    <div class="space-y-3">
                      <div
                        v-for="assetPlan in currentPlan.assetPlans"
                        :key="assetPlan.apiCode"
                        class="rounded-[12px] border border-[rgb(34_34_34_/_0.06)] bg-white/70 px-3 py-3"
                      >
                        <div class="flex flex-wrap items-center gap-2">
                          <span class="text-sm font-medium text-foreground">{{ assetPlan.assetName }}</span>
                          <DisplayTag tone="neutral" :label="assetPlan.apiCode" />
                          <DisplayTag tone="info" :label="assetPlan.assetType" />
                          <DisplayTag
                            :tone="assetPlan.publishAfterImport ? 'success' : 'neutral'"
                            :label="
                              assetPlan.publishAfterImport
                                ? t('console.importAgent.publishAfterImport')
                                : t('console.importAgent.keepAsDraft')
                            "
                          />
                        </div>
                        <dl class="mt-3 grid gap-2 text-xs text-muted-foreground sm:grid-cols-2">
                          <div v-if="assetPlan.categoryCode">
                            <dt>{{ t('console.importAgent.assetMetaCategory') }}</dt>
                            <dd class="mt-1 text-foreground">{{ assetPlan.categoryCode }}</dd>
                          </div>
                          <div v-if="assetPlan.requestMethod">
                            <dt>{{ t('console.importAgent.assetMetaMethod') }}</dt>
                            <dd class="mt-1 text-foreground">{{ assetPlan.requestMethod }}</dd>
                          </div>
                          <div v-if="assetPlan.upstreamUrl" class="sm:col-span-2">
                            <dt>{{ t('console.importAgent.assetMetaUpstreamUrl') }}</dt>
                            <dd class="mt-1 break-all text-foreground">{{ assetPlan.upstreamUrl }}</dd>
                          </div>
                        </dl>
                      </div>
                    </div>
                  </div>

                  <p v-if="sessionError" class="mt-4 text-sm text-destructive">{{ sessionError }}</p>

                  <div class="mt-4 flex flex-wrap gap-2">
                    <Button variant="outline" :disabled="!canConfirmPlan" @click="confirmPlan">
                      <Loader2 v-if="confirming" class="mr-2 size-4 animate-spin" />
                      <CheckCircle2 v-else class="mr-2 size-4" />
                      {{ confirming ? t('console.importAgent.confirming') : t('console.importAgent.confirmPlan') }}
                    </Button>
                    <Button :disabled="!canStartRun" @click="startRun">
                      <Loader2 v-if="startingRun" class="mr-2 size-4 animate-spin" />
                      <Play v-else class="mr-2 size-4" />
                      {{ startingRun ? t('console.importAgent.startingRun') : t('console.importAgent.startRun') }}
                    </Button>
                  </div>
                </div>
              </div>

              <div v-if="activeRun || runError" class="flex justify-start">
                <div class="w-full max-w-[92%] rounded-[22px] border border-[rgb(34_34_34_/_0.06)] bg-white px-4 py-4 shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.04)_0px_2px_6px]">
                  <div class="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <p class="text-sm font-semibold text-foreground">
                        {{ t('console.importAgent.runTitle') }}
                      </p>
                      <p class="mt-1 text-xs text-muted-foreground">
                        {{ t('console.importAgent.runDescription') }}
                      </p>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      :disabled="!activeRun || refreshingRun"
                      @click="refreshRun()"
                    >
                      <RefreshCw class="mr-2 size-4" :class="refreshingRun ? 'animate-spin' : ''" />
                      {{ t('console.importAgent.refreshRun') }}
                    </Button>
                  </div>

                  <template v-if="activeRun">
                    <div class="mt-4 flex flex-wrap items-center gap-2">
                      <DisplayTag :tone="runStatusTone(activeRun.status)" :label="runStatusLabel(activeRun.status)" />
                      <DisplayTag tone="neutral" :label="`#${activeRun.runId}`" />
                      <DisplayTag
                        tone="info"
                        :label="t('console.importAgent.planVersionLabel', { version: activeRun.planVersion })"
                      />
                    </div>

                    <div v-if="activeRun.summary" class="mt-4 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/30 p-4 text-sm text-foreground">
                      {{ activeRun.summary }}
                    </div>

                    <p v-if="activeRun.failureReason" class="mt-4 text-sm text-destructive">
                      {{ activeRun.failureReason }}
                    </p>
                    <p v-if="runError" class="mt-3 text-sm text-destructive">{{ runError }}</p>

                    <div v-if="activeRun.affectedApiCodes.length" class="mt-4 space-y-2">
                      <p class="text-sm font-semibold text-foreground">
                        {{ t('console.importAgent.affectedApisTitle') }}
                      </p>
                      <div class="flex flex-wrap gap-2">
                        <DisplayTag
                          v-for="apiCode in activeRun.affectedApiCodes"
                          :key="apiCode"
                          tone="neutral"
                          :label="apiCode"
                        />
                      </div>
                    </div>

                    <div class="mt-4 space-y-3">
                      <div
                        v-for="stepResult in activeRun.stepResults"
                        :key="`${stepResult.stepType}:${stepResult.targetRef}`"
                        class="rounded-[12px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/20 px-3 py-3"
                      >
                        <div class="flex flex-wrap items-center gap-2">
                          <DisplayTag tone="info" :label="stepResult.stepType" />
                          <DisplayTag :tone="stepStatusTone(stepResult.status)" :label="stepResult.status" />
                          <span class="text-sm text-foreground">{{ stepResult.targetRef }}</span>
                        </div>
                        <p v-if="stepResult.message" class="mt-2 text-sm text-muted-foreground">
                          {{ stepResult.message }}
                        </p>
                      </div>
                    </div>

                    <div class="mt-4 grid gap-3 text-sm sm:grid-cols-2">
                      <div class="rounded-[12px] border border-[rgb(34_34_34_/_0.06)] px-3 py-3">
                        <p class="text-xs text-muted-foreground">{{ t('console.importAgent.createdAt') }}</p>
                        <p class="mt-1 text-foreground">{{ formatDateTime(activeRun.createdAt) }}</p>
                      </div>
                      <div class="rounded-[12px] border border-[rgb(34_34_34_/_0.06)] px-3 py-3">
                        <p class="text-xs text-muted-foreground">{{ t('console.importAgent.updatedAt') }}</p>
                        <p class="mt-1 text-foreground">{{ formatDateTime(activeRun.updatedAt) }}</p>
                      </div>
                    </div>
                  </template>

                  <p v-else-if="runError" class="mt-4 text-sm text-destructive">{{ runError }}</p>
                </div>
              </div>
            </div>

            <div class="space-y-4 border-t border-border/70 pt-4">
              <div v-if="draftAttachments.length" class="space-y-2">
                <p class="text-sm font-semibold text-foreground">
                  {{ t('console.importAgent.filesAttachedTitle', { count: draftAttachments.length }) }}
                </p>
                <div class="flex flex-wrap gap-3">
                  <div
                    v-for="attachment in draftAttachments"
                    :key="attachment.id"
                    class="min-w-[220px] max-w-[320px] rounded-[16px] border border-[rgb(34_34_34_/_0.08)] bg-secondary/40 px-3 py-3"
                  >
                    <div class="flex items-start justify-between gap-2">
                      <div class="min-w-0">
                        <p class="truncate text-sm font-medium text-foreground">{{ attachment.fileName }}</p>
                        <p class="mt-1 text-xs text-muted-foreground">
                          {{ formatFileSize(attachment.size) }}
                        </p>
                      </div>
                      <button
                        type="button"
                        class="rounded-full p-1 text-muted-foreground transition hover:bg-white hover:text-foreground"
                        @click="removeDraftAttachment(attachment.id)"
                      >
                        <X class="size-4" />
                      </button>
                    </div>
                    <p class="mt-2 line-clamp-4 whitespace-pre-wrap text-xs leading-5 text-muted-foreground">
                      {{ attachment.excerpt }}
                    </p>
                    <p v-if="attachment.truncated" class="mt-2 text-[11px] font-medium text-foreground">
                      {{ t('console.importAgent.fileTruncated') }}
                    </p>
                  </div>
                </div>
              </div>

              <p class="text-xs text-muted-foreground">
                {{ t('console.importAgent.attachmentLocalOnly') }}
              </p>
              <p v-if="attachmentError" class="text-sm text-destructive">{{ attachmentError }}</p>

              <textarea
                v-model="messageDraft"
                :rows="hasActiveSession ? 4 : 5"
                class="w-full resize-y rounded-[16px] border border-[rgb(34_34_34_/_0.12)] bg-white p-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                :placeholder="composerPlaceholder"
              />

              <div
                v-if="!hasActiveSession && showContextPanel"
                class="grid gap-3 rounded-[18px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/30 p-4"
              >
                <div class="space-y-2">
                  <FieldLabel :label="t('console.importAgent.fieldDocumentSource')" optional />
                  <Input
                    v-model="documentSource"
                    :placeholder="t('console.importAgent.documentSourcePlaceholder')"
                  />
                </div>

                <div class="space-y-2">
                  <FieldLabel :label="t('console.importAgent.fieldDocumentSummary')" optional />
                  <textarea
                    v-model="documentSummary"
                    rows="4"
                    class="w-full resize-y rounded-[12px] border border-[rgb(34_34_34_/_0.12)] bg-white p-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                    :placeholder="t('console.importAgent.documentSummaryPlaceholder')"
                  />
                </div>

                <div class="space-y-2">
                  <FieldLabel :label="t('console.importAgent.fieldPublisherDisplayName')" optional />
                  <Input
                    v-model="publisherDisplayName"
                    :placeholder="t('console.importAgent.publisherDisplayNamePlaceholder')"
                  />
                </div>

                <p class="text-xs leading-5 text-muted-foreground">
                  {{ t('console.importAgent.contextDescription') }}
                </p>
              </div>

              <div class="flex flex-wrap items-center justify-between gap-3">
                <div class="flex flex-wrap items-center gap-2">
                  <input
                    ref="fileInputRef"
                    type="file"
                    class="hidden"
                    multiple
                    :accept="attachedFileAccept"
                    @change="handleFileSelection"
                  >
                  <Button
                    variant="outline"
                    size="sm"
                    :disabled="attachingFiles || sendButtonBusy"
                    @click="openFilePicker"
                  >
                    <Loader2 v-if="attachingFiles" class="mr-2 size-4 animate-spin" />
                    <Paperclip v-else class="mr-2 size-4" />
                    {{ t('console.importAgent.addFiles') }}
                  </Button>
                  <Button
                    v-if="!hasActiveSession"
                    variant="outline"
                    size="sm"
                    @click="showContextPanel = !showContextPanel"
                  >
                    <ChevronUp v-if="showContextPanel" class="mr-2 size-4" />
                    <ChevronDown v-else class="mr-2 size-4" />
                    {{
                      showContextPanel
                        ? t('console.importAgent.hideAdvancedContext')
                        : t('console.importAgent.showAdvancedContext')
                    }}
                  </Button>
                </div>

                <div class="flex items-center gap-3">
                  <p v-if="composerError" class="text-sm text-destructive">{{ composerError }}</p>
                  <Button :disabled="!canSendMessage" @click="sendMessage">
                    <Loader2 v-if="sendButtonBusy" class="mr-2 size-4 animate-spin" />
                    <Send v-else class="mr-2 size-4" />
                    {{ sendButtonLabel }}
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div class="space-y-5">
        <Card>
          <CardHeader>
            <div class="flex items-start justify-between gap-3">
              <div>
                <CardTitle>{{ t('console.importAgent.sessionTitle') }}</CardTitle>
                <CardDescription>{{ t('console.importAgent.sessionDescription') }}</CardDescription>
              </div>
              <Button
                v-if="activeSession"
                size="sm"
                variant="outline"
                :disabled="refreshingSession"
                @click="refreshSession"
              >
                <RefreshCw class="mr-2 size-4" :class="refreshingSession ? 'animate-spin' : ''" />
                {{ t('console.importAgent.refreshSession') }}
              </Button>
            </div>
          </CardHeader>
          <CardContent class="space-y-4">
            <StateBlock
              v-if="!activeSession"
              tone="empty"
              :title="t('console.importAgent.sessionEmptyTitle')"
              :description="t('console.importAgent.sessionEmptyDescription')"
            />
            <div v-else class="space-y-3">
              <div class="flex flex-wrap items-center gap-2">
                <DisplayTag
                  :tone="sessionStatusTone(activeSession!.status)"
                  :label="sessionStatusLabel(activeSession!.status)"
                />
                <DisplayTag tone="neutral" :label="`#${activeSession!.sessionId}`" />
                <DisplayTag
                  v-if="currentPlan"
                  tone="info"
                  :label="t('console.importAgent.planVersionLabel', { version: currentPlan.version })"
                />
                <DisplayTag
                  v-if="activeRun"
                  :tone="runStatusTone(activeRun.status)"
                  :label="runStatusLabel(activeRun.status)"
                />
              </div>
              <dl class="grid gap-3 text-sm sm:grid-cols-2">
                <div>
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.importAgent.fieldImportIntent') }}
                  </dt>
                  <dd class="mt-1 text-foreground">{{ activeSession!.importIntent }}</dd>
                </div>
                <div>
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.importAgent.fieldPublisherDisplayName') }}
                  </dt>
                  <dd class="mt-1 text-foreground">{{ activeSession!.publisherDisplayName }}</dd>
                </div>
                <div v-if="activeSession!.documentSource">
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.importAgent.fieldDocumentSource') }}
                  </dt>
                  <dd class="mt-1 break-words text-foreground">{{ activeSession!.documentSource }}</dd>
                </div>
                <div v-if="activeSession!.documentSummary" class="sm:col-span-2">
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.importAgent.fieldDocumentSummary') }}
                  </dt>
                  <dd class="mt-1 whitespace-pre-wrap text-foreground">{{ activeSession!.documentSummary }}</dd>
                </div>
                <div>
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.importAgent.createdAt') }}
                  </dt>
                  <dd class="mt-1 text-foreground">{{ formatDateTime(activeSession!.createdAt) }}</dd>
                </div>
                <div>
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.importAgent.updatedAt') }}
                  </dt>
                  <dd class="mt-1 text-foreground">{{ formatDateTime(activeSession!.updatedAt) }}</dd>
                </div>
                <div>
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.importAgent.turnsTitle') }}
                  </dt>
                  <dd class="mt-1 text-foreground">{{ activeSession!.turns.length }}</dd>
                </div>
                <div v-if="activeRun">
                  <dt class="text-xs text-muted-foreground">
                    {{ t('console.importAgent.runTitle') }}
                  </dt>
                  <dd class="mt-1 text-foreground">#{{ activeRun.runId }}</dd>
                </div>
              </dl>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  </div>
</template>