import { http } from '@/api/http'
import type { NormalizedHttpError } from '@/api/http'
import type {
  ApiImportAgentRunRespDto,
  ApiImportAgentSessionRespDto,
  AppendImportAgentTurnReqDto,
  ConfirmImportAgentPlanReqDto,
  CreateImportAgentSessionReqDto,
  ImportAgentClarificationItemDto,
  ImportAgentClarificationOptionDto,
  ImportAgentPlanDto,
  ImportAgentStreamErrorEventDto,
  ImportAgentStreamMessageEventDto,
  ImportAgentStreamStatusEventDto,
  ImportAgentTurnDto,
  ImportAiProfileDto,
  ImportAsyncTaskConfigDto,
  ImportAssetPlanDto,
  ImportCategoryPlanDto,
  ImportStepResultDto,
  StartImportAgentRunReqDto,
} from './import-agent.dto'
import type {
  AppendImportAgentTurnInput,
  CreateImportAgentSessionInput,
  ImportAgentClarificationItem,
  ImportAgentClarificationOption,
  ImportAgentPlan,
  ImportAgentRun,
  ImportAgentSession,
  ImportAgentStreamCallbacks,
  ImportAgentTurn,
  ImportAiProfile,
  ImportAsyncTaskConfig,
  ImportAssetPlan,
  ImportCategoryPlan,
  ImportStepResult,
} from './import-agent.types'

function mapClarificationOption(
  dto: ImportAgentClarificationOptionDto,
): ImportAgentClarificationOption {
  return {
    value: dto.value,
    label: dto.label,
  }
}

function mapClarificationItem(dto: ImportAgentClarificationItemDto): ImportAgentClarificationItem {
  return {
    id: dto.id,
    targetPath: dto.targetPath,
    fieldKey: dto.fieldKey,
    label: dto.label,
    description: dto.description ?? undefined,
    inputType: dto.inputType,
    required: dto.required,
    options: (dto.options ?? []).map(mapClarificationOption),
    currentValue: dto.currentValue ?? undefined,
    defaultValue: dto.defaultValue ?? undefined,
    defaultLabel: dto.defaultLabel ?? undefined,
    defaultSource: dto.defaultSource ?? undefined,
    defaultConfidence: dto.defaultConfidence ?? undefined,
  }
}

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

