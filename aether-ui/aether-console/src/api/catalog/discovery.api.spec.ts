import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/http', () => ({
  http: {
    get: vi.fn(),
  },
}))

import { http } from '@/api/http'
import { getDiscoveryAssetDetail, listDiscoveryAssets } from './discovery.api'

const mockedGet = vi.mocked(http.get)

describe('discovery api', () => {
  beforeEach(() => {
    mockedGet.mockReset()
  })

  it('maps discovery list response and forwards query params', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        items: [
          {
            apiCode: 'chat-completions',
            assetName: 'Chat Completions',
            assetType: 'AI_API',
            category: { categoryCode: 'ai', categoryName: 'AI' },
            publisher: { displayName: 'Ada Publisher' },
            publishedAt: '2026-04-26T12:00:00Z',
          },
        ],
        page: 1,
        pageSize: 10,
        total: 1,
      },
    })

    const result = await listDiscoveryAssets({
      page: 1,
      pageSize: 10,
      keyword: 'chat',
      categoryCode: 'ai',
    })

    expect(mockedGet).toHaveBeenCalledWith('v1/discovery/assets', {
      params: {
        page: 1,
        pageSize: 10,
        keyword: 'chat',
        categoryCode: 'ai',
      },
    })
    expect(result).toEqual({
      items: [
        {
          apiCode: 'chat-completions',
          displayName: 'Chat Completions',
          assetType: 'AI_API',
          categoryCode: 'ai',
          categoryName: 'AI',
          publisherDisplayName: 'Ada Publisher',
          publishedAt: '2026-04-26T12:00:00Z',
        },
      ],
      total: 1,
      page: 1,
      pageSize: 10,
    })
  })

  it('tolerates discovery list responses without pagination metadata', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        items: [
          {
            apiCode: 'weather-api',
            assetName: 'Weather API',
            assetType: 'STANDARD_API',
            category: null,
            publisher: null,
          },
        ],
      },
    })

    const result = await listDiscoveryAssets()

    expect(result.total).toBe(1)
    expect(result.page).toBe(1)
    expect(result.pageSize).toBe(1)
    expect(result.items[0].publisherDisplayName).toBeUndefined()
  })

  it('maps discovery detail response', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        apiCode: 'chat-completions',
        assetName: 'Chat Completions',
        assetType: 'AI_API',
        category: { categoryCode: 'ai', categoryName: 'AI' },
        publisher: { displayName: 'Ada Publisher' },
        publishedAt: '2026-04-26T12:00:00Z',
        description: 'LLM chat completion endpoint',
        authScheme: 'HEADER_TOKEN',
        requestMethod: 'POST',
        requestTemplate: '{"model":"gpt-4.1"}',
        exampleSnapshot: { requestExample: '{"messages":[]}' },
        aiCapabilityProfile: {
          provider: 'OpenAI',
          model: 'gpt-4.1',
          streamingSupported: true,
          capabilityTags: ['chat', 'llm'],
        },
      },
    })

    const result = await getDiscoveryAssetDetail('chat-completions')

    expect(mockedGet).toHaveBeenCalledWith('v1/discovery/assets/chat-completions')
    expect(result.aiProfile?.provider).toBe('OpenAI')
    expect(result.requestMethod).toBe('POST')
    expect(result.publisherDisplayName).toBe('Ada Publisher')
    expect(result.publishedAt).toBe('2026-04-26T12:00:00Z')
  })
})
