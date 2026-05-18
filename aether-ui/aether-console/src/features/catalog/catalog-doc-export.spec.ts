import { describe, expect, it, vi } from 'vitest'
import type { DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import {
  buildBatchDocFileName,
  buildMarketplaceDocsMarkdown,
  buildSingleApiDocFileName,
  buildSingleApiMarkdown,
  defaultCatalogDocLabels,
  downloadMarkdownFile,
} from './catalog-doc-export'

function detail(overrides: Partial<DiscoveryAssetDetail> = {}): DiscoveryAssetDetail {
  return {
    apiCode: 'weather-api',
    displayName: 'Weather API',
    assetType: 'STANDARD_API',
    categoryCode: 'tools',
    categoryName: 'Tools',
    publisherDisplayName: 'Ada Publisher',
    publishedAt: '2026-05-01T10:00:00Z',
    description: 'Forecast data for a requested city.',
    authScheme: 'HEADER_TOKEN',
    requestMethod: 'POST',
    requestTemplate: '{"city":"{{city}}"}',
    requestJsonSchema: '{"type":"object","required":["city"]}',
    exampleSnapshot: {
      requestExample: '{"city":"Shanghai"}',
      responseExample: '{"temp":26}',
    },
    responseJsonSchema: '{"type":"object","properties":{"temp":{"type":"number"}}}',
    ...overrides,
  }
}

describe('catalog doc export helpers', () => {
  it('builds a single markdown document from contract-backed discovery fields', () => {
    const markdown = buildSingleApiMarkdown(detail(), {
      labels: defaultCatalogDocLabels,
      generatedAt: new Date('2026-05-12T04:00:00Z'),
    })

    expect(markdown).toContain('# Weather API')
    expect(markdown).toContain('| API Code | weather-api |')
    expect(markdown).toContain('| Asset Type | STANDARD_API |')
    expect(markdown).toContain('| Platform Unified Access URL | /api/v1/access/weather-api |')
    expect(markdown).toContain('## Request Template')
    expect(markdown).toContain('```json\n{"city":"{{city}}"}\n```')
    expect(markdown).toContain('## Request Body Schema')
    expect(markdown).toContain('```json\n{"type":"object","required":["city"]}\n```')
    expect(markdown).toContain('## Response Example')
    expect(markdown).toContain('## Response Body Schema')
    expect(markdown).toContain(
      '```json\n{"type":"object","properties":{"temp":{"type":"number"}}}\n```',
    )
    expect(markdown).toContain('Generated At: 2026-05-12T04:00:00.000Z')
  })

  it('omits missing optional sections and never exports internal fields', () => {
    const markdown = buildSingleApiMarkdown(
      detail({
        description: undefined,
        requestTemplate: undefined,
        exampleSnapshot: undefined,
        // DiscoveryAssetDetail does not expose this field; this guards against accidental spread output.
        upstreamUrl: 'https://internal.example.test',
      } as Partial<DiscoveryAssetDetail>),
    )

    expect(markdown).not.toContain('## Description')
    expect(markdown).not.toContain('## Request Template')
    expect(markdown).not.toContain('internal.example.test')
    expect(markdown).not.toContain('upstreamUrl')
  })

  it('renders request and response schemas independently when only one schema is present', () => {
    const markdown = buildSingleApiMarkdown(
      detail({
        requestJsonSchema: '{"type":"object","required":["city"]}',
        responseJsonSchema: undefined,
      }),
    )

    expect(markdown).toContain('## Request Body Schema')
    expect(markdown).not.toContain('## Response Body Schema')
  })

  it('includes AI capability only for AI API details', () => {
    const markdown = buildSingleApiMarkdown(
      detail({
        apiCode: 'chat-api',
        displayName: 'Chat API',
        assetType: 'AI_API',
        aiProfile: {
          provider: 'OpenAI',
          model: 'gpt-4.1',
          streaming: true,
          tags: ['chat', 'reasoning'],
        },
      }),
    )

    expect(markdown).toContain('## AI Capability')
    expect(markdown).toContain('| Provider | OpenAI |')
    expect(markdown).toContain('| Model | gpt-4.1 |')
    expect(markdown).toContain('| Streaming | Yes |')
    expect(markdown).toContain('| Tags | chat, reasoning |')
  })

  it('includes async task query docs and derives response structure from paths', () => {
    const markdown = buildSingleApiMarkdown(
      detail({
        apiCode: 'image-generate',
        displayName: 'Image Generate',
        asyncTaskConfig: {
          enabled: true,
          queryMethod: 'GET',
          queryUrlTemplate: 'http://provider.example.com/v1/tasks/{taskId}',
          authMode: 'SAME_AS_SUBMIT',
          authScheme: 'HEADER_TOKEN',
          authConfig: '{"headerName":"Authorization","token":"secret"}',
          statusPath: '$.data.status',
          resultPath: '$.data.result',
          errorPath: '$.data.error',
        },
      }),
    )

    expect(markdown).toContain('## Async Task Query')
    expect(markdown).toContain(
      '| Task Query Endpoint | /api/v1/access/image-generate/tasks/{taskId} |',
    )
    expect(markdown).toContain('| Task Query Auth Mode | SAME_AS_SUBMIT |')
    expect(markdown).toContain('| Task Status Path | $.data.status |')
    expect(markdown).toContain('## Task Query Response Structure')
    expect(markdown).toContain('"status": "<Task Status Path>"')
    expect(markdown).toContain('"result": "<Task Result Path>"')
    expect(markdown).toContain('"error": "<Task Error Path>"')
    expect(markdown).not.toContain('secret')
    expect(markdown).not.toContain('authConfig')
  })

  it('keeps raw async task paths when response structure cannot be derived', () => {
    const markdown = buildSingleApiMarkdown(
      detail({
        asyncTaskConfig: {
          enabled: true,
          queryMethod: 'GET',
          authMode: 'SAME_AS_SUBMIT',
          statusPath: '$.items[*].status',
        },
      }),
    )

    expect(markdown).toContain('| Task Status Path | $.items[*].status |')
    expect(markdown).not.toContain('## Task Query Response Structure')
  })

  it('builds a merged markdown document that preserves success order and lists failures first', () => {
    const markdown = buildMarketplaceDocsMarkdown({
      details: [
        detail({ apiCode: 'first-api', displayName: 'First API' }),
        detail({ apiCode: 'second-api', displayName: 'Second API' }),
      ],
      failures: [{ apiCode: 'broken-api', reason: 'Detail failed' }],
      labels: defaultCatalogDocLabels,
      generatedAt: new Date('2026-05-12T04:00:00Z'),
    })

    expect(markdown.indexOf('## Failed Items')).toBeLessThan(markdown.indexOf('## First API'))
    expect(markdown).toContain('- `broken-api`: Detail failed')
    expect(markdown.indexOf('## First API')).toBeLessThan(markdown.indexOf('## Second API'))
    expect(markdown).toContain('Success: 2, Failed: 1')
  })

  it('generates safe markdown file names', () => {
    expect(buildSingleApiDocFileName('weather/api')).toBe('aetherapi-weather-api-doc.md')
    expect(buildBatchDocFileName(new Date('2026-05-12T04:00:00Z'))).toBe(
      'aetherapi-market-docs-2026-05-12.md',
    )
  })

  it('downloads markdown text through a browser object url', () => {
    const appendChild = vi.fn()
    const removeChild = vi.fn()
    const click = vi.fn()
    const createElement = vi.fn(() => ({ click }))
    const createObjectURL = vi.fn(() => 'blob:markdown')
    const revokeObjectURL = vi.fn()

    downloadMarkdownFile('hello', 'hello.md', {
      createObjectURL,
      revokeObjectURL,
      createElement,
      appendChild,
      removeChild,
    })

    expect(createObjectURL).toHaveBeenCalledWith(expect.any(Blob))
    expect(createElement).toHaveBeenCalledWith('a')
    expect(appendChild).toHaveBeenCalled()
    expect(click).toHaveBeenCalled()
    expect(removeChild).toHaveBeenCalled()
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:markdown')
  })
})
