import { http } from '@/api/http'
import type { DiscoveryAssetDetailDto, DiscoveryAssetDto, PageDto } from './catalog.dto'
import type { DiscoveryAsset, DiscoveryAssetDetail, PageResult } from './catalog.types'

function mapAsset(dto: DiscoveryAssetDto): DiscoveryAsset {
  return {
    apiCode: dto.apiCode,
    displayName: dto.displayName,
    assetType: dto.assetType,
    categoryCode: dto.categoryCode,
    categoryName: dto.categoryName,
  }
}

function mapAssetDetail(dto: DiscoveryAssetDetailDto): DiscoveryAssetDetail {
  return {
    ...mapAsset(dto),
    description: dto.description,
    authScheme: dto.authScheme,
    methods: dto.methods,
    requestTemplate: dto.requestTemplate,
    exampleSnapshot: dto.exampleSnapshot,
    aiProfile: dto.aiProfile,
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