function mapAsyncTaskConfig(dto?: ImportAsyncTaskConfigDto | null): ImportAsyncTaskConfig | null {
  if (!dto) {
    return null
  }

  return {
    enabled: dto.enabled,
    queryMethod: dto.queryMethod,
    queryUrlTemplate: dto.queryUrlTemplate ?? undefined,
    authMode: dto.authMode,
    authScheme: dto.authScheme ?? null,
    authConfig: dto.authConfig ?? null,
    statusPath: dto.statusPath ?? null,
    resultPath: dto.resultPath ?? null,
    errorPath: dto.errorPath ?? null,
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
    asyncTaskConfig: mapAsyncTaskConfig(dto.asyncTaskConfig),
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
    clarificationQuestions: dto.clarificationQuestions ?? [],
    clarificationItems: (dto.clarificationItems ?? []).map(mapClarificationItem),
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

function toAppendTurnReq(input: string | AppendImportAgentTurnInput): AppendImportAgentTurnReqDto {
  if (typeof input === 'string') {
    return { message: input }
  }
  const message = input.message?.trim()
  return {
    ...(message ? { message } : {}),
    ...(input.clarificationAnswers && input.clarificationAnswers.length > 0
      ? { clarificationAnswers: input.clarificationAnswers }
      : {}),
  }
}

function toStreamError(error: ImportAgentStreamErrorEventDto): NormalizedHttpError {
  return {
    status: 200,
    code: error.code,
    message: error.message,
  }
}

function parseSseEventBlock(block: string) {
  const lines = block
    .split('\n')
    .map((line) => line.replace(/\r$/, ''))
    .filter((line) => line.length > 0)

  if (lines.length === 0) {
    return null
  }

  let eventName = 'message'
  const dataLines: string[] = []

  for (const line of lines) {
    if (line.startsWith('event:')) {
      eventName = line.slice('event:'.length).trim()
      continue
    }
    if (line.startsWith('data:')) {
      dataLines.push(line.slice('data:'.length).trimStart())
    }
  }

  if (dataLines.length === 0) {
    return null
  }

  return {
    eventName,
    payloadText: dataLines.join('\n'),
  }
}

function consumeSseChunk(
  chunk: string,
  onEvent: (eventName: string, payloadText: string) => void,
  state: { buffer: string },
) {
  state.buffer += chunk

  while (true) {
    const separatorIndex = state.buffer.indexOf('\n\n')
    if (separatorIndex < 0) {
      return
    }

    const block = state.buffer.slice(0, separatorIndex)
    state.buffer = state.buffer.slice(separatorIndex + 2)
    const parsed = parseSseEventBlock(block)
    if (!parsed) {
      continue
    }
    onEvent(parsed.eventName, parsed.payloadText)
  }
}

function finalizeSseBuffer(
  state: { buffer: string },
  onEvent: (eventName: string, payloadText: string) => void,
) {
  const trailing = state.buffer.trim()
  state.buffer = ''
  if (!trailing) {
    return
  }
  const parsed = parseSseEventBlock(trailing)
  if (parsed) {
    onEvent(parsed.eventName, parsed.payloadText)
  }
}

async function requestImportAgentSessionStream(
  url: string,
  body: CreateImportAgentSessionReqDto | AppendImportAgentTurnReqDto,
  callbacks?: ImportAgentStreamCallbacks,
): Promise<ImportAgentSession> {
  const parserState = { buffer: '' }
  let consumedLength = 0
  let latestSession: ImportAgentSession | null = null
  let streamError: NormalizedHttpError | null = null

  const handleEvent = (eventName: string, payloadText: string) => {
    switch (eventName) {
      case 'status': {
        const payload = JSON.parse(payloadText) as ImportAgentStreamStatusEventDto
        callbacks?.onStatus?.(payload)
        return
      }
      case 'message': {
        const payload = JSON.parse(payloadText) as ImportAgentStreamMessageEventDto
        callbacks?.onMessage?.(payload)
        return
      }
      case 'session': {
        const payload = JSON.parse(payloadText) as ApiImportAgentSessionRespDto
        latestSession = mapSession(payload)
        callbacks?.onSession?.(latestSession)
        return
      }
      case 'error': {
        const payload = JSON.parse(payloadText) as ImportAgentStreamErrorEventDto
        callbacks?.onError?.(payload)
        streamError = toStreamError(payload)
        return
      }
      case 'done': {
        callbacks?.onDone?.()
        return
      }
      default:
        return
    }
  }

  const response = await http.request<string>({
    url,
    method: 'post',
    data: body,
    timeout: 0,
    responseType: 'text',
    transformResponse: [(data) => data],
    onDownloadProgress: (progressEvent) => {
      const responseText = (progressEvent.event?.target as { responseText?: string } | undefined)
        ?.responseText
      if (typeof responseText !== 'string' || responseText.length <= consumedLength) {
        return
      }
      const nextChunk = responseText.slice(consumedLength)
      consumedLength = responseText.length
      consumeSseChunk(nextChunk, handleEvent, parserState)
    },
  })

  if (typeof response.data === 'string' && response.data.length > consumedLength) {
    consumeSseChunk(response.data.slice(consumedLength), handleEvent, parserState)
    consumedLength = response.data.length
  }
  finalizeSseBuffer(parserState, handleEvent)

  if (streamError) {
    throw streamError
  }
  if (!latestSession) {
    throw {
      status: response.status,
      code: 'IMPORT_AGENT_STREAM_SESSION_MISSING',
      message: 'Stream completed without a session snapshot.',
    } satisfies NormalizedHttpError
  }

  return latestSession
}

export async function createImportAgentSession(
  body: CreateImportAgentSessionInput,
  callbacks?: ImportAgentStreamCallbacks,
): Promise<ImportAgentSession> {
  return requestImportAgentSessionStream(
    '/v1/current-user/import-agent/sessions/stream',
    toCreateSessionReq(body),
    callbacks,
  )
}

export async function getImportAgentSession(sessionId: string): Promise<ImportAgentSession> {
  const { data } = await http.get<ApiImportAgentSessionRespDto>(
    `/v1/current-user/import-agent/sessions/${encodeURIComponent(sessionId)}`,
  )
  return mapSession(data)
}

export async function appendImportAgentTurn(
  sessionId: string,
  input: string | AppendImportAgentTurnInput,
  callbacks?: ImportAgentStreamCallbacks,
): Promise<ImportAgentSession> {
  const body = toAppendTurnReq(input)
  return requestImportAgentSessionStream(
    `/v1/current-user/import-agent/sessions/${encodeURIComponent(sessionId)}/turns/stream`,
    body,
    callbacks,
  )
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
