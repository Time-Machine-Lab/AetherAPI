import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from '@/App.vue'
import { i18n } from '@/app/i18n'
import router from '@/app/router'
import { useAppShellStore } from '@/stores/useAppShellStore'

export function createAetherApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(i18n)

  const appShellStore = useAppShellStore(pinia)
  appShellStore.initialize()

  app.use(router)

  return {
    app,
    pinia,
    router,
  }
}
