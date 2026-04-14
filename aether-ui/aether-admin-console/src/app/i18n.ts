import { createI18n } from 'vue-i18n'
import enUS from '@/locales/en-US/common'
import zhCN from '@/locales/zh-CN/common'
import { env, isSupportedLocale, type SupportedLocale } from '@/utils/env'

const messages = {
  'en-US': enUS,
  'zh-CN': zhCN,
} as const

export const i18n = createI18n({
  legacy: false,
  locale: env.defaultLocale,
  fallbackLocale: 'en-US',
  messages,
})

export function resolveLocale(candidate?: string): SupportedLocale {
  if (isSupportedLocale(candidate)) {
    return candidate
  }

  return env.defaultLocale
}

export function setLocale(locale: SupportedLocale) {
  i18n.global.locale.value = locale
}
