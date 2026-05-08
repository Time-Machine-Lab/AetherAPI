import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

import { http } from '@/api/http'
import {
  bindProxyProfileToAsset,
  createPlatformProxyProfile,
  deletePlatformProxyProfile,
  disablePlatformProxyProfile,
  enablePlatformProxyProfile,
  getPlatformProxyProfile,
  listPlatformProxyAssetCandidates,
  listPlatformProxyProfiles,
  unbindProxyProfileFromAsset,
  updatePlatformProxyProfile,
} from './platform-proxy-profile.api'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPut = vi.mocked(http.put)
const mockedPatch = vi.mocked(http.patch)
const mockedDelete = vi.mocked(http.delete)

describe('platform proxy profile api', () => {
  beforeEach(() => {
    mockedGet.mockReset()
    mockedPost.mockReset()
    mockedPut.mockReset()
    mockedPatch.mockReset()
    mockedDelete.mockReset()
  })

  it('maps list query and response through the platform contract', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        items: [
          {
            id: 'proxy-1',
            profileCode: 'corp-egress',
            profileName: 'Corporate egress',
            proxyType: 'HTTP',
            proxyHost: 'proxy.internal',
            proxyPort: 8080,
            username: 'operator',
            credentialConfigured: true,
            enabled: true,
            deleted: false,
            createdAt: '2026-05-08T00:00:00Z',
            updatedAt: '2026-05-08T01:00:00Z',
          },
        ],
        page: 2,
        size: 10,
        total: 11,
      },
    })

    const result = await listPlatformProxyProfiles({
      enabled: true,
      keyword: 'corp',
      page: 2,
      size: 10,
    })

    expect(mockedGet).toHaveBeenCalledWith('v1/platform/proxy-profiles', {
      params: { enabled: true, keyword: 'corp', page: 2, size: 10 },
    })
    expect(result.items[0]).toEqual(
      expect.objectContaining({
        id: 'proxy-1',
        profileCode: 'corp-egress',
        proxyHost: 'proxy.internal',
        proxyPort: 8080,
        credentialConfigured: true,
        enabled: true,
      }),
    )
    expect(result.pageSize).toBe(10)
    expect(result.total).toBe(11)
  })

  it('uses profile detail and lifecycle endpoints', async () => {
    mockedGet.mockResolvedValueOnce({ data: { id: 'proxy-1', profileCode: 'corp-egress' } })
    mockedPatch.mockResolvedValue({ data: { id: 'proxy-1', enabled: true } })
    mockedDelete.mockResolvedValueOnce({ data: { id: 'proxy-1', deleted: true } })

    await getPlatformProxyProfile('proxy-1')
    await enablePlatformProxyProfile('proxy-1')
    await disablePlatformProxyProfile('proxy-1')
    await deletePlatformProxyProfile('proxy-1')

    expect(mockedGet).toHaveBeenCalledWith('v1/platform/proxy-profiles/proxy-1')
    expect(mockedPatch).toHaveBeenNthCalledWith(1, 'v1/platform/proxy-profiles/proxy-1/enable')
    expect(mockedPatch).toHaveBeenNthCalledWith(2, 'v1/platform/proxy-profiles/proxy-1/disable')
    expect(mockedDelete).toHaveBeenCalledWith('v1/platform/proxy-profiles/proxy-1')
  })

  it('maps create and update bodies without non-contract fields', async () => {
    mockedPost.mockResolvedValueOnce({ data: { id: 'proxy-1' } })
    mockedPut.mockResolvedValueOnce({ data: { id: 'proxy-1' } })

    const body = {
      profileCode: 'corp-egress',
      profileName: 'Corporate egress',
      proxyType: 'HTTP' as const,
      proxyHost: 'proxy.internal',
      proxyPort: 8080,
      username: 'operator',
      password: 'secret',
      enabled: true,
    }

    await createPlatformProxyProfile(body)
    await updatePlatformProxyProfile('proxy-1', body)

    expect(mockedPost).toHaveBeenCalledWith('v1/platform/proxy-profiles', body)
    expect(mockedPut).toHaveBeenCalledWith('v1/platform/proxy-profiles/proxy-1', body)
  })

  it('maps nullable asset binding fields', async () => {
    mockedPut.mockResolvedValueOnce({
      data: {
        apiCode: 'weather-api',
        proxyProfileId: 'proxy-1',
        proxyProfileCode: 'corp-egress',
        proxyProfileName: 'Corporate egress',
      },
    })
    mockedDelete.mockResolvedValueOnce({
      data: {
        apiCode: 'weather-api',
        proxyProfileId: null,
        proxyProfileCode: null,
        proxyProfileName: null,
      },
    })

    const bound = await bindProxyProfileToAsset('weather-api', { profileId: 'proxy-1' })
    const unbound = await unbindProxyProfileFromAsset('weather-api')

    expect(mockedPut).toHaveBeenCalledWith(
      'v1/platform/proxy-profiles/asset-bindings/weather-api',
      { profileId: 'proxy-1' },
    )
    expect(mockedDelete).toHaveBeenCalledWith(
      'v1/platform/proxy-profiles/asset-bindings/weather-api',
    )
    expect(bound.proxyProfileId).toBe('proxy-1')
    expect(unbound.proxyProfileId).toBeNull()
  })

  it('maps asset binding candidates with safe whitelisted fields only', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        items: [
          {
            apiCode: 'weather-api',
            assetName: 'Weather API',
            assetType: 'STANDARD_API',
            status: 'PUBLISHED',
            publisherDisplayName: 'Operations Team',
            proxyProfileId: 'proxy-1',
            proxyProfileCode: 'corp-egress',
            proxyProfileName: 'Corporate egress',
            createdAt: '2026-05-08T00:00:00Z',
            updatedAt: '2026-05-08T01:00:00Z',
            proxyHost: 'proxy.internal',
            proxyPort: 8080,
            username: 'operator',
            password: 'secret',
            authConfig: 'Authorization: Bearer token',
            requestTemplate: '{"secret":true}',
            requestExample: '{"token":"secret"}',
          },
        ],
        page: 2,
        size: 5,
        total: 8,
      },
    })

    const result = await listPlatformProxyAssetCandidates({
      keyword: 'weather',
      status: 'PUBLISHED',
      boundProfileId: 'proxy-1',
      page: 2,
      size: 5,
    })

    expect(mockedGet).toHaveBeenCalledWith('v1/platform/proxy-profiles/asset-binding-candidates', {
      params: {
        keyword: 'weather',
        status: 'PUBLISHED',
        boundProfileId: 'proxy-1',
        page: 2,
        size: 5,
      },
    })
    expect(result).toEqual({
      items: [
        {
          apiCode: 'weather-api',
          assetName: 'Weather API',
          assetType: 'STANDARD_API',
          status: 'PUBLISHED',
          publisherDisplayName: 'Operations Team',
          proxyProfileId: 'proxy-1',
          proxyProfileCode: 'corp-egress',
          proxyProfileName: 'Corporate egress',
          createdAt: '2026-05-08T00:00:00Z',
          updatedAt: '2026-05-08T01:00:00Z',
        },
      ],
      page: 2,
      pageSize: 5,
      total: 8,
    })
    const candidate = result.items[0]
    expect(candidate).not.toHaveProperty('proxyHost')
    expect(candidate).not.toHaveProperty('proxyPort')
    expect(candidate).not.toHaveProperty('username')
    expect(candidate).not.toHaveProperty('password')
    expect(candidate).not.toHaveProperty('authConfig')
    expect(candidate).not.toHaveProperty('requestTemplate')
    expect(candidate).not.toHaveProperty('requestExample')
  })
})
