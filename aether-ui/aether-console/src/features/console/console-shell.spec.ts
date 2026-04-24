import { describe, expect, it } from 'vitest'
import {
  consoleSidebarGroups,
  consoleTopUtilities,
  consoleWorkspacePanels,
  normalizeConsoleWorkspaceNavId,
} from '@/features/console/console-shell'

describe('console shell blueprint', () => {
  it('keeps key operations navigation entries wired to workspace hashes', () => {
    const operationsGroup = consoleSidebarGroups.find((group) => group.id === 'operations')
    expect(operationsGroup).toBeDefined()

    const operationIds = operationsGroup!.items.map((item) => item.id)
    expect(operationIds).toEqual(['credentials', 'api-call-logs'])
    expect(operationsGroup!.items.find((item) => item.id === 'api-call-logs')?.hash).toBe(
      '#api-call-logs',
    )
  })

  it('keeps top utilities and workspace panels available for shell rendering', () => {
    expect(consoleTopUtilities.map((item) => item.id)).toEqual(['new', 'messages', 'workorder'])
    expect(consoleWorkspacePanels.map((item) => item.id)).toEqual(['asset-manage', 'recent-assets'])
  })

  it('normalizes hidden workspace hashes back to the default visible section', () => {
    expect(normalizeConsoleWorkspaceNavId('#category-manage')).toBe('catalog-manage')
    expect(normalizeConsoleWorkspaceNavId('#usage')).toBe('catalog-manage')
    expect(normalizeConsoleWorkspaceNavId('#orders')).toBe('catalog-manage')
    expect(normalizeConsoleWorkspaceNavId('#billing')).toBe('catalog-manage')
    expect(normalizeConsoleWorkspaceNavId('#docs')).toBe('catalog-manage')
  })
})
