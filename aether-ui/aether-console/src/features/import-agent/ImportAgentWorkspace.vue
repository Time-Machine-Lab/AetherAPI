<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import {
  Bot,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Clock3,
  CircleAlert,
  ListChecks,
  Loader2,
  Paperclip,
  Play,
  RefreshCw,
  RotateCcw,
  Send,
  ShieldCheck,
  Sparkles,
  UserRound,
  X,
} from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/useAuthStore'
import { useImportAgentWorkspace } from '@/composables/useImportAgentWorkspace'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import CodeBlock from '@/components/console/CodeBlock.vue'
import DisplayTag from '@/components/console/DisplayTag.vue'
import FieldLabel from '@/components/console/FieldLabel.vue'
import JsonSchemaViewer from '@/components/console/JsonSchemaViewer.vue'
import StateBlock from '@/components/console/StateBlock.vue'
import type {
  ImportAgentClarificationItem,
  ImportAgentStreamPhase,
  ImportAssetPlan,
  ImportAsyncTaskConfig,
} from '@/api/import-agent/import-agent.types'

const { t } = useI18n()
const authStore = useAuthStore()
const { currentUser } = storeToRefs(authStore)

const workspace = useImportAgentWorkspace({ t })

const {
  documentSource,
  documentSummary,
  publisherDisplayName,
  clarificationDrafts,
  messageDraft,
  draftAttachments,
  activeSession,
  activeRun,
  pendingTurn,
  streamingReply,
  streamingPhase,
  streamingStatusMessage,
  streamingThoughts,
  currentPlan,
  currentClarificationItems,
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
  adoptClarificationDefault,
  resetDraft,
} = workspace

const fileInputRef = ref<HTMLInputElement | null>(null)
const conversationBottomRef = ref<HTMLElement | null>(null)
const showContextPanel = ref(false)
const planExpanded = ref(true)
const shouldExpandPlanAfterStream = ref(false)
const streamStartPlanSnapshotKey = ref('')

const isPlanConfirmed = computed(
  () => activeSession.value?.confirmedPlanVersion === currentPlan.value?.version,
)
const composerError = computed(() =>
  hasActiveSession.value ? turnError.value : sessionError.value,
)
const sendButtonBusy = computed(() => (hasActiveSession.value ? appending.value : creating.value))
const conversationTurns = computed(() => activeSession.value?.turns ?? [])
const planStreamActive = computed(() => Boolean(pendingTurn.value) || Boolean(streamingPhase.value))
const planSnapshotKey = computed(() => {
  const session = activeSession.value
  const plan = currentPlan.value
  return [session?.sessionId ?? '', session?.updatedAt ?? '', plan?.version ?? ''].join(':')
})
const showStreamingReply = computed(
  () =>
    Boolean(streamingPhase.value) ||
    streamingReply.value.trim().length > 0 ||
    streamingThoughts.value.length > 0,
)
const composerPlaceholder = computed(() => {
  if (hasActiveSession.value) {
    return t('console.importAgent.turnMessagePlaceholder')
  }
  return t('console.importAgent.importIntentPlaceholder')
})
const attachedFileAccept =
  '.txt,.md,.markdown,.json,.yaml,.yml,.csv,.http,.xml,.log,.js,.ts,.mjs,.cjs,text/*,application/json,application/xml,text/xml,text/csv'

const clarificationGroups = computed(() => {
  const groups = new Map<
    string,
    { key: string; title: string; items: ImportAgentClarificationItem[] }
  >()
  currentClarificationItems.value.forEach((item) => {
    const group = resolveClarificationGroup(item)
    const existing = groups.get(group.key)
    if (existing) {
      existing.items.push(item)
      return
    }
    groups.set(group.key, { ...group, items: [item] })
  })
  return Array.from(groups.values())
})

function streamingPhaseLabel(phase: ImportAgentStreamPhase | null) {
  if (!phase) {
    return ''
  }
  return t(`console.importAgent.streamPhase.${phase}`)
}

function thinkingStageLabel(stage: string) {
  const key = `console.importAgent.thinkingStage.${stage}`
  const label = t(key)
  return label === key ? stage : label
}

function stepStatusTone(status: 'SUCCEEDED' | 'FAILED') {
  return status === 'SUCCEEDED' ? 'success' : 'danger'
}

function hasText(value?: string | null) {
  return Boolean(value?.trim())
}

function resolveClarificationGroup(item: ImportAgentClarificationItem) {
  const assetMatch = item.targetPath.match(/^\/assetPlans\/(\d+)/)
  if (assetMatch) {
    const index = Number(assetMatch[1])
    const assetPlan = currentPlan.value?.assetPlans[index]
    return {
      key: `asset-${index}`,
      title:
        assetPlan?.assetName ||
        assetPlan?.apiCode ||
        t('console.importAgent.clarificationAssetGroup', { index: index + 1 }),
    }
  }
  const categoryMatch = item.targetPath.match(/^\/categoryPlans\/(\d+)/)
  if (categoryMatch) {
    const index = Number(categoryMatch[1])
    const categoryPlan = currentPlan.value?.categoryPlans[index]
    return {
      key: `category-${index}`,
      title:
        categoryPlan?.categoryName ||
        categoryPlan?.categoryCode ||
        t('console.importAgent.clarificationCategoryGroup', { index: index + 1 }),
    }
  }
  return {
    key: 'plan',
    title: t('console.importAgent.clarificationPlanGroup'),
  }
}

function clarificationValue(item: ImportAgentClarificationItem) {
  return clarificationDrafts.value[item.id] ?? item.currentValue ?? ''
}

