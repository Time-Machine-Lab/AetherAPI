import { describe, expect, it } from 'vitest'
import { isPlatformAdminRole } from './platform-admin'

describe('platform admin role helper', () => {
  it('matches backend administrator-capable role labels', () => {
    expect(isPlatformAdminRole('OWNER')).toBe(true)
    expect(isPlatformAdminRole('admin')).toBe(true)
    expect(isPlatformAdminRole(' PLATFORM_ADMIN ')).toBe(true)
  })

  it('rejects non-administrator or missing roles', () => {
    expect(isPlatformAdminRole('USER')).toBe(false)
    expect(isPlatformAdminRole('')).toBe(false)
    expect(isPlatformAdminRole(null)).toBe(false)
    expect(isPlatformAdminRole(undefined)).toBe(false)
  })
})
