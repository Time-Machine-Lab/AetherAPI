import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/useAuthStore'
import { createConsoleSession, createHttpError, createStorageMock, installWindowWithStorage } from '@/test/console-test-kit'
import type {
  CreateImportAgentSessionInput,
  ImportAgentRun,
  ImportAgentSession,
  ImportAgentStreamCallbacks,
} from '@/api/import-agent/import-agent.types'

vi.mock('@/api/import-agent/import-agent.api', () => ({
  createImportAgentSession: vi.fn(),
  getImportAgentSession: vi.fn(),
  appendImportAgentTurn: vi.fn(),
  confirmImportAgentPlan: vi.fn(),
  startImportAgentRun: vi.fn(),
  getImportAgentRun: vi.fn(),
}))

import { useImportAgentWorkspace } from './useImportAgentWorkspace'

type CreateSessionDep = (
  body: CreateImportAgentSessionInput,
  callbacks?: ImportAgentStreamCallbacks,
) => Promise<ImportAgentSession>
type GetSessionDep = (sessionId: string) => Promise<ImportAgentSession>
type AppendTurnDep = (
  sessionId: string,
  message: string,
  callbacks?: ImportAgentStreamCallbacks,
) => Promise<ImportAgentSession>
type ConfirmPlanDep = (sessionId: string, planVersion: number) => Promise<ImportAgentSession>
type StartRunDep = (sessionId: string, planVersion: number) => Promise<ImportAgentRun>
type GetRunDep = (runId: string) => Promise<ImportAgentRun>

interface WorkspaceTestOptions {
  storage?: ReturnType<typeof createStorageMock>
  readFileText?: (file: { name: string; size: number; type?: string }) => Promise<string>
  deps?: Partial<{
    createSession: ReturnType<typeof vi.fn<CreateSessionDep>>
    getSession: ReturnType<typeof vi.fn<GetSessionDep>>
    appendTurn: ReturnType<typeof vi.fn<AppendTurnDep>>
    confirmPlan: ReturnType<typeof vi.fn<ConfirmPlanDep>>
    startRun: ReturnType<typeof vi.fn<StartRunDep>>
    getRun: ReturnType<typeof vi.fn<GetRunDep>>
  }>
}

function createSession(overrides: Partial<ImportAgentSession> = {}): ImportAgentSession {
  return {
    sessionId: 'session-001',
    status: 'WAITING_FOR_CONFIRMATION',
    documentSource: 'https://docs.example.com/weather',
    documentSummary: 'Import weather API',
    importIntent: 'Import weather API',
    publisherDisplayName: 'Console Operator',
    currentPlanVersion: 2,
    confirmedPlanVersion: undefined,
    latestRunId: undefined,
    currentPlan: {
      version: 2,
      executable: true,
      summary: 'Import weather API',
      clarificationQuestions: [],
      categoryPlans: [],
      assetPlans: [],
    },
    turns: [],
    createdAt: '2026-05-18T12:00:00Z',
    updatedAt: '2026-05-18T12:05:00Z',
    ...overrides,
  }
}

function createRun(overrides: Partial<ImportAgentRun> = {}): ImportAgentRun {
  return {
    runId: 'run-001',
    sessionId: 'session-001',
    planVersion: 2,
    status: 'RUNNING',
    summary: 'Importing weather API',
    failureReason: undefined,
    affectedApiCodes: ['weather-forecast'],
    stepResults: [],
    createdAt: '2026-05-18T12:10:00Z',
    updatedAt: '2026-05-18T12:10:30Z',
    ...overrides,
  }
}

function createWorkspace(options: WorkspaceTestOptions = {}) {
  const deps = {
    createSession: vi.fn<CreateSessionDep>(),
    getSession: vi.fn<GetSessionDep>(),
    appendTurn: vi.fn<AppendTurnDep>(),
    confirmPlan: vi.fn<ConfirmPlanDep>(),
    startRun: vi.fn<StartRunDep>(),
    getRun: vi.fn<GetRunDep>(),
    ...(options?.deps ?? {}),
  }

  const schedule = vi.fn((callback: () => void, delayMs: number) => {
    void callback
    void delayMs
    return 100 as unknown as ReturnType<typeof setTimeout>
  })
  const cancelSchedule = vi.fn()

  const workspace = useImportAgentWorkspace({
    t: (key) => key,
    storage: options?.storage,
    readFileText: options?.readFileText,
    deps,
    pollDelayMs: 250,
    schedule,
    cancelSchedule,
  })

  return { workspace, deps, schedule, cancelSchedule }
}

