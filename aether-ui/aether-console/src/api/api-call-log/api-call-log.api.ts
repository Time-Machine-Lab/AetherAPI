import { http } from '@/api/http'
import type { PageResult } from '@/api/catalog/catalog.types'
import type {
  ApiCallLogAiExtensionDto,
  ApiCallLogDetailDto,
  ApiCallLogDto,
  ApiCallLogErrorDto,
  ApiCallLogPageDto,
  ListCurrentUserApiCallLogsParams,
} from './api-call-log.dto'
import type {
  ApiCallLogAiExtension,
  ApiCallLogDetail,
  ApiCallLogError,
  ApiCallLogItem,
} from './api-call-log.types'

function mapApiCallLogError(dto: ApiCallLogErrorDto | null): ApiCallLogError | null {
  if (!dto) {
    return null
  }

  return {
    errorCode: dto.errorCode,
    errorType: dto.errorType,
    errorSummary: dto.errorSummary,
  }
}

function mapApiCallLogAiExtension(dto: ApiCallLogAiExtensionDto | null): ApiCallLogAiExtension | null {
  if (!dto) {
    return null
  }

  return {
    provider: dto.provider,
    model: dto.model,
    streaming: dto.streaming,
    usageSnapshot: dto.usageSnapshot,
  }
}

function mapApiCallLogItem(dto: ApiCallLogDto): ApiCallLogItem {
  return {
    logId: dto.logId,
    targetApiCode: dto.targetApiCode,
    targetApiName: dto.targetApiName,
    requestMethod: dto.requestMethod,
    invocationTime: dto.invocationTime,
    durationMs: dto.durationMs,
    resultType: dto.resultType,
    success: dto.success,
    httpStatusCode: dto.httpStatusCode,
  }
}

function mapApiCallLogDetail(dto: ApiCallLogDetailDto): ApiCallLogDetail {
  return {
    ...mapApiCallLogItem(dto),
    accessChannel: dto.accessChannel,
    credentialCode: dto.credentialCode,
    credentialStatus: dto.credentialStatus,
    error: mapApiCallLogError(dto.error),
    aiExtension: mapApiCallLogAiExtension(dto.aiExtension),
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  }
}

export async function listCurrentUserApiCallLogs(
  params: ListCurrentUserApiCallLogsParams,
): Promise<PageResult<ApiCallLogItem>> {
  const { data } = await http.get<ApiCallLogPageDto>('v1/current-user/api-call-logs', { params })

  return {
    items: data.items.map(mapApiCallLogItem),
    total: data.total,
    page: data.page,
    pageSize: data.size,
  }
}

export async function getCurrentUserApiCallLogDetail(logId: string): Promise<ApiCallLogDetail> {
  const { data } = await http.get<ApiCallLogDetailDto>(
    `v1/current-user/api-call-logs/${encodeURIComponent(logId)}`,
  )
  return mapApiCallLogDetail(data)
}
