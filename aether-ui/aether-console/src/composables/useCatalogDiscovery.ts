import { ref, watch } from 'vue'
import { getDiscoveryAssetDetail, listDiscoveryAssets } from '@/api/catalog/discovery.api'
import type { DiscoveryAsset, DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import { pushRecentAsset } from '@/features/catalog/catalog-helpers'

interface CatalogDiscoveryDeps {
  listAssets: typeof listDiscoveryAssets
  getDetail: typeof getDiscoveryAssetDetail
  pushRecent: typeof pushRecentAsset
}

const defaultDeps: CatalogDiscoveryDeps = {
  listAssets: listDiscoveryAssets,
  getDetail: getDiscoveryAssetDetail,
  pushRecent: pushRecentAsset,
}

export function useCatalogDiscovery(deps: CatalogDiscoveryDeps = defaultDeps) {
  const keyword = ref('')
  const assets = ref<DiscoveryAsset[]>([])
  const listError = ref(false)
  const listLoading = ref(false)

  const selectedAsset = ref<DiscoveryAsset | null>(null)
  const detail = ref<DiscoveryAssetDetail | null>(null)
  const detailLoading = ref(false)
  const detailError = ref(false)

  async function loadList() {
    listLoading.value = true
    listError.value = false
    try {
      const result = await deps.listAssets({ keyword: keyword.value || undefined })
      assets.value = result.items
    } catch {
      listError.value = true
    } finally {
      listLoading.value = false
    }
  }

  async function selectAsset(asset: DiscoveryAsset) {
    selectedAsset.value = asset
    detail.value = null
    detailError.value = false
    detailLoading.value = true
    deps.pushRecent(asset)
    try {
      detail.value = await deps.getDetail(asset.apiCode)
    } catch {
      detailError.value = true
    } finally {
      detailLoading.value = false
    }
  }

  let debounceTimer: ReturnType<typeof setTimeout>
  watch(keyword, () => {
    clearTimeout(debounceTimer)
    debounceTimer = setTimeout(loadList, 300)
  })

  return {
    keyword,
    assets,
    listError,
    listLoading,
    selectedAsset,
    detail,
    detailLoading,
    detailError,
    loadList,
    selectAsset,
  }
}
