import { getCurrentConsoleSession, signInConsole } from '@/api/console-auth/console-auth.api'
import type { NormalizedHttpError } from '@/api/http'
import { useAuthStore } from '@/stores/useAuthStore'

export function useConsoleAuth() {
  const authStore = useAuthStore()

  async function signIn(loginName: string, password: string): Promise<void> {
    const session = await signInConsole({ loginName, password })
    authStore.setSession(session)
  }

  async function restoreSession(): Promise<void> {
    if (!authStore.accessToken) {
      authStore.markInitialized()
      return
    }

    try {
      const currentUser = await getCurrentConsoleSession()
      authStore.setSession({
        accessToken: authStore.accessToken,
        expiresAt: '',
        currentUser,
      })
    } catch (err) {
      const httpError = err as NormalizedHttpError
      if (httpError.status === 401) {
        authStore.clearSession()
      } else {
        // Non-auth failure (network error, 5xx): mark initialized but do not clear session
        // to avoid a false logout on transient backend issues
        authStore.markInitialized()
      }
    }
  }

  function signOut() {
    authStore.clearSession()
  }

  return { signIn, restoreSession, signOut }
}
