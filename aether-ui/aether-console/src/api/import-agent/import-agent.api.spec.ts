import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/http', () => ({
  http: {
    post: vi.fn(),
    get: vi.fn(),
    patch: vi.fn(),
    request: vi.fn(),
  },
}))

import { http } from '@/api/http'
import {
  appendImportAgentTurn,
  confirmImportAgentPlan,
  createImportAgentSession,
  getImportAgentRun,
  getImportAgentSession,
  startImportAgentRun,
} from './import-agent.api'

const mockedPost = vi.mocked(http.post)
const mockedGet = vi.mocked(http.get)
const mockedPatch = vi.mocked(http.patch)
const mockedRequest = vi.mocked(http.request)

function createSessionDto(overrides: Record<string, unknown> = {}) {
  return {
    sessionId: 'session-001',
    status: 'WAITING_FOR_CONFIRMATION',
    documentSource: 'https://docs.example.com/weather',
    documentSummary: 'Import weather API',
    importIntent: 'Import weather API and publish after import',
    publisherDisplayName: 'Console Operator',
    currentPlanVersion: 2,
    confirmedPlanVersion: null,
    latestRunId: null,
    currentPlan: {
      version: 2,
      executable: true,
      summary: 'Create tools category and import weather API.',
      clarificationQuestions: [],
      categoryPlans: [
        {
          categoryCode: 'tools',
          categoryName: 'Tools',
          action: 'CREATE_IF_MISSING',
        },
      ],
      assetPlans: [
        {
          apiCode: 'weather-forecast',
          assetName: 'Weather Forecast',
          assetType: 'AI_API',
          categoryCode: 'tools',
          requestMethod: 'POST',
          upstreamUrl: 'https://upstream.example.com/weather',
          authScheme: 'HEADER_TOKEN',
          authConfig: 'Authorization: Bearer xxx',
          requestTemplate: '{"location":"{{city}}"}',
          requestExample: '{"location":"Shanghai"}',
          responseExample: '{"forecast":"Sunny"}',
          requestJsonSchema: '{"type":"object"}',
          responseJsonSchema: '{"type":"object"}',
          publishAfterImport: true,
          asyncTaskConfig: {
            enabled: true,
            queryMethod: 'GET',
            queryUrlTemplate: 'https://upstream.example.com/tasks/{taskId}',
            authMode: 'SAME_AS_SUBMIT',
            authScheme: null,
            authConfig: null,
            statusPath: '$.status',
            resultPath: '$.result',
            errorPath: '$.error',
          },
          aiProfile: {
            provider: 'OpenAI',
            model: 'gpt-4.1',
            streamingSupported: true,
            capabilityTags: ['chat'],
          },
        },
      ],
    },
    turns: [
      {
        turnId: 'turn-001',
        actorType: 'USER',
        message: 'Import the weather API.',
        planVersion: 1,
        createdAt: '2026-05-18T12:00:00Z',
      },
    ],
    createdAt: '2026-05-18T12:00:00Z',
    updatedAt: '2026-05-18T12:05:00Z',
    ...overrides,
  }
}

function createRunDto(overrides: Record<string, unknown> = {}) {
  return {
    runId: 'run-001',
    sessionId: 'session-001',
    planVersion: 2,
    status: 'RUNNING',
    summary: 'Importing weather API',
    failureReason: null,
    affectedApiCodes: ['weather-forecast'],
    stepResults: [
      {
        stepType: 'REGISTER_ASSET',
        targetRef: 'weather-forecast',
        status: 'SUCCEEDED',
        message: 'Asset draft created',
      },
    ],
    createdAt: '2026-05-18T12:10:00Z',
    updatedAt: '2026-05-18T12:11:00Z',
    ...overrides,
  }
}

