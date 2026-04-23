import { http } from '@/api/http'
import type { AssetDto, BindAiProfileBody, RegisterAssetBody, ReviseAssetBody } from './catalog.dto'
import type { ApiAsset } from './catalog.types'

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

export async function registerAsset(body: RegisterAssetBody): Promise<ApiAsset> {
  const { data } = await http.post<AssetDto>('v1/assets', body)
  return mapAsset(data)
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
