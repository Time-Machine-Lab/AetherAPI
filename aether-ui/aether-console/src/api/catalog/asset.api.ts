import { http } from '@/api/http'
import type {
  AssetDto,
  AssetPageDto,
  AssetSummaryDto,
  BindAiProfileBody,
  ListAssetsQuery,
  RegisterAssetBody,
  ReviseAssetBody,
} from './catalog.dto'
import type { ApiAsset, ApiAssetSummary, PageResult } from './catalog.types'

function mapAiProfile(dto: AssetDto): ApiAsset['aiProfile'] {
  const profile = dto.aiCapabilityProfile ?? dto.aiProfile
  if (!profile) {
    return undefined
  }

  return {
    provider: profile.provider,
    model: profile.model,
    streaming: 'streamingSupported' in profile ? profile.streamingSupported : profile.streaming,
    tags: 'capabilityTags' in profile ? profile.capabilityTags : profile.tags,
  }
}

function mapAsset(dto: AssetDto): ApiAsset {
  return {
    apiCode: dto.apiCode,
    displayName: dto.assetName ?? dto.displayName ?? dto.apiCode,
    assetType: dto.assetType,
    categoryCode: dto.categoryCode,
    status: dto.status,
    description: dto.description,
    requestMethod: dto.requestMethod ?? undefined,
    upstreamUrl: dto.upstreamUrl ?? undefined,
    authScheme: dto.authScheme,
    authConfig: dto.authConfig ?? undefined,
    requestTemplate: dto.requestTemplate ?? undefined,
    requestExample: dto.requestExample ?? undefined,
    responseExample: dto.responseExample ?? undefined,
    aiProfile: mapAiProfile(dto),
  }
}

function mapAssetSummary(dto: AssetSummaryDto): ApiAssetSummary {
  return {
    apiCode: dto.apiCode,
    assetName: dto.assetName,
    assetType: dto.assetType,
    categoryCode: dto.categoryCode,
    categoryName: dto.categoryName,
    status: dto.status,
    updatedAt: dto.updatedAt,
  }
}

export async function registerAsset(body: RegisterAssetBody): Promise<ApiAsset> {
  const { data } = await http.post<AssetDto>('v1/assets', {
    apiCode: body.apiCode,
    assetName: body.displayName,
    assetType: body.assetType,
    categoryCode: body.categoryCode,
    description: body.description,
    authScheme: body.authScheme,
  })
  return mapAsset(data)
}

export async function listAssets(query?: ListAssetsQuery): Promise<PageResult<ApiAssetSummary>> {
  const { data } = await http.get<AssetPageDto>('v1/assets', { params: query })
  return {
    items: data.items.map(mapAssetSummary),
    total: data.total,
    page: data.page,
    pageSize: data.size,
  }
}

export async function getAsset(apiCode: string): Promise<ApiAsset> {
  const { data } = await http.get<AssetDto>(`v1/assets/${apiCode}`)
  return mapAsset(data)
}

export async function reviseAsset(apiCode: string, body: ReviseAssetBody): Promise<ApiAsset> {
  const { data } = await http.put<AssetDto>(`v1/assets/${apiCode}`, {
    assetName: body.displayName,
    categoryCode: body.categoryCode,
    description: body.description,
    requestMethod: body.requestMethod,
    upstreamUrl: body.upstreamUrl,
    authScheme: body.authScheme,
    requestTemplate: body.requestTemplate,
    requestExample: body.requestExample,
    responseExample: body.responseExample,
  })
  return mapAsset(data)
}

export async function enableAsset(apiCode: string): Promise<ApiAsset> {
  const { data } = await http.patch<AssetDto>(`v1/assets/${apiCode}/enable`)
  return mapAsset(data)
}

export async function disableAsset(apiCode: string): Promise<ApiAsset> {
  const { data } = await http.patch<AssetDto>(`v1/assets/${apiCode}/disable`)
  return mapAsset(data)
}

export async function bindAiProfile(apiCode: string, body: BindAiProfileBody): Promise<ApiAsset> {
  const { data } = await http.put<AssetDto>(`v1/assets/${apiCode}/ai-profile`, body)
  return mapAsset(data)
}
