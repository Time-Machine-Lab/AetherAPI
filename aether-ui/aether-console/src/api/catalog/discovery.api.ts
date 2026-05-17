import { http } from '@/api/http'
import type {
  AsyncTaskConfigDto,
  DiscoveryAssetDetailDto,
  DiscoveryAssetDto,
  DiscoveryListDto,
} from './catalog.dto'
import type {
  AsyncTaskConfig,
  DiscoveryAsset,
  DiscoveryAssetDetail,
  PageResult,
} from './catalog.types'

function mapAsset(dto: DiscoveryAssetDto): DiscoveryAsset {
  return {
    apiCode: dto.apiCode,
    displayName: dto.assetName ?? dto.apiCode,
    assetType: dto.assetType,
    categoryCode: dto.category?.categoryCode ?? '',
    categoryName: dto.category?.categoryName ?? undefined,
    publisherDisplayName: dto.publisher?.displayName ?? undefined,
    publishedAt: dto.publishedAt ?? undefined,
  }
}

function mapAsyncTaskConfig(dto?: AsyncTaskConfigDto | null): AsyncTaskConfig | undefined {
  if (!dto) {
    return undefined
  }

  return {
    enabled: dto.enabled,
    queryMethod: dto.queryMethod,
    queryUrlTemplate: dto.queryUrlTemplate ?? undefined,
    authMode: dto.authMode,
    authScheme: dto.authScheme ?? undefined,
    authConfig: dto.authConfig ?? undefined,
    statusPath: dto.statusPath,
    resultPath: dto.resultPath,
    errorPath: dto.errorPath,
  }
}

function mapAssetDetail(dto: DiscoveryAssetDetailDto): DiscoveryAssetDetail {
  return {
    ...mapAsset(dto),
    description: dto.description,
    authScheme: dto.authScheme ?? undefined,
    requestMethod: dto.requestMethod ?? undefined,
    requestTemplate: dto.requestTemplate ?? undefined,
    requestJsonSchema: dto.requestJsonSchema ?? undefined,
    responseJsonSchema: dto.responseJsonSchema ?? undefined,
    exampleSnapshot: dto.exampleSnapshot
      ? {
          requestExample: dto.exampleSnapshot.requestExample ?? undefined,
          responseExample: dto.exampleSnapshot.responseExample ?? undefined,
        }
      : undefined,
    asyncTaskConfig: mapAsyncTaskConfig(dto.asyncTaskConfig),
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
  const { data } = await http.get<DiscoveryListDto<DiscoveryAssetDto>>('v1/discovery/assets', {
    params,
  })
  return {
    items: data.items.map(mapAsset),
    total: data.total ?? data.items.length,
    page: data.page ?? 1,
    pageSize: data.pageSize ?? data.size ?? data.items.length,
  }
}

export async function getDiscoveryAssetDetail(apiCode: string): Promise<DiscoveryAssetDetail> {
  const { data } = await http.get<DiscoveryAssetDetailDto>(`v1/discovery/assets/${apiCode}`)
  return mapAssetDetail(data)
}
