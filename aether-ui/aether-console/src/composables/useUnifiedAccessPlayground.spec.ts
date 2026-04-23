import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useUnifiedAccessPlayground } from './useUnifiedAccessPlayground'
import { getDiscoveryAssetDetail, listDiscoveryAssets } from '@/api/catalog/discovery.api'
import { invokeUnifiedAccess } from '@/api/unified-access/unified-access.api'
import type { DiscoveryAsset, DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import type { UnifiedAccessResult } from '@/api/unified-access/unified-access.types'

vi.mock('@/api/catalog/discovery.api', () => ({
  listDiscoveryAssets: vi.fn(),
  getDiscoveryAssetDetail: vi.fn(),
}))

vi.mock('@/api/unified-access/unified-access.api', () => ({
  invokeUnifiedAccess: vi.fn(),
}))

const mockedListDiscoveryAssets = vi.mocked(listDiscoveryAssets)
const mockedGetDiscoveryAssetDetail = vi.mocked(getDiscoveryAssetDetail)
const mockedInvokeUnifiedAccess = vi.mocked(invokeUnifiedAccess)

function asset(overrides: Partial<DiscoveryAsset> = {}): DiscoveryAsset {
  return {
    apiCode: 'weather-api',
    displayName: 'Weather API',
    assetType: 'STANDARD_API',
    categoryCode: 'tools',
    ...overrides,
  }
}

function detail(overrides: Partial<DiscoveryAssetDetail> = {}): DiscoveryAssetDetail {
  return {
    ...asset(),
    methods: ['GET'],
    exampleSnapshot: '{"city":"Shanghai"}',
    authScheme: 'NONE',
    ...overrides,
  }
}

function jsonResult(overrides: Partial<UnifiedAccessResult> = {}): UnifiedAccessResult {
  return {
    kind: 'json',
    status: 200,
    contentType: 'application/json',
    jsonBody: { ok: true },
    rawHeaders: { 'content-type': 'application/json' },
    ...overrides,
  }
}

describe('useUnifiedAccessPlayground', () => {
  beforeEach(() => {
    mockedListDiscoveryAssets.mockReset()
    mockedGetDiscoveryAssetDetail.mockReset()
    mockedInvokeUnifiedAccess.mockReset()
  })

  it('loads discovery assets and pre-fills method/body from selected asset detail', async () => {
    mockedListDiscoveryAssets.mockResolvedValueOnce({
      items: [asset()],
      total: 1,
      page: 1,
      pageSize: 200,
    })
    mockedGetDiscoveryAssetDetail.mockResolvedValueOnce(detail({ methods: ['POST'] }))
    const playground = useUnifiedAccessPlayground()

    await playground.loadDiscoveryAssets()
    await playground.selectDiscoveryAsset(asset())

    expect(mockedListDiscoveryAssets).toHaveBeenCalledWith({ pageSize: 200 })
    expect(playground.discoveryAssets.value).toHaveLength(1)
    expect(playground.apiCode.value).toBe('weather-api')
    expect(playground.method.value).toBe('POST')
    expect(playground.requestBody.value).toBe('{"city":"Shanghai"}')
  })

  it('invokes Unified Access with parsed extra headers and preserves success response', async () => {
    mockedInvokeUnifiedAccess.mockResolvedValueOnce(jsonResult())
    const playground = useUnifiedAccessPlayground()
    playground.apiCode.value = 'weather-api'
    playground.apiKey.value = 'ak_live_valid'
    playground.method.value = 'POST'
    playground.requestBody.value = '{"city":"Shanghai"}'
    playground.extraHeaders.value = '{"Accept":"application/json"}'

    await playground.invoke()

    expect(mockedInvokeUnifiedAccess).toHaveBeenCalledWith(
      'weather-api',
      'POST',
      'ak_live_valid',
      '{"city":"Shanghai"}',
      { Accept: 'application/json' },
    )
    expect(playground.result.value?.kind).toBe('json')
    expect(playground.invokeError.value).toBe('')
    expect(playground.invoking.value).toBe(false)
  })

  it('shows platform failures without clearing the console session concept', async () => {
    mockedInvokeUnifiedAccess.mockResolvedValueOnce(
      jsonResult({
        kind: 'platform-failure',
        status: 404,
        platformFailure: {
          code: 'ASSET_NOT_FOUND',
          message: 'Asset not found: unknown-api',
          failureType: 'TARGET_NOT_FOUND',
          traceId: 'trace-target-not-found',
          apiCode: 'unknown-api',
        },
      }),
    )
    const playground = useUnifiedAccessPlayground()
    playground.apiCode.value = 'unknown-api'
    playground.apiKey.value = 'ak_live_valid'
    playground.method.value = 'GET'

    await playground.invoke()

    expect(playground.result.value?.kind).toBe('platform-failure')
    expect(playground.result.value?.platformFailure?.failureType).toBe('TARGET_NOT_FOUND')
  })

  it('blocks invocation on invalid extra header JSON and keeps result empty', async () => {
    const playground = useUnifiedAccessPlayground()
    playground.apiCode.value = 'weather-api'
    playground.apiKey.value = 'ak_live_valid'
    playground.extraHeaders.value = '{invalid-json'

    await playground.invoke()

    expect(mockedInvokeUnifiedAccess).not.toHaveBeenCalled()
    expect(playground.invokeError.value).toBe('Invalid JSON in extra headers')
    expect(playground.result.value).toBeNull()
  })

  it('clears API key, result, and form state independently', async () => {
    const playground = useUnifiedAccessPlayground()
    playground.apiCode.value = 'weather-api'
    playground.apiKey.value = 'ak_live_valid'
    playground.result.value = jsonResult()

    playground.clearApiKey()
    expect(playground.apiKey.value).toBe('')

    playground.clearResult()
    expect(playground.result.value).toBeNull()

    playground.resetForm()
    expect(playground.apiCode.value).toBe('')
    expect(playground.method.value).toBe('POST')
  })
})
