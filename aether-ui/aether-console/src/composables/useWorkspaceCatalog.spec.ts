import { describe, expect, it, vi } from 'vitest'
import { useWorkspaceCatalog } from './useWorkspaceCatalog'
import type {
  ApiAsset,
  ApiAssetSummary,
  ApiCategory,
  PageResult,
} from '@/api/catalog/catalog.types'

vi.mock('@/api/catalog/category.api', () => ({
  listCategories: vi.fn(),
  createCategory: vi.fn(),
  renameCategory: vi.fn(),
  enableCategory: vi.fn(),
  disableCategory: vi.fn(),
}))

vi.mock('@/api/catalog/asset.api', () => ({
  getAsset: vi.fn(),
  listAssets: vi.fn(),
  registerAsset: vi.fn(),
  reviseAsset: vi.fn(),
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
    requestMethod: 'GET',
    upstreamUrl: 'https://upstream.example.com/weather',
    authScheme: 'NONE',
    ...overrides,
  }
}

function assetSummary(overrides: Partial<ApiAssetSummary> = {}): ApiAssetSummary {
  return {
    apiCode: 'weather-api',
    assetName: 'Weather API',
    assetType: 'STANDARD_API',
    categoryCode: 'tools',
    categoryName: 'Tools',
    status: 'DRAFT',
    updatedAt: '2026-04-24T00:00:00Z',
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

  it('loads asset list with filter and paging state', async () => {
    const summaries = [
      assetSummary({ apiCode: 'api-a', assetName: 'API A', status: 'ENABLED' }),
      assetSummary({ apiCode: 'api-b', assetName: 'API B', status: 'DRAFT' }),
    ]
    const listPage: PageResult<ApiAssetSummary> = {
      items: summaries,
      total: 2,
      page: 1,
      pageSize: 20,
    }
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      listAssets: vi.fn().mockResolvedValueOnce(listPage),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListAssets(1)

    expect(workspace.assetListItems.value).toHaveLength(2)
    expect(workspace.assetListItems.value[0].apiCode).toBe('api-a')
    expect(workspace.assetListTotal.value).toBe(2)
    expect(workspace.assetListPage.value).toBe(1)
    expect(workspace.assetListLoading.value).toBe(false)
    expect(workspace.assetListError.value).toBe(false)
  })

  it('passes filter fields to listAssets and reflects them in state', async () => {
    const listFn = vi.fn().mockResolvedValueOnce({
      items: [assetSummary({ apiCode: 'ai-api-1', status: 'ENABLED' })],
      total: 1,
      page: 1,
      pageSize: 20,
    } satisfies PageResult<ApiAssetSummary>)
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      listAssets: listFn,
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    workspace.assetListFilterKeyword.value = 'weather'
    workspace.assetListFilterStatus.value = 'ENABLED'
    await workspace.handleListAssets(1)

    expect(listFn).toHaveBeenCalledWith(
      expect.objectContaining({ keyword: 'weather', status: 'ENABLED', page: 1 }),
    )
    expect(workspace.assetListItems.value[0].apiCode).toBe('ai-api-1')
  })

  it('exposes list load failure in assetListError without throwing', async () => {
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      listAssets: vi.fn().mockRejectedValueOnce(new Error('network error')),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListAssets(1)

    expect(workspace.assetListError.value).toBe(true)
    expect(workspace.assetListLoading.value).toBe(false)
    expect(workspace.assetListItems.value).toHaveLength(0)
  })

  it('list selection hydrates currentAsset via getAsset', async () => {
    const onAssetSelected = vi.fn()
    const selectedAsset = asset({ apiCode: 'weather-api', status: 'ENABLED' })
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      onAssetSelected,
      getAsset: vi.fn().mockResolvedValueOnce(selectedAsset),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListSelectAsset('weather-api')

    expect(workspace.currentAsset.value?.apiCode).toBe('weather-api')
    expect(workspace.currentAsset.value?.status).toBe('ENABLED')
    expect(workspace.assetConfigForm.value.requestMethod).toBe('GET')
    expect(workspace.assetConfigForm.value.upstreamUrl).toBe('https://upstream.example.com/weather')
    expect(workspace.assetError.value).toBe('')
    expect(onAssetSelected).toHaveBeenCalledWith(selectedAsset)
  })

  it('list selection failure maps to existing assetNotFound i18n key', async () => {
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      getAsset: vi.fn().mockRejectedValueOnce(new Error('not found')),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListSelectAsset('ghost-api')

    expect(workspace.currentAsset.value).toBeNull()
    expect(workspace.assetError.value).toBe('console.workspace.assetNotFound')
  })

  it('assetListTotalPages returns correct page count', async () => {
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      listAssets: vi.fn().mockResolvedValueOnce({
        items: Array.from({ length: 20 }, (_, i) => assetSummary({ apiCode: `api-${i}` })),
        total: 45,
        page: 1,
        pageSize: 20,
      } satisfies PageResult<ApiAssetSummary>),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListAssets(1)
    expect(workspace.assetListTotalPages()).toBe(3)
  })

  it('saves edited asset config through reviseAsset and updates current asset', async () => {
    const revisedAsset = asset({
      status: 'ENABLED',
      requestMethod: 'POST',
      upstreamUrl: 'https://upstream.example.com/weather/v2',
    })
    const reviseAsset = vi.fn().mockResolvedValueOnce(revisedAsset)
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      getAsset: vi.fn().mockResolvedValueOnce(asset()),
      reviseAsset,
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListSelectAsset('weather-api')
    workspace.assetConfigForm.value.requestMethod = 'POST'
    workspace.assetConfigForm.value.upstreamUrl = 'https://upstream.example.com/weather/v2'

    await workspace.handleSaveAssetConfig()

    expect(reviseAsset).toHaveBeenCalledWith('weather-api', {
      displayName: 'Weather API',
      categoryCode: 'tools',
      requestMethod: 'POST',
      upstreamUrl: 'https://upstream.example.com/weather/v2',
      authScheme: 'NONE',
      requestTemplate: null,
      requestExample: null,
      responseExample: null,
    })
    expect(workspace.currentAsset.value?.requestMethod).toBe('POST')
    expect(workspace.currentAsset.value?.upstreamUrl).toBe(
      'https://upstream.example.com/weather/v2',
    )
  })
})
