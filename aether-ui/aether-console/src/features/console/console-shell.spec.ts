import { describe, expect, it } from 'vitest'
import {
  consoleSidebarGroups,
  consoleTopUtilities,
  consoleWorkspacePanels,
} from '@/features/console/console-shell'

describe('console shell blueprint', () => {
  it('keeps key operations navigation entries wired to workspace hashes', () => {
    const operationsGroup = consoleSidebarGroups.find((group) => group.id === 'operations')
    expect(operationsGroup).toBeDefined()

    const operationIds = operationsGroup!.items.map((item) => item.id)
    expect(operationIds).toEqual([
      'credentials',
      'api-call-logs',
      'usage',
      'orders',
      'billing',
      'docs',
    ])
    expect(operationsGroup!.items.find((item) => item.id === 'api-call-logs')?.hash).toBe(
      '#api-call-logs',
    )
  })

  it('keeps top utilities and workspace panels available for shell rendering', () => {
    expect(consoleTopUtilities.map((item) => item.id)).toEqual([
      'new',
      'messages',
      'docs',
      'workorder',
      'usage',
    ])
    expect(consoleWorkspacePanels.map((item) => item.id)).toEqual([
      'category-manage',
      'asset-manage',
      'recent-assets',
    ])
  })
})
