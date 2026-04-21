import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/http', () => ({
  http: {
    get: vi.fn(),
  },
}))

import { http } from '@/api/http'
import {
  getCurrentUserApiCallLogDetail,
  listCurrentUserApiCallLogs,
} from './api-call-log.api'

const mockedGet = vi.mocked(http.get)

describe('api call log api', () => {
  beforeEach(() => {
    mockedGet.mockReset()
  })

  it('maps list response and forwards query params', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        items: [
          {
            logId: 'log-1',
            targetApiCode: 'chat-completions',
            targetApiName: 'Chat Completions',
            requestMethod: 'POST',
            invocationTime: '2026-04-19T09:30:00Z',
            durationMs: 842,
            resultType: 'SUCCESS',
            success: true,
            httpStatusCode: 200,
          },
        ],
        page: 2,
        size: 10,
        total: 31,
      },
    })

    const result = await listCurrentUserApiCallLogs({
      targetApiCode: 'chat-completions',
      invocationStartAt: '2026-04-19T08:00:00Z',
      invocationEndAt: '2026-04-19T12:00:00Z',
      page: 2,
      size: 10,
    })

    expect(mockedGet).toHaveBeenCalledWith('v1/current-user/api-call-logs', {
      params: {
        targetApiCode: 'chat-completions',
        invocationStartAt: '2026-04-19T08:00:00Z',
        invocationEndAt: '2026-04-19T12:00:00Z',
        page: 2,
        size: 10,
      },
    })
    expect(result).toEqual({
      items: [
        {
          logId: 'log-1',
          targetApiCode: 'chat-completions',
          targetApiName: 'Chat Completions',
          requestMethod: 'POST',
          invocationTime: '2026-04-19T09:30:00Z',
          durationMs: 842,
          resultType: 'SUCCESS',
          success: true,
          httpStatusCode: 200,
        },
      ],
      total: 31,
      page: 2,
      pageSize: 10,
    })
  })

  it('maps detail response', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        logId: 'log-1',
        targetApiCode: 'chat-completions',
        targetApiName: 'Chat Completions',
        requestMethod: 'POST',
        invocationTime: '2026-04-19T09:30:00Z',
        durationMs: 842,
        resultType: 'SUCCESS',
        success: true,
        httpStatusCode: 200,
        accessChannel: 'UNIFIED_ACCESS',
        credentialCode: 'cred_001',
        credentialStatus: 'ENABLED',
        error: null,
        aiExtension: {
          provider: 'OpenAI',
          model: 'gpt-4.1',
          streaming: false,
          usageSnapshot: '{"promptTokens":12}',
        },
        createdAt: '2026-04-19T09:30:00Z',
        updatedAt: '2026-04-19T09:30:01Z',
      },
    })

    const result = await getCurrentUserApiCallLogDetail('log-1')

    expect(mockedGet).toHaveBeenCalledWith('v1/current-user/api-call-logs/log-1')
    expect(result.logId).toBe('log-1')
    expect(result.aiExtension?.provider).toBe('OpenAI')
    expect(result.error).toBeNull()
  })
})
