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
  publishAsset: vi.fn(),
  unpublishAsset: vi.fn(),
  deleteAsset: vi.fn(),
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

  it('loads, registers, publishes, and binds AI profile for assets', async () => {
    const publishedAsset = asset({ status: 'PUBLISHED' })
    const aiAsset = asset({ assetType: 'AI_API', status: 'PUBLISHED' })
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      getAsset: vi.fn().mockResolvedValueOnce(asset()),
      registerAsset: vi.fn().mockResolvedValueOnce(asset({ apiCode: 'new-api' })),
      publishAsset: vi.fn().mockResolvedValueOnce(publishedAsset),
      unpublishAsset: vi.fn().mockResolvedValueOnce(asset({ status: 'UNPUBLISHED' })),
      bindAiProfile: vi.fn().mockResolvedValueOnce(aiAsset),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    workspace.assetCodeInput.value = ' weather-api '
    await workspace.handleLoadAsset()
    expect(workspace.currentAsset.value?.apiCode).toBe('weather-api')

    workspace.registerForm.value = {
      apiCode: 'new-api',
      assetName: 'New API',
      assetType: 'STANDARD_API',
    }
    await workspace.handleRegisterAsset()
    expect(workspace.currentAsset.value?.apiCode).toBe('new-api')
    expect(workspace.registerForm.value.apiCode).toBe('')

    await workspace.handleToggleAsset()
    expect(workspace.currentAsset.value?.status).toBe('PUBLISHED')

    workspace.aiTagInput.value = 'vision'
    workspace.addAiTag()
    await workspace.handleBindAiProfile()
    expect(workspace.aiProfileForm.value.capabilityTags).toEqual(['vision'])
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
      assetSummary({ apiCode: 'api-a', assetName: 'API A', status: 'PUBLISHED' }),
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
      items: [assetSummary({ apiCode: 'ai-api-1', status: 'PUBLISHED' })],
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
    workspace.assetListFilterStatus.value = 'PUBLISHED'
    await workspace.handleListAssets(1)

    expect(listFn).toHaveBeenCalledWith(
      expect.objectContaining({ keyword: 'weather', status: 'PUBLISHED', page: 1 }),
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
    const selectedAsset = asset({
      apiCode: 'weather-api',
      status: 'PUBLISHED',
      authScheme: 'HEADER_TOKEN',
      authConfig: 'Authorization: Bearer upstream-token',
    })
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      onAssetSelected,
      getAsset: vi.fn().mockResolvedValueOnce(selectedAsset),
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListSelectAsset('weather-api')

    expect(workspace.currentAsset.value?.apiCode).toBe('weather-api')
    expect(workspace.currentAsset.value?.status).toBe('PUBLISHED')
    expect(workspace.assetConfigForm.value.requestMethod).toBe('GET')
    expect(workspace.assetConfigForm.value.upstreamUrl).toBe('https://upstream.example.com/weather')
    expect(workspace.assetConfigForm.value.authConfig).toBe('Authorization: Bearer upstream-token')
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
      status: 'UNPUBLISHED',
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
    workspace.assetConfigForm.value.authScheme = 'QUERY_TOKEN'
    workspace.assetConfigForm.value.authConfig = ' access_token=upstream-token '

    await workspace.handleSaveAssetConfig()

    expect(reviseAsset).toHaveBeenCalledWith('weather-api', {
      displayName: 'Weather API',
      categoryCode: 'tools',
      requestMethod: 'POST',
      upstreamUrl: 'https://upstream.example.com/weather/v2',
      authScheme: 'QUERY_TOKEN',
      authConfig: 'access_token=upstream-token',
      requestTemplate: null,
      requestExample: null,
      responseExample: null,
    })
    expect(workspace.currentAsset.value?.status).toBe('UNPUBLISHED')
    expect(workspace.currentAsset.value?.requestMethod).toBe('POST')
    expect(workspace.currentAsset.value?.upstreamUrl).toBe(
      'https://upstream.example.com/weather/v2',
    )
  })

  it('saves empty auth config as null when no upstream token auth is needed', async () => {
    const reviseAsset = vi.fn().mockResolvedValueOnce(asset())
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      getAsset: vi.fn().mockResolvedValueOnce(
        asset({
          authScheme: 'HEADER_TOKEN',
          authConfig: 'Authorization: Bearer old-token',
        }),
      ),
      reviseAsset,
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListSelectAsset('weather-api')
    workspace.assetConfigForm.value.authScheme = 'NONE'
    workspace.assetConfigForm.value.authConfig = '   '

    await workspace.handleSaveAssetConfig()

    expect(reviseAsset).toHaveBeenCalledWith(
      'weather-api',
      expect.objectContaining({
        authScheme: 'NONE',
        authConfig: null,
      }),
    )
  })

  it('unpublishes and deletes the selected asset', async () => {
    const selectedAsset = asset({ apiCode: 'weather-api', status: 'PUBLISHED' })
    const unpublishAsset = vi.fn().mockResolvedValueOnce(asset({ status: 'UNPUBLISHED' }))
    const deleteAsset = vi.fn().mockResolvedValueOnce(asset({ deleted: true }))
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      getAsset: vi.fn().mockResolvedValueOnce(selectedAsset),
      unpublishAsset,
      deleteAsset,
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    await workspace.handleListSelectAsset('weather-api')
    await workspace.handleToggleAsset()
    expect(unpublishAsset).toHaveBeenCalledWith('weather-api')
    expect(workspace.currentAsset.value?.status).toBe('UNPUBLISHED')

    workspace.assetListItems.value = [assetSummary({ apiCode: 'weather-api' })]
    workspace.assetListTotal.value = 1
    await workspace.handleDeleteAsset()
    expect(deleteAsset).toHaveBeenCalledWith('weather-api')
    expect(workspace.currentAsset.value).toBeNull()
    expect(workspace.assetListItems.value).toHaveLength(0)
    expect(workspace.assetListTotal.value).toBe(0)
  })

  it('opens focused create/edit flows and preserves base config when binding AI profile', async () => {
    const selectedAsset = asset({
      assetType: 'AI_API',
      requestMethod: 'POST',
      upstreamUrl: 'https://upstream.example.com/ai',
      requestTemplate: '{"messages":"{{messages}}"}',
      requestExample: '{"messages":["hi"]}',
      responseExample: '{"reply":"hello"}',
    })
    const bindAiProfile = vi.fn().mockResolvedValueOnce({
      apiCode: 'weather-api',
      assetType: 'AI_API',
      status: 'DRAFT',
      aiProfile: {
        provider: 'OpenAI',
        model: 'gpt-test',
        streaming: true,
        tags: ['chat'],
      },
    } as ApiAsset)
    const workspace = useWorkspaceCatalog({
      t,
      autoLoad: false,
      getAsset: vi.fn().mockResolvedValueOnce(selectedAsset),
      bindAiProfile,
      getRecentAssets: vi.fn().mockReturnValue([]),
    })

    workspace.openCreateAsset()
    expect(workspace.assetCreateOpen.value).toBe(true)
    workspace.closeCreateAsset()
    expect(workspace.assetCreateOpen.value).toBe(false)

    await workspace.handleListSelectAsset('weather-api')
    workspace.openAssetEditor()
    expect(workspace.assetEditorOpen.value).toBe(true)

    workspace.aiProfileForm.value = {
      provider: 'OpenAI',
      model: 'gpt-test',
      streamingSupported: true,
      capabilityTags: ['chat'],
    }
    await workspace.handleBindAiProfile()

    expect(workspace.currentAsset.value?.upstreamUrl).toBe('https://upstream.example.com/ai')
    expect(workspace.currentAsset.value?.requestTemplate).toBe('{"messages":"{{messages}}"}')
    expect(workspace.currentAsset.value?.requestExample).toBe('{"messages":["hi"]}')
    expect(workspace.currentAsset.value?.responseExample).toBe('{"reply":"hello"}')
    expect(workspace.assetConfigForm.value.upstreamUrl).toBe('https://upstream.example.com/ai')
    expect(workspace.currentAsset.value?.aiProfile?.model).toBe('gpt-test')
  })
})
