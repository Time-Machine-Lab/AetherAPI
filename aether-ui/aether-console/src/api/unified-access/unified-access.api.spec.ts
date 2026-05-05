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

  it('creates a streaming-safe axios instance for unified access calls', () => {
    expect(uaHarness.createSpy).toHaveBeenCalledWith({
      baseURL: '/api',
      timeout: 0,
      headers: { 'X-App-Id': 'console' },
      validateStatus: expect.any(Function),
      responseType: 'arraybuffer',
    })
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
      timeout: 0,
      onDownloadProgress: expect.any(Function),
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

  it('classifies subscription-required platform failures before upstream passthrough', async () => {
    uaHarness.requestMock.mockResolvedValueOnce({
      status: 403,
      headers: {
        'content-type': 'application/json',
      },
      data: jsonBuffer({
        code: 'API_SUBSCRIPTION_REQUIRED',
        message: 'API subscription is required: weather-api',
        failureType: 'SUBSCRIPTION_REQUIRED',
        traceId: 'trace-subscription-required',
        apiCode: 'weather-api',
      }),
    })

    const result = await invokeUnifiedAccess('weather-api', 'GET', 'ak_live_plaintext_1234')

    expect(result).toMatchObject({
      kind: 'platform-failure',
      status: 403,
      platformFailure: {
        code: 'API_SUBSCRIPTION_REQUIRED',
        failureType: 'SUBSCRIPTION_REQUIRED',
        traceId: 'trace-subscription-required',
        apiCode: 'weather-api',
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

  it('uses axios download progress for event-stream responses', async () => {
    uaHarness.requestMock.mockResolvedValueOnce({
      status: 200,
      headers: {
        'content-type': 'text/event-stream; charset=utf-8',
      },
      data: textBuffer('data: hello\n\ndata: world\n\n'),
    })

    const result = await invokeUnifiedAccess(
      'chat-completions',
      'POST',
      'ak_live_plaintext_1234',
      '{"stream":true}',
    )

    expect(uaHarness.requestMock).toHaveBeenCalledWith({
      url: 'v1/access/chat-completions',
      method: 'post',
      headers: {
        'X-Aether-Api-Key': 'ak_live_plaintext_1234',
        'Content-Type': 'application/json',
      },
      data: '{"stream":true}',
      timeout: 0,
      onDownloadProgress: expect.any(Function),
    })
    expect(result).toMatchObject({
      kind: 'text',
      status: 200,
      contentType: 'text/event-stream',
      textBody: 'data: hello\n\ndata: world\n\n',
    })
  })
})
