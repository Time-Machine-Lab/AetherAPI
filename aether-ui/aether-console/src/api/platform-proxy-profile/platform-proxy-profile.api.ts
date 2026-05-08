import { http } from '@/api/http'
import type {
  AssetProxyBindingRespDto,
  BindProxyProfileReqDto,
  CreatePlatformProxyProfileReqDto,
  ListPlatformProxyProfilesQueryDto,
  PlatformProxyProfilePageRespDto,
  PlatformProxyProfileRespDto,
  UpdatePlatformProxyProfileReqDto,
} from './platform-proxy-profile.dto'
import type {
  AssetProxyBinding,
  BindProxyProfileBody,
  ListPlatformProxyProfilesQuery,
  PlatformProxyProfile,
  PlatformProxyProfilePage,
  SavePlatformProxyProfileBody,
} from './platform-proxy-profile.types'

function mapProfile(dto: PlatformProxyProfileRespDto): PlatformProxyProfile {
  return {
    id: dto.id ?? '',
    profileCode: dto.profileCode ?? '',
    profileName: dto.profileName ?? '',
    proxyType: dto.proxyType ?? 'HTTP',
    proxyHost: dto.proxyHost ?? '',
    proxyPort: dto.proxyPort ?? 0,
    username: dto.username ?? null,
    credentialConfigured: Boolean(dto.credentialConfigured),
    enabled: Boolean(dto.enabled),
    deleted: Boolean(dto.deleted),
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  }
}

function mapBinding(dto: AssetProxyBindingRespDto): AssetProxyBinding {
  return {
    apiCode: dto.apiCode ?? '',
    proxyProfileId: dto.proxyProfileId ?? null,
    proxyProfileCode: dto.proxyProfileCode ?? null,
    proxyProfileName: dto.proxyProfileName ?? null,
  }
}

function toSaveReq(body: SavePlatformProxyProfileBody): CreatePlatformProxyProfileReqDto {
  return {
    profileCode: body.profileCode,
    profileName: body.profileName,
    proxyType: body.proxyType,
    proxyHost: body.proxyHost,
    proxyPort: body.proxyPort,
    username: body.username,
    password: body.password,
    enabled: body.enabled,
  }
}

export async function listPlatformProxyProfiles(
  query?: ListPlatformProxyProfilesQuery,
): Promise<PlatformProxyProfilePage> {
  const params: ListPlatformProxyProfilesQueryDto | undefined = query
  const { data } = await http.get<PlatformProxyProfilePageRespDto>('v1/platform/proxy-profiles', {
    params,
  })
  return {
    items: (data.items ?? []).map(mapProfile),
    page: data.page ?? query?.page ?? 1,
    pageSize: data.size ?? query?.size ?? 20,
    total: data.total ?? 0,
  }
}

export async function getPlatformProxyProfile(profileId: string): Promise<PlatformProxyProfile> {
  const { data } = await http.get<PlatformProxyProfileRespDto>(
    `v1/platform/proxy-profiles/${profileId}`,
  )
  return mapProfile(data)
}

export async function createPlatformProxyProfile(
  body: SavePlatformProxyProfileBody,
): Promise<PlatformProxyProfile> {
  const { data } = await http.post<PlatformProxyProfileRespDto>(
    'v1/platform/proxy-profiles',
    toSaveReq(body),
  )
  return mapProfile(data)
}

export async function updatePlatformProxyProfile(
  profileId: string,
  body: SavePlatformProxyProfileBody,
): Promise<PlatformProxyProfile> {
  const req: UpdatePlatformProxyProfileReqDto = toSaveReq(body)
  const { data } = await http.put<PlatformProxyProfileRespDto>(
    `v1/platform/proxy-profiles/${profileId}`,
    req,
  )
  return mapProfile(data)
}

export async function enablePlatformProxyProfile(profileId: string): Promise<PlatformProxyProfile> {
  const { data } = await http.patch<PlatformProxyProfileRespDto>(
    `v1/platform/proxy-profiles/${profileId}/enable`,
  )
  return mapProfile(data)
}

export async function disablePlatformProxyProfile(
  profileId: string,
): Promise<PlatformProxyProfile> {
  const { data } = await http.patch<PlatformProxyProfileRespDto>(
    `v1/platform/proxy-profiles/${profileId}/disable`,
  )
  return mapProfile(data)
}

export async function deletePlatformProxyProfile(profileId: string): Promise<PlatformProxyProfile> {
  const { data } = await http.delete<PlatformProxyProfileRespDto>(
    `v1/platform/proxy-profiles/${profileId}`,
  )
  return mapProfile(data)
}

export async function bindProxyProfileToAsset(
  apiCode: string,
  body: BindProxyProfileBody,
): Promise<AssetProxyBinding> {
  const req: BindProxyProfileReqDto = { profileId: body.profileId }
  const { data } = await http.put<AssetProxyBindingRespDto>(
    `v1/platform/proxy-profiles/asset-bindings/${apiCode}`,
    req,
  )
  return mapBinding(data)
}

export async function unbindProxyProfileFromAsset(apiCode: string): Promise<AssetProxyBinding> {
  const { data } = await http.delete<AssetProxyBindingRespDto>(
    `v1/platform/proxy-profiles/asset-bindings/${apiCode}`,
  )
  return mapBinding(data)
}
