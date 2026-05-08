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
