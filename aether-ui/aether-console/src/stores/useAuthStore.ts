import { defineStore } from 'pinia'
import { appConfig } from '@/app/app-config'
import type { ConsoleCurrentUser, ConsoleSession } from '@/api/console-auth/console-auth.types'

interface PersistedAuthState {
  accessToken: string | null
  currentUser: ConsoleCurrentUser | null
}

interface AuthState extends PersistedAuthState {
  sessionInitialized: boolean
}

function readPersistedSession(): PersistedAuthState {
  if (typeof window === 'undefined') {
    return { accessToken: null, currentUser: null }
  }

  const raw = window.localStorage.getItem(appConfig.storageKey)

  if (!raw) {
    return { accessToken: null, currentUser: null }
  }

  try {
    return JSON.parse(raw) as PersistedAuthState
  } catch {
    return { accessToken: null, currentUser: null }
  }
}

function persistSession(state: PersistedAuthState) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(appConfig.storageKey, JSON.stringify(state))
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    ...readPersistedSession(),
    sessionInitialized: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken) && state.sessionInitialized,
  },
  actions: {
    setSession(session: ConsoleSession) {
      this.accessToken = session.accessToken
      this.currentUser = session.currentUser
      this.sessionInitialized = true
      persistSession({ accessToken: session.accessToken, currentUser: session.currentUser })
    },
    clearSession() {
      this.accessToken = null
      this.currentUser = null
      this.sessionInitialized = true
      persistSession({ accessToken: null, currentUser: null })
    },
    markInitialized() {
      this.sessionInitialized = true
    },
  },
})
