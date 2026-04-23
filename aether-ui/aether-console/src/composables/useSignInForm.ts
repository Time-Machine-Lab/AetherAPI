import { ref } from 'vue'
import type { RouteRecordName, Router, RouteLocationNormalizedLoaded } from 'vue-router'
import { useRoute, useRouter } from 'vue-router'
import { appConfig } from '@/app/app-config'
import { useConsoleAuth } from '@/composables/useConsoleAuth'
import type { NormalizedHttpError } from '@/api/http'

interface SignInFormDeps {
  signIn: (loginName: string, password: string) => Promise<void>
  route: Pick<RouteLocationNormalizedLoaded, 'query'>
  router: Pick<Router, 'push'>
}

function createDefaultDeps(): SignInFormDeps {
  const { signIn } = useConsoleAuth()
  return {
    signIn,
    route: useRoute(),
    router: useRouter(),
  }
}

export function useSignInForm(deps: SignInFormDeps = createDefaultDeps()) {
  const loginName = ref('')
  const password = ref('')
  const isSubmitting = ref(false)
  const errorCode = ref<string | null>(null)

  async function handleSignIn() {
    isSubmitting.value = true
    errorCode.value = null

    try {
      await deps.signIn(loginName.value, password.value)

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
    isSubmitting,
    errorCode,
    handleSignIn,
  }
}
