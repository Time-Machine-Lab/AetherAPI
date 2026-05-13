import { describe, expect, it, vi } from 'vitest'
import { createHttpError, createRouterPushMock } from '@/test/console-test-kit'

vi.mock('@/composables/useConsoleAuth', () => ({
  useConsoleAuth: () => ({ signIn: vi.fn() }),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {} }),
  useRouter: () => ({ push: vi.fn() }),
}))

import { useSignInForm } from './useSignInForm'

function createMemoryStorage(initial: Record<string, string> = {}) {
  const entries = new Map(Object.entries(initial))

  return {
    getItem: vi.fn((key: string) => entries.get(key) ?? null),
    setItem: vi.fn((key: string, value: string) => {
      entries.set(key, value)
    }),
    read(key: string) {
      return entries.get(key) ?? null
    },
  }
}

describe('useSignInForm', () => {
  it('submits credentials and redirects to requested protected route', async () => {
    const signIn = vi.fn<() => Promise<void>>()
    const push = createRouterPushMock()
    const storage = createMemoryStorage()
    const form = useSignInForm({
      signIn,
      route: { query: { redirectName: 'console-workspace' } },
      router: { push },
      storage,
    })
    form.loginName.value = 'console@aetherapi.local'
    form.password.value = 'change-me-console-password'

    await form.handleSignIn()

    expect(signIn).toHaveBeenCalledWith('console@aetherapi.local', 'change-me-console-password')
    expect(push).toHaveBeenCalledWith({ name: 'console-workspace' })
    expect(form.errorCode.value).toBeNull()
    expect(form.isSubmitting.value).toBe(false)
    expect(JSON.parse(storage.read('aether:console:sign-in-memory') ?? '{}')).toEqual({
      loginName: 'console@aetherapi.local',
      rememberPassword: false,
    })
  })

  it('stores a mapped error code and keeps the user on sign-in page when login fails', async () => {
    const signIn = vi.fn().mockRejectedValueOnce(
      createHttpError({
        status: 401,
        code: 'CONSOLE_SIGN_IN_CREDENTIALS_INVALID',
      }),
    )
    const push = createRouterPushMock()
    const storage = createMemoryStorage({
      'aether:console:sign-in-memory': JSON.stringify({
        loginName: 'saved@aetherapi.local',
        rememberPassword: true,
        password: 'saved-password',
      }),
    })
    const form = useSignInForm({
      signIn,
      route: { query: {} },
      router: { push },
      storage,
    })
    form.loginName.value = 'wrong@aetherapi.local'
    form.password.value = 'wrong-password'

    await form.handleSignIn()

    expect(push).not.toHaveBeenCalled()
    expect(form.errorCode.value).toBe('CONSOLE_SIGN_IN_CREDENTIALS_INVALID')
    expect(form.isSubmitting.value).toBe(false)
    expect(storage.setItem).not.toHaveBeenCalled()
    expect(JSON.parse(storage.read('aether:console:sign-in-memory') ?? '{}')).toEqual({
      loginName: 'saved@aetherapi.local',
      rememberPassword: true,
      password: 'saved-password',
    })
  })

  it('hydrates a saved account without a password', () => {
    const form = useSignInForm({
      signIn: vi.fn<() => Promise<void>>(),
      route: { query: {} },
      router: { push: createRouterPushMock() },
      storage: createMemoryStorage({
        'aether:console:sign-in-memory': JSON.stringify({
          loginName: 'console@aetherapi.local',
          rememberPassword: false,
        }),
      }),
    })

    expect(form.loginName.value).toBe('console@aetherapi.local')
    expect(form.password.value).toBe('')
    expect(form.rememberPassword.value).toBe(false)
  })

  it('hydrates a saved account and password', () => {
    const form = useSignInForm({
      signIn: vi.fn<() => Promise<void>>(),
      route: { query: {} },
      router: { push: createRouterPushMock() },
      storage: createMemoryStorage({
        'aether:console:sign-in-memory': JSON.stringify({
          loginName: 'console@aetherapi.local',
          rememberPassword: true,
          password: 'change-me-console-password',
        }),
      }),
    })

    expect(form.loginName.value).toBe('console@aetherapi.local')
    expect(form.password.value).toBe('change-me-console-password')
    expect(form.rememberPassword.value).toBe(true)
  })

  it('falls back to empty fields when saved credential state is malformed', () => {
    const form = useSignInForm({
      signIn: vi.fn<() => Promise<void>>(),
      route: { query: {} },
      router: { push: createRouterPushMock() },
      storage: createMemoryStorage({
        'aether:console:sign-in-memory': '{broken',
      }),
    })

    expect(form.loginName.value).toBe('')
    expect(form.password.value).toBe('')
    expect(form.rememberPassword.value).toBe(false)
  })

  it('falls back to empty fields when storage is unavailable', () => {
    const form = useSignInForm({
      signIn: vi.fn<() => Promise<void>>(),
      route: { query: {} },
      router: { push: createRouterPushMock() },
      storage: {
        getItem: vi.fn(() => {
          throw new Error('Storage unavailable')
        }),
        setItem: vi.fn(),
      },
    })

    expect(form.loginName.value).toBe('')
    expect(form.password.value).toBe('')
    expect(form.rememberPassword.value).toBe(false)
  })

  it('persists the submitted password only when remember password is checked', async () => {
    const signIn = vi.fn<() => Promise<void>>()
    const storage = createMemoryStorage()
    const form = useSignInForm({
      signIn,
      route: { query: {} },
      router: { push: createRouterPushMock() },
      storage,
    })
    form.loginName.value = 'console@aetherapi.local'
    form.password.value = 'change-me-console-password'
    form.rememberPassword.value = true

    await form.handleSignIn()

    expect(JSON.parse(storage.read('aether:console:sign-in-memory') ?? '{}')).toEqual({
      loginName: 'console@aetherapi.local',
      rememberPassword: true,
      password: 'change-me-console-password',
    })
  })
})
