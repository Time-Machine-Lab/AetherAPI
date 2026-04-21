import type { Router } from 'vue-router'
import { appConfig } from '@/app/app-config'
import { useAuthStore } from '@/stores/useAuthStore'
import { useConsoleAuth } from '@/composables/useConsoleAuth'

export function applyRouteGuards(router: Router) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const { restoreSession } = useConsoleAuth()

    // Attempt session restore once per app lifecycle before accessing any protected route
    if (!authStore.sessionInitialized && to.meta.requiresAuth) {
      await restoreSession()
    }

    if (to.meta.guestOnly && authStore.isAuthenticated) {
      return { name: appConfig.protectedHomeRouteName }
    }

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      return {
        name: appConfig.signInRouteName,
        query: to.name ? { redirectName: String(to.name) } : undefined,
      }
    }

    return true
  })
}
