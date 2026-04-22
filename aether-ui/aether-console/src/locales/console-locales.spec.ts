import { describe, expect, it } from 'vitest'
import enUS from '@/locales/en-US/common'
import zhCN from '@/locales/zh-CN/common'

describe('console locales', () => {
  it('exposes the current console copy in english and chinese', () => {
    expect(enUS.app.subtitle).toBe('Console')
    expect(enUS.console.home.metaTitle).toBe('API Market')
    expect(enUS.console.signIn.submit).toBe('Sign in')

    expect(zhCN.app.subtitle).toBe('控制台')
    expect(zhCN.console.home.metaTitle).toBe('API 市场')
    expect(zhCN.console.signIn.submit).toBe('登录')
  })
})
