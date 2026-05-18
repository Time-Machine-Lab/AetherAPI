import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/http', () => ({
  http: {
    post: vi.fn(),
    get: vi.fn(),
    patch: vi.fn(),
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
  })

  it('creates session and maps nested plan fields', async () => {
    mockedPost.mockResolvedValueOnce({
      data: createSessionDto(),
    })

    const result = await createImportAgentSession({
      documentSource: 'https://docs.example.com/weather',
      documentSummary: 'Import weather API',
      importIntent: 'Import weather API and publish after import',
      publisherDisplayName: 'Console Operator',
    })

    expect(mockedPost).toHaveBeenCalledWith('/v1/current-user/import-agent/sessions', {
      documentSource: 'https://docs.example.com/weather',
      documentSummary: 'Import weather API',
      importIntent: 'Import weather API and publish after import',
      publisherDisplayName: 'Console Operator',
    })
    expect(result.currentPlan?.assetPlans[0].aiProfile?.provider).toBe('OpenAI')
    expect(result.turns[0].planVersion).toBe(1)
  })

  it('gets and mutates session through turn and confirm endpoints', async () => {
    mockedGet.mockResolvedValueOnce({ data: createSessionDto() })
    mockedPost.mockResolvedValueOnce({
      data: createSessionDto({
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
    })
    mockedPatch.mockResolvedValueOnce({
      data: createSessionDto({ confirmedPlanVersion: 2 }),
    })

    const loaded = await getImportAgentSession('session-001')
    const appended = await appendImportAgentTurn('session-001', 'Use category weather-tools instead.')
    const confirmed = await confirmImportAgentPlan('session-001', 2)

    expect(mockedGet).toHaveBeenCalledWith('/v1/current-user/import-agent/sessions/session-001')
    expect(mockedPost).toHaveBeenCalledWith(
      '/v1/current-user/import-agent/sessions/session-001/turns',
      { message: 'Use category weather-tools instead.' },
    )
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

    expect(mockedPost).toHaveBeenCalledWith('/v1/current-user/import-agent/sessions/session-001/runs', {
      planVersion: 2,
    })
    expect(mockedGet).toHaveBeenCalledWith('/v1/current-user/import-agent/runs/run-001')
    expect(started.status).toBe('RUNNING')
    expect(loaded.status).toBe('SUCCEEDED')
    expect(loaded.stepResults[0].stepType).toBe('REGISTER_ASSET')
  })
})