export type AssetType = 'AI_API' | 'STANDARD_API'
export type AssetStatus = 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED'
export type CategoryStatus = 'ENABLED' | 'DISABLED'

export interface AiProfile {
  provider: string
  model: string
  streaming: boolean
  tags: string[]
}

export interface AsyncTaskConfig {
  enabled?: boolean
  queryMethod?: 'GET' | 'POST'
  queryUrlTemplate?: string
  authMode?: 'SAME_AS_SUBMIT' | 'OVERRIDE'
  authScheme?: 'NONE' | 'HEADER_TOKEN' | 'QUERY_TOKEN' | null
  authConfig?: string | null
  statusPath?: string | null
  resultPath?: string | null
  errorPath?: string | null
}

export interface ApiAsset {
  id?: string
  apiCode: string
  displayName: string
  assetType: AssetType
  categoryCode: string | null
  status: AssetStatus
  publisherDisplayName?: string
  publishedAt?: string
  description?: string
  requestMethod?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  upstreamUrl?: string
  authScheme?: string
  authConfig?: string
  requestTemplate?: string
  requestExample?: string
  responseExample?: string
  asyncTaskConfig?: AsyncTaskConfig
  aiProfile?: AiProfile
  deleted?: boolean
  createdAt?: string
  updatedAt?: string
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
  publisherDisplayName?: string
  publishedAt?: string
}

export interface DiscoveryExampleSnapshot {
  requestExample?: string
  responseExample?: string
}

export interface DiscoveryAssetDetail extends DiscoveryAsset {
  description?: string
  authScheme?: string
  requestMethod?: string
  requestTemplate?: string
  exampleSnapshot?: DiscoveryExampleSnapshot
  asyncTaskConfig?: AsyncTaskConfig
  aiProfile?: AiProfile
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

export interface ApiAssetSummary {
  apiCode: string
  assetName: string | null
  assetType: AssetType
  categoryCode: string | null
  categoryName: string | null
  status: AssetStatus
  publisherDisplayName?: string | null
  publishedAt?: string | null
  updatedAt: string
  asyncTaskQueryEnabled?: boolean
}
