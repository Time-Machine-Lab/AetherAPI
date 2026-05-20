export type ImportAgentSessionStatusDto =
  | 'WAITING_FOR_PLAN'
  | 'WAITING_FOR_CONFIRMATION'
  | 'WAITING_FOR_CLARIFICATION'
  | 'CONFIRMED'
  | 'EXECUTING'
  | 'COMPLETED'
  | 'FAILED'

export type ImportAgentRunStatusDto = 'RUNNING' | 'SUCCEEDED' | 'PARTIALLY_FAILED' | 'FAILED'
export type ImportAgentActorTypeDto = 'USER' | 'AGENT'
export type ImportAgentStreamPhaseDto = 'planning' | 'replying' | 'completed'
export type ImportCategoryPlanActionDto = 'USE_EXISTING' | 'CREATE_IF_MISSING'
export type ImportAssetTypeDto = 'STANDARD_API' | 'AI_API'
export type ImportAgentClarificationInputTypeDto = 'TEXT' | 'SELECT' | 'BOOLEAN' | 'MULTILINE'
export type ImportAgentClarificationDefaultSourceDto =
  | 'DOCUMENT'
  | 'INFERRED_FROM_URL'
  | 'CURRENT_PLAN'
  | 'AGENT_HEURISTIC'
export type ImportAgentClarificationDefaultConfidenceDto = 'HIGH' | 'MEDIUM' | 'LOW'
export type ImportStepTypeDto =
  | 'ENSURE_CATEGORY'
  | 'REGISTER_ASSET'
  | 'REVISE_ASSET'
  | 'ATTACH_AI_PROFILE'
  | 'PUBLISH_ASSET'
export type ImportStepResultStatusDto = 'SUCCEEDED' | 'FAILED'

export interface CreateImportAgentSessionReqDto {
  documentSource?: string
  documentSummary?: string
  importIntent: string
  publisherDisplayName?: string
}

export interface AppendImportAgentTurnReqDto {
  message?: string
  clarificationAnswers?: ImportAgentClarificationAnswerDto[]
}

export interface ImportAgentClarificationAnswerDto {
  clarificationId?: string
  targetPath?: string
  fieldKey?: string
  value: string
}

export interface ImportAgentClarificationOptionDto {
  value: string
  label: string
}

export interface ImportAgentClarificationItemDto {
  id: string
  targetPath: string
  fieldKey: string
  label: string
  description?: string | null
  inputType: ImportAgentClarificationInputTypeDto
  required: boolean
  options?: ImportAgentClarificationOptionDto[] | null
  currentValue?: string | null
  defaultValue?: string | null
  defaultLabel?: string | null
  defaultSource?: ImportAgentClarificationDefaultSourceDto | null
  defaultConfidence?: ImportAgentClarificationDefaultConfidenceDto | null
}

export interface ImportAgentStreamStatusEventDto {
  phase: ImportAgentStreamPhaseDto
  message?: string
}

export interface ImportAgentStreamMessageEventDto {
  actorType: ImportAgentActorTypeDto
  delta: string
}

export interface ImportAgentStreamErrorEventDto {
  code?: string
  message: string
}

export interface ConfirmImportAgentPlanReqDto {
  planVersion: number
}

export interface StartImportAgentRunReqDto {
  planVersion: number
}

export interface ImportAiProfileDto {
  provider: string
  model: string
  streamingSupported: boolean
  capabilityTags: string[]
}

export interface ImportAsyncTaskConfigDto {
  enabled?: boolean
  queryMethod?: 'GET' | 'POST'
  queryUrlTemplate?: string | null
  authMode?: 'SAME_AS_SUBMIT' | 'OVERRIDE'
  authScheme?: 'NONE' | 'HEADER_TOKEN' | 'QUERY_TOKEN' | null
  authConfig?: string | null
  statusPath?: string | null
  resultPath?: string | null
  errorPath?: string | null
}

export interface ImportCategoryPlanDto {
  categoryCode: string
  categoryName: string
  action: ImportCategoryPlanActionDto
}

export interface ImportAssetPlanDto {
  apiCode: string
  assetName: string
  assetType: ImportAssetTypeDto
  categoryCode?: string | null
  requestMethod?: string | null
  upstreamUrl?: string | null
  authScheme?: string | null
  authConfig?: string | null
  requestTemplate?: string | null
  requestExample?: string | null
  responseExample?: string | null
  requestJsonSchema?: string | null
  responseJsonSchema?: string | null
  publishAfterImport: boolean
  asyncTaskConfig?: ImportAsyncTaskConfigDto | null
  aiProfile?: ImportAiProfileDto | null
}

export interface ImportAgentPlanDto {
  version: number
  executable: boolean
  summary: string
  clarificationQuestions?: string[] | null
  clarificationItems?: ImportAgentClarificationItemDto[] | null
  categoryPlans: ImportCategoryPlanDto[]
  assetPlans: ImportAssetPlanDto[]
}

export interface ImportAgentTurnDto {
  turnId: string
  actorType: ImportAgentActorTypeDto
  message: string
  planVersion?: number | null
  createdAt: string
}

export interface ApiImportAgentSessionRespDto {
  sessionId: string
  status: ImportAgentSessionStatusDto
  documentSource?: string | null
  documentSummary?: string | null
  importIntent: string
  publisherDisplayName: string
  currentPlanVersion?: number | null
  confirmedPlanVersion?: number | null
  latestRunId?: string | null
  currentPlan?: ImportAgentPlanDto | null
  turns: ImportAgentTurnDto[]
  createdAt: string
  updatedAt: string
}

export interface ImportStepResultDto {
  stepType: ImportStepTypeDto
  targetRef: string
  status: ImportStepResultStatusDto
  message?: string | null
}

export interface ApiImportAgentRunRespDto {
  runId: string
  sessionId: string
  planVersion: number
  status: ImportAgentRunStatusDto
  summary?: string | null
  failureReason?: string | null
  affectedApiCodes: string[]
  stepResults: ImportStepResultDto[]
  createdAt: string
  updatedAt: string
}
