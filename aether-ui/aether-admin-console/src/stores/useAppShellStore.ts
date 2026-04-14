import { defineStore } from 'pinia'
import { resolveLocale, setLocale } from '@/app/i18n'
import { appConfig } from '@/app/app-config'
import { env, type SupportedLocale } from '@/utils/env'

const localeStorageKey = `${appConfig.appId}:locale`

function readLocalePreference(): SupportedLocale {
  if (typeof window === 'undefined') {
    return env.defaultLocale
  }

  return resolveLocale(window.localStorage.getItem(localeStorageKey) ?? env.defaultLocale)
}

export const useAppShellStore = defineStore('app-shell', {
  state: () => ({
    locale: readLocalePreference() as SupportedLocale,
  }),
  actions: {
    initialize() {
      setLocale(this.locale)
    },
    setLocalePreference(locale: SupportedLocale) {
      this.locale = locale
      setLocale(locale)

      if (typeof window !== 'undefined') {
        window.localStorage.setItem(localeStorageKey, locale)
      }
    },
  },
})
