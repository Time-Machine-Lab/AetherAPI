export type ImportAgentSessionStatus =
  | 'WAITING_FOR_PLAN'
  | 'WAITING_FOR_CONFIRMATION'
  | 'WAITING_FOR_CLARIFICATION'
  | 'CONFIRMED'
  | 'EXECUTING'
  | 'COMPLETED'
  | 'FAILED'

export type ImportAgentRunStatus = 'RUNNING' | 'SUCCEEDED' | 'PARTIALLY_FAILED' | 'FAILED'
export type ImportAgentActorType = 'USER' | 'AGENT'
export type ImportCategoryPlanAction = 'USE_EXISTING' | 'CREATE_IF_MISSING'
export type ImportAssetType = 'STANDARD_API' | 'AI_API'
export type ImportStepType =
  | 'ENSURE_CATEGORY'
  | 'REGISTER_ASSET'
  | 'REVISE_ASSET'
  | 'ATTACH_AI_PROFILE'
  | 'PUBLISH_ASSET'
export type ImportStepResultStatus = 'SUCCEEDED' | 'FAILED'

export interface CreateImportAgentSessionInput {
  documentSource?: string
  documentSummary?: string
  importIntent: string
  publisherDisplayName?: string
}

export interface ImportAiProfile {
  provider: string
  model: string
  streamingSupported: boolean
  capabilityTags: string[]
}

export interface ImportCategoryPlan {
  categoryCode: string
  categoryName: string
  action: ImportCategoryPlanAction
}

export interface ImportAssetPlan {
  apiCode: string
  assetName: string
  assetType: ImportAssetType
  categoryCode?: string
  requestMethod?: string
  upstreamUrl?: string
  authScheme?: string
  authConfig?: string
  requestTemplate?: string
  requestExample?: string
  responseExample?: string
  requestJsonSchema?: string
  responseJsonSchema?: string
  publishAfterImport: boolean
  aiProfile: ImportAiProfile | null
}

export interface ImportAgentPlan {
  version: number
  executable: boolean
  summary: string
  clarificationQuestions: string[]
  categoryPlans: ImportCategoryPlan[]
  assetPlans: ImportAssetPlan[]
}

export interface ImportAgentTurn {
  turnId: string
  actorType: ImportAgentActorType
  message: string
  planVersion?: number
  createdAt: string
}

export interface ImportAgentSession {
  sessionId: string
  status: ImportAgentSessionStatus
  documentSource?: string
  documentSummary?: string
  importIntent: string
  publisherDisplayName: string
  currentPlanVersion?: number
  confirmedPlanVersion?: number
  latestRunId?: string
  currentPlan: ImportAgentPlan | null
  turns: ImportAgentTurn[]
  createdAt: string
  updatedAt: string
}

export interface ImportStepResult {
  stepType: ImportStepType
  targetRef: string
  status: ImportStepResultStatus
  message?: string
}

export interface ImportAgentRun {
  runId: string
  sessionId: string
  planVersion: number
  status: ImportAgentRunStatus
  summary?: string
  failureReason?: string
  affectedApiCodes: string[]
  stepResults: ImportStepResult[]
  createdAt: string
  updatedAt: string
}