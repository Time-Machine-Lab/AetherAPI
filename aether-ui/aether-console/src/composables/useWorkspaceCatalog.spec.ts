import { describe, expect, it, vi } from 'vitest'
import { useWorkspaceCatalog } from './useWorkspaceCatalog'
import type { ApiAsset, ApiCategory, PageResult } from '@/api/catalog/catalog.types'

vi.mock('@/api/catalog/category.api', () => ({
  listCategories: vi.fn(),
  createCategory: vi.fn(),
  renameCategory: vi.fn(),
  enableCategory: vi.fn(),
  disableCategory: vi.fn(),
}))

vi.mock('@/api/catalog/asset.api', () => ({
  getAsset: vi.fn(),
  registerAsset: vi.fn(),
  enableAsset: vi.fn(),
  disableAsset: vi.fn(),
  bindAiProfile: vi.fn(),
}))

function t(key: string) {
  return key
}

function category(overrides: Partial<ApiCategory> = {}): ApiCategory {
  return {
    categoryCode: 'tools',
    name: 'Tools',
    status: 'ENABLED',
    createdAt: '2026-04-23T09:00:00Z',
    updatedAt: '2026-04-23T09:00:00Z',
    ...overrides,
  }
}

function asset(overrides: Partial<ApiAsset> = {}): ApiAsset {
  return {
    apiCode: 'weather-api',
    displayName: 'Weather API',
    assetType: 'STANDARD_API',
    categoryCode: 'tools',
    status: 'DRAFT',
    ...overrides,
  }
}

function page<T>(items: T[]): PageResult<T> {
  return { items, total: items.length, page: 1, pageSize: 20 }
}

describe('useWorkspaceCatalog', () => {
  it('loads, creates, renames, and toggles categories', async () => {
    const disabled = category({ status: 'DISABLED', name: 'Disabled Tools' })
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      listCategories: vi.fn().mockResolvedValueOnce(page([category()])),
      createCategory: vi.fn().mockResolvedValueOnce(category({ categoryCode: 'ai', name: 'AI' })),
      renameCategory: vi.fn().mockResolvedValueOnce(category({ name: 'Internal Tools' })),
      enableCategory: vi.fn().mockResolvedValueOnce(category({ status: 'ENABLED' })),
      disableCategory: vi.fn().mockResolvedValueOnce(disabled),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.loadCategories()
    expect(workspace.categories.value).toHaveLength(1)

    workspace.newCatName.value = ' AI '
    await workspace.handleCreateCategory()
    expect(workspace.categories.value[0].categoryCode).toBe('ai')
    expect(workspace.newCatName.value).toBe('')

    workspace.renameValue.value = 'Internal Tools'
    await workspace.handleRenameCategory('tools')
    expect(workspace.categories.value.find((item) => item.categoryCode === 'tools')?.name).toBe(
      'Internal Tools',
    )

    await workspace.handleToggleCategory(category())
    expect(workspace.categories.value.find((item) => item.categoryCode === 'tools')?.status).toBe(
      'DISABLED',
    )
  })

  it('exposes category load failures for workspace empty/error states', async () => {
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      listCategories: vi.fn().mockRejectedValueOnce(new Error('list failed')),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.loadCategories()

    expect(workspace.catError.value).toBe(true)
    expect(workspace.catLoading.value).toBe(false)
  })

  it('loads, registers, toggles, and binds AI profile for assets', async () => {
    const enabledAsset = asset({ status: 'ENABLED' })
    const aiAsset = asset({ assetType: 'AI_API', status: 'ENABLED' })
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      getAsset: vi.fn().mockResolvedValueOnce(asset()),
      registerAsset: vi.fn().mockResolvedValueOnce(asset({ apiCode: 'new-api' })),
      enableAsset: vi.fn().mockResolvedValueOnce(enabledAsset),
      disableAsset: vi.fn().mockResolvedValueOnce(asset({ status: 'DISABLED' })),
      bindAiProfile: vi.fn().mockResolvedValueOnce(aiAsset),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    workspace.assetCodeInput.value = ' weather-api '
    await workspace.handleLoadAsset()
    expect(workspace.currentAsset.value?.apiCode).toBe('weather-api')

    workspace.registerForm.value = {
      apiCode: 'new-api',
      displayName: 'New API',
      assetType: 'STANDARD_API',
      categoryCode: 'tools',
    }
    await workspace.handleRegisterAsset()
    expect(workspace.currentAsset.value?.apiCode).toBe('new-api')
    expect(workspace.registerForm.value.apiCode).toBe('')

    await workspace.handleToggleAsset()
    expect(workspace.currentAsset.value?.status).toBe('ENABLED')

    workspace.aiTagInput.value = 'vision'
    workspace.addAiTag()
    await workspace.handleBindAiProfile()
    expect(workspace.aiProfileForm.value.tags).toEqual(['vision'])
    expect(workspace.currentAsset.value?.assetType).toBe('AI_API')
  })

  it('maps asset load and register failures to existing i18n feedback keys', async () => {
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      getAsset: vi.fn().mockRejectedValueOnce(new Error('missing')),
      registerAsset: vi.fn().mockRejectedValueOnce(new Error('invalid')),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    workspace.assetCodeInput.value = 'missing-api'
    await workspace.handleLoadAsset()
    expect(workspace.assetError.value).toBe('console.workspace.assetNotFound')

    await workspace.handleRegisterAsset()
    expect(workspace.assetError.value).toBe('console.workspace.registerFailed')
  })
})
