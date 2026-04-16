export type AssetType = 'AI_API' | 'STANDARD_API'
export type AssetStatus = 'DRAFT' | 'ENABLED' | 'DISABLED'
export type CategoryStatus = 'ENABLED' | 'DISABLED'

export interface AiProfile {
  provider: string
  model: string
  streaming: boolean
  tags: string[]
}

export interface ApiAsset {
  apiCode: string
  displayName: string
  assetType: AssetType
  categoryCode: string
  status: AssetStatus
  description?: string
  authScheme?: string
  aiProfile?: AiProfile
}

export interface ApiCategory {
  categoryCode: string
  name: string
  status: CategoryStatus
  createdAt: string
  updatedAt: string
}

export interface DiscoveryAsset {
  apiCode: string
  displayName: string
  assetType: AssetType
  categoryCode: string
  categoryName?: string
}

export interface DiscoveryAssetDetail extends DiscoveryAsset {
  description?: string
  authScheme?: string
  methods?: string[]
  requestTemplate?: string
  exampleSnapshot?: string
  aiProfile?: AiProfile
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}
