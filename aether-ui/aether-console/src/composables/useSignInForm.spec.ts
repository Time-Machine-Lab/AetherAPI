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

describe('useSignInForm', () => {
  it('submits credentials and redirects to requested protected route', async () => {
    const signIn = vi.fn<() => Promise<void>>()
    const push = createRouterPushMock()
    const form = useSignInForm({
      signIn,
      route: { query: { redirectName: 'console-workspace' } },
      router: { push },
    })
    form.loginName.value = 'console@aetherapi.local'
    form.password.value = 'change-me-console-password'

    await form.handleSignIn()

    expect(signIn).toHaveBeenCalledWith('console@aetherapi.local', 'change-me-console-password')
    expect(push).toHaveBeenCalledWith({ name: 'console-workspace' })
    expect(form.errorCode.value).toBeNull()
    expect(form.isSubmitting.value).toBe(false)
  })

  it('stores a mapped error code and keeps the user on sign-in page when login fails', async () => {
    const signIn = vi.fn().mockRejectedValueOnce(
      createHttpError({
        status: 401,
        code: 'CONSOLE_SIGN_IN_CREDENTIALS_INVALID',
      }),
    )
    const push = createRouterPushMock()
    const form = useSignInForm({
      signIn,
      route: { query: {} },
      router: { push },
    })

    await form.handleSignIn()

    expect(push).not.toHaveBeenCalled()
    expect(form.errorCode.value).toBe('CONSOLE_SIGN_IN_CREDENTIALS_INVALID')
    expect(form.isSubmitting.value).toBe(false)
  })
})
