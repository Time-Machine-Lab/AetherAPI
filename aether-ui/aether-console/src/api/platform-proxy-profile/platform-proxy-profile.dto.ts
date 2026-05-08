export type PlatformProxyTypeDto = 'HTTP'

export interface CreatePlatformProxyProfileReqDto {
  profileCode: string
  profileName: string
  proxyType: PlatformProxyTypeDto
  proxyHost: string
  proxyPort: number
  username?: string | null
  password?: string | null
  enabled?: boolean | null
}

export type UpdatePlatformProxyProfileReqDto = CreatePlatformProxyProfileReqDto

export interface BindProxyProfileReqDto {
  profileId: string
}

export interface PlatformProxyProfileRespDto {
  id?: string
  profileCode?: string
  profileName?: string
  proxyType?: PlatformProxyTypeDto
  proxyHost?: string
  proxyPort?: number
  username?: string | null
  credentialConfigured?: boolean
  enabled?: boolean
  deleted?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface PlatformProxyProfilePageRespDto {
  items?: PlatformProxyProfileRespDto[]
  page?: number
  size?: number
  total?: number
}

export interface AssetProxyBindingRespDto {
  apiCode?: string
  proxyProfileId?: string | null
  proxyProfileCode?: string | null
  proxyProfileName?: string | null
}

export interface ListPlatformProxyProfilesQueryDto {
  enabled?: boolean
  keyword?: string
  page?: number
  size?: number
}

export type PlatformProxyAssetStatusDto = 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED'
export type PlatformProxyAssetTypeDto = 'STANDARD_API' | 'AI_API'

export interface ListPlatformProxyAssetCandidatesQueryDto {
  keyword?: string
  status?: PlatformProxyAssetStatusDto
  boundProfileId?: string
  page?: number
  size?: number
}

export interface PlatformProxyAssetCandidateRespDto {
  apiCode?: string
  assetName?: string | null
  assetType?: PlatformProxyAssetTypeDto
  status?: PlatformProxyAssetStatusDto
  publisherDisplayName?: string | null
  proxyProfileId?: string | null
  proxyProfileCode?: string | null
  proxyProfileName?: string | null
  createdAt?: string | null
  updatedAt?: string | null
}

export interface PlatformProxyAssetCandidatePageRespDto {
  items?: PlatformProxyAssetCandidateRespDto[]
  page?: number
  size?: number
  total?: number
}
