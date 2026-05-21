import { computed, getCurrentInstance, onBeforeUnmount, ref } from 'vue'
import { appConfig } from '@/app/app-config'
import type { NormalizedHttpError } from '@/api/http'
import {
  appendImportAgentTurn,
  confirmImportAgentPlan,
  createImportAgentSession,
  getImportAgentRun,
  getImportAgentSession,
  startImportAgentRun,
} from '@/api/import-agent/import-agent.api'
import type {
  AppendImportAgentTurnInput,
  CreateImportAgentSessionInput,
  ImportAgentClarificationAnswerInput,
  ImportAgentClarificationItem,
  ImportAgentStreamCallbacks,
  ImportAgentStreamPhase,
  ImportAgentStreamThinkingEvent,
  ImportAgentRun,
  ImportAgentSession,
  ImportAgentTurn,
} from '@/api/import-agent/import-agent.types'
import { useAuthStore } from '@/stores/useAuthStore'

type Translate = (key: string, params?: Record<string, unknown>) => string

interface ImportAgentWorkspaceDeps {
  createSession: typeof createImportAgentSession
  getSession: typeof getImportAgentSession
  appendTurn: typeof appendImportAgentTurn
  confirmPlan: typeof confirmImportAgentPlan
  startRun: typeof startImportAgentRun
  getRun: typeof getImportAgentRun
}

interface ImportAgentWorkspaceOptions {
  t: Translate
  pollDelayMs?: number
  deps?: ImportAgentWorkspaceDeps
  storage?: Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>
  schedule?: (callback: () => void, delayMs: number) => ReturnType<typeof setTimeout>
  cancelSchedule?: (timer: ReturnType<typeof setTimeout>) => void
  readFileText?: (file: ImportAgentDraftFileLike) => Promise<string>
}

export interface ImportAgentDraftFileLike {
  name: string
  size: number
  type?: string
  text?: () => Promise<string>
}

export interface ImportAgentDraftAttachment {
  id: string
  fileName: string
  mimeType?: string
  size: number
  excerpt: string
  truncated: boolean
}

const defaultDeps: ImportAgentWorkspaceDeps = {
  createSession: createImportAgentSession,
  getSession: getImportAgentSession,
  appendTurn: appendImportAgentTurn,
  confirmPlan: confirmImportAgentPlan,
  startRun: startImportAgentRun,
  getRun: getImportAgentRun,
}

const ACTIVE_SESSION_STORAGE_KEY = `${appConfig.storageKey}:import-agent:active-session`
const MAX_DRAFT_ATTACHMENT_SIZE = 512 * 1024
const MAX_DRAFT_ATTACHMENT_COUNT = 5
const MAX_ATTACHMENT_EXCERPT_LENGTH = 64000
const MAX_DOCUMENT_SUMMARY_LENGTH = 20000
const MAX_TURN_MESSAGE_LENGTH = 20000
const SUPPORTED_TEXT_FILE_EXTENSIONS = new Set([
  'txt',
  'md',
  'markdown',
  'json',
  'yaml',
  'yml',
  'csv',
  'http',
  'xml',
  'log',
  'js',
  'ts',
  'mjs',
  'cjs',
])

function defaultStorage() {
  if (typeof window === 'undefined') {
    return undefined
  }
  return window.localStorage
}

function buildActiveSessionKey(userId?: string | null) {
  return `${ACTIVE_SESSION_STORAGE_KEY}:${userId ?? 'anonymous'}`
}

function extensionOf(fileName: string) {
  const index = fileName.lastIndexOf('.')
  return index < 0 ? '' : fileName.slice(index + 1).toLowerCase()
}

function supportsTextExtraction(file: ImportAgentDraftFileLike) {
  if (typeof file.type === 'string' && file.type.startsWith('text/')) {
    return true
  }
  return SUPPORTED_TEXT_FILE_EXTENSIONS.has(extensionOf(file.name))
}

