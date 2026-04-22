import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/useAuthStore'
import {
  createConsoleCurrentUser,
  createConsoleSession,
  createHttpError,
  installWindowWithStorage,
} from '@/test/console-test-kit'

vi.mock('@/api/console-auth/console-auth.api', () => ({
  signInConsole: vi.fn(),
  getCurrentConsoleSession: vi.fn(),
}))

import { getCurrentConsoleSession, signInConsole } from '@/api/console-auth/console-auth.api'
import { useConsoleAuth } from './useConsoleAuth'

const mockedSignInConsole = vi.mocked(signInConsole)
const mockedGetCurrentConsoleSession = vi.mocked(getCurrentConsoleSession)

describe('useConsoleAuth', () => {
  beforeEach(() => {
    installWindowWithStorage()
    setActivePinia(createPinia())
    mockedSignInConsole.mockReset()
    mockedGetCurrentConsoleSession.mockReset()
  })

  it('stores returned session after sign-in succeeds', async () => {
    const session = createConsoleSession()
    mockedSignInConsole.mockResolvedValueOnce(session)

    const auth = useConsoleAuth()
    const authStore = useAuthStore()

    await auth.signIn(session.currentUser.loginName, 'change-me-console-password')

    expect(mockedSignInConsole).toHaveBeenCalledWith({
      loginName: session.currentUser.loginName,
      password: 'change-me-console-password',
    })
    expect(authStore.accessToken).toBe(session.accessToken)
    expect(authStore.currentUser).toEqual(session.currentUser)
    expect(authStore.sessionInitialized).toBe(true)
  })

  it('marks session initialized immediately when no token exists', async () => {
    const auth = useConsoleAuth()
    const authStore = useAuthStore()

    await auth.restoreSession()

    expect(mockedGetCurrentConsoleSession).not.toHaveBeenCalled()
    expect(authStore.sessionInitialized).toBe(true)
    expect(authStore.accessToken).toBeNull()
  })

  it('restores current user when persisted token is still valid', async () => {
    const authStore = useAuthStore()
    authStore.accessToken = 'persisted-token'
    mockedGetCurrentConsoleSession.mockResolvedValueOnce(
      createConsoleCurrentUser({
        displayName: 'Recovered Console User',
      }),
    )

    const auth = useConsoleAuth()
    await auth.restoreSession()

    expect(mockedGetCurrentConsoleSession).toHaveBeenCalledTimes(1)
    expect(authStore.accessToken).toBe('persisted-token')
    expect(authStore.currentUser?.displayName).toBe('Recovered Console User')
    expect(authStore.sessionInitialized).toBe(true)
  })

  it('clears session when restore sees unauthorized error', async () => {
    const authStore = useAuthStore()
    authStore.accessToken = 'expired-token'
    authStore.currentUser = createConsoleCurrentUser()
    mockedGetCurrentConsoleSession.mockRejectedValueOnce(
      createHttpError({
        status: 401,
        code: 'CONSOLE_SESSION_UNAUTHORIZED',
        message: 'Console session authentication required',
      }),
    )

    const auth = useConsoleAuth()
    await auth.restoreSession()

    expect(authStore.accessToken).toBeNull()
    expect(authStore.currentUser).toBeNull()
    expect(authStore.sessionInitialized).toBe(true)
  })

  it('keeps existing token when restore hits transient server failure', async () => {
    const authStore = useAuthStore()
    authStore.accessToken = 'still-valid-token'
    authStore.currentUser = createConsoleCurrentUser({
      displayName: 'Existing Console User',
    })
    mockedGetCurrentConsoleSession.mockRejectedValueOnce(
      createHttpError({
        status: 503,
        code: 'SERVICE_UNAVAILABLE',
        message: 'Service temporarily unavailable',
      }),
    )

    const auth = useConsoleAuth()
    await auth.restoreSession()

    expect(authStore.accessToken).toBe('still-valid-token')
    expect(authStore.currentUser?.displayName).toBe('Existing Console User')
    expect(authStore.sessionInitialized).toBe(true)
  })
})
