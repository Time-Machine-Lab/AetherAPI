import { vi } from 'vitest'
import type { NormalizedHttpError } from '@/api/http'
import type { ConsoleCurrentUser, ConsoleSession } from '@/api/console-auth/console-auth.types'

export function createConsoleCurrentUser(
  overrides: Partial<ConsoleCurrentUser> = {},
): ConsoleCurrentUser {
  return {
    userId: 'console-user-001',
    loginName: 'console@aetherapi.local',
    displayName: 'Aether Console Operator',
    email: 'console@aetherapi.local',
    role: 'OWNER',
    ...overrides,
  }
}

export function createConsoleSession(
  overrides: Partial<ConsoleSession> = {},
): ConsoleSession {
  return {
    accessToken: 'console-access-token',
    expiresAt: '2026-04-22T12:00:00Z',
    currentUser: createConsoleCurrentUser(),
    ...overrides,
  }
}

export function createHttpError(
  overrides: Partial<NormalizedHttpError> = {},
): NormalizedHttpError {
  return {
    status: 500,
    code: 'INTERNAL_ERROR',
    message: 'Internal error',
    traceId: 'trace-internal-error',
    ...overrides,
  }
}

export interface StorageMock {
  getItem: ReturnType<typeof vi.fn<(key: string) => string | null>>
  setItem: ReturnType<typeof vi.fn<(key: string, value: string) => void>>
  removeItem: ReturnType<typeof vi.fn<(key: string) => void>>
  clear: ReturnType<typeof vi.fn<() => void>>
  dump: () => Record<string, string>
}

export function createStorageMock(seed: Record<string, string> = {}): StorageMock {
  const state = new Map(Object.entries(seed))

  return {
    getItem: vi.fn((key: string) => state.get(key) ?? null),
    setItem: vi.fn((key: string, value: string) => {
      state.set(key, value)
    }),
    removeItem: vi.fn((key: string) => {
      state.delete(key)
    }),
    clear: vi.fn(() => {
      state.clear()
    }),
    dump: () => Object.fromEntries(state.entries()),
  }
}

export function installWindowWithStorage(storage = createStorageMock()) {
  Object.defineProperty(globalThis, 'window', {
    value: {
      localStorage: storage,
    },
    writable: true,
    configurable: true,
  })

  return storage
}

export function createRouterPushMock() {
  return vi.fn()
}
