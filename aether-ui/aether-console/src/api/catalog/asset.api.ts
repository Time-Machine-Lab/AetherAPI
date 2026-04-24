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

function mapAsset(dto: AssetDto): ApiAsset {
  return {
    apiCode: dto.apiCode,
    displayName: dto.displayName,
    assetType: dto.assetType,
    categoryCode: dto.categoryCode,
    status: dto.status,
    description: dto.description,
    authScheme: dto.authScheme,
    aiProfile: dto.aiProfile,
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
  const { data } = await http.post<AssetDto>('v1/assets', body)
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
  const { data } = await http.patch<AssetDto>(`v1/assets/${apiCode}`, body)
  return mapAsset(data)
}

export async function enableAsset(apiCode: string): Promise<ApiAsset> {
  const { data } = await http.post<AssetDto>(`v1/assets/${apiCode}/enable`)
  return mapAsset(data)
}

export async function disableAsset(apiCode: string): Promise<ApiAsset> {
  const { data } = await http.post<AssetDto>(`v1/assets/${apiCode}/disable`)
  return mapAsset(data)
}

export async function bindAiProfile(apiCode: string, body: BindAiProfileBody): Promise<ApiAsset> {
  const { data } = await http.put<AssetDto>(`v1/assets/${apiCode}/ai-profile`, body)
  return mapAsset(data)
}
