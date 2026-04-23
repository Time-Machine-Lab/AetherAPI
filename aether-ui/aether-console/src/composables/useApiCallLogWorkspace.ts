import { computed, onMounted, ref } from 'vue'
import {
  getCurrentUserApiCallLogDetail,
  listCurrentUserApiCallLogs,
} from '@/api/api-call-log/api-call-log.api'
import type { ListCurrentUserApiCallLogsParams } from '@/api/api-call-log/api-call-log.dto'
import type { ApiCallLogDetail, ApiCallLogItem } from '@/api/api-call-log/api-call-log.types'

interface ApiCallLogWorkspaceDeps {
  t: (key: string) => string
  listLogs: typeof listCurrentUserApiCallLogs
  getDetail: typeof getCurrentUserApiCallLogDetail
  autoLoad?: boolean
}

type ApiCallLogWorkspaceOptions = Partial<Omit<ApiCallLogWorkspaceDeps, 't'>> &
  Pick<ApiCallLogWorkspaceDeps, 't'>

function buildDeps(options: ApiCallLogWorkspaceOptions): ApiCallLogWorkspaceDeps {
  return {
    listLogs: listCurrentUserApiCallLogs,
    getDetail: getCurrentUserApiCallLogDetail,
    autoLoad: true,
    ...options,
  }
}

function toIsoString(input: string): string | undefined {
  if (!input) return undefined
  const timestamp = Date.parse(input)
  if (Number.isNaN(timestamp)) return undefined
  return new Date(timestamp).toISOString()
}

export function useApiCallLogWorkspace(options: ApiCallLogWorkspaceOptions) {
  const deps = buildDeps(options)

  const listLoading = ref(false)
  const listError = ref(false)
  const listItems = ref<ApiCallLogItem[]>([])
  const listTotal = ref(0)
  const listPage = ref(1)
  const listSize = ref(20)

  const targetApiCodeFilter = ref('')
  const invocationStartAt = ref('')
  const invocationEndAt = ref('')
  const filterError = ref('')

  const selectedLogId = ref<string | null>(null)
  const selectedLogDetail = ref<ApiCallLogDetail | null>(null)
  const detailLoading = ref(false)
  const detailError = ref(false)

  const pageCount = computed(() => {
    if (listTotal.value <= 0) return 1
    return Math.ceil(listTotal.value / listSize.value)
  })

  const showingFrom = computed(() => {
    if (listTotal.value === 0) return 0
    return (listPage.value - 1) * listSize.value + 1
  })

  const showingTo = computed(() => {
    if (listTotal.value === 0) return 0
    return Math.min(listPage.value * listSize.value, listTotal.value)
  })

  function hasValidDateRange() {
    if (!invocationStartAt.value || !invocationEndAt.value) return true

    const startTime = Date.parse(invocationStartAt.value)
    const endTime = Date.parse(invocationEndAt.value)

    if (Number.isNaN(startTime) || Number.isNaN(endTime)) return false
    return startTime <= endTime
  }

  function buildListParams(): ListCurrentUserApiCallLogsParams {
    return {
      targetApiCode: targetApiCodeFilter.value.trim() || undefined,
      invocationStartAt: toIsoString(invocationStartAt.value),
      invocationEndAt: toIsoString(invocationEndAt.value),
      page: listPage.value,
      size: listSize.value,
    }
  }

  async function loadApiCallLogs() {
    if (!hasValidDateRange()) {
      filterError.value = deps.t('console.apiCallLogs.filterRangeError')
      return
    }

    listLoading.value = true
    listError.value = false
    filterError.value = ''

    try {
      const result = await deps.listLogs(buildListParams())
      listItems.value = result.items
      listTotal.value = result.total

      if (selectedLogId.value && !result.items.some((item) => item.logId === selectedLogId.value)) {
        selectedLogId.value = null
        selectedLogDetail.value = null
        detailError.value = false
      }
    } catch {
      listError.value = true
    } finally {
      listLoading.value = false
    }
  }

  async function loadLogDetail(logId: string) {
    detailLoading.value = true
    detailError.value = false

    try {
      selectedLogDetail.value = await deps.getDetail(logId)
    } catch {
      selectedLogDetail.value = null
      detailError.value = true
    } finally {
      detailLoading.value = false
    }
  }

  async function handleSelectLog(log: ApiCallLogItem) {
    if (selectedLogId.value === log.logId) {
      selectedLogId.value = null
      selectedLogDetail.value = null
      detailError.value = false
      return
    }

    selectedLogId.value = log.logId
    await loadLogDetail(log.logId)
  }

  async function handleSearch() {
    listPage.value = 1
    await loadApiCallLogs()
  }

  async function handleReset() {
    targetApiCodeFilter.value = ''
    invocationStartAt.value = ''
    invocationEndAt.value = ''
    listPage.value = 1
    selectedLogId.value = null
    selectedLogDetail.value = null
    detailError.value = false
    await loadApiCallLogs()
  }

  async function handlePrevPage() {
    if (listPage.value <= 1 || listLoading.value) return
    listPage.value -= 1
    await loadApiCallLogs()
  }

  async function handleNextPage() {
    if (listPage.value >= pageCount.value || listLoading.value) return
    listPage.value += 1
    await loadApiCallLogs()
  }

  if (deps.autoLoad) {
    onMounted(loadApiCallLogs)
  }

  return {
    listLoading,
    listError,
    listItems,
    listTotal,
    listPage,
    listSize,
    targetApiCodeFilter,
    invocationStartAt,
    invocationEndAt,
    filterError,
    selectedLogId,
    selectedLogDetail,
    detailLoading,
    detailError,
    pageCount,
    showingFrom,
    showingTo,
    loadApiCallLogs,
    loadLogDetail,
    handleSelectLog,
    handleSearch,
    handleReset,
    handlePrevPage,
    handleNextPage,
  }
}
