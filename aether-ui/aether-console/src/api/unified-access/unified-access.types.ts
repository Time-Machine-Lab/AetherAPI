export type UnifiedAccessMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export type UnifiedAccessFailureType =
  | 'INVALID_API_CODE'
  | 'INVALID_CREDENTIAL'
  | 'SUBSCRIPTION_REQUIRED'
  | 'TARGET_NOT_FOUND'
  | 'TARGET_UNAVAILABLE'

export interface UnifiedAccessPlatformFailure {
  code: string
  message: string
  failureType: UnifiedAccessFailureType
  traceId?: string | null
  apiCode?: string | null
}

export type UnifiedAccessResponseKind = 'platform-failure' | 'json' | 'text' | 'binary'

export interface UnifiedAccessResult {
  kind: UnifiedAccessResponseKind
  status: number
  contentType: string
  platformFailure?: UnifiedAccessPlatformFailure
  jsonBody?: unknown
  textBody?: string
  blobBody?: Blob
  rawHeaders: Record<string, string>
}
