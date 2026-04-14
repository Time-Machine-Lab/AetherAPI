import { appConfig } from '@/app/app-config'

export const supportedLocales = ['zh-CN', 'en-US'] as const

export type SupportedLocale = (typeof supportedLocales)[number]

export function isSupportedLocale(candidate?: string): candidate is SupportedLocale {
  return supportedLocales.includes(candidate as SupportedLocale)
}

const defaultLocale = import.meta.env.VITE_DEFAULT_LOCALE

export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api',
  appId: import.meta.env.VITE_APP_ID ?? appConfig.appId,
  appName: import.meta.env.VITE_APP_NAME ?? appConfig.appName,
  defaultLocale: isSupportedLocale(defaultLocale) ? defaultLocale : 'zh-CN',
  requestTimeoutMs: Number(import.meta.env.VITE_REQUEST_TIMEOUT_MS ?? '12000'),
} as const
