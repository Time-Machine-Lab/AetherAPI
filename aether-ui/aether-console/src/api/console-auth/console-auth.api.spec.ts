import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  createConsoleCurrentUser,
  createConsoleSession,
  createHttpError,
} from '@/test/console-test-kit'

vi.mock('@/api/http', () => ({
  http: {
    post: vi.fn(),
    get: vi.fn(),
  },
}))

import { http } from '@/api/http'
import { getCurrentConsoleSession, signInConsole } from './console-auth.api'

const mockedPost = vi.mocked(http.post)
const mockedGet = vi.mocked(http.get)

describe('console auth api', () => {
  beforeEach(() => {
    mockedPost.mockReset()
    mockedGet.mockReset()
  })

  it('maps sign-in response into console session', async () => {
    const session = createConsoleSession()

    mockedPost.mockResolvedValueOnce({
      data: {
        accessToken: session.accessToken,
        tokenType: 'Bearer',
        expiresAt: session.expiresAt,
        expiresInSeconds: 43200,
        currentUser: session.currentUser,
      },
    })

    const result = await signInConsole({
      loginName: session.currentUser.loginName,
      password: 'change-me-console-password',
    })

    expect(mockedPost).toHaveBeenCalledWith('/console/auth/sign-in', {
      loginName: session.currentUser.loginName,
      password: 'change-me-console-password',
    })
    expect(result).toEqual(session)
  })

  it('keeps sign-in parameter errors untouched for upper-layer handling', async () => {
    const error = createHttpError({
      status: 400,
      code: 'CONSOLE_SIGN_IN_REQUEST_INVALID',
      message: 'Invalid console sign-in request parameters',
    })
    mockedPost.mockRejectedValueOnce(error)

    await expect(
      signInConsole({
        loginName: '',
        password: '',
      }),
    ).rejects.toEqual(error)
  })

  it('keeps sign-in credential errors untouched for upper-layer handling', async () => {
    const error = createHttpError({
      status: 401,
      code: 'CONSOLE_SIGN_IN_CREDENTIALS_INVALID',
      message: 'Invalid console sign-in credentials',
    })
    mockedPost.mockRejectedValueOnce(error)

    await expect(
      signInConsole({
        loginName: 'console@aetherapi.local',
        password: 'wrong-password',
      }),
    ).rejects.toEqual(error)
  })

  it('maps current-session response into current user', async () => {
    const currentUser = createConsoleCurrentUser({
      displayName: 'Recovered Console User',
    })
    mockedGet.mockResolvedValueOnce({
      data: {
        currentUser,
      },
    })

    const result = await getCurrentConsoleSession()

    expect(mockedGet).toHaveBeenCalledWith('/console/auth/current-session')
    expect(result).toEqual(currentUser)
  })

  it('keeps current-session unauthorized errors untouched for upper-layer handling', async () => {
    const error = createHttpError({
      status: 401,
      code: 'CONSOLE_SESSION_UNAUTHORIZED',
      message: 'Console session authentication required',
    })
    mockedGet.mockRejectedValueOnce(error)

    await expect(getCurrentConsoleSession()).rejects.toEqual(error)
  })
})
