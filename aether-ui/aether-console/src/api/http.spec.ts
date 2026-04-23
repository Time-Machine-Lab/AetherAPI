import { beforeEach, describe, expect, it, vi } from 'vitest'

const httpHarness = vi.hoisted(() => {
  let requestHandler: ((config: Record<string, unknown>) => Record<string, unknown>) | undefined
  let responseRejected:
    | ((error: {
        response?: { status?: number; data?: { code?: string; message?: string; traceId?: string } }
        message: string
        config?: { method?: string }
      }) => Promise<never>)
    | undefined

  const pushMock = vi.fn()
  const clearSessionMock = vi.fn()
  const store = {
    accessToken: null as string | null,
    clearSession: clearSessionMock,
  }

  const mockHttp = Object.assign(vi.fn(), {
    interceptors: {
      request: {
        use: vi.fn((handler: typeof requestHandler) => {
          requestHandler = handler
          return 0
        }),
      },
      response: {
        use: vi.fn((_onFulfilled: unknown, onRejected: typeof responseRejected) => {
          responseRejected = onRejected
          return 0
        }),
      },
    },
  })

  return {
    pushMock,
    clearSessionMock,
    store,
    mockHttp,
    createSpy: vi.fn(() => mockHttp),
    getRequestHandler: () => requestHandler,
    getResponseRejected: () => responseRejected,
  }
})

vi.mock('axios', () => ({
  default: {
    create: httpHarness.createSpy,
  },
}))

vi.mock('@/app/router', () => ({
  default: {
    push: httpHarness.pushMock,
  },
}))

vi.mock('@/stores/useAuthStore', () => ({
  useAuthStore: () => httpHarness.store,
}))

vi.mock('@/api/catalog/catalog.mock', () => ({
  mockAdapter: vi.fn(),
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
    signInRouteName: 'console-sign-in',
  },
}))

import './http'

describe('http client interceptors', () => {
  beforeEach(() => {
    httpHarness.pushMock.mockReset()
    httpHarness.clearSessionMock.mockReset()
    httpHarness.store.accessToken = null
  })

  it('injects Authorization header when console token exists', async () => {
    httpHarness.store.accessToken = 'console-access-token'

    const config = await httpHarness.getRequestHandler()!({
      headers: {},
    })

    expect(config.headers).toMatchObject({
      Authorization: 'Bearer console-access-token',
    })
  })

  it('keeps request headers unchanged when no token exists', async () => {
    const config = await httpHarness.getRequestHandler()!({
      headers: {
        'X-Test': 'yes',
      },
    })

    expect(config.headers).toEqual({
      'X-Test': 'yes',
    })
  })

  it('clears session and redirects on console unauthorized error', async () => {
    const rejection = httpHarness.getResponseRejected()!({
      response: {
        status: 401,
        data: {
          code: 'CONSOLE_SESSION_UNAUTHORIZED',
          message: 'Console session authentication required',
          traceId: 'trace-console-401',
        },
      },
      message: 'Request failed with status code 401',
      config: {
        method: 'GET',
      },
    })

    await expect(rejection).rejects.toEqual({
      status: 401,
      code: 'CONSOLE_SESSION_UNAUTHORIZED',
      message: 'Console session authentication required',
      traceId: 'trace-console-401',
    })
    expect(httpHarness.clearSessionMock).toHaveBeenCalledTimes(1)
    expect(httpHarness.pushMock).toHaveBeenCalledWith({
      name: 'console-sign-in',
    })
  })

  it('does not treat Unified Access invalid credential as console session failure', async () => {
    const rejection = httpHarness.getResponseRejected()!({
      response: {
        status: 401,
        data: {
          code: 'INVALID_CREDENTIAL',
          message: 'The API Key is invalid',
          traceId: 'trace-api-key-401',
        },
      },
      message: 'Request failed with status code 401',
      config: {
        method: 'POST',
      },
    })

    await expect(rejection).rejects.toEqual({
      status: 401,
      code: 'INVALID_CREDENTIAL',
      message: 'The API Key is invalid',
      traceId: 'trace-api-key-401',
    })
    expect(httpHarness.clearSessionMock).not.toHaveBeenCalled()
    expect(httpHarness.pushMock).not.toHaveBeenCalled()
  })
})
