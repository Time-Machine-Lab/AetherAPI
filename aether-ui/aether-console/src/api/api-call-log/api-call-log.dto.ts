export interface ApiCallLogErrorDto {
  errorCode: string | null
  errorType: string | null
  errorSummary: string | null
}

export interface ApiCallLogAiExtensionDto {
  provider: string | null
  model: string | null
  streaming: boolean | null
  usageSnapshot: string | null
}

export interface ApiCallLogDto {
  logId: string
  targetApiCode: string
  targetApiName: string | null
  requestMethod: string
  invocationTime: string
  durationMs: number
  resultType: string
  success: boolean
  httpStatusCode: number | null
}

export interface ApiCallLogDetailDto extends ApiCallLogDto {
  accessChannel: string
  credentialCode: string | null
  credentialStatus: string | null
  error: ApiCallLogErrorDto | null
  aiExtension: ApiCallLogAiExtensionDto | null
  createdAt: string | null
  updatedAt: string | null
}

export interface ApiCallLogPageDto {
  items: ApiCallLogDto[]
  page: number
  size: number
  total: number
}

export interface ListCurrentUserApiCallLogsParams {
  targetApiCode?: string
  invocationStartAt?: string
  invocationEndAt?: string
  page?: number
  size?: number
}
