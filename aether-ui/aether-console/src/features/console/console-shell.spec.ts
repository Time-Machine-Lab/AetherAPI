import { describe, expect, it } from 'vitest'
import { summarizeConsoleSkeleton } from '@/features/console/console-shell'

describe('console shell blueprint', () => {
  it('keeps the core scaffold mapped before feature work lands', () => {
    const summary = summarizeConsoleSkeleton()

    expect(summary.navIds).toEqual([
      'marketplace',
      'agents',
      'credentials',
      'usage',
      'orders',
      'billing',
      'docs',
      'overview',
    ])
    expect(summary.moduleIds).toEqual([
      'agents',
      'credentials',
      'usage',
      'orders',
      'billing',
      'docs',
    ])
    expect(summary.quickActionIds).toEqual([
      'new',
      'messages',
      'docs',
      'workorder',
      'usage',
      'cloud',
    ])
    expect(summary.readyCount).toBe(6)
    expect(summary.plannedCount).toBe(3)
    expect(summary.sidebarGroupCount).toBe(2)
    expect(summary.topUtilityCount).toBe(6)
    expect(summary.marketplaceCardCount).toBeGreaterThanOrEqual(8)
  })
})
