export interface ApiCallLogError {
  errorCode: string | null
  errorType: string | null
  errorSummary: string | null
}

export interface ApiCallLogAiExtension {
  provider: string | null
  model: string | null
  streaming: boolean | null
  usageSnapshot: string | null
}

export interface ApiCallLogItem {
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

export interface ApiCallLogDetail extends ApiCallLogItem {
  accessChannel: string
  credentialCode: string | null
  credentialStatus: string | null
  error: ApiCallLogError | null
  aiExtension: ApiCallLogAiExtension | null
  createdAt: string | null
  updatedAt: string | null
}
