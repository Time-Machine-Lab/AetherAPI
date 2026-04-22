import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { appConfig } from '@/app/app-config'
import {
  createConsoleCurrentUser,
  createConsoleSession,
  createStorageMock,
  installWindowWithStorage,
} from '@/test/console-test-kit'
import { useAuthStore } from './useAuthStore'

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('persists session payload when setSession is called', () => {
    const storage = installWindowWithStorage(createStorageMock())
    const store = useAuthStore()
    const session = createConsoleSession()

    store.setSession(session)

    expect(store.isAuthenticated).toBe(true)
    expect(storage.setItem).toHaveBeenCalledWith(
      appConfig.storageKey,
      JSON.stringify({
        accessToken: session.accessToken,
        currentUser: session.currentUser,
      }),
    )
  })

  it('restores persisted session on first store creation', () => {
    const currentUser = createConsoleCurrentUser({
      displayName: 'Persisted Console User',
    })
    installWindowWithStorage(
      createStorageMock({
        [appConfig.storageKey]: JSON.stringify({
          accessToken: 'persisted-token',
          currentUser,
        }),
      }),
    )

    const store = useAuthStore()

    expect(store.accessToken).toBe('persisted-token')
    expect(store.currentUser).toEqual(currentUser)
    expect(store.sessionInitialized).toBe(false)
    expect(store.isAuthenticated).toBe(false)
  })

  it('clears persisted session and marks store initialized', () => {
    const storage = installWindowWithStorage(createStorageMock())
    const store = useAuthStore()

    store.setSession(createConsoleSession())
    store.clearSession()

    expect(store.accessToken).toBeNull()
    expect(store.currentUser).toBeNull()
    expect(store.sessionInitialized).toBe(true)
    expect(store.isAuthenticated).toBe(false)
    expect(storage.setItem).toHaveBeenLastCalledWith(
      appConfig.storageKey,
      JSON.stringify({
        accessToken: null,
        currentUser: null,
      }),
    )
  })

  it('marks session initialized without creating authentication state', () => {
    installWindowWithStorage(createStorageMock())
    const store = useAuthStore()

    store.markInitialized()

    expect(store.sessionInitialized).toBe(true)
    expect(store.accessToken).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
})
