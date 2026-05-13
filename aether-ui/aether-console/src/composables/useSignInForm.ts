import { ref } from 'vue'
import type { RouteRecordName, Router, RouteLocationNormalizedLoaded } from 'vue-router'
import { useRoute, useRouter } from 'vue-router'
import { appConfig } from '@/app/app-config'
import { useConsoleAuth } from '@/composables/useConsoleAuth'
import type { NormalizedHttpError } from '@/api/http'

interface SignInMemoryStorage {
  getItem: (key: string) => string | null
  setItem: (key: string, value: string) => void
}

interface SignInFormDeps {
  signIn: (loginName: string, password: string) => Promise<void>
  route: Pick<RouteLocationNormalizedLoaded, 'query'>
  router: Pick<Router, 'push'>
  storage?: SignInMemoryStorage | null
}

const signInMemoryStorageKey = 'aether:console:sign-in-memory'

function createDefaultDeps(): SignInFormDeps {
  const { signIn } = useConsoleAuth()
  return {
    signIn,
    route: useRoute(),
    router: useRouter(),
    storage: typeof window === 'undefined' ? null : window.localStorage,
  }
}

function readSignInMemory(storage?: SignInMemoryStorage | null) {
  if (!storage) {
    return { loginName: '', password: '', rememberPassword: false }
  }

  try {
    const raw = storage.getItem(signInMemoryStorageKey)
    if (!raw) {
      return { loginName: '', password: '', rememberPassword: false }
    }

    const parsed: unknown = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object') {
      return { loginName: '', password: '', rememberPassword: false }
    }

    const memory = parsed as Record<string, unknown>
    const savedLoginName = typeof memory.loginName === 'string' ? memory.loginName : ''
    const savedPassword = typeof memory.password === 'string' ? memory.password : ''
    const rememberPassword = memory.rememberPassword === true && savedPassword.length > 0

    return {
      loginName: savedLoginName,
      password: rememberPassword ? savedPassword : '',
      rememberPassword,
    }
  } catch {
    return { loginName: '', password: '', rememberPassword: false }
  }
}

function writeSignInMemory(
  storage: SignInMemoryStorage | null | undefined,
  loginName: string,
  password: string,
  rememberPassword: boolean,
) {
  if (!storage) return

  try {
    const payload: { loginName: string; rememberPassword: boolean; password?: string } = {
      loginName,
      rememberPassword,
    }

    if (rememberPassword) {
      payload.password = password
    }

    storage.setItem(signInMemoryStorageKey, JSON.stringify(payload))
  } catch {
    // Remembered credentials are optional convenience state.
  }
}

export function useSignInForm(deps: SignInFormDeps = createDefaultDeps()) {
  const remembered = readSignInMemory(deps.storage)
  const loginName = ref(remembered.loginName)
  const password = ref(remembered.password)
  const rememberPassword = ref(remembered.rememberPassword)
  const isSubmitting = ref(false)
  const errorCode = ref<string | null>(null)

  async function handleSignIn() {
    isSubmitting.value = true
    errorCode.value = null

    try {
      await deps.signIn(loginName.value, password.value)
      writeSignInMemory(deps.storage, loginName.value, password.value, rememberPassword.value)

      const redirectName =
        typeof deps.route.query.redirectName === 'string'
          ? (deps.route.query.redirectName as RouteRecordName)
          : (appConfig.protectedHomeRouteName as RouteRecordName)

      await deps.router.push({ name: redirectName })
    } catch (err) {
      const httpError = err as NormalizedHttpError
      errorCode.value = httpError.code ?? 'UNKNOWN'
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    loginName,
    password,
    rememberPassword,
    isSubmitting,
    errorCode,
    handleSignIn,
  }
}
