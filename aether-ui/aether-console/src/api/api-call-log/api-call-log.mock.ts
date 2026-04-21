import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type {
  ApiCallLogDetailDto,
  ApiCallLogDto,
  ApiCallLogPageDto,
} from './api-call-log.dto'

const apiCallLogs: ApiCallLogDetailDto[] = [
  {
    logId: '550e8400-e29b-41d4-a716-446655440000',
    targetApiCode: 'chat-completions',
    targetApiName: 'OpenAI Chat Completions',
    requestMethod: 'POST',
    invocationTime: '2026-04-19T09:30:00Z',
    durationMs: 842,
    resultType: 'SUCCESS',
    success: true,
    httpStatusCode: 200,
    accessChannel: 'UNIFIED_ACCESS',
    credentialCode: 'cred_20260419_0001',
    credentialStatus: 'ENABLED',
    error: null,
    aiExtension: {
      provider: 'OpenAI',
      model: 'gpt-4.1',
      streaming: false,
      usageSnapshot: '{"promptTokens":123,"completionTokens":456}',
    },
    createdAt: '2026-04-19T09:30:00Z',
    updatedAt: '2026-04-19T09:30:01Z',
  },
  {
    logId: '660e8400-e29b-41d4-a716-446655440001',
    targetApiCode: 'chat-completions',
    targetApiName: 'OpenAI Chat Completions',
    requestMethod: 'POST',
    invocationTime: '2026-04-19T09:45:00Z',
    durationMs: 1120,
    resultType: 'UPSTREAM_ERROR',
    success: false,
    httpStatusCode: 503,
    accessChannel: 'UNIFIED_ACCESS',
    credentialCode: 'cred_20260419_0001',
    credentialStatus: 'ENABLED',
    error: {
      errorCode: 'UPSTREAM_TIMEOUT',
      errorType: 'UPSTREAM_ERROR',
      errorSummary: 'Upstream service timeout',
    },
    aiExtension: {
      provider: 'OpenAI',
      model: 'gpt-4.1',
      streaming: false,
      usageSnapshot: null,
    },
    createdAt: '2026-04-19T09:45:00Z',
    updatedAt: '2026-04-19T09:45:00Z',
  },
  {
    logId: '770e8400-e29b-41d4-a716-446655440002',
    targetApiCode: 'text-embedding',
    targetApiName: 'Embedding API',
    requestMethod: 'POST',
    invocationTime: '2026-04-19T10:05:00Z',
    durationMs: 267,
    resultType: 'SUCCESS',
    success: true,
    httpStatusCode: 200,
    accessChannel: 'UNIFIED_ACCESS',
    credentialCode: 'cred_20260418_0002',
    credentialStatus: 'ENABLED',
    error: null,
    aiExtension: {
      provider: 'OpenAI',
      model: 'text-embedding-3-large',
      streaming: false,
      usageSnapshot: '{"promptTokens":20}',
    },
    createdAt: '2026-04-19T10:05:00Z',
    updatedAt: '2026-04-19T10:05:00Z',
  },
]

function ok<T>(data: T): AxiosResponse<T> {
  return {
    data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: { headers: {} } as InternalAxiosRequestConfig,
  }
}

function notFound(): never {
  throw Object.assign(new Error('Not Found'), {
    response: {
      status: 404,
      data: {
        code: 'API_CALL_LOG_NOT_FOUND',
        message: 'API call log not found for current user',
      },
    },
  })
}

type MockHandler = (
  params: Record<string, string>,
  body?: unknown,
  match?: RegExpMatchArray,
) => AxiosResponse

function filterByDateRange(items: ApiCallLogDetailDto[], startAt?: string, endAt?: string) {
  const start = startAt ? Date.parse(startAt) : undefined
  const end = endAt ? Date.parse(endAt) : undefined
  return items.filter((item) => {
    const invocation = Date.parse(item.invocationTime)
    if (Number.isNaN(invocation)) {
      return false
    }
    if (start !== undefined && invocation < start) {
      return false
    }
    if (end !== undefined && invocation > end) {
      return false
    }
    return true
  })
}

export const apiCallLogMockRoutes: { method: string; pattern: RegExp; handler: MockHandler }[] = [
  {
    method: 'GET',
    pattern: /^\/api\/v1\/current-user\/api-call-logs$/,
    handler: (params) => {
      const targetApiCode = params.targetApiCode?.trim()
      const invocationStartAt = params.invocationStartAt
      const invocationEndAt = params.invocationEndAt
      const page = Number(params.page ?? 1)
      const size = Number(params.size ?? 20)

      let filtered = [...apiCallLogs]
      if (targetApiCode) {
        filtered = filtered.filter((item) => item.targetApiCode === targetApiCode)
      }
      filtered = filterByDateRange(filtered, invocationStartAt, invocationEndAt)

      filtered.sort((left, right) => Date.parse(right.invocationTime) - Date.parse(left.invocationTime))

      const startIndex = (page - 1) * size
      const pageItems = filtered.slice(startIndex, startIndex + size)
      const response: ApiCallLogPageDto = {
        items: pageItems.map<ApiCallLogDto>((item) => ({
          logId: item.logId,
          targetApiCode: item.targetApiCode,
          targetApiName: item.targetApiName,
          requestMethod: item.requestMethod,
          invocationTime: item.invocationTime,
          durationMs: item.durationMs,
          resultType: item.resultType,
          success: item.success,
          httpStatusCode: item.httpStatusCode,
        })),
        page,
        size,
        total: filtered.length,
      }
      return ok(response)
    },
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/current-user\/api-call-logs\/([^/]+)$/,
    handler: (_, __, match) => {
      const detail = apiCallLogs.find((item) => item.logId === match![1])
      if (!detail) {
        notFound()
      }
      return ok({ ...detail })
    },
  },
]