function truncateContent(content: string, maxLength: number) {
  if (content.length <= maxLength) {
    return { excerpt: content, truncated: false }
  }
  return {
    excerpt: `${content.slice(0, maxLength)}\n...[已截断]`,
    truncated: true,
  }
}

function trimJoinedSections(sections: string[], maxLength: number) {
  const joined = sections.filter((section) => section.trim().length > 0).join('\n\n')
  return truncateContent(joined, maxLength).excerpt
}

function formatAttachmentSection(attachment: ImportAgentDraftAttachment) {
  return [
    `文件：${attachment.fileName}`,
    attachment.mimeType ? `类型：${attachment.mimeType}` : undefined,
    `大小：${attachment.size} 字节`,
    '内容：',
    attachment.excerpt,
  ]
    .filter((line) => typeof line === 'string' && line.length > 0)
    .join('\n')
}

function defaultReadFileText(file: ImportAgentDraftFileLike) {
  if (typeof file.text === 'function') {
    return file.text()
  }
  return Promise.reject(new Error('IMPORT_AGENT_FILE_TEXT_UNAVAILABLE'))
}

function normalizeOptional(value: string) {
  const normalized = value.trim()
  return normalized.length > 0 ? normalized : undefined
}

function resolveErrorMessage(error: unknown, t: Translate, fallbackKey: string) {
  if (error && typeof error === 'object') {
    const httpError = error as Partial<NormalizedHttpError>
    if (typeof httpError.code === 'string' && httpError.code.length > 0) {
      const key = `console.importAgent.errors.${httpError.code}`
      const translated = t(key)
      if (translated !== key) {
        return translated
      }
    }
    if (typeof httpError.message === 'string' && httpError.message.trim().length > 0) {
      return httpError.message
    }
  }

  return t(fallbackKey)
}

function isTerminalRunStatus(status: ImportAgentRun['status']) {
  return status !== 'RUNNING'
}

