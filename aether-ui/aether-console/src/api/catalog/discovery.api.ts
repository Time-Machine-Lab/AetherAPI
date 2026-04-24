import { http } from '@/api/http'
import type { DiscoveryAssetDetailDto, DiscoveryAssetDto, PageDto } from './catalog.dto'
import type { DiscoveryAsset, DiscoveryAssetDetail, PageResult } from './catalog.types'

function mapAsset(dto: DiscoveryAssetDto): DiscoveryAsset {
  return {
    apiCode: dto.apiCode,
    displayName: dto.assetName ?? dto.apiCode,
    assetType: dto.assetType,
    categoryCode: dto.category?.categoryCode ?? '',
    categoryName: dto.category?.categoryName ?? undefined,
  }
}

function mapAssetDetail(dto: DiscoveryAssetDetailDto): DiscoveryAssetDetail {
  return {
    ...mapAsset(dto),
    description: dto.description,
    authScheme: dto.authScheme ?? undefined,
    requestMethod: dto.requestMethod ?? undefined,
    requestTemplate: dto.requestTemplate ?? undefined,
    exampleSnapshot: dto.exampleSnapshot
      ? {
          requestExample: dto.exampleSnapshot.requestExample ?? undefined,
          responseExample: dto.exampleSnapshot.responseExample ?? undefined,
        }
      : undefined,
    aiProfile: dto.aiCapabilityProfile
      ? {
          provider: dto.aiCapabilityProfile.provider,
          model: dto.aiCapabilityProfile.model,
          streaming: dto.aiCapabilityProfile.streamingSupported,
          tags: dto.aiCapabilityProfile.capabilityTags,
        }
      : undefined,
  }
}

export async function listDiscoveryAssets(params?: {
  page?: number
  pageSize?: number
  keyword?: string
  categoryCode?: string
}): Promise<PageResult<DiscoveryAsset>> {
  const { data } = await http.get<PageDto<DiscoveryAssetDto>>('v1/discovery/assets', { params })
  return {
    items: data.items.map(mapAsset),
    total: data.total,
    page: data.page,
    pageSize: data.pageSize,
  }
}

export async function getDiscoveryAssetDetail(apiCode: string): Promise<DiscoveryAssetDetail> {
  const { data } = await http.get<DiscoveryAssetDetailDto>(`v1/discovery/assets/${apiCode}`)
  return mapAssetDetail(data)
}
