import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
  },
}))

import { http } from '@/api/http'
import { disableAsset, enableAsset, getAsset, registerAsset, reviseAsset } from './asset.api'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPut = vi.mocked(http.put)
const mockedPatch = vi.mocked(http.patch)

describe('asset api', () => {
  beforeEach(() => {
    mockedGet.mockReset()
    mockedPost.mockReset()
    mockedPut.mockReset()
    mockedPatch.mockReset()
  })

  it('maps backend asset detail fields into frontend asset shape', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        apiCode: 'weather-api',
        assetName: 'Weather API',
        assetType: 'STANDARD_API',
        categoryCode: 'tools',
        status: 'ENABLED',
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
      displayName: 'Weather API',
      assetType: 'STANDARD_API',
      categoryCode: 'tools',
    })

    expect(mockedPost).toHaveBeenCalledWith('v1/assets', {
      apiCode: 'weather-api',
      assetName: 'Weather API',
      assetType: 'STANDARD_API',
      categoryCode: 'tools',
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

    expect(mockedPut).toHaveBeenCalledWith('v1/assets/weather-api', {
      assetName: 'Weather API',
      categoryCode: 'tools',
      requestMethod: 'GET',
      upstreamUrl: 'https://upstream.example.com/weather',
    })
  })

  it('uses backend patch contract for enable and disable actions', async () => {
    mockedPatch.mockResolvedValue({
      data: {
        apiCode: 'weather-api',
        assetName: 'Weather API',
        assetType: 'STANDARD_API',
        categoryCode: 'tools',
        status: 'ENABLED',
      },
    })

    await enableAsset('weather-api')
    await disableAsset('weather-api')

    expect(mockedPatch).toHaveBeenNthCalledWith(1, 'v1/assets/weather-api/enable')
    expect(mockedPatch).toHaveBeenNthCalledWith(2, 'v1/assets/weather-api/disable')
  })
})