export function useImportAgentWorkspace(options: ImportAgentWorkspaceOptions) {
  const authStore = useAuthStore()
  const deps = options.deps ?? defaultDeps
  const storage = options.storage ?? defaultStorage()
  const pollDelayMs = options.pollDelayMs ?? 3000
  const schedule =
    options.schedule ?? ((callback: () => void, delayMs: number) => setTimeout(callback, delayMs))
  const cancelSchedule = options.cancelSchedule ?? clearTimeout

  const documentSource = ref('')
  const documentSummary = ref('')
  const importIntent = ref('')
  const publisherDisplayName = ref('')
  const turnMessage = ref('')
  const clarificationDrafts = ref<Record<string, string>>({})
  const draftAttachments = ref<ImportAgentDraftAttachment[]>([])

  const activeSession = ref<ImportAgentSession | null>(null)
  const activeRun = ref<ImportAgentRun | null>(null)
  const pendingTurn = ref<ImportAgentTurn | null>(null)
  const streamingReply = ref('')
  const streamingPhase = ref<ImportAgentStreamPhase | null>(null)
  const streamingStatusMessage = ref('')
  const streamingThoughts = ref<ImportAgentStreamThinkingEvent[]>([])

  const restoring = ref(false)
  const creating = ref(false)
  const refreshingSession = ref(false)
  const appending = ref(false)
  const confirming = ref(false)
  const startingRun = ref(false)
  const refreshingRun = ref(false)
  const attachingFiles = ref(false)

  const sessionError = ref('')
  const turnError = ref('')
  const runError = ref('')
  const attachmentError = ref('')

  let pollHandle: ReturnType<typeof setTimeout> | null = null
  let attachmentSequence = 0

  const currentPlan = computed(() => activeSession.value?.currentPlan ?? null)
  const currentClarificationItems = computed(() => currentPlan.value?.clarificationItems ?? [])
  const hasActiveSession = computed(() => activeSession.value !== null)
  const messageDraft = computed({
    get: () => (hasActiveSession.value ? turnMessage.value : importIntent.value),
    set: (value: string) => {
      if (hasActiveSession.value) {
        turnMessage.value = value
        return
      }
      importIntent.value = value
    },
  })
  const isBusy = computed(
    () =>
      restoring.value ||
      creating.value ||
      refreshingSession.value ||
      appending.value ||
      confirming.value ||
      startingRun.value ||
      refreshingRun.value ||
      attachingFiles.value,
  )
  const canCreateSession = computed(() => importIntent.value.trim().length > 0 && !creating.value)
  const hasClarificationDrafts = computed(() =>
    Object.values(clarificationDrafts.value).some((value) => value.trim().length > 0),
  )
  const canAppendTurn = computed(
    () =>
      hasActiveSession.value &&
      (turnMessage.value.trim().length > 0 ||
        draftAttachments.value.length > 0 ||
        hasClarificationDrafts.value) &&
      !appending.value,
  )
  const canSendMessage = computed(() => {
    if (hasActiveSession.value) {
      return canAppendTurn.value && !attachingFiles.value
    }
    return canCreateSession.value && !attachingFiles.value
  })
  const canConfirmPlan = computed(
    () =>
      Boolean(currentPlan.value?.executable) &&
      activeSession.value?.confirmedPlanVersion !== currentPlan.value?.version &&
      !confirming.value,
  )
  const canStartRun = computed(
    () =>
      Boolean(currentPlan.value?.executable) &&
      activeSession.value?.confirmedPlanVersion === currentPlan.value?.version &&
      !startingRun.value,
  )

  function clearErrors() {
    sessionError.value = ''
    turnError.value = ''
    runError.value = ''
    attachmentError.value = ''
  }

  function clearDraftAttachments() {
    draftAttachments.value = []
    attachmentError.value = ''
  }

  function clearClarificationDrafts() {
    clarificationDrafts.value = {}
  }

  function nextAttachmentId() {
    attachmentSequence += 1
    return `attachment-${attachmentSequence}`
  }

  function buildAttachmentSections() {
    return draftAttachments.value.map(formatAttachmentSection)
  }

  function buildCreateDocumentSource() {
    const sections: string[] = []
    const manualSource = normalizeOptional(documentSource.value)
    if (manualSource) {
      sections.push(manualSource)
    }
    if (draftAttachments.value.length > 0) {
      sections.push(
        `本地文件：${draftAttachments.value.map((attachment) => attachment.fileName).join('、')}`,
      )
    }
    const normalized = trimJoinedSections(sections, 1024)
    return normalized.length > 0 ? normalized : undefined
  }

  function buildCreateDocumentSummary() {
    const sections: string[] = []
    const manualSummary = normalizeOptional(documentSummary.value)
    if (manualSummary) {
      sections.push(manualSummary)
    }
    const attachmentSections = buildAttachmentSections()
    if (attachmentSections.length > 0) {
      sections.push('已附加文件')
      sections.push(...attachmentSections)
    }
    const normalized = trimJoinedSections(sections, MAX_DOCUMENT_SUMMARY_LENGTH)
    return normalized.length > 0 ? normalized : undefined
  }

  function buildAppendTurnMessage() {
    const sections = [turnMessage.value.trim(), ...buildAttachmentSections()]
    return trimJoinedSections(sections, MAX_TURN_MESSAGE_LENGTH)
  }

  function buildClarificationAnswers(): ImportAgentClarificationAnswerInput[] {
    const answers: ImportAgentClarificationAnswerInput[] = []
    currentClarificationItems.value.forEach((item) => {
      const value = clarificationDrafts.value[item.id]?.trim()
      if (!value) {
        return
      }
      answers.push({
          clarificationId: item.id,
          targetPath: item.targetPath,
          fieldKey: item.fieldKey,
          value,
      })
    })
    return answers
  }

  function adoptClarificationDefault(item: ImportAgentClarificationItem) {
    const value = item.defaultValue?.trim()
    if (!value) {
      return
    }
    clarificationDrafts.value = {
      ...clarificationDrafts.value,
      [item.id]: value,
    }
  }

  function buildAppendTurnInput(): AppendImportAgentTurnInput {
    const message = buildAppendTurnMessage()
    const clarificationAnswers = buildClarificationAnswers()
    return {
      ...(message.length > 0 ? { message } : {}),
      ...(clarificationAnswers.length > 0 ? { clarificationAnswers } : {}),
    }
  }

  function appendTurnPendingSummary(input: AppendImportAgentTurnInput) {
    if (input.message && input.message.trim().length > 0) {
      return input.message
    }
    const count = input.clarificationAnswers?.length ?? 0
    return options.t('console.importAgent.clarificationAnswerSummary', { count })
  }

  function persistSessionId(sessionId?: string | null) {
    const userId = authStore.currentUser?.userId
    if (!storage || !userId) {
      return
    }

    const key = buildActiveSessionKey(userId)
    if (!sessionId) {
      storage.removeItem(key)
      return
    }

    storage.setItem(key, sessionId)
  }

  function readPersistedSessionId() {
    const userId = authStore.currentUser?.userId
    if (!storage || !userId) {
      return null
    }
    return storage.getItem(buildActiveSessionKey(userId))
  }

  function stopRunPolling() {
    if (!pollHandle) {
      return
    }

    cancelSchedule(pollHandle)
    pollHandle = null
  }

  function applySession(session: ImportAgentSession) {
    activeSession.value = session
    persistSessionId(session.sessionId)
  }

  function resetStreamingState(clearThoughts = true, clearReply = true) {
    pendingTurn.value = null
    if (clearReply) {
      streamingReply.value = ''
    }
    streamingPhase.value = null
    streamingStatusMessage.value = ''
    if (clearThoughts) {
      streamingThoughts.value = []
    }
  }

  function beginStreamingTurn(message: string) {
    pendingTurn.value = {
      turnId: `pending-${Date.now()}`,
      actorType: 'USER',
      message,
      createdAt: new Date().toISOString(),
    }
    streamingReply.value = ''
    streamingThoughts.value = []
    streamingPhase.value = 'planning'
    streamingStatusMessage.value = ''
  }

  function buildStreamingCallbacks(): ImportAgentStreamCallbacks {
    return {
      onStatus: (event) => {
        streamingPhase.value = event.phase
        streamingStatusMessage.value = event.message ?? ''
      },
      onThinking: (event) => {
        streamingThoughts.value = [...streamingThoughts.value, event]
      },
      onMessage: (event) => {
        if (event.actorType !== 'AGENT') {
          return
        }
        streamingPhase.value = 'replying'
        streamingReply.value += event.delta
      },
      onSession: (session) => {
        applySession(session)
        activeRun.value = null
      },
      onError: () => {
        resetStreamingState()
      },
      onDone: () => {
        streamingPhase.value = 'completed'
      },
    }
  }

  function resetDraft(createDefaults = false) {
    activeSession.value = null
    activeRun.value = null
    documentSource.value = ''
    documentSummary.value = ''
    importIntent.value = ''
    turnMessage.value = ''
    clearClarificationDrafts()
    clearErrors()
    clearDraftAttachments()
    resetStreamingState()
    stopRunPolling()
    persistSessionId(null)

    if (createDefaults && authStore.currentUser?.displayName) {
      publisherDisplayName.value = authStore.currentUser.displayName
    }
  }

  async function createSession() {
    if (!canCreateSession.value) {
      return null
    }

    creating.value = true
    sessionError.value = ''
    runError.value = ''
    try {
      const payload: CreateImportAgentSessionInput = {
        documentSource: buildCreateDocumentSource(),
        documentSummary: buildCreateDocumentSummary(),
        importIntent: importIntent.value.trim(),
        publisherDisplayName: normalizeOptional(publisherDisplayName.value),
      }
      beginStreamingTurn(payload.importIntent)
      importIntent.value = ''
      const session = await deps.createSession(payload, buildStreamingCallbacks())
      applySession(session)
      activeRun.value = null
      turnMessage.value = ''
      clearDraftAttachments()
      resetStreamingState(false, false)
      return session
    } catch (error) {
      resetStreamingState()
      sessionError.value = resolveErrorMessage(
        error,
        options.t,
        'console.importAgent.errors.createSessionFallback',
      )
      return null
    } finally {
      creating.value = false
    }
  }

  async function refreshSession() {
    if (!activeSession.value) {
      return null
    }

    refreshingSession.value = true
    sessionError.value = ''
    try {
      const session = await deps.getSession(activeSession.value.sessionId)
      applySession(session)
      return session
    } catch (error) {
      sessionError.value = resolveErrorMessage(
        error,
        options.t,
        'console.importAgent.errors.loadSessionFallback',
      )
      return null
    } finally {
      refreshingSession.value = false
    }
  }

  async function restoreActiveSession() {
    const persistedSessionId = readPersistedSessionId()
    if (!persistedSessionId) {
      if (!publisherDisplayName.value && authStore.currentUser?.displayName) {
        publisherDisplayName.value = authStore.currentUser.displayName
      }
      return null
    }

    restoring.value = true
    sessionError.value = ''
    try {
      const session = await deps.getSession(persistedSessionId)
      applySession(session)
      if (session.latestRunId) {
        await refreshRun(session.latestRunId)
      }
      return session
    } catch (error) {
      resetDraft(true)
      sessionError.value = resolveErrorMessage(
        error,
        options.t,
        'console.importAgent.errors.restoreSessionFallback',
      )
      return null
    } finally {
      restoring.value = false
    }
  }

  async function appendTurn() {
    if (!activeSession.value || !canAppendTurn.value) {
      return null
    }

    appending.value = true
    turnError.value = ''
    try {
      const input = buildAppendTurnInput()
      const pendingSummary = appendTurnPendingSummary(input)
      beginStreamingTurn(pendingSummary)
      turnMessage.value = ''
      const session = await deps.appendTurn(
        activeSession.value.sessionId,
        input,
        buildStreamingCallbacks(),
      )
      applySession(session)
      activeRun.value = null
      turnMessage.value = ''
      clearClarificationDrafts()
      clearDraftAttachments()
      resetStreamingState(false, false)
      return session
    } catch (error) {
      resetStreamingState()
      turnError.value = resolveErrorMessage(
        error,
        options.t,
        'console.importAgent.errors.appendTurnFallback',
      )
      return null
    } finally {
      appending.value = false
    }
  }

  async function confirmPlan() {
    if (!activeSession.value || !currentPlan.value) {
      return null
    }

    confirming.value = true
    sessionError.value = ''
    try {
      const session = await deps.confirmPlan(
        activeSession.value.sessionId,
        currentPlan.value.version,
      )
      applySession(session)
      return session
    } catch (error) {
      sessionError.value = resolveErrorMessage(
        error,
        options.t,
        'console.importAgent.errors.confirmPlanFallback',
      )
      return null
    } finally {
      confirming.value = false
    }
  }

  function scheduleNextRunPoll(runId: string) {
    stopRunPolling()
    pollHandle = schedule(() => {
      void refreshRun(runId)
    }, pollDelayMs)
  }

  async function refreshRun(runId = activeRun.value?.runId) {
    if (!runId) {
      return null
    }

    refreshingRun.value = true
    runError.value = ''
    try {
      const run = await deps.getRun(runId)
      activeRun.value = run
      if (isTerminalRunStatus(run.status)) {
        stopRunPolling()
        await refreshSession()
      } else {
        scheduleNextRunPoll(run.runId)
      }
      return run
    } catch (error) {
      runError.value = resolveErrorMessage(
        error,
        options.t,
        'console.importAgent.errors.loadRunFallback',
      )
      stopRunPolling()
      return null
    } finally {
      refreshingRun.value = false
    }
  }

  async function startRun() {
    if (!activeSession.value || !currentPlan.value || !canStartRun.value) {
      return null
    }

    startingRun.value = true
    runError.value = ''
    try {
      const run = await deps.startRun(activeSession.value.sessionId, currentPlan.value.version)
      activeRun.value = run
      if (isTerminalRunStatus(run.status)) {
        stopRunPolling()
        await refreshSession()
      } else {
        scheduleNextRunPoll(run.runId)
      }
      return run
    } catch (error) {
      runError.value = resolveErrorMessage(
        error,
        options.t,
        'console.importAgent.errors.startRunFallback',
      )
      return null
    } finally {
      startingRun.value = false
    }
  }

  async function sendMessage() {
    if (hasActiveSession.value) {
      return appendTurn()
    }
    return createSession()
  }

  function removeDraftAttachment(attachmentId: string) {
    draftAttachments.value = draftAttachments.value.filter(
      (attachment) => attachment.id !== attachmentId,
    )
    if (draftAttachments.value.length === 0) {
      attachmentError.value = ''
    }
  }

  async function addDraftFiles(files: Iterable<ImportAgentDraftFileLike>) {
    const fileList = Array.from(files)
    if (fileList.length === 0) {
      return []
    }

    attachingFiles.value = true
    attachmentError.value = ''
    try {
      const availableSlots = Math.max(0, MAX_DRAFT_ATTACHMENT_COUNT - draftAttachments.value.length)
      const selectedFiles = fileList.slice(0, availableSlots)
      if (selectedFiles.length < fileList.length) {
        attachmentError.value = options.t('console.importAgent.fileLimitExceeded', {
          count: MAX_DRAFT_ATTACHMENT_COUNT,
        })
      }

      const readFileText = options.readFileText ?? defaultReadFileText
      const importedAttachments: ImportAgentDraftAttachment[] = []

      for (const file of selectedFiles) {
        if (!supportsTextExtraction(file)) {
          attachmentError.value = options.t('console.importAgent.fileUnsupported', {
            fileName: file.name,
          })
          continue
        }

        if (file.size > MAX_DRAFT_ATTACHMENT_SIZE) {
          attachmentError.value = options.t('console.importAgent.fileTooLarge', {
            fileName: file.name,
            sizeKb: Math.floor(MAX_DRAFT_ATTACHMENT_SIZE / 1024),
          })
          continue
        }

        const content = await readFileText(file)
        const normalizedContent = content.trim()
        if (normalizedContent.length === 0) {
          attachmentError.value = options.t('console.importAgent.fileEmpty', {
            fileName: file.name,
          })
          continue
        }

        const { excerpt, truncated } = truncateContent(
          normalizedContent,
          MAX_ATTACHMENT_EXCERPT_LENGTH,
        )
        importedAttachments.push({
          id: nextAttachmentId(),
          fileName: file.name,
          mimeType: normalizeOptional(file.type ?? ''),
          size: file.size,
          excerpt,
          truncated,
        })
      }

      if (importedAttachments.length > 0) {
        draftAttachments.value = [...draftAttachments.value, ...importedAttachments]
      }

      return importedAttachments
    } catch (error) {
      attachmentError.value = resolveErrorMessage(
        error,
        options.t,
        'console.importAgent.errors.readFileFallback',
      )
      return []
    } finally {
      attachingFiles.value = false
    }
  }

  if (getCurrentInstance()) {
    onBeforeUnmount(() => {
      stopRunPolling()
      clearClarificationDrafts()
    })
  }

  return {
    documentSource,
    documentSummary,
    importIntent,
    publisherDisplayName,
    turnMessage,
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
    isBusy,
    canCreateSession,
    canAppendTurn,
    hasClarificationDrafts,
    canSendMessage,
    canConfirmPlan,
    canStartRun,
    createSession,
    refreshSession,
    restoreActiveSession,
    appendTurn,
    sendMessage,
    confirmPlan,
    startRun,
    refreshRun,
    addDraftFiles,
    removeDraftAttachment,
    adoptClarificationDefault,
    clearDraftAttachments,
    resetDraft,
    stopRunPolling,
  }
}
