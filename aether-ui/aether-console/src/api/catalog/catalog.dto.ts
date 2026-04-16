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
  displayName: string
  assetType: 'AI_API' | 'STANDARD_API'
  categoryCode: string
  status: 'DRAFT' | 'ENABLED' | 'DISABLED'
  description?: string
  authScheme?: string
  aiProfile?: AiProfileDto
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
  displayName?: string
  categoryCode?: string
  description?: string
  authScheme?: string
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
