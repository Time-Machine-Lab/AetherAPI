import { describe, expect, it, vi } from 'vitest'
import { useApiCallLogWorkspace } from './useApiCallLogWorkspace'
import type {
  ApiCallLogDetail,
  ApiCallLogItem,
} from '@/api/api-call-log/api-call-log.types'
import type { PageResult } from '@/api/catalog/catalog.types'

vi.mock('@/api/api-call-log/api-call-log.api', () => ({
  listCurrentUserApiCallLogs: vi.fn(),
  getCurrentUserApiCallLogDetail: vi.fn(),
}))

function t(key: string) {
  return key
}

function log(overrides: Partial<ApiCallLogItem> = {}): ApiCallLogItem {
  return {
    logId: 'log-1',
    targetApiCode: 'weather-api',
    targetApiName: 'Weather API',
    requestMethod: 'GET',
    invocationTime: '2026-04-23T09:00:00Z',
    durationMs: 42,
    resultType: 'SUCCESS',
    success: true,
    httpStatusCode: 200,
    ...overrides,
  }
}

function detail(overrides: Partial<ApiCallLogDetail> = {}): ApiCallLogDetail {
  return {
    ...log(),
    accessChannel: 'UNIFIED_ACCESS',
    credentialCode: 'cred_1234',
    credentialStatus: 'ENABLED',
    error: null,
    aiExtension: null,
    createdAt: '2026-04-23T09:00:01Z',
    updatedAt: '2026-04-23T09:00:01Z',
    ...overrides,
  }
}

function page(items: ApiCallLogItem[], total = items.length): PageResult<ApiCallLogItem> {
  return { items, total, page: 1, pageSize: 20 }
}

describe('useApiCallLogWorkspace', () => {
  it('loads call logs with filters and normalized date range params', async () => {
    const listLogs = vi.fn().mockResolvedValue(page([log()], 25))
    const workspace = useApiCallLogWorkspace({ t, autoLoad: false, listLogs })
    workspace.targetApiCodeFilter.value = ' weather-api '
    workspace.invocationStartAt.value = '2026-04-23T09:00'
    workspace.invocationEndAt.value = '2026-04-23T10:00'

    await workspace.handleSearch()

    expect(listLogs).toHaveBeenCalledWith({
      targetApiCode: 'weather-api',
      invocationStartAt: '2026-04-23T01:00:00.000Z',
      invocationEndAt: '2026-04-23T02:00:00.000Z',
      page: 1,
      size: 20,
    })
    expect(workspace.listItems.value).toHaveLength(1)
    expect(workspace.pageCount.value).toBe(2)
    expect(workspace.showingFrom.value).toBe(1)
    expect(workspace.showingTo.value).toBe(20)
  })

  it('blocks invalid date ranges before sending the list request', async () => {
    const listLogs = vi.fn()
    const workspace = useApiCallLogWorkspace({ t, autoLoad: false, listLogs })
    workspace.invocationStartAt.value = '2026-04-23T10:00'
    workspace.invocationEndAt.value = '2026-04-23T09:00'

    await workspace.loadApiCallLogs()

    expect(listLogs).not.toHaveBeenCalled()
    expect(workspace.filterError.value).toBe('console.apiCallLogs.filterRangeError')
  })

  it('loads detail including error code and trace-oriented failure fields', async () => {
    const failedDetail = detail({
      success: false,
      resultType: 'TARGET_NOT_FOUND',
      httpStatusCode: 404,
      error: {
        errorCode: 'ASSET_NOT_FOUND',
        errorType: 'TARGET_NOT_FOUND',
        errorSummary: 'Asset not found: unknown-api; traceId=trace-target',
      },
    })
    const getDetail = vi.fn().mockResolvedValue(failedDetail)
    const workspace = useApiCallLogWorkspace({ t, autoLoad: false, getDetail })

    await workspace.handleSelectLog(log({ logId: 'log-target-not-found', success: false }))

    expect(getDetail).toHaveBeenCalledWith('log-target-not-found')
    expect(workspace.selectedLogDetail.value?.error?.errorCode).toBe('ASSET_NOT_FOUND')
    expect(workspace.selectedLogDetail.value?.resultType).toBe('TARGET_NOT_FOUND')
    expect(workspace.detailError.value).toBe(false)
  })

  it('clears stale detail when refreshed list no longer contains the selected log', async () => {
    const listLogs = vi.fn().mockResolvedValue(page([log({ logId: 'log-2' })]))
    const workspace = useApiCallLogWorkspace({ t, autoLoad: false, listLogs })
    workspace.selectedLogId.value = 'log-1'
    workspace.selectedLogDetail.value = detail()

    await workspace.loadApiCallLogs()

    expect(workspace.selectedLogId.value).toBeNull()
    expect(workspace.selectedLogDetail.value).toBeNull()
  })

  it('separates list failures from detail failures and supports reset', async () => {
    const listLogs = vi.fn()
      .mockRejectedValueOnce(new Error('list failed'))
      .mockResolvedValueOnce(page([]))
    const getDetail = vi.fn().mockRejectedValueOnce(new Error('detail failed'))
    const workspace = useApiCallLogWorkspace({ t, autoLoad: false, listLogs, getDetail })

    await workspace.loadApiCallLogs()
    expect(workspace.listError.value).toBe(true)

    await workspace.handleSelectLog(log())
    expect(workspace.detailError.value).toBe(true)
    expect(workspace.selectedLogDetail.value).toBeNull()

    workspace.targetApiCodeFilter.value = 'weather-api'
    await workspace.handleReset()
    expect(workspace.targetApiCodeFilter.value).toBe('')
    expect(workspace.listError.value).toBe(false)
    expect(workspace.listItems.value).toEqual([])
  })
})
