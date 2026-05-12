import { computed, ref } from 'vue'
import { getDiscoveryAssetDetail } from '@/api/catalog/discovery.api'
import type { DiscoveryAsset, DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import {
  buildBatchDocFileName,
  buildMarketplaceDocsMarkdown,
  buildSingleApiDocFileName,
  buildSingleApiMarkdown,
  defaultCatalogDocLabels,
  downloadMarkdownFile,
  type CatalogDocExportFailure,
  type CatalogDocLabels,
} from '@/features/catalog/catalog-doc-export'

export type CatalogDocExportFeedback = 'idle' | 'success' | 'partial' | 'failed'

interface CatalogDocExportDeps {
  getDetail?: typeof getDiscoveryAssetDetail
  download?: (content: string, fileName: string) => void
  now?: () => Date
  getLabels?: () => CatalogDocLabels
}

const defaultDeps: Required<CatalogDocExportDeps> = {
  getDetail: getDiscoveryAssetDetail,
  download: downloadMarkdownFile,
  now: () => new Date(),
  getLabels: () => defaultCatalogDocLabels,
}

export function useCatalogDocExport(deps: CatalogDocExportDeps = {}) {
  const resolvedDeps = { ...defaultDeps, ...deps }
  const selectedApiCodes = ref<string[]>([])
  const exporting = ref(false)
  const exportFeedback = ref<CatalogDocExportFeedback>('idle')
  const lastFailureCount = ref(0)

  const selectedCount = computed(() => selectedApiCodes.value.length)
  const selectedApiCodeSet = computed(() => new Set(selectedApiCodes.value))

  function resetExportFeedback() {
    exportFeedback.value = 'idle'
    lastFailureCount.value = 0
  }

  function toggleExportSelection(assetOrCode: DiscoveryAsset | string) {
    const apiCode = typeof assetOrCode === 'string' ? assetOrCode : assetOrCode.apiCode
    const next = selectedApiCodes.value.filter((code) => code !== apiCode)
    if (next.length === selectedApiCodes.value.length) {
      next.push(apiCode)
    }
    selectedApiCodes.value = next
    resetExportFeedback()
  }

  function clearExportSelection() {
    selectedApiCodes.value = []
    resetExportFeedback()
  }

  function isSelectedForExport(apiCode: string): boolean {
    return selectedApiCodeSet.value.has(apiCode)
  }

  async function exportCurrentDetail(detail: DiscoveryAssetDetail | null | undefined) {
    if (exporting.value || !detail) {
      return
    }

    exporting.value = true
    resetExportFeedback()
    try {
      const generatedAt = resolvedDeps.now()
      const markdown = buildSingleApiMarkdown(detail, {
        labels: resolvedDeps.getLabels(),
        generatedAt,
      })
      resolvedDeps.download(markdown, buildSingleApiDocFileName(detail.apiCode))
      exportFeedback.value = 'success'
    } catch {
      exportFeedback.value = 'failed'
    } finally {
      exporting.value = false
    }
  }

  async function exportSelectedDocs() {
    if (exporting.value || selectedApiCodes.value.length === 0) {
      return
    }

    exporting.value = true
    resetExportFeedback()

    const details: DiscoveryAssetDetail[] = []
    const failures: CatalogDocExportFailure[] = []

    for (const apiCode of selectedApiCodes.value) {
      try {
        details.push(await resolvedDeps.getDetail(apiCode))
      } catch {
        failures.push({ apiCode, reason: resolvedDeps.getLabels().detailLoadFailed })
      }
    }

    lastFailureCount.value = failures.length

    try {
      if (details.length === 0) {
        exportFeedback.value = 'failed'
        return
      }

      const generatedAt = resolvedDeps.now()
      const markdown = buildMarketplaceDocsMarkdown({
        details,
        failures,
        labels: resolvedDeps.getLabels(),
        generatedAt,
      })
      resolvedDeps.download(markdown, buildBatchDocFileName(generatedAt))
      exportFeedback.value = failures.length > 0 ? 'partial' : 'success'
    } catch {
      exportFeedback.value = 'failed'
    } finally {
      exporting.value = false
    }
  }

  return {
    selectedApiCodes,
    selectedCount,
    exporting,
    exportFeedback,
    lastFailureCount,
    toggleExportSelection,
    clearExportSelection,
    isSelectedForExport,
    exportCurrentDetail,
    exportSelectedDocs,
    resetExportFeedback,
  }
}
