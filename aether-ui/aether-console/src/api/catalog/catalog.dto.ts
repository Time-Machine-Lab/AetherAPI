export interface DiscoveryCategoryDto {
  categoryCode?: string | null
  categoryName?: string | null
}

export interface DiscoveryPublisherDto {
  displayName?: string | null
}

export interface DiscoveryAssetDto {
  apiCode: string
  assetName?: string | null
  assetType: 'AI_API' | 'STANDARD_API'
  category?: DiscoveryCategoryDto | null
  publisher?: DiscoveryPublisherDto | null
  publishedAt?: string | null
}

export interface AiProfileDto {
  provider: string
  model: string
  streaming: boolean
  tags: string[]
}

export interface DiscoveryExampleSnapshotDto {
  requestExample?: string | null
  responseExample?: string | null
}

export interface DiscoveryAiCapabilityProfileDto {
  provider: string
  model: string
  streamingSupported: boolean
  capabilityTags: string[]
}

export interface DiscoveryAssetDetailDto extends DiscoveryAssetDto {
  description?: string
  authScheme?: 'NONE' | 'HEADER_TOKEN' | 'QUERY_TOKEN' | null
  requestMethod?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | null
  requestTemplate?: string | null
  exampleSnapshot?: DiscoveryExampleSnapshotDto | null
  aiCapabilityProfile?: DiscoveryAiCapabilityProfileDto | null
}

export interface CategoryDto {
  categoryCode: string
  name: string
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
  updatedAt: string
}

export interface AssetDto {
  id?: string
  apiCode: string
  assetName?: string | null
  displayName?: string | null
  assetType: 'AI_API' | 'STANDARD_API'
  categoryCode: string | null
  categoryName?: string | null
  status: 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED'
  publisherDisplayName?: string | null
  publishedAt?: string | null
  description?: string
  requestMethod?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | null
  upstreamUrl?: string | null
  authScheme?: string
  authConfig?: string | null
  requestTemplate?: string | null
  requestExample?: string | null
  responseExample?: string | null
  aiProfile?: AiProfileDto
  aiCapabilityProfile?: {
    provider: string
    model: string
    streamingSupported: boolean
    capabilityTags: string[]
  } | null
  deleted?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface PageDto<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

export interface DiscoveryListDto<T> {
  items: T[]
  total?: number
  page?: number
  pageSize?: number
  size?: number
}

export interface RegisterAssetBody {
  apiCode: string
  assetName: string
  assetType: 'AI_API' | 'STANDARD_API'
}

export interface ReviseAssetBody {
  displayName?: string | null
  assetType?: 'AI_API' | 'STANDARD_API'
  categoryCode?: string | null
  description?: string | null
  requestMethod?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | null
  upstreamUrl?: string | null
  authScheme?: string | null
  requestTemplate?: string | null
  requestExample?: string | null
  responseExample?: string | null
}

export interface BindAiProfileBody {
  provider: string
  model: string
  streamingSupported: boolean
  capabilityTags: string[]
}

export interface CreateCategoryBody {
  name: string
}

export interface RenameCategoryBody {
  name: string
}

export interface ListAssetsQuery {
  status?: 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED'
  categoryCode?: string
  keyword?: string
  page?: number
  size?: number
}

export interface AssetSummaryDto {
  apiCode: string
  assetName: string | null
  assetType: 'AI_API' | 'STANDARD_API'
  categoryCode: string | null
  categoryName: string | null
  status: 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED'
  publisherDisplayName?: string | null
  publishedAt?: string | null
  updatedAt: string
}

export interface AssetPageDto {
  items: AssetSummaryDto[]
  page: number
  size: number
  total: number
}
