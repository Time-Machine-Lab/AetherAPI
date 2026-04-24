import { describe, expect, it, vi } from 'vitest'

vi.mock('@/api/catalog/discovery.api', () => ({
  listDiscoveryAssets: vi.fn(),
  getDiscoveryAssetDetail: vi.fn(),
}))

import { useCatalogDiscovery } from './useCatalogDiscovery'
import type { DiscoveryAsset, DiscoveryAssetDetail, PageResult } from '@/api/catalog/catalog.types'

function createDiscoveryAsset(overrides: Partial<DiscoveryAsset> = {}): DiscoveryAsset {
  return {
    apiCode: 'weather-api',
    displayName: 'Weather API',
    assetType: 'STANDARD_API',
    categoryCode: 'tools',
    categoryName: 'Tools',
    ...overrides,
  }
}

function createDetail(overrides: Partial<DiscoveryAssetDetail> = {}): DiscoveryAssetDetail {
  return {
    ...createDiscoveryAsset(),
    authScheme: 'NONE',
    requestMethod: 'GET',
    exampleSnapshot: { requestExample: '{"city":"Shanghai"}' },
    ...overrides,
  }
}

function createPage(items: DiscoveryAsset[]): PageResult<DiscoveryAsset> {
  return { items, total: items.length, page: 1, pageSize: 20 }
}

describe('useCatalogDiscovery', () => {
  it('loads market assets with optional keyword and clears loading state', async () => {
    const asset = createDiscoveryAsset()
    const listAssets = vi.fn().mockResolvedValueOnce(createPage([asset]))
    const discovery = useCatalogDiscovery({
      listAssets,
      getDetail: vi.fn(),
      pushRecent: vi.fn(),
    })
    discovery.keyword.value = 'weather'

    await discovery.loadList()

    expect(listAssets).toHaveBeenCalledWith({ keyword: 'weather' })
    expect(discovery.assets.value).toEqual([asset])
    expect(discovery.listLoading.value).toBe(false)
    expect(discovery.listError.value).toBe(false)
  })

  it('records recent access and exposes detail failures without losing selected asset', async () => {
    const asset = createDiscoveryAsset({ apiCode: 'broken-api' })
    const pushRecent = vi.fn()
    const discovery = useCatalogDiscovery({
      listAssets: vi.fn(),
      getDetail: vi.fn().mockRejectedValueOnce(new Error('detail failed')),
      pushRecent,
    })

    await discovery.selectAsset(asset)

    expect(pushRecent).toHaveBeenCalledWith(asset)
    expect(discovery.selectedAsset.value).toEqual(asset)
    expect(discovery.detail.value).toBeNull()
    expect(discovery.detailError.value).toBe(true)
    expect(discovery.detailLoading.value).toBe(false)
  })

  it('loads selected asset detail and resets stale error state', async () => {
    const asset = createDiscoveryAsset()
    const detail = createDetail({ description: 'Ready for Unified Access' })
    const discovery = useCatalogDiscovery({
      listAssets: vi.fn(),
      getDetail: vi.fn().mockResolvedValueOnce(detail),
      pushRecent: vi.fn(),
    })
    discovery.detailError.value = true

    await discovery.selectAsset(asset)

    expect(discovery.detail.value).toEqual(detail)
    expect(discovery.detailError.value).toBe(false)
  })
})