function clarificationDefaultLabel(item: ImportAgentClarificationItem) {
  return item.defaultLabel || item.defaultValue || ''
}

function clarificationDefaultMeta(item: ImportAgentClarificationItem) {
  const parts: string[] = []
  if (item.defaultSource) {
    parts.push(t(`console.importAgent.clarificationDefaultSource.${item.defaultSource}`))
  }
  if (item.defaultConfidence) {
    parts.push(t(`console.importAgent.clarificationDefaultConfidence.${item.defaultConfidence}`))
  }
  return parts.join(' · ')
}

function setClarificationDraft(item: ImportAgentClarificationItem, value: string) {
  clarificationDrafts.value = {
    ...clarificationDrafts.value,
    [item.id]: value,
  }
}

function shouldShowSecurityConfig(assetPlan: ImportAssetPlan) {
  return hasText(assetPlan.authScheme) || hasText(assetPlan.authConfig)
}

function needsSecurityConfig(assetPlan: ImportAssetPlan) {
  return (
    hasText(assetPlan.authScheme) &&
    assetPlan.authScheme !== 'NONE' &&
    !hasText(assetPlan.authConfig)
  )
}

function shouldShowAsyncAuthConfig(config: ImportAsyncTaskConfig) {
  return (
    config.authMode === 'OVERRIDE' ||
    (hasText(config.authScheme) && config.authScheme !== 'NONE') ||
    hasText(config.authConfig)
  )
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
  planExpanded.value = true
  shouldExpandPlanAfterStream.value = false
  streamStartPlanSnapshotKey.value = ''
}

function togglePlanExpanded() {
  planExpanded.value = !planExpanded.value
}

async function scrollConversationToBottom() {
  await nextTick()
  conversationBottomRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'end',
  })
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

watch(planStreamActive, (active) => {
  if (active && currentPlan.value) {
    planExpanded.value = false
    shouldExpandPlanAfterStream.value = true
    streamStartPlanSnapshotKey.value = planSnapshotKey.value
  }
})

watch([planStreamActive, planSnapshotKey], ([active, snapshotKey]) => {
  if (
    active ||
    !shouldExpandPlanAfterStream.value ||
    !currentPlan.value ||
    snapshotKey === streamStartPlanSnapshotKey.value
  ) {
    return
  }
  planExpanded.value = true
  shouldExpandPlanAfterStream.value = false
  streamStartPlanSnapshotKey.value = ''
})

watch(
  [
    () => pendingTurn.value?.message,
    () => streamingPhase.value,
    () => streamingStatusMessage.value,
    () => streamingReply.value,
    () => streamingThoughts.value.length,
    () => conversationTurns.value.length,
    () => currentPlan.value?.version,
  ],
  () => {
    if (!pendingTurn.value && !showStreamingReply.value) {
      return
    }
    void scrollConversationToBottom()
  },
)
</script>

