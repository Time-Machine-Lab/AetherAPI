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
  listAssets,
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
        authScheme: 'HEADER_TOKEN',
        authConfig: 'Authorization: Bearer upstream-token',
        requestJsonSchema: '{"type":"object","required":["city"]}',
        responseJsonSchema: '{"type":"object","properties":{"temp":{"type":"number"}}}',
        asyncTaskConfig: {
          enabled: true,
          queryMethod: 'GET',
          queryUrlTemplate: 'http://provider.example.com/v1/tasks/{taskId}',
          authMode: 'SAME_AS_SUBMIT',
          authScheme: null,
          authConfig: null,
          statusPath: '$.data.status',
          resultPath: '$.data.result',
          errorPath: '$.data.error',
        },
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
        authScheme: 'HEADER_TOKEN',
        authConfig: 'Authorization: Bearer upstream-token',
        requestJsonSchema: '{"type":"object","required":["city"]}',
        responseJsonSchema: '{"type":"object","properties":{"temp":{"type":"number"}}}',
        asyncTaskConfig: {
          enabled: true,
          queryMethod: 'GET',
          queryUrlTemplate: 'http://provider.example.com/v1/tasks/{taskId}',
          authMode: 'SAME_AS_SUBMIT',
          authScheme: null,
          authConfig: null,
          statusPath: '$.data.status',
          resultPath: '$.data.result',
          errorPath: '$.data.error',
        },
        aiProfile: {
          provider: 'OpenAI',
          model: 'gpt-4.1',
          streaming: true,
          tags: ['chat', 'tools'],
        },
      }),
    )
  })

  it('does not map platform proxy metadata into current-user asset shape', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        apiCode: 'weather-api',
        assetName: 'Weather API',
        assetType: 'STANDARD_API',
        categoryCode: 'tools',
        status: 'PUBLISHED',
        proxyProfileId: 'proxy-1',
        proxyHost: 'proxy.internal',
        proxyPort: 8080,
        proxyUsername: 'operator',
        proxyPassword: 'secret',
      },
    })

    const result = await getAsset('weather-api')

    expect(result).not.toHaveProperty('proxyProfileId')
    expect(result).not.toHaveProperty('proxyHost')
    expect(result).not.toHaveProperty('proxyPort')
    expect(result).not.toHaveProperty('proxyUsername')
    expect(result).not.toHaveProperty('proxyPassword')
  })

  it('maps asset summary async task query availability', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        items: [
          {
            apiCode: 'image-generate',
            assetName: 'Image Generate',
            assetType: 'AI_API',
            categoryCode: 'image',
            categoryName: 'Image',
            status: 'PUBLISHED',
            updatedAt: '2026-05-13T01:00:00Z',
            asyncTaskQueryEnabled: true,
          },
        ],
        page: 1,
        size: 10,
        total: 1,
      },
    })

    const result = await listAssets({ status: 'PUBLISHED' })

    expect(mockedGet).toHaveBeenCalledWith('v1/current-user/assets', {
      params: { status: 'PUBLISHED' },
    })
    expect(result.items[0]).toEqual(
      expect.objectContaining({
        apiCode: 'image-generate',
        assetName: 'Image Generate',
        asyncTaskQueryEnabled: true,
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
      requestJsonSchema: undefined,
      responseJsonSchema: undefined,
    })

    expect(mockedPost).toHaveBeenCalledWith('v1/current-user/assets', {
      apiCode: 'weather-api',
      assetName: 'Weather API',
      assetType: 'STANDARD_API',
      requestJsonSchema: undefined,
      responseJsonSchema: undefined,
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
      authScheme: 'HEADER_TOKEN',
      authConfig: 'Authorization: Bearer upstream-token',
      requestJsonSchema: '{"type":"object","required":["city"]}',
      responseJsonSchema: null,
      asyncTaskConfig: {
        enabled: true,
        queryMethod: 'GET',
        queryUrlTemplate: 'http://provider.example.com/v1/tasks/{taskId}',
        authMode: 'SAME_AS_SUBMIT',
        authScheme: null,
        authConfig: null,
        statusPath: '$.data.status',
        resultPath: '$.data.result',
        errorPath: '$.data.error',
      },
    })

    expect(mockedPut).toHaveBeenCalledWith('v1/current-user/assets/weather-api', {
      assetName: 'Weather API',
      assetType: undefined,
      categoryCode: 'tools',
      description: undefined,
      requestMethod: 'GET',
      upstreamUrl: 'https://upstream.example.com/weather',
      authScheme: 'HEADER_TOKEN',
      authConfig: 'Authorization: Bearer upstream-token',
      requestTemplate: undefined,
      requestExample: undefined,
      responseExample: undefined,
      requestJsonSchema: '{"type":"object","required":["city"]}',
      responseJsonSchema: null,
      asyncTaskConfig: {
        enabled: true,
        queryMethod: 'GET',
        queryUrlTemplate: 'http://provider.example.com/v1/tasks/{taskId}',
        authMode: 'SAME_AS_SUBMIT',
        authScheme: null,
        authConfig: null,
        statusPath: '$.data.status',
        resultPath: '$.data.result',
        errorPath: '$.data.error',
      },
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
