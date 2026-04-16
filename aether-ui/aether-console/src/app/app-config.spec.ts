import { describe, expect, it } from 'vitest'
import { appConfig } from '@/app/app-config'

describe('appConfig', () => {
  it('uses the console namespace everywhere', () => {
    expect(appConfig.appId).toBe('console')
    expect(appConfig.appName).toBe('AetherAPI Console')
    expect(appConfig.defaultLayout).toBe('ConsoleLayout')
    expect(appConfig.protectedHomeRouteName).toBe('console-workspace')
    expect(appConfig.signInRouteName).toBe('console-sign-in')
    expect(appConfig.storageKey).toBe('aether:console:auth')
  })
})