<template>
  <div class="w-full px-4 pb-[13rem] pt-3 sm:px-6 sm:pb-[14rem] lg:px-8">
    <section class="flex w-full justify-end">
      <div class="flex flex-wrap gap-2">
        <Button
          v-if="activeSession"
          variant="outline"
          size="sm"
          class="rounded-full bg-white/92 px-5 shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.05)_0px_8px_18px]"
          :disabled="refreshingSession"
          @click="refreshSession"
        >
          <RefreshCw class="mr-2 size-4" :class="refreshingSession ? 'animate-spin' : ''" />
          {{ t('console.importAgent.refreshSession') }}
        </Button>
        <Button
          variant="outline"
          size="sm"
          class="rounded-full bg-white/92 px-5 shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.05)_0px_8px_18px]"
          @click="handleStartFreshSession"
        >
          <RotateCcw class="mr-2 size-4" />
          {{ t('console.importAgent.startFresh') }}
        </Button>
      </div>
    </section>

    <StateBlock
      v-if="restoring"
      tone="loading"
      :title="t('console.importAgent.restoreLoadingTitle')"
      :description="t('console.importAgent.restoreLoadingDescription')"
    />

    <section class="relative flex min-h-[calc(100vh-12rem)] w-full flex-col overflow-hidden">
      <div class="pointer-events-none absolute inset-x-0 top-10 h-[28rem] rounded-[48px]" />

      <div class="relative flex-1">
        <div
          class="mx-auto flex h-full w-full max-w-[88rem] flex-col gap-6 px-0 py-4 sm:px-2 sm:py-6 lg:px-4"
        >
          <div
            v-if="!activeSession && !pendingTurn"
            class="flex flex-1 items-center justify-center py-12"
          ></div>

          <div
            v-for="turn in conversationTurns"
            :key="turn.turnId"
            class="flex items-start gap-3"
            :class="turn.actorType === 'USER' ? 'justify-end' : 'justify-start'"
          >
            <div
              v-if="turn.actorType === 'AGENT'"
              class="mt-1 flex size-10 shrink-0 items-center justify-center rounded-full border border-[rgb(34_34_34_/_0.08)] bg-white text-[rgb(48_92_57)] shadow-[rgba(0,0,0,0.04)_0px_6px_18px]"
              :title="t('console.importAgent.actor.AGENT')"
              :aria-label="t('console.importAgent.actor.AGENT')"
            >
              <Bot class="size-5" />
            </div>
            <div
              class="rounded-[16px] px-3 py-2"
              :class="
                turn.actorType === 'USER'
                  ? 'max-w-[38rem] border border-[rgb(181_213_248)] bg-[rgba(244,248,255,0.98)] text-[rgb(21_46_83)] shadow-[rgba(86,135,194,0.12)_0px_16px_30px]'
                  : 'w-full max-w-[56rem] border border-[rgb(34_34_34_/_0.06)] bg-white/94 text-foreground shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.04)_0px_2px_6px,rgba(0,0,0,0.08)_0px_14px_30px]'
              "
            >
              <p class="whitespace-pre-wrap text-sm leading-7">{{ turn.message }}</p>
            </div>
            <div
              v-if="turn.actorType === 'USER'"
              class="mt-1 flex size-10 shrink-0 items-center justify-center rounded-full border border-[rgb(181_213_248)] bg-[rgb(244,248,255)] text-[rgb(62_96_139)] shadow-[rgba(86,135,194,0.12)_0px_6px_18px]"
              :title="t('console.importAgent.actor.USER')"
              :aria-label="t('console.importAgent.actor.USER')"
            >
              <UserRound class="size-5" />
            </div>
          </div>

          <div v-if="pendingTurn" class="flex items-start justify-end gap-3">
            <div
              class="max-w-[38rem] rounded-[16px] border border-[rgb(181_213_248)] px-3 py-2 text-[rgb(21_46_83)] shadow-[rgba(86,135,194,0.12)_0px_16px_30px]"
            >
              <p class="whitespace-pre-wrap text-sm leading-7">{{ pendingTurn.message }}</p>
            </div>
            <div
              class="mt-1 flex size-10 shrink-0 items-center justify-center rounded-full border border-[rgb(181_213_248)] bg-[rgb(244,248,255)] text-[rgb(62_96_139)] shadow-[rgba(86,135,194,0.12)_0px_6px_18px]"
              :title="t('console.importAgent.actor.USER')"
              :aria-label="t('console.importAgent.actor.USER')"
            >
              <UserRound class="size-5" />
            </div>
          </div>

          <div v-if="showStreamingReply" class="flex items-start justify-start gap-3">
            <div
              class="mt-1 flex size-10 shrink-0 items-center justify-center rounded-full border border-[rgb(34_34_34_/_0.08)] bg-white text-[rgb(48_92_57)] shadow-[rgba(0,0,0,0.04)_0px_6px_18px]"
              :title="t('console.importAgent.actor.AGENT')"
              :aria-label="t('console.importAgent.actor.AGENT')"
            >
              <Bot class="size-5" />
            </div>
            <div
              class="w-full max-w-[56rem] rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-white/96 px-3 py-2 shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.04)_0px_2px_6px,rgba(0,0,0,0.08)_0px_14px_30px]"
            >
              <div class="space-y-3">
                <div class="flex items-center gap-2 text-sm font-medium text-foreground">
                  <Loader2
                    v-if="sendButtonBusy"
                    class="size-4 animate-spin text-[rgb(62_96_139)]"
                  />
                  <span>{{ t('console.importAgent.streamingReplyTitle') }}</span>
                </div>
                <p v-if="streamingPhase" class="text-xs text-muted-foreground">
                  {{ streamingPhaseLabel(streamingPhase) }}
                </p>
                <p v-if="streamingStatusMessage" class="text-sm text-muted-foreground">
                  {{ streamingStatusMessage }}
                </p>
                <div
                  v-if="streamingThoughts.length > 0"
                  class="space-y-2 rounded-[12px] border border-[rgb(216_226_219)] bg-[rgb(250_252_250)] px-3 py-3"
                >
                  <div class="flex items-center gap-2 text-xs font-medium text-[rgb(48_92_57)]">
                    <Sparkles class="size-3.5" />
                    <span>{{ t('console.importAgent.thinkingTitle') }}</span>
                  </div>
                  <div class="space-y-2">
                    <div
                      v-for="thought in streamingThoughts"
                      :key="`${thought.sequence ?? 0}-${thought.stage}-${thought.title}`"
                      class="border-l border-[rgb(170_199_176)] pl-3"
                    >
                      <div class="flex flex-wrap items-center gap-2">
                        <span class="text-xs font-medium text-foreground">{{ thought.title }}</span>
                        <span class="text-[11px] text-muted-foreground">
                          {{ thinkingStageLabel(thought.stage) }}
                        </span>
                      </div>
                      <p class="mt-1 text-xs leading-5 text-muted-foreground">
                        {{ thought.summary }}
                      </p>
                      <p
                        v-if="thought.detail"
                        class="mt-1 text-[11px] leading-5 text-muted-foreground"
                      >
                        {{ thought.detail }}
                      </p>
                    </div>
                  </div>
                </div>
                <p
                  v-if="streamingReply"
                  class="whitespace-pre-wrap text-sm leading-7 text-foreground"
                >
                  {{ streamingReply }}
                </p>
              </div>
            </div>
          </div>

          <div v-if="currentPlan" class="flex justify-start">
            <div
              class="w-full max-w-[56rem] rounded-[16px] border border-[rgb(34_34_34_/_0.06)] px-6 pt-6 shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.04)_0px_2px_6px,rgba(0,0,0,0.08)_0px_16px_32px]"
              :class="planExpanded ? 'pb-6' : 'pb-0'"
            >
              <div
                role="button"
                tabindex="0"
                class="-mx-6 -mt-6 flex min-h-20 cursor-pointer select-none flex-col gap-4 bg-white/92 px-6 py-6 transition hover:bg-secondary/20 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-ring/20 lg:flex-row lg:items-center lg:justify-between"
                :class="
                  planExpanded
                    ? 'rounded-t-[16px] border-b border-[rgb(34_34_34_/_0.08)]'
                    : 'rounded-[16px]'
                "
                :aria-expanded="planExpanded"
                :aria-label="
                  planExpanded
                    ? t('console.importAgent.collapsePlan')
                    : t('console.importAgent.expandPlan')
                "
                @click="togglePlanExpanded"
                @keydown.enter.prevent="togglePlanExpanded"
                @keydown.space.prevent="togglePlanExpanded"
              >
                <div class="space-y-2">
                  <div class="flex items-center gap-2 text-sm font-semibold text-foreground">
                    <ListChecks class="size-4 text-[rgb(48_92_57)]" />
                    <span>{{ t('console.importAgent.planTitle') }}</span>
                  </div>
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
                    :label="
                      t('console.importAgent.planVersionLabel', { version: currentPlan.version })
                    "
                  />
                  <ChevronUp v-if="planExpanded" class="size-4 text-muted-foreground" />
                  <ChevronDown v-else class="size-4 text-muted-foreground" />
                </div>
              </div>

              <div v-if="planExpanded">
                <div
                  class="mt-5 rounded-[16px] border border-[rgb(181_213_248_/_0.6)] bg-[rgb(244,248,255)] p-4"
                >
                  <p
                    class="text-[11px] font-semibold uppercase tracking-[0.18em] text-[rgb(62_96_139)]"
                  >
                    {{ t('console.importAgent.planSummaryTitle') }}
                  </p>
                  <p class="mt-3 whitespace-pre-wrap text-sm leading-7 text-[rgb(21_46_83)]">
                    {{ currentPlan.summary }}
                  </p>
                </div>

                <div v-if="currentClarificationItems.length" class="mt-5 space-y-4">
                  <div class="flex items-center gap-2 text-sm font-semibold text-[rgb(118_85_5)]">
                    <Sparkles class="size-4" />
                    <span>{{ t('console.importAgent.clarificationTitle') }}</span>
                  </div>
                  <div class="space-y-4">
                    <section
                      v-for="group in clarificationGroups"
                      :key="group.key"
                      class="rounded-[18px] border border-[rgb(234_197_79_/_0.45)] bg-[rgb(255,249,226)] p-4"
                    >
                      <p class="text-sm font-semibold text-[rgb(92_64_0)]">{{ group.title }}</p>
                      <div class="mt-4 grid gap-4 md:grid-cols-2">
                        <div v-for="item in group.items" :key="item.id" class="space-y-2">
                          <div class="flex items-center gap-2">
                            <FieldLabel
                              :label="item.label || item.fieldKey"
                              :optional="!item.required"
                            />
                          </div>
                          <p v-if="item.description" class="text-xs leading-5 text-[rgb(92_64_0)]">
                            {{ item.description }}
                          </p>
                          <div
                            v-if="item.defaultValue"
                            class="rounded-[14px] border border-[rgb(234_197_79_/_0.55)] bg-white/70 p-3"
                          >
                            <div
                              class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between"
                            >
                              <div class="min-w-0 space-y-1">
                                <p class="text-[11px] font-semibold text-[rgb(118_85_5)]">
                                  {{ t('console.importAgent.clarificationDefaultTitle') }}
                                </p>
                                <p
                                  class="max-h-24 overflow-auto whitespace-pre-wrap break-words font-mono text-xs leading-5 text-foreground"
                                >
                                  {{ clarificationDefaultLabel(item) }}
                                </p>
                                <p
                                  v-if="clarificationDefaultMeta(item)"
                                  class="text-[11px] leading-5 text-[rgb(92_64_0)]"
                                >
                                  {{ clarificationDefaultMeta(item) }}
                                </p>
                              </div>
                              <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                class="shrink-0"
                                @click="adoptClarificationDefault(item)"
                              >
                                {{ t('console.importAgent.useClarificationDefault') }}
                              </Button>
                            </div>
                          </div>
                          <select
                            v-if="item.inputType === 'SELECT'"
                            class="h-11 w-full rounded-[14px] border border-[rgb(34_34_34_/_0.14)] bg-white px-3 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                            :value="clarificationValue(item)"
                            @change="
                              setClarificationDraft(
                                item,
                                ($event.target as HTMLSelectElement).value,
                              )
                            "
                          >
                            <option value="">
                              {{ t('console.importAgent.clarificationSelectPlaceholder') }}
                            </option>
                            <option
                              v-for="option in item.options"
                              :key="`${item.id}:${option.value}`"
                              :value="option.value"
                            >
                              {{ option.label }}
                            </option>
                          </select>
                          <select
                            v-else-if="item.inputType === 'BOOLEAN'"
                            class="h-11 w-full rounded-[14px] border border-[rgb(34_34_34_/_0.14)] bg-white px-3 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                            :value="clarificationValue(item)"
                            @change="
                              setClarificationDraft(
                                item,
                                ($event.target as HTMLSelectElement).value,
                              )
                            "
                          >
                            <option value="">
                              {{ t('console.importAgent.clarificationSelectPlaceholder') }}
                            </option>
                            <option value="true">{{ t('console.shared.yes') }}</option>
                            <option value="false">{{ t('console.shared.no') }}</option>
                          </select>
                          <textarea
                            v-else-if="item.inputType === 'MULTILINE'"
                            rows="3"
                            class="w-full resize-y rounded-[14px] border border-[rgb(34_34_34_/_0.14)] bg-white p-3 text-sm leading-6 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                            :value="clarificationValue(item)"
                            :placeholder="t('console.importAgent.clarificationTextPlaceholder')"
                            @input="
                              setClarificationDraft(
                                item,
                                ($event.target as HTMLTextAreaElement).value,
                              )
                            "
                          />
                          <Input
                            v-else
                            :model-value="clarificationValue(item)"
                            :placeholder="t('console.importAgent.clarificationTextPlaceholder')"
                            @update:model-value="setClarificationDraft(item, String($event))"
                          />
                        </div>
                      </div>
                    </section>
                  </div>
                </div>

                <div v-else-if="currentPlan.clarificationQuestions.length" class="mt-5 space-y-3">
                  <div class="flex items-center gap-2 text-sm font-semibold text-[rgb(118_85_5)]">
                    <Sparkles class="size-4" />
                    <span>{{ t('console.importAgent.clarificationTitle') }}</span>
                  </div>
                  <div class="grid gap-3 md:grid-cols-2">
                    <div
                      v-for="(question, index) in currentPlan.clarificationQuestions"
                      :key="question"
                      class="rounded-[18px] border border-[rgb(234_197_79_/_0.5)] bg-[linear-gradient(135deg,rgba(255,248,219,0.98),rgba(255,241,188,0.88))] p-4"
                    >
                      <p
                        class="text-[11px] font-semibold uppercase tracking-[0.18em] text-[rgb(141_94_0)]"
                      >
                        {{ t('console.importAgent.questionLabel', { index: index + 1 }) }}
                      </p>
                      <p class="mt-3 text-sm leading-6 text-[rgb(92_64_0)]">
                        {{ question }}
                      </p>
                    </div>
                  </div>
                </div>

                <div v-if="currentPlan.categoryPlans.length" class="mt-5 space-y-3">
                  <p class="text-sm font-semibold text-foreground">
                    {{ t('console.importAgent.categoryPlansTitle') }}
                  </p>
                  <div class="grid gap-3 md:grid-cols-2">
                    <div
                      v-for="categoryPlan in currentPlan.categoryPlans"
                      :key="`${categoryPlan.categoryCode}:${categoryPlan.action}`"
                      class="rounded-[18px] border border-[rgb(34_34_34_/_0.06)] bg-white/84 p-4"
                    >
                      <div class="flex flex-wrap items-center gap-2">
                        <span class="text-sm font-medium text-foreground">{{
                          categoryPlan.categoryName
                        }}</span>
                        <DisplayTag tone="neutral" :label="categoryPlan.categoryCode" />
                        <DisplayTag
                          tone="info"
                          :label="t(`console.importAgent.categoryAction.${categoryPlan.action}`)"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="currentPlan.assetPlans.length" class="mt-5 space-y-3">
                  <p class="text-sm font-semibold text-foreground">
                    {{ t('console.importAgent.assetPlansTitle') }}
                  </p>
                  <div class="space-y-3">
                    <div
                      v-for="assetPlan in currentPlan.assetPlans"
                      :key="assetPlan.apiCode"
                      class="rounded-[20px] border border-[rgb(34_34_34_/_0.06)] bg-white/86 p-4"
                    >
                      <div
                        class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between"
                      >
                        <div class="space-y-2">
                          <div class="flex flex-wrap items-center gap-2">
                            <span class="text-sm font-semibold text-foreground">{{
                              assetPlan.assetName
                            }}</span>
                            <DisplayTag tone="neutral" :label="assetPlan.apiCode" />
                            <DisplayTag tone="info" :label="assetPlan.assetType" />
                          </div>
                          <div class="flex flex-wrap gap-2">
                            <DisplayTag
                              :tone="assetPlan.publishAfterImport ? 'success' : 'neutral'"
                              :label="
                                assetPlan.publishAfterImport
                                  ? t('console.importAgent.publishAfterImport')
                                  : t('console.importAgent.keepAsDraft')
                              "
                            />
                            <DisplayTag
                              v-if="assetPlan.aiProfile"
                              tone="info"
                              :label="`${assetPlan.aiProfile.provider} · ${assetPlan.aiProfile.model}`"
                            />
                          </div>
                        </div>
                        <div class="flex flex-wrap gap-2">
                          <DisplayTag
                            v-if="assetPlan.requestMethod"
                            tone="neutral"
                            :label="assetPlan.requestMethod"
                          />
                          <DisplayTag
                            v-if="assetPlan.authScheme"
                            tone="neutral"
                            :label="assetPlan.authScheme"
                          />
                        </div>
                      </div>

                      <dl class="mt-4 grid gap-3 text-xs text-muted-foreground sm:grid-cols-2">
                        <div v-if="assetPlan.categoryCode">
                          <dt>{{ t('console.importAgent.assetMetaCategory') }}</dt>
                          <dd class="mt-1 text-sm text-foreground">{{ assetPlan.categoryCode }}</dd>
                        </div>
                        <div v-if="assetPlan.requestMethod">
                          <dt>{{ t('console.importAgent.assetMetaMethod') }}</dt>
                          <dd class="mt-1 text-sm text-foreground">
                            {{ assetPlan.requestMethod }}
                          </dd>
                        </div>
                        <div v-if="assetPlan.authScheme">
                          <dt>{{ t('console.importAgent.assetMetaAuthScheme') }}</dt>
                          <dd class="mt-1 text-sm text-foreground">{{ assetPlan.authScheme }}</dd>
                        </div>
                        <div v-if="assetPlan.aiProfile">
                          <dt>{{ t('console.importAgent.assetMetaAiModel') }}</dt>
                          <dd class="mt-1 text-sm text-foreground">
                            {{ `${assetPlan.aiProfile.provider} · ${assetPlan.aiProfile.model}` }}
                          </dd>
                        </div>
                        <div v-if="assetPlan.upstreamUrl" class="sm:col-span-2">
                          <dt>{{ t('console.importAgent.assetMetaUpstreamUrl') }}</dt>
                          <dd class="mt-1 break-all text-sm text-foreground">
                            {{ assetPlan.upstreamUrl }}
                          </dd>
                        </div>
                      </dl>

                      <div
                        v-if="shouldShowSecurityConfig(assetPlan)"
                        class="mt-4 rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-[color-mix(in_srgb,var(--primary)_10%,white)] p-4"
                      >
                        <div
                          class="flex flex-wrap items-center gap-2 text-sm font-semibold text-foreground"
                        >
                          <ShieldCheck class="size-4 text-[rgb(28_100_82)]" />
                          <span>{{ t('console.importAgent.assetSecurityConfigTitle') }}</span>
                          <DisplayTag
                            v-if="assetPlan.authScheme"
                            tone="neutral"
                            :label="assetPlan.authScheme"
                          />
                        </div>
                        <dl class="mt-3 grid gap-3 text-xs text-muted-foreground sm:grid-cols-2">
                          <div v-if="assetPlan.authScheme">
                            <dt>{{ t('console.importAgent.assetMetaAuthScheme') }}</dt>
                            <dd class="mt-1 text-sm text-foreground">{{ assetPlan.authScheme }}</dd>
                          </div>
                          <div class="sm:col-span-2">
                            <dt>{{ t('console.workspace.fieldAuthConfig') }}</dt>
                            <dd
                              v-if="assetPlan.authConfig"
                              class="mt-1 break-all font-mono text-xs leading-5 text-foreground"
                            >
                              {{ assetPlan.authConfig }}
                            </dd>
                            <dd
                              v-else-if="needsSecurityConfig(assetPlan)"
                              class="mt-1 flex items-center gap-2 text-sm text-destructive"
                            >
                              <CircleAlert class="size-4 shrink-0" />
                              <span>{{ t('console.importAgent.assetSecurityConfigMissing') }}</span>
                            </dd>
                            <dd v-else class="mt-1 text-sm text-muted-foreground">
                              {{ t('console.importAgent.assetSecurityConfigNone') }}
                            </dd>
                          </div>
                        </dl>
                      </div>

                      <div
                        v-if="assetPlan.asyncTaskConfig?.enabled"
                        class="mt-4 rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-[color-mix(in_srgb,var(--accent)_18%,white)] p-4"
                      >
                        <div
                          class="flex flex-wrap items-center gap-2 text-sm font-semibold text-foreground"
                        >
                          <Clock3 class="size-4 text-[rgb(62_96_139)]" />
                          <span>{{ t('console.workspace.asyncTaskConfigGroup') }}</span>
                          <DisplayTag
                            v-if="assetPlan.asyncTaskConfig.queryMethod"
                            tone="neutral"
                            :label="assetPlan.asyncTaskConfig.queryMethod"
                          />
                          <DisplayTag
                            v-if="assetPlan.asyncTaskConfig.authMode"
                            tone="info"
                            :label="assetPlan.asyncTaskConfig.authMode"
                          />
                        </div>
                        <dl class="mt-3 grid gap-3 text-xs text-muted-foreground sm:grid-cols-2">
                          <div
                            v-if="assetPlan.asyncTaskConfig.queryUrlTemplate"
                            class="sm:col-span-2"
                          >
                            <dt>{{ t('console.workspace.fieldAsyncTaskQueryUrlTemplate') }}</dt>
                            <dd class="mt-1 break-all text-sm text-foreground">
                              {{ assetPlan.asyncTaskConfig.queryUrlTemplate }}
                            </dd>
                          </div>
                          <div v-if="assetPlan.asyncTaskConfig.authScheme">
                            <dt>{{ t('console.workspace.fieldAsyncTaskAuthScheme') }}</dt>
                            <dd class="mt-1 text-sm text-foreground">
                              {{ assetPlan.asyncTaskConfig.authScheme }}
                            </dd>
                          </div>
                          <div
                            v-if="shouldShowAsyncAuthConfig(assetPlan.asyncTaskConfig)"
                            class="sm:col-span-2"
                          >
                            <dt>{{ t('console.workspace.fieldAsyncTaskAuthConfig') }}</dt>
                            <dd
                              v-if="assetPlan.asyncTaskConfig.authConfig"
                              class="mt-1 break-all font-mono text-xs leading-5 text-foreground"
                            >
                              {{ assetPlan.asyncTaskConfig.authConfig }}
                            </dd>
                            <dd v-else class="mt-1 text-sm text-muted-foreground">
                              {{ t('console.importAgent.assetAsyncAuthConfigInherited') }}
                            </dd>
                          </div>
                          <div v-if="assetPlan.asyncTaskConfig.statusPath">
                            <dt>{{ t('console.workspace.fieldAsyncTaskStatusPath') }}</dt>
                            <dd class="mt-1 text-sm text-foreground">
                              {{ assetPlan.asyncTaskConfig.statusPath }}
                            </dd>
                          </div>
                          <div v-if="assetPlan.asyncTaskConfig.resultPath">
                            <dt>{{ t('console.workspace.fieldAsyncTaskResultPath') }}</dt>
                            <dd class="mt-1 text-sm text-foreground">
                              {{ assetPlan.asyncTaskConfig.resultPath }}
                            </dd>
                          </div>
                          <div v-if="assetPlan.asyncTaskConfig.errorPath">
                            <dt>{{ t('console.workspace.fieldAsyncTaskErrorPath') }}</dt>
                            <dd class="mt-1 text-sm text-foreground">
                              {{ assetPlan.asyncTaskConfig.errorPath }}
                            </dd>
                          </div>
                        </dl>
                      </div>

                      <div
                        v-if="assetPlan.aiProfile"
                        class="mt-4 rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-[color-mix(in_srgb,var(--secondary)_18%,white)] p-4"
                      >
                        <div
                          class="flex flex-wrap items-center gap-2 text-sm font-semibold text-foreground"
                        >
                          <Bot class="size-4 text-[rgb(48_92_57)]" />
                          <span>{{ t('console.home.aiCapability') }}</span>
                        </div>
                        <dl class="mt-3 grid gap-3 text-xs text-muted-foreground sm:grid-cols-3">
                          <div>
                            <dt>{{ t('console.workspace.fieldProvider') }}</dt>
                            <dd class="mt-1 text-sm text-foreground">
                              {{ assetPlan.aiProfile.provider }}
                            </dd>
                          </div>
                          <div>
                            <dt>{{ t('console.workspace.fieldModel') }}</dt>
                            <dd class="mt-1 text-sm text-foreground">
                              {{ assetPlan.aiProfile.model }}
                            </dd>
                          </div>
                          <div>
                            <dt>{{ t('console.playground.streamingSupported') }}</dt>
                            <dd class="mt-1 text-sm text-foreground">
                              {{
                                assetPlan.aiProfile.streamingSupported
                                  ? t('console.shared.yes')
                                  : t('console.shared.no')
                              }}
                            </dd>
                          </div>
                        </dl>
                        <div
                          v-if="assetPlan.aiProfile.capabilityTags.length"
                          class="mt-3 space-y-2"
                        >
                          <p
                            class="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground"
                          >
                            {{ t('console.home.docExport.markdown.tags') }}
                          </p>
                          <div class="flex flex-wrap gap-2">
                            <DisplayTag
                              v-for="capabilityTag in assetPlan.aiProfile.capabilityTags"
                              :key="capabilityTag"
                              tone="ai"
                              :label="capabilityTag"
                            />
                          </div>
                        </div>
                      </div>

                      <div v-if="assetPlan.requestTemplate" class="mt-4">
                        <CodeBlock
                          :label="t('console.workspace.fieldRequestTemplate')"
                          :value="assetPlan.requestTemplate"
                          collapsible
                          default-collapsed
                          max-height-class="max-h-[260px]"
                        />
                      </div>

                      <div v-if="assetPlan.requestExample" class="mt-4">
                        <CodeBlock
                          :label="t('console.workspace.fieldRequestExample')"
                          :value="assetPlan.requestExample"
                          collapsible
                          default-collapsed
                          max-height-class="max-h-[260px]"
                        />
                      </div>

                      <div v-if="assetPlan.responseExample" class="mt-4">
                        <CodeBlock
                          :label="t('console.workspace.fieldResponseExample')"
                          :value="assetPlan.responseExample"
                          collapsible
                          default-collapsed
                          max-height-class="max-h-[260px]"
                        />
                      </div>

                      <div
                        v-if="assetPlan.requestJsonSchema || assetPlan.responseJsonSchema"
                        class="mt-4 grid gap-3"
                      >
                        <JsonSchemaViewer
                          v-if="assetPlan.requestJsonSchema"
                          :label="t('console.workspace.fieldRequestJsonSchema')"
                          :value="assetPlan.requestJsonSchema"
                          presentation="overlay"
                        />
                        <JsonSchemaViewer
                          v-if="assetPlan.responseJsonSchema"
                          :label="t('console.workspace.fieldResponseJsonSchema')"
                          :value="assetPlan.responseJsonSchema"
                          presentation="overlay"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <p v-if="sessionError" class="mt-4 text-sm text-destructive">{{ sessionError }}</p>

                <div class="mt-5 flex flex-wrap gap-2">
                  <Button
                    variant="outline"
                    class="rounded-full"
                    :disabled="!canConfirmPlan"
                    @click="confirmPlan"
                  >
                    <Loader2 v-if="confirming" class="mr-2 size-4 animate-spin" />
                    <CheckCircle2 v-else class="mr-2 size-4" />
                    {{
                      confirming
                        ? t('console.importAgent.confirming')
                        : t('console.importAgent.confirmPlan')
                    }}
                  </Button>
                  <Button class="rounded-full" :disabled="!canStartRun" @click="startRun">
                    <Loader2 v-if="startingRun" class="mr-2 size-4 animate-spin" />
                    <Play v-else class="mr-2 size-4" />
                    {{
                      startingRun
                        ? t('console.importAgent.startingRun')
                        : t('console.importAgent.startRun')
                    }}
                  </Button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="activeRun || runError" class="flex justify-start">
            <div
              class="w-full max-w-[56rem] rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-white/96 px-6 py-6 shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.04)_0px_2px_6px,rgba(0,0,0,0.08)_0px_16px_32px]"
            >
              <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div class="space-y-2">
                  <div class="flex items-center gap-2 text-sm font-semibold text-foreground">
                    <Clock3 class="size-4 text-[rgb(62_96_139)]" />
                    <span>{{ t('console.importAgent.runTitle') }}</span>
                  </div>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  class="rounded-full"
                  :disabled="!activeRun || refreshingRun"
                  @click="refreshRun()"
                >
                  <RefreshCw class="mr-2 size-4" :class="refreshingRun ? 'animate-spin' : ''" />
                  {{ t('console.importAgent.refreshRun') }}
                </Button>
              </div>

              <template v-if="activeRun">
                <div
                  v-if="activeRun.summary"
                  class="mt-4 rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/18 p-4 text-sm leading-7 text-foreground"
                >
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
                    class="rounded-[16px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/15 p-4"
                  >
                    <div class="flex flex-wrap items-center gap-2">
                      <DisplayTag tone="info" :label="stepResult.stepType" />
                      <DisplayTag
                        :tone="stepStatusTone(stepResult.status)"
                        :label="stepResult.status"
                      />
                      <span class="text-sm text-foreground">{{ stepResult.targetRef }}</span>
                    </div>
                    <p
                      v-if="stepResult.message"
                      class="mt-2 text-sm leading-6 text-muted-foreground"
                    >
                      {{ stepResult.message }}
                    </p>
                  </div>
                </div>
              </template>

              <p v-else-if="runError" class="mt-4 text-sm text-destructive">{{ runError }}</p>
            </div>
          </div>

          <div ref="conversationBottomRef" aria-hidden="true" />
        </div>
      </div>

      <div
        class="pointer-events-none fixed inset-x-0 bottom-0 z-10 h-28 bg-[linear-gradient(180deg,rgba(255,255,255,0),rgba(255,255,255,0.9),rgba(255,255,255,0.98))]"
      />
      <div class="fixed bottom-5 left-4 right-4 z-20 lg:left-[calc(248px+2rem)] lg:right-8">
        <div class="mx-auto w-full max-w-[88rem]">
          <div
            class="rounded-[16px] p-4 shadow-[rgba(0,0,0,0.02)_0px_0px_0px_1px,rgba(0,0,0,0.04)_0px_2px_6px,rgba(0,0,0,0.1)_0px_18px_38px] backdrop-blur"
          >
            <div v-if="draftAttachments.length" class="space-y-3">
              <p class="text-sm font-semibold text-foreground">
                {{
                  t('console.importAgent.filesAttachedTitle', { count: draftAttachments.length })
                }}
              </p>
              <div class="flex flex-wrap gap-3">
                <div
                  v-for="attachment in draftAttachments"
                  :key="attachment.id"
                  class="min-w-[220px] max-w-[320px] rounded-[20px] border border-[rgb(34_34_34_/_0.08)] bg-secondary/25 p-4"
                >
                  <div class="flex items-start justify-between gap-2">
                    <div class="min-w-0">
                      <p class="truncate text-sm font-medium text-foreground">
                        {{ attachment.fileName }}
                      </p>
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
                  <p
                    class="mt-3 line-clamp-4 whitespace-pre-wrap text-xs leading-5 text-muted-foreground"
                  >
                    {{ attachment.excerpt }}
                  </p>
                  <p
                    v-if="attachment.truncated"
                    class="mt-2 text-[11px] font-medium text-foreground"
                  >
                    {{ t('console.importAgent.fileTruncated') }}
                  </p>
                </div>
              </div>
            </div>

            <p v-if="attachmentError" class="mt-2 text-sm text-destructive">
              {{ attachmentError }}
            </p>

            <div
              class="mt-4 rounded-[40px] border border-[rgb(34_34_34_/_0.14)] px-4 py-3 shadow-[inset_0px_1px_0px_rgba(255,255,255,0.8)]"
            >
              <div class="flex items-end gap-3">
                <Button
                  variant="ghost"
                  size="icon-sm"
                  class="mb-1 size-11 rounded-full text-foreground hover:bg-secondary/60"
                  :disabled="attachingFiles || sendButtonBusy"
                  @click="openFilePicker"
                >
                  <Loader2 v-if="attachingFiles" class="size-5 animate-spin" />
                  <Paperclip v-else class="size-5" />
                </Button>

                <div class="mb-1 h-8 w-px shrink-0 bg-border/80" />

                <textarea
                  v-model="messageDraft"
                  :rows="1"
                  class="max-h-32 min-h-[2.75rem] flex-1 resize-none border-0 bg-transparent px-0 py-2 text-lg leading-8 text-foreground placeholder:text-[rgb(138_138_138)] focus:outline-none focus:ring-0"
                  :placeholder="composerPlaceholder"
                />

                <Button
                  class="size-14 rounded-full bg-black text-white hover:bg-black/90"
                  :disabled="!canSendMessage"
                  @click="sendMessage"
                >
                  <Loader2 v-if="sendButtonBusy" class="size-5 animate-spin" />
                  <Send v-else class="size-5" />
                </Button>
              </div>
            </div>

            <div
              v-if="!hasActiveSession && showContextPanel"
              class="mt-4 grid gap-3 rounded-[22px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/18 p-4"
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
                  class="w-full resize-y rounded-[16px] border border-[rgb(34_34_34_/_0.12)] bg-white p-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                  :placeholder="t('console.importAgent.documentSummaryPlaceholder')"
                />
              </div>

              <p class="text-xs leading-5 text-muted-foreground">
                {{ t('console.importAgent.contextDescription') }}
              </p>
            </div>

            <input
              ref="fileInputRef"
              type="file"
              class="hidden"
              multiple
              :accept="attachedFileAccept"
              @change="handleFileSelection"
            />

            <div class="mt-4 flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
              <div class="flex flex-wrap items-center gap-2">
                <Button
                  v-if="!hasActiveSession"
                  variant="outline"
                  size="sm"
                  class="rounded-full"
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

              <p v-if="composerError" class="text-sm text-destructive">{{ composerError }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
