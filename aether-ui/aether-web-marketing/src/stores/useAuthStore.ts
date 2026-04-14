import { defineStore } from 'pinia'
import { appConfig } from '@/app/app-config'

interface AuthUser {
  displayName: string
  email: string
  role: string
}

interface AuthState {
  token: string | null
  user: AuthUser | null
}

function readSession(): AuthState {
  if (typeof window === 'undefined') {
    return { token: null, user: null }
  }

  const raw = window.localStorage.getItem(appConfig.storageKey)

  if (!raw) {
    return { token: null, user: null }
  }

  try {
    return JSON.parse(raw) as AuthState
  } catch {
    return { token: null, user: null }
  }
}

function persistSession(state: AuthState) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(appConfig.storageKey, JSON.stringify(state))
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => readSession(),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    signIn(user?: Partial<AuthUser>) {
      const nextState: AuthState = {
        token: `${appConfig.appId}-demo-token`,
        user: {
          displayName: user?.displayName ?? 'AetherAPI Operator',
          email: user?.email ?? 'operator@aetherapi.local',
          role: user?.role ?? 'owner',
        },
      }

      this.token = nextState.token
      this.user = nextState.user
      persistSession(nextState)
    },
    signOut() {
      this.token = null
      this.user = null
      persistSession({ token: null, user: null })
    },
  },
})
