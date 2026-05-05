import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useUnifiedAccessPlayground } from './useUnifiedAccessPlayground'
import { getDiscoveryAssetDetail, listDiscoveryAssets } from '@/api/catalog/discovery.api'
import { invokeUnifiedAccess } from '@/api/unified-access/unified-access.api'
import { getCurrentUserApiSubscriptionStatus } from '@/api/subscription/subscription.api'
import type { DiscoveryAsset, DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import type { UnifiedAccessResult } from '@/api/unified-access/unified-access.types'

vi.mock('@/api/catalog/discovery.api', () => ({
  listDiscoveryAssets: vi.fn(),
  getDiscoveryAssetDetail: vi.fn(),
}))

vi.mock('@/api/unified-access/unified-access.api', () => ({
  invokeUnifiedAccess: vi.fn(),
}))

vi.mock('@/api/subscription/subscription.api', () => ({
  getCurrentUserApiSubscriptionStatus: vi.fn(),
}))

const mockedListDiscoveryAssets = vi.mocked(listDiscoveryAssets)
const mockedGetDiscoveryAssetDetail = vi.mocked(getDiscoveryAssetDetail)
const mockedInvokeUnifiedAccess = vi.mocked(invokeUnifiedAccess)
const mockedGetCurrentUserApiSubscriptionStatus = vi.mocked(getCurrentUserApiSubscriptionStatus)

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
    requestMethod: 'GET',
    exampleSnapshot: { requestExample: '{"city":"Shanghai"}' },
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
    mockedGetCurrentUserApiSubscriptionStatus.mockReset()
    mockedGetCurrentUserApiSubscriptionStatus.mockResolvedValue({
      apiCode: 'weather-api',
      accessStatus: 'NOT_SUBSCRIBED',
      subscriptionId: null,
      subscriptionStatus: null,
      subscribed: false,
      ownerAccess: false,
    })
  })

  it('loads discovery assets and pre-fills method/body from selected asset detail', async () => {
    mockedListDiscoveryAssets.mockResolvedValueOnce({
      items: [asset()],
      total: 1,
      page: 1,
      pageSize: 200,
    })
    mockedGetDiscoveryAssetDetail.mockResolvedValueOnce(detail({ requestMethod: 'POST' }))
    mockedGetCurrentUserApiSubscriptionStatus.mockResolvedValueOnce({
      apiCode: 'weather-api',
      accessStatus: 'SUBSCRIBED',
      subscriptionId: 'sub-1',
      subscriptionStatus: 'ACTIVE',
      subscribed: true,
      ownerAccess: false,
    })
    const playground = useUnifiedAccessPlayground()

    await playground.loadDiscoveryAssets()
    await playground.selectDiscoveryAsset(asset())

    expect(mockedListDiscoveryAssets).toHaveBeenCalledWith({ pageSize: 200 })
    expect(playground.discoveryAssets.value).toHaveLength(1)
    expect(playground.apiCode.value).toBe('weather-api')
    expect(playground.subscriptionStatus.value?.accessStatus).toBe('SUBSCRIBED')
    expect(playground.method.value).toBe('POST')
    expect(playground.requestBody.value).toBe('{"city":"Shanghai"}')
  })

  it('loads selected detail directly from an apiCode for routed playground context', async () => {
    mockedGetDiscoveryAssetDetail.mockResolvedValueOnce(detail({ requestMethod: 'PATCH' }))
    mockedGetCurrentUserApiSubscriptionStatus.mockResolvedValueOnce({
      apiCode: 'weather-api',
      accessStatus: 'OWNER',
      subscriptionId: null,
      subscriptionStatus: null,
      subscribed: false,
      ownerAccess: true,
    })
    const playground = useUnifiedAccessPlayground()

    await playground.loadSelectedAssetDetail(' weather-api ')

    expect(mockedGetDiscoveryAssetDetail).toHaveBeenCalledWith('weather-api')
    expect(playground.apiCode.value).toBe('weather-api')
    expect(playground.method.value).toBe('PATCH')
    expect(playground.subscriptionStatus.value?.accessStatus).toBe('OWNER')
    expect(playground.requestBody.value).toBe('{"city":"Shanghai"}')
  })

  it('keeps manual invocation available when subscription status cannot load', async () => {
    mockedGetDiscoveryAssetDetail.mockResolvedValueOnce(detail())
    mockedGetCurrentUserApiSubscriptionStatus.mockRejectedValueOnce(new Error('status failed'))
    const playground = useUnifiedAccessPlayground()

    await playground.loadSelectedAssetDetail('weather-api')

    expect(playground.selectedAssetDetail.value?.apiCode).toBe('weather-api')
    expect(playground.subscriptionStatus.value).toBeNull()
    expect(playground.subscriptionStatusError.value).toBe(true)
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

  it('exposes subscription-required platform failures as invocation results', async () => {
    mockedInvokeUnifiedAccess.mockResolvedValueOnce(
      jsonResult({
        kind: 'platform-failure',
        status: 403,
        platformFailure: {
          code: 'API_SUBSCRIPTION_REQUIRED',
          message: 'API subscription is required: weather-api',
          failureType: 'SUBSCRIPTION_REQUIRED',
          traceId: 'trace-sub-required',
          apiCode: 'weather-api',
        },
      }),
    )
    const playground = useUnifiedAccessPlayground()
    playground.apiCode.value = 'weather-api'
    playground.apiKey.value = 'ak_live_valid'
    playground.method.value = 'GET'

    await playground.invoke()

    expect(playground.result.value?.kind).toBe('platform-failure')
    expect(playground.result.value?.platformFailure?.failureType).toBe('SUBSCRIPTION_REQUIRED')
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
