import type { NavigationGuard } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { appConfig } from '@/app/app-config'
import { useAuthStore } from '@/stores/useAuthStore'
import { createConsoleSession, installWindowWithStorage } from '@/test/console-test-kit'

const restoreSessionMock = vi.hoisted(() => vi.fn())

vi.mock('@/composables/useConsoleAuth', () => ({
  useConsoleAuth: () => ({
    restoreSession: restoreSessionMock,
  }),
}))

import { applyRouteGuards } from './route-guards'

describe('route guards', () => {
  beforeEach(() => {
    installWindowWithStorage()
    setActivePinia(createPinia())
    restoreSessionMock.mockReset()
  })

  function captureGuard() {
    let guard: NavigationGuard | undefined

    applyRouteGuards({
      beforeEach(callback: NavigationGuard) {
        guard = callback as typeof guard
      },
    } as never)

    return guard as NavigationGuard
  }

  it('restores session before entering protected route for the first time', async () => {
    const authStore = useAuthStore()
    expect(authStore.sessionInitialized).toBe(false)
    const guard = captureGuard()

    await guard(
      {
        name: 'console-workspace',
        meta: { requiresAuth: true },
      } as never,
      {} as never,
      vi.fn(),
    )

    expect(restoreSessionMock).toHaveBeenCalledTimes(1)
  })

  it('redirects authenticated guest-only routes back to protected home', async () => {
    const authStore = useAuthStore()
    authStore.setSession(createConsoleSession())
    const guard = captureGuard()

    const result = await guard(
      {
        name: 'console-sign-in',
        meta: { guestOnly: true },
      } as never,
      {} as never,
      vi.fn(),
    )

    expect(result).toEqual({ name: appConfig.protectedHomeRouteName })
  })

  it('redirects unauthenticated protected route access to sign-in with redirectName', async () => {
    const authStore = useAuthStore()
    authStore.markInitialized()
    const guard = captureGuard()

    const result = await guard(
      {
        name: 'console-playground',
        meta: { requiresAuth: true },
      } as never,
      {} as never,
      vi.fn(),
    )

    expect(result).toEqual({
      name: appConfig.signInRouteName,
      query: {
        redirectName: 'console-playground',
      },
    })
  })

  it('allows route when authenticated and initialized', async () => {
    const authStore = useAuthStore()
    authStore.setSession(createConsoleSession())
    const guard = captureGuard()

    const result = await guard(
      {
        name: 'console-workspace',
        meta: { requiresAuth: true },
      } as never,
      {} as never,
      vi.fn(),
    )

    expect(result).toBe(true)
  })
})
