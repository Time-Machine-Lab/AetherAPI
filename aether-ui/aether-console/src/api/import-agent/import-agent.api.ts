import { http } from '@/api/http'
import type {
  ApiImportAgentRunRespDto,
  ApiImportAgentSessionRespDto,
  AppendImportAgentTurnReqDto,
  ConfirmImportAgentPlanReqDto,
  CreateImportAgentSessionReqDto,
  ImportAgentPlanDto,
  ImportAgentTurnDto,
  ImportAiProfileDto,
  ImportAssetPlanDto,
  ImportCategoryPlanDto,
  ImportStepResultDto,
  StartImportAgentRunReqDto,
} from './import-agent.dto'
import type {
  CreateImportAgentSessionInput,
  ImportAgentPlan,
  ImportAgentRun,
  ImportAgentSession,
  ImportAgentTurn,
  ImportAiProfile,
  ImportAssetPlan,
  ImportCategoryPlan,
  ImportStepResult,
} from './import-agent.types'

function mapAiProfile(dto?: ImportAiProfileDto | null): ImportAiProfile | null {
  if (!dto) {
    return null
  }

  return {
    provider: dto.provider,
    model: dto.model,
    streamingSupported: dto.streamingSupported,
    capabilityTags: dto.capabilityTags,
  }
}

function mapCategoryPlan(dto: ImportCategoryPlanDto): ImportCategoryPlan {
  return {
    categoryCode: dto.categoryCode,
    categoryName: dto.categoryName,
    action: dto.action,
  }
}

function mapAssetPlan(dto: ImportAssetPlanDto): ImportAssetPlan {
  return {
    apiCode: dto.apiCode,
    assetName: dto.assetName,
    assetType: dto.assetType,
    categoryCode: dto.categoryCode ?? undefined,
    requestMethod: dto.requestMethod ?? undefined,
    upstreamUrl: dto.upstreamUrl ?? undefined,
    authScheme: dto.authScheme ?? undefined,
    authConfig: dto.authConfig ?? undefined,
    requestTemplate: dto.requestTemplate ?? undefined,
    requestExample: dto.requestExample ?? undefined,
    responseExample: dto.responseExample ?? undefined,
    requestJsonSchema: dto.requestJsonSchema ?? undefined,
    responseJsonSchema: dto.responseJsonSchema ?? undefined,
    publishAfterImport: dto.publishAfterImport,
    aiProfile: mapAiProfile(dto.aiProfile),
  }
}

function mapPlan(dto?: ImportAgentPlanDto | null): ImportAgentPlan | null {
  if (!dto) {
    return null
  }

  return {
    version: dto.version,
    executable: dto.executable,
    summary: dto.summary,
    clarificationQuestions: dto.clarificationQuestions,
    categoryPlans: dto.categoryPlans.map(mapCategoryPlan),
    assetPlans: dto.assetPlans.map(mapAssetPlan),
  }
}

function mapTurn(dto: ImportAgentTurnDto): ImportAgentTurn {
  return {
    turnId: dto.turnId,
    actorType: dto.actorType,
    message: dto.message,
    planVersion: dto.planVersion ?? undefined,
    createdAt: dto.createdAt,
  }
}

function mapSession(dto: ApiImportAgentSessionRespDto): ImportAgentSession {
  return {
    sessionId: dto.sessionId,
    status: dto.status,
    documentSource: dto.documentSource ?? undefined,
    documentSummary: dto.documentSummary ?? undefined,
    importIntent: dto.importIntent,
    publisherDisplayName: dto.publisherDisplayName,
    currentPlanVersion: dto.currentPlanVersion ?? undefined,
    confirmedPlanVersion: dto.confirmedPlanVersion ?? undefined,
    latestRunId: dto.latestRunId ?? undefined,
    currentPlan: mapPlan(dto.currentPlan),
    turns: dto.turns.map(mapTurn),
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  }
}

function mapStepResult(dto: ImportStepResultDto): ImportStepResult {
  return {
    stepType: dto.stepType,
    targetRef: dto.targetRef,
    status: dto.status,
    message: dto.message ?? undefined,
  }
}

function mapRun(dto: ApiImportAgentRunRespDto): ImportAgentRun {
  return {
    runId: dto.runId,
    sessionId: dto.sessionId,
    planVersion: dto.planVersion,
    status: dto.status,
    summary: dto.summary ?? undefined,
    failureReason: dto.failureReason ?? undefined,
    affectedApiCodes: dto.affectedApiCodes,
    stepResults: dto.stepResults.map(mapStepResult),
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  }
}

function toCreateSessionReq(body: CreateImportAgentSessionInput): CreateImportAgentSessionReqDto {
  return {
    documentSource: body.documentSource,
    documentSummary: body.documentSummary,
    importIntent: body.importIntent,
    publisherDisplayName: body.publisherDisplayName,
  }
}

export async function createImportAgentSession(
  body: CreateImportAgentSessionInput,
): Promise<ImportAgentSession> {
  const { data } = await http.post<ApiImportAgentSessionRespDto>(
    '/v1/current-user/import-agent/sessions',
    toCreateSessionReq(body),
  )
  return mapSession(data)
}

export async function getImportAgentSession(sessionId: string): Promise<ImportAgentSession> {
  const { data } = await http.get<ApiImportAgentSessionRespDto>(
    `/v1/current-user/import-agent/sessions/${encodeURIComponent(sessionId)}`,
  )
  return mapSession(data)
}

export async function appendImportAgentTurn(
  sessionId: string,
  message: string,
): Promise<ImportAgentSession> {
  const body: AppendImportAgentTurnReqDto = { message }
  const { data } = await http.post<ApiImportAgentSessionRespDto>(
    `/v1/current-user/import-agent/sessions/${encodeURIComponent(sessionId)}/turns`,
    body,
  )
  return mapSession(data)
}

export async function confirmImportAgentPlan(
  sessionId: string,
  planVersion: number,
): Promise<ImportAgentSession> {
  const body: ConfirmImportAgentPlanReqDto = { planVersion }
  const { data } = await http.patch<ApiImportAgentSessionRespDto>(
    `/v1/current-user/import-agent/sessions/${encodeURIComponent(sessionId)}/confirm`,
    body,
  )
  return mapSession(data)
}

export async function startImportAgentRun(
  sessionId: string,
  planVersion: number,
): Promise<ImportAgentRun> {
  const body: StartImportAgentRunReqDto = { planVersion }
  const { data } = await http.post<ApiImportAgentRunRespDto>(
    `/v1/current-user/import-agent/sessions/${encodeURIComponent(sessionId)}/runs`,
    body,
  )
  return mapRun(data)
}

export async function getImportAgentRun(runId: string): Promise<ImportAgentRun> {
  const { data } = await http.get<ApiImportAgentRunRespDto>(
    `/v1/current-user/import-agent/runs/${encodeURIComponent(runId)}`,
  )
  return mapRun(data)
}