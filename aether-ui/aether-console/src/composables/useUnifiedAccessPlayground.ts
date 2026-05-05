import { ref, computed } from 'vue'
import { invokeUnifiedAccess } from '@/api/unified-access/unified-access.api'
import { listDiscoveryAssets, getDiscoveryAssetDetail } from '@/api/catalog/discovery.api'
import { getCurrentUserApiSubscriptionStatus } from '@/api/subscription/subscription.api'
import type { DiscoveryAsset, DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import type { ApiSubscriptionStatus } from '@/api/subscription/subscription.types'
import type {
  UnifiedAccessMethod,
  UnifiedAccessResult,
} from '@/api/unified-access/unified-access.types'

export function useUnifiedAccessPlayground() {
  // ── Form state ──────────────────────────────────────────
  const apiCode = ref('')
  const method = ref<UnifiedAccessMethod>('POST')
  const apiKey = ref('')
  const requestBody = ref('')
  const extraHeaders = ref('')

  // ── Discovery assist ────────────────────────────────────
  const discoveryAssets = ref<DiscoveryAsset[]>([])
  const discoveryLoading = ref(false)
  const discoveryError = ref(false)
  const selectedAssetDetail = ref<DiscoveryAssetDetail | null>(null)
  const detailLoading = ref(false)
  const subscriptionStatus = ref<ApiSubscriptionStatus | null>(null)
  const subscriptionStatusLoading = ref(false)
  const subscriptionStatusError = ref(false)

  // ── Invocation state ────────────────────────────────────
  const invoking = ref(false)
  const result = ref<UnifiedAccessResult | null>(null)
  const invokeError = ref('')
  const elapsedMs = ref(0)

  // ── Computed ────────────────────────────────────────────
  const methodSupportsBody = computed(() => !['GET', 'DELETE'].includes(method.value))
  const canInvoke = computed(
    () => apiCode.value.trim().length > 0 && apiKey.value.trim().length > 0,
  )

  // ── Discovery helpers ───────────────────────────────────
  async function loadDiscoveryAssets() {
    discoveryLoading.value = true
    discoveryError.value = false
    try {
      const page = await listDiscoveryAssets({ pageSize: 200 })
      discoveryAssets.value = page.items
    } catch {
      discoveryError.value = true
    } finally {
      discoveryLoading.value = false
    }
  }

  async function selectDiscoveryAsset(asset: DiscoveryAsset) {
    apiCode.value = asset.apiCode
    await loadSelectedAssetDetail(asset.apiCode)
  }

  async function loadSubscriptionStatus(nextApiCode = apiCode.value) {
    const normalizedApiCode = nextApiCode.trim()
    subscriptionStatus.value = null
    subscriptionStatusError.value = false
    if (!normalizedApiCode) return
    subscriptionStatusLoading.value = true
    try {
      subscriptionStatus.value = await getCurrentUserApiSubscriptionStatus(normalizedApiCode)
    } catch {
      subscriptionStatusError.value = true
    } finally {
      subscriptionStatusLoading.value = false
    }
  }

  async function loadSelectedAssetDetail(nextApiCode = apiCode.value) {
    const normalizedApiCode = nextApiCode.trim()
    if (!normalizedApiCode) return
    apiCode.value = normalizedApiCode
    detailLoading.value = true
    try {
      const detail = await getDiscoveryAssetDetail(normalizedApiCode)
      selectedAssetDetail.value = detail
      if (detail.requestMethod) {
        method.value = detail.requestMethod as UnifiedAccessMethod
      }
      if (detail.exampleSnapshot?.requestExample) {
        requestBody.value = detail.exampleSnapshot.requestExample
      }
      await loadSubscriptionStatus(normalizedApiCode)
    } catch {
      selectedAssetDetail.value = null
      subscriptionStatus.value = null
      subscriptionStatusError.value = false
    } finally {
      detailLoading.value = false
    }
  }

  // ── Invocation ──────────────────────────────────────────
  async function invoke() {
    if (!canInvoke.value) return
    invoking.value = true
    invokeError.value = ''
    result.value = null
    const start = performance.now()

    let parsedHeaders: Record<string, string> | undefined
    if (extraHeaders.value.trim()) {
      try {
        parsedHeaders = JSON.parse(extraHeaders.value.trim())
      } catch {
        invokeError.value = 'Invalid JSON in extra headers'
        invoking.value = false
        return
      }
    }

    try {
      result.value = await invokeUnifiedAccess(
        apiCode.value.trim(),
        method.value,
        apiKey.value.trim(),
        methodSupportsBody.value ? requestBody.value : undefined,
        parsedHeaders,
      )
    } catch (err: unknown) {
      invokeError.value = err instanceof Error ? err.message : String(err)
    } finally {
      elapsedMs.value = Math.round(performance.now() - start)
      invoking.value = false
    }
  }

  // ── Reset helpers ───────────────────────────────────────
  function resetForm() {
    apiCode.value = ''
    method.value = 'POST'
    apiKey.value = ''
    requestBody.value = ''
    extraHeaders.value = ''
    result.value = null
    invokeError.value = ''
    selectedAssetDetail.value = null
    subscriptionStatus.value = null
    subscriptionStatusError.value = false
  }

  function clearApiKey() {
    apiKey.value = ''
  }

  function clearResult() {
    result.value = null
    invokeError.value = ''
  }

  return {
    // form
    apiCode,
    method,
    apiKey,
    requestBody,
    extraHeaders,
    // discovery
    discoveryAssets,
    discoveryLoading,
    discoveryError,
    selectedAssetDetail,
    detailLoading,
    subscriptionStatus,
    subscriptionStatusLoading,
    subscriptionStatusError,
    loadDiscoveryAssets,
    selectDiscoveryAsset,
    loadSelectedAssetDetail,
    loadSubscriptionStatus,
    // invocation
    invoking,
    result,
    invokeError,
    elapsedMs,
    // computed
    methodSupportsBody,
    canInvoke,
    // actions
    invoke,
    resetForm,
    clearApiKey,
    clearResult,
  }
}
