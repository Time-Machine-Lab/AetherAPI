import { describe, expect, it } from 'vitest'
import enUS from '@/locales/en-US/common'
import zhCN from '@/locales/zh-CN/common'

describe('console locales', () => {
  it('exposes the renamed console copy in english and chinese', () => {
    expect(enUS.app.subtitle).toBe('Console')
    expect(enUS.console.home.metaTitle).toBe('Model Marketplace')
    expect(enUS.console.signIn.submit).toBe('Enter console')

    expect(zhCN.app.subtitle).toBe('控制台')
    expect(zhCN.console.home.metaTitle).toBe('模型广场')
    expect(zhCN.console.signIn.submit).toBe('进入控制台')
  })
})
