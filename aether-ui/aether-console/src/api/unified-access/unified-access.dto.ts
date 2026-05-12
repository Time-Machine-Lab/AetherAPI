export interface UnifiedAccessPlatformFailureDto {
  code: string
  message: string
  failureType:
    | 'INVALID_API_CODE'
    | 'INVALID_CREDENTIAL'
    | 'SUBSCRIPTION_REQUIRED'
    | 'TARGET_NOT_FOUND'
    | 'TARGET_UNAVAILABLE'
    | 'ASYNC_TASK_QUERY_UNAVAILABLE'
    | 'INVALID_TASK_ID'
  traceId?: string | null
  apiCode?: string | null
}