describe('useImportAgentWorkspace', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    installWindowWithStorage()
    useAuthStore().setSession(createConsoleSession())
  })

  it('creates a session and persists the active session id', async () => {
    const storage = createStorageMock()
    installWindowWithStorage(storage)
    const { workspace, deps } = createWorkspace({ storage })

    workspace.importIntent.value = 'Import weather API'
    deps.createSession.mockResolvedValueOnce(createSession())

    await workspace.createSession()

    expect(deps.createSession.mock.calls[0]?.[0]).toEqual({
      documentSource: undefined,
      documentSummary: undefined,
      importIntent: 'Import weather API',
      publisherDisplayName: undefined,
    })
    expect(deps.createSession.mock.calls[0]?.[1]).toEqual(
      expect.objectContaining({
        onStatus: expect.any(Function),
        onMessage: expect.any(Function),
        onSession: expect.any(Function),
        onError: expect.any(Function),
        onDone: expect.any(Function),
      }),
    )
    expect(workspace.activeSession.value?.sessionId).toBe('session-001')
    expect(storage.dump()).toHaveProperty('aether:console:auth:import-agent:active-session:console-user-001', 'session-001')
  })

  it('creates the first session message with attached file context', async () => {
    const readFileText = vi.fn().mockResolvedValueOnce('openapi: 3.0.0\ninfo:\n  title: Weather API')
    const { workspace, deps } = createWorkspace({ readFileText })

    workspace.messageDraft.value = 'Import weather API'
    await workspace.addDraftFiles([
      {
        name: 'weather.yaml',
        size: 128,
        type: 'application/yaml',
      },
    ])
    deps.createSession.mockResolvedValueOnce(createSession())

    await workspace.sendMessage()

    expect(readFileText).toHaveBeenCalledTimes(1)
    expect(deps.createSession).toHaveBeenCalledTimes(1)
    expect(deps.createSession.mock.calls[0]?.[0]).toEqual(
      expect.objectContaining({
        importIntent: 'Import weather API',
        documentSource: expect.stringContaining('weather.yaml'),
        documentSummary: expect.stringContaining('Weather API'),
      }),
    )
    expect(workspace.draftAttachments.value).toHaveLength(0)
  })

  it('restores persisted session and clears invalid cache on access failure', async () => {
    const storage = createStorageMock({
      'aether:console:auth:import-agent:active-session:console-user-001': 'session-001',
    })
    installWindowWithStorage(storage)
    const { workspace, deps } = createWorkspace({ storage })

    deps.getSession.mockResolvedValueOnce(createSession({ latestRunId: 'run-001' }))
    deps.getRun.mockResolvedValueOnce(createRun({ status: 'SUCCEEDED' }))
    deps.getSession.mockResolvedValueOnce(createSession({ latestRunId: 'run-001' }))

    await workspace.restoreActiveSession()

    expect(workspace.activeSession.value?.sessionId).toBe('session-001')
    expect(workspace.activeRun.value?.status).toBe('SUCCEEDED')

    deps.getSession.mockRejectedValueOnce(
      createHttpError({
        status: 404,
        code: 'IMPORT_AGENT_SESSION_NOT_FOUND',
        message: 'not found',
      }),
    )

    await workspace.restoreActiveSession()

    expect(workspace.activeSession.value).toBeNull()
    expect(storage.dump()).not.toHaveProperty('aether:console:auth:import-agent:active-session:console-user-001')
  })

  it('appends a clarification turn and clears the draft message', async () => {
    const { workspace, deps } = createWorkspace()

    deps.createSession.mockResolvedValueOnce(createSession())
    workspace.importIntent.value = 'Import weather API'
    await workspace.createSession()

    workspace.turnMessage.value = 'Use category weather-tools instead.'
    deps.appendTurn.mockResolvedValueOnce(
      createSession({
        turns: [
          {
            turnId: 'turn-002',
            actorType: 'USER',
            message: 'Use category weather-tools instead.',
            planVersion: 2,
            createdAt: '2026-05-18T12:08:00Z',
          },
        ],
      }),
    )

    await workspace.appendTurn()

    expect(deps.appendTurn.mock.calls[0]?.[0]).toBe('session-001')
    expect(deps.appendTurn.mock.calls[0]?.[1]).toBe('Use category weather-tools instead.')
    expect(deps.appendTurn.mock.calls[0]?.[2]).toEqual(
      expect.objectContaining({
        onStatus: expect.any(Function),
        onMessage: expect.any(Function),
        onSession: expect.any(Function),
        onError: expect.any(Function),
        onDone: expect.any(Function),
      }),
    )
    expect(workspace.turnMessage.value).toBe('')
    expect(workspace.activeSession.value?.turns[0].message).toContain('weather-tools')
  })

  it('sends attached files together with a follow-up clarification', async () => {
    const readFileText = vi.fn().mockResolvedValueOnce('{"baseUrl":"https://api.example.com"}')
    const { workspace, deps } = createWorkspace({ readFileText })

    deps.createSession.mockResolvedValueOnce(createSession())
    workspace.messageDraft.value = 'Import weather API'
    await workspace.sendMessage()

    workspace.messageDraft.value = 'Use this upstream profile.'
    await workspace.addDraftFiles([
      {
        name: 'profile.json',
        size: 96,
        type: 'application/json',
      },
    ])
    deps.appendTurn.mockResolvedValueOnce(createSession())

    await workspace.sendMessage()

    expect(deps.appendTurn).toHaveBeenCalledTimes(1)
    expect(deps.appendTurn.mock.calls[0]?.[1]).toContain('Use this upstream profile.')
    expect(deps.appendTurn.mock.calls[0]?.[1]).toContain('profile.json')
    expect(deps.appendTurn.mock.calls[0]?.[1]).toContain('https://api.example.com')
    expect(workspace.draftAttachments.value).toHaveLength(0)
  })

  it('tracks transient streaming reply state before the session snapshot lands', async () => {
    const { workspace, deps } = createWorkspace()

    workspace.importIntent.value = 'Import weather API'
    let resolveSession!: (session: ImportAgentSession) => void
    deps.createSession.mockImplementationOnce(
      (_body, callbacks) =>
        new Promise<ImportAgentSession>((resolve) => {
          resolveSession = resolve
          callbacks?.onStatus?.({ phase: 'planning', message: 'Planning' })
          callbacks?.onMessage?.({ actorType: 'AGENT', delta: 'Let me inspect the source.' })
        }),
    )

    const pendingRequest = workspace.createSession()

    expect(workspace.pendingTurn.value?.message).toBe('Import weather API')
    expect(workspace.streamingPhase.value).toBe('replying')
    expect(workspace.streamingReply.value).toBe('Let me inspect the source.')
    expect(workspace.streamingStatusMessage.value).toBe('Planning')

    resolveSession(createSession())
    await pendingRequest

    expect(workspace.pendingTurn.value).toBeNull()
    expect(workspace.streamingReply.value).toBe('')
    expect(workspace.streamingPhase.value).toBeNull()
  })

  it('confirms the plan and starts a run with polling when the run is still active', async () => {
    const { workspace, deps, schedule } = createWorkspace()

    deps.createSession.mockResolvedValueOnce(createSession())
    workspace.importIntent.value = 'Import weather API'
    await workspace.createSession()

    deps.confirmPlan.mockResolvedValueOnce(createSession({ confirmedPlanVersion: 2 }))
    await workspace.confirmPlan()

    deps.startRun.mockResolvedValueOnce(createRun())
    await workspace.startRun()

    expect(deps.confirmPlan).toHaveBeenCalledWith('session-001', 2)
    expect(workspace.activeSession.value?.confirmedPlanVersion).toBe(2)
    expect(deps.startRun).toHaveBeenCalledWith('session-001', 2)
    expect(workspace.activeRun.value?.status).toBe('RUNNING')
    expect(schedule).toHaveBeenCalledTimes(1)
  })
})
