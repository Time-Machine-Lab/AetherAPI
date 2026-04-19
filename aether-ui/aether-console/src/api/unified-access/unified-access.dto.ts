export interface UnifiedAccessPlatformFailureDto {
  code: string
  message: string
  failureType: 'INVALID_API_CODE' | 'INVALID_CREDENTIAL' | 'TARGET_NOT_FOUND' | 'TARGET_UNAVAILABLE'
  traceId?: string | null
  apiCode?: string | null
}