describe('import-agent api', () => {
  beforeEach(() => {
    mockedPost.mockReset()
    mockedGet.mockReset()
    mockedPatch.mockReset()
    mockedRequest.mockReset()
  })

  it('creates session from streaming events and exposes progress callbacks', async () => {
    mockedRequest.mockImplementationOnce(async (config) => {
      config.onDownloadProgress?.({
        event: {
          target: {
            responseText:
              'event: status\ndata: {"phase":"planning","message":"Planning"}\n\n' +
              'event: message\ndata: {"actorType":"AGENT","delta":"Hello"}\n\n',
          },
        },
      } as never)

      return {
        status: 200,
        data:
          'event: status\ndata: {"phase":"planning","message":"Planning"}\n\n' +
          'event: message\ndata: {"actorType":"AGENT","delta":"Hello"}\n\n' +
          `event: session\ndata: ${JSON.stringify(createSessionDto())}\n\n` +
          'event: done\ndata: {"phase":"completed"}\n\n',
      }
    })

    const onStatus = vi.fn()
    const onMessage = vi.fn()
    const onDone = vi.fn()

    const result = await createImportAgentSession(
      {
        documentSource: 'https://docs.example.com/weather',
        documentSummary: 'Import weather API',
        importIntent: 'Import weather API and publish after import',
        publisherDisplayName: 'Console Operator',
      },
      {
        onStatus,
        onMessage,
        onDone,
      },
    )

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/v1/current-user/import-agent/sessions/stream',
      method: 'post',
      data: {
        documentSource: 'https://docs.example.com/weather',
        documentSummary: 'Import weather API',
        importIntent: 'Import weather API and publish after import',
        publisherDisplayName: 'Console Operator',
      },
      timeout: 0,
      responseType: 'text',
      transformResponse: expect.any(Array),
      onDownloadProgress: expect.any(Function),
    })
    expect(onStatus).toHaveBeenCalledWith({ phase: 'planning', message: 'Planning' })
    expect(onMessage).toHaveBeenCalledWith({ actorType: 'AGENT', delta: 'Hello' })
    expect(onDone).toHaveBeenCalledTimes(1)
    expect(result.currentPlan?.assetPlans[0].asyncTaskConfig?.queryUrlTemplate).toBe(
      'https://upstream.example.com/tasks/{taskId}',
    )
    expect(result.currentPlan?.assetPlans[0].aiProfile?.provider).toBe('OpenAI')
    expect(result.turns[0].planVersion).toBe(1)
  })

  it('gets and mutates session through turn and confirm endpoints', async () => {
    mockedGet.mockResolvedValueOnce({ data: createSessionDto() })
    mockedRequest.mockResolvedValueOnce({
      status: 200,
      data:
        `event: session\ndata: ${JSON.stringify(
          createSessionDto({
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
        )}\n\n` + 'event: done\ndata: {"phase":"completed"}\n\n',
    })
    mockedPatch.mockResolvedValueOnce({
      data: createSessionDto({ confirmedPlanVersion: 2 }),
    })

    const loaded = await getImportAgentSession('session-001')
    const appended = await appendImportAgentTurn(
      'session-001',
      'Use category weather-tools instead.',
    )
    const confirmed = await confirmImportAgentPlan('session-001', 2)

    expect(mockedGet).toHaveBeenCalledWith('/v1/current-user/import-agent/sessions/session-001')
    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/v1/current-user/import-agent/sessions/session-001/turns/stream',
      method: 'post',
      data: { message: 'Use category weather-tools instead.' },
      timeout: 0,
      responseType: 'text',
      transformResponse: expect.any(Array),
      onDownloadProgress: expect.any(Function),
    })
    expect(mockedPatch).toHaveBeenCalledWith(
      '/v1/current-user/import-agent/sessions/session-001/confirm',
      { planVersion: 2 },
    )
    expect(loaded.sessionId).toBe('session-001')
    expect(appended.turns[0].message).toContain('weather-tools')
    expect(confirmed.confirmedPlanVersion).toBe(2)
  })

  it('starts and loads import runs', async () => {
    mockedPost.mockResolvedValueOnce({ data: createRunDto() })
    mockedGet.mockResolvedValueOnce({ data: createRunDto({ status: 'SUCCEEDED' }) })

    const started = await startImportAgentRun('session-001', 2)
    const loaded = await getImportAgentRun('run-001')

    expect(mockedPost).toHaveBeenCalledWith(
      '/v1/current-user/import-agent/sessions/session-001/runs',
      {
        planVersion: 2,
      },
    )
    expect(mockedGet).toHaveBeenCalledWith('/v1/current-user/import-agent/runs/run-001')
    expect(started.status).toBe('RUNNING')
    expect(loaded.status).toBe('SUCCEEDED')
    expect(loaded.stepResults[0].stepType).toBe('REGISTER_ASSET')
  })
})
