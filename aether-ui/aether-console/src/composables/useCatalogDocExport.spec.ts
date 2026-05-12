import { describe, expect, it, vi } from 'vitest'
import type { DiscoveryAsset, DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import { defaultCatalogDocLabels } from '@/features/catalog/catalog-doc-export'

vi.mock('@/api/catalog/discovery.api', () => ({
  getDiscoveryAssetDetail: vi.fn(),
}))

import { useCatalogDocExport } from './useCatalogDocExport'

function asset(apiCode: string): DiscoveryAsset {
  return {
    apiCode,
    displayName: apiCode,
    assetType: 'STANDARD_API',
    categoryCode: 'tools',
  }
}

function detail(apiCode: string): DiscoveryAssetDetail {
  return {
    ...asset(apiCode),
    requestMethod: 'GET',
    authScheme: 'NONE',
  }
}

describe('useCatalogDocExport', () => {
  it('keeps export selection independent from current detail selection', () => {
    const exporter = useCatalogDocExport({
      getDetail: vi.fn(),
      download: vi.fn(),
      now: () => new Date('2026-05-12T04:00:00Z'),
      getLabels: () => defaultCatalogDocLabels,
    })

    exporter.toggleExportSelection(asset('weather-api'))
    exporter.toggleExportSelection(asset('chat-api'))

    expect(exporter.selectedApiCodes.value).toEqual(['weather-api', 'chat-api'])
    expect(exporter.selectedCount.value).toBe(2)
    expect(exporter.isSelectedForExport('weather-api')).toBe(true)

    exporter.clearExportSelection()

    expect(exporter.selectedApiCodes.value).toEqual([])
    expect(exporter.selectedCount.value).toBe(0)
  })

  it('exports the current detail as a single markdown file', async () => {
    const download = vi.fn()
    const exporter = useCatalogDocExport({
      getDetail: vi.fn(),
      download,
      now: () => new Date('2026-05-12T04:00:00Z'),
      getLabels: () => defaultCatalogDocLabels,
    })

    await exporter.exportCurrentDetail(detail('weather-api'))

    expect(download).toHaveBeenCalledWith(
      expect.stringContaining('# weather-api'),
      'aetherapi-weather-api-doc.md',
    )
    expect(exporter.exportFeedback.value).toBe('success')
    expect(exporter.exporting.value).toBe(false)
  })

  it('exports selected details in selection order and records partial failures in the file', async () => {
    const getDetail = vi
      .fn()
      .mockResolvedValueOnce(detail('first-api'))
      .mockRejectedValueOnce(new Error('broken'))
      .mockResolvedValueOnce(detail('third-api'))
    const download = vi.fn()
    const exporter = useCatalogDocExport({
      getDetail,
      download,
      now: () => new Date('2026-05-12T04:00:00Z'),
      getLabels: () => defaultCatalogDocLabels,
    })
    exporter.toggleExportSelection(asset('first-api'))
    exporter.toggleExportSelection(asset('broken-api'))
    exporter.toggleExportSelection(asset('third-api'))

    await exporter.exportSelectedDocs()

    expect(getDetail).toHaveBeenNthCalledWith(1, 'first-api')
    expect(getDetail).toHaveBeenNthCalledWith(2, 'broken-api')
    expect(getDetail).toHaveBeenNthCalledWith(3, 'third-api')
    expect(download).toHaveBeenCalledWith(
      expect.stringMatching(/broken-api[\s\S]*first-api[\s\S]*third-api/),
      'aetherapi-market-docs-2026-05-12.md',
    )
    expect(exporter.exportFeedback.value).toBe('partial')
    expect(exporter.lastFailureCount.value).toBe(1)
  })

  it('does not download an empty batch when all selected details fail', async () => {
    const exporter = useCatalogDocExport({
      getDetail: vi.fn().mockRejectedValue(new Error('down')),
      download: vi.fn(),
      now: () => new Date('2026-05-12T04:00:00Z'),
      getLabels: () => defaultCatalogDocLabels,
    })
    exporter.toggleExportSelection(asset('first-api'))
    exporter.toggleExportSelection(asset('second-api'))

    await exporter.exportSelectedDocs()

    expect(exporter.exportFeedback.value).toBe('failed')
    expect(exporter.lastFailureCount.value).toBe(2)
  })

  it('ignores repeated export triggers while exporting', async () => {
    let resolveDetail!: (detail: DiscoveryAssetDetail) => void
    const getDetail = vi.fn(
      () =>
        new Promise<DiscoveryAssetDetail>((resolve) => {
          resolveDetail = resolve
        }),
    )
    const exporter = useCatalogDocExport({
      getDetail,
      download: vi.fn(),
      now: () => new Date('2026-05-12T04:00:00Z'),
      getLabels: () => defaultCatalogDocLabels,
    })
    exporter.toggleExportSelection(asset('first-api'))

    const first = exporter.exportSelectedDocs()
    const second = exporter.exportSelectedDocs()

    expect(exporter.exporting.value).toBe(true)
    expect(getDetail).toHaveBeenCalledTimes(1)

    resolveDetail(detail('first-api'))
    await first
    await second

    expect(getDetail).toHaveBeenCalledTimes(1)
  })
})
