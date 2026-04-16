import type { Router } from 'vue-router'
import { appConfig } from '@/app/app-config'
import { useAuthStore } from '@/stores/useAuthStore'

export function applyRouteGuards(router: Router) {
  router.beforeEach((to) => {
    const authStore = useAuthStore()

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
