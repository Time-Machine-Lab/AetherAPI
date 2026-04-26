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
  deleteAsset,
  getAsset,
  publishAsset,
  registerAsset,
  reviseAsset,
  unpublishAsset,
} from './asset.api'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPut = vi.mocked(http.put)
const mockedPatch = vi.mocked(http.patch)
const mockedDelete = vi.mocked(http.delete)

describe('asset api', () => {
  beforeEach(() => {
    mockedGet.mockReset()
    mockedPost.mockReset()
    mockedPut.mockReset()
    mockedPatch.mockReset()
    mockedDelete.mockReset()
  })

  it('maps backend asset detail fields into frontend asset shape', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        apiCode: 'weather-api',
        assetName: 'Weather API',
        assetType: 'STANDARD_API',
        categoryCode: 'tools',
        status: 'PUBLISHED',
        publisherDisplayName: 'Ada',
        publishedAt: '2026-04-26T12:00:00Z',
        requestMethod: 'GET',
        upstreamUrl: 'https://upstream.example.com/weather',
        authScheme: 'NONE',
        aiCapabilityProfile: {
          provider: 'OpenAI',
          model: 'gpt-4.1',
          streamingSupported: true,
          capabilityTags: ['chat', 'tools'],
        },
      },
    })

    const result = await getAsset('weather-api')

    expect(result).toEqual(
      expect.objectContaining({
        apiCode: 'weather-api',
        displayName: 'Weather API',
        status: 'PUBLISHED',
        publisherDisplayName: 'Ada',
        publishedAt: '2026-04-26T12:00:00Z',
        requestMethod: 'GET',
        upstreamUrl: 'https://upstream.example.com/weather',
        authScheme: 'NONE',
        aiProfile: {
          provider: 'OpenAI',
          model: 'gpt-4.1',
          streaming: true,
          tags: ['chat', 'tools'],
        },
      }),
    )
  })

  it('maps frontend register payload to backend assetName contract', async () => {
    mockedPost.mockResolvedValueOnce({
      data: {
        apiCode: 'weather-api',
        assetName: 'Weather API',
        assetType: 'STANDARD_API',
        categoryCode: 'tools',
        status: 'DRAFT',
      },
    })

    await registerAsset({
      apiCode: 'weather-api',
      assetName: 'Weather API',
      assetType: 'STANDARD_API',
    })

    expect(mockedPost).toHaveBeenCalledWith('v1/current-user/assets', {
      apiCode: 'weather-api',
      assetName: 'Weather API',
      assetType: 'STANDARD_API',
    })
  })

  it('uses backend put contract when revising asset config', async () => {
    mockedPut.mockResolvedValueOnce({
      data: {
        apiCode: 'weather-api',
        assetName: 'Weather API',
        assetType: 'STANDARD_API',
        categoryCode: 'tools',
        status: 'DRAFT',
      },
    })

    await reviseAsset('weather-api', {
      displayName: 'Weather API',
      categoryCode: 'tools',
      requestMethod: 'GET',
      upstreamUrl: 'https://upstream.example.com/weather',
    })

    expect(mockedPut).toHaveBeenCalledWith('v1/current-user/assets/weather-api', {
      assetName: 'Weather API',
      assetType: undefined,
      categoryCode: 'tools',
      description: undefined,
      requestMethod: 'GET',
      upstreamUrl: 'https://upstream.example.com/weather',
      authScheme: undefined,
      requestTemplate: undefined,
      requestExample: undefined,
      responseExample: undefined,
    })
  })

  it('uses backend patch contract for publish and unpublish actions', async () => {
    mockedPatch.mockResolvedValue({
      data: {
        apiCode: 'weather-api',
        assetName: 'Weather API',
        assetType: 'STANDARD_API',
        categoryCode: 'tools',
        status: 'PUBLISHED',
      },
    })

    await publishAsset('weather-api')
    await unpublishAsset('weather-api')

    expect(mockedPatch).toHaveBeenNthCalledWith(1, 'v1/current-user/assets/weather-api/publish')
    expect(mockedPatch).toHaveBeenNthCalledWith(2, 'v1/current-user/assets/weather-api/unpublish')
  })

  it('uses current-user delete contract for owner soft delete', async () => {
    mockedDelete.mockResolvedValueOnce({
      data: {
        apiCode: 'weather-api',
        assetName: 'Weather API',
        assetType: 'STANDARD_API',
        categoryCode: 'tools',
        status: 'UNPUBLISHED',
        deleted: true,
      },
    })

    const result = await deleteAsset('weather-api')

    expect(mockedDelete).toHaveBeenCalledWith('v1/current-user/assets/weather-api')
    expect(result.deleted).toBe(true)
  })
})
