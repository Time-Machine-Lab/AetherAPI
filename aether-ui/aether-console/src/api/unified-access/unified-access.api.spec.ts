import { beforeEach, describe, expect, it, vi } from 'vitest'

const uaHarness = vi.hoisted(() => {
  const requestMock = vi.fn()
  const mockInstance = {
    request: requestMock,
  }

  return {
    requestMock,
    createSpy: vi.fn(() => mockInstance),
  }
})

vi.mock('axios', () => ({
  default: {
    create: uaHarness.createSpy,
  },
}))

vi.mock('@/utils/env', () => ({
  env: {
    apiBaseUrl: '/api',
    requestTimeoutMs: 12000,
  },
}))

vi.mock('@/app/app-config', () => ({
  appConfig: {
    appId: 'console',
  },
}))

import { invokeUnifiedAccess } from './unified-access.api'

function jsonBuffer(payload: unknown) {
  return new TextEncoder().encode(JSON.stringify(payload)).buffer
}

function textBuffer(payload: string) {
  return new TextEncoder().encode(payload).buffer
}

describe('unified access api', () => {
  beforeEach(() => {
    uaHarness.requestMock.mockReset()
  })

  it('returns parsed json success payloads', async () => {
    uaHarness.requestMock.mockResolvedValueOnce({
      status: 200,
      headers: {
        'content-type': 'application/json; charset=utf-8',
        'x-request-id': 'req-1',
      },
      data: jsonBuffer({
        id: 'chatcmpl-1',
        choices: [],
      }),
    })

    const result = await invokeUnifiedAccess(
      'chat-completions',
      'POST',
      'ak_live_plaintext_1234',
      '{"model":"gpt-4.1"}',
    )

    expect(uaHarness.requestMock).toHaveBeenCalledWith({
      url: 'v1/access/chat-completions',
      method: 'post',
      headers: {
        'X-Aether-Api-Key': 'ak_live_plaintext_1234',
        'Content-Type': 'application/json',
      },
      data: '{"model":"gpt-4.1"}',
    })
    expect(result).toMatchObject({
      kind: 'json',
      status: 200,
      contentType: 'application/json',
      rawHeaders: {
        'content-type': 'application/json; charset=utf-8',
        'x-request-id': 'req-1',
      },
    })
    expect(result.jsonBody).toEqual({
      id: 'chatcmpl-1',
      choices: [],
    })
  })

  it('classifies platform failures without confusing them with passthrough json', async () => {
    uaHarness.requestMock.mockResolvedValueOnce({
      status: 401,
      headers: {
        'content-type': 'application/json',
      },
      data: jsonBuffer({
        code: 'API_CREDENTIAL_NOT_FOUND',
        message: 'API credential was not found',
        failureType: 'INVALID_CREDENTIAL',
        traceId: 'trace-invalid-credential',
        apiCode: 'unknown-api',
      }),
    })

    const result = await invokeUnifiedAccess('unknown-api', 'GET', 'invalid-key')

    expect(result).toMatchObject({
      kind: 'platform-failure',
      status: 401,
      contentType: 'application/json',
      platformFailure: {
        code: 'API_CREDENTIAL_NOT_FOUND',
        message: 'API credential was not found',
        failureType: 'INVALID_CREDENTIAL',
        traceId: 'trace-invalid-credential',
        apiCode: 'unknown-api',
      },
    })
  })

  it('returns text payloads when upstream responds with plain text', async () => {
    uaHarness.requestMock.mockResolvedValueOnce({
      status: 503,
      headers: {
        'content-type': 'text/plain; charset=utf-8',
      },
      data: textBuffer('target upstream unavailable'),
    })

    const result = await invokeUnifiedAccess('chat-completions', 'GET', 'ak_live_plaintext_1234')

    expect(result).toMatchObject({
      kind: 'text',
      status: 503,
      contentType: 'text/plain',
      textBody: 'target upstream unavailable',
    })
  })
})
