/// <reference types="vite/client" />

export {}

type AppLayoutKey = 'MarketingLayout' | 'PortalLayout' | 'AdminLayout'

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_APP_ID?: string
  readonly VITE_APP_NAME?: string
  readonly VITE_DEFAULT_LOCALE?: 'zh-CN' | 'en-US'
  readonly VITE_REQUEST_TIMEOUT_MS?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare module 'vue-router' {
  interface RouteMeta {
    layout?: AppLayoutKey
    titleKey?: string
    requiresAuth?: boolean
    guestOnly?: boolean
  }
}
