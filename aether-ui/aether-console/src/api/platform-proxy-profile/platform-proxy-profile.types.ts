export type PlatformProxyType = 'HTTP'

export interface PlatformProxyProfile {
  id: string
  profileCode: string
  profileName: string
  proxyType: PlatformProxyType
  proxyHost: string
  proxyPort: number
  username?: string | null
  credentialConfigured: boolean
  enabled: boolean
  deleted: boolean
  createdAt?: string
  updatedAt?: string
}

export interface PlatformProxyProfilePage {
  items: PlatformProxyProfile[]
  page: number
  pageSize: number
  total: number
}

export interface ListPlatformProxyProfilesQuery {
  enabled?: boolean
  keyword?: string
  page?: number
  size?: number
}

export interface SavePlatformProxyProfileBody {
  profileCode: string
  profileName: string
  proxyType: PlatformProxyType
  proxyHost: string
  proxyPort: number
  username?: string | null
  password?: string | null
  enabled?: boolean | null
}

export interface AssetProxyBinding {
  apiCode: string
  proxyProfileId: string | null
  proxyProfileCode: string | null
  proxyProfileName: string | null
}

export interface BindProxyProfileBody {
  profileId: string
}

export type PlatformProxyAssetStatus = 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED'
export type PlatformProxyAssetType = 'STANDARD_API' | 'AI_API'

export interface PlatformProxyAssetCandidate {
  apiCode: string
  assetName: string | null
  assetType: PlatformProxyAssetType
  status: PlatformProxyAssetStatus
  publisherDisplayName: string | null
  proxyProfileId: string | null
  proxyProfileCode: string | null
  proxyProfileName: string | null
  createdAt: string | null
  updatedAt: string | null
}

export interface PlatformProxyAssetCandidatePage {
  items: PlatformProxyAssetCandidate[]
  page: number
  pageSize: number
  total: number
}

export interface ListPlatformProxyAssetCandidatesQuery {
  keyword?: string
  status?: PlatformProxyAssetStatus
  boundProfileId?: string
  page?: number
  size?: number
}
