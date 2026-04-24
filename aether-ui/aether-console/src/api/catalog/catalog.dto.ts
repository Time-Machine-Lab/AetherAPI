export interface DiscoveryAssetDto {
  apiCode: string
  displayName: string
  assetType: 'AI_API' | 'STANDARD_API'
  categoryCode: string
  categoryName?: string
}

export interface AiProfileDto {
  provider: string
  model: string
  streaming: boolean
  tags: string[]
}

export interface DiscoveryAssetDetailDto extends DiscoveryAssetDto {
  description?: string
  authScheme?: string
  methods?: string[]
  requestTemplate?: string
  exampleSnapshot?: string
  aiProfile?: AiProfileDto
}

export interface CategoryDto {
  categoryCode: string
  name: string
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
  updatedAt: string
}

export interface AssetDto {
  apiCode: string
  assetName?: string | null
  displayName?: string | null
  assetType: 'AI_API' | 'STANDARD_API'
  categoryCode: string | null
  status: 'DRAFT' | 'ENABLED' | 'DISABLED'
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
}

export interface PageDto<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

export interface RegisterAssetBody {
  apiCode: string
  displayName: string
  assetType: 'AI_API' | 'STANDARD_API'
  categoryCode: string
  description?: string
  authScheme?: string
}

export interface ReviseAssetBody {
  displayName?: string | null
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
  streaming: boolean
  tags: string[]
}

export interface CreateCategoryBody {
  name: string
}

export interface RenameCategoryBody {
  name: string
}

export interface ListAssetsQuery {
  status?: 'DRAFT' | 'ENABLED' | 'DISABLED'
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
  status: 'DRAFT' | 'ENABLED' | 'DISABLED'
  updatedAt: string
}

export interface AssetPageDto {
  items: AssetSummaryDto[]
  page: number
  size: number
  total: number
}
