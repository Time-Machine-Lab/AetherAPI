import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type {
  AssetProxyBindingRespDto,
  BindProxyProfileReqDto,
  CreatePlatformProxyProfileReqDto,
  PlatformProxyAssetCandidateRespDto,
  PlatformProxyProfileRespDto,
} from './platform-proxy-profile.dto'

type MockHandler = (
  params: Record<string, string>,
  body?: unknown,
  match?: RegExpMatchArray,
) => AxiosResponse

interface MockRoute {
  method: string
  pattern: RegExp
  handler: MockHandler
}

const proxyProfiles: PlatformProxyProfileRespDto[] = [
  {
    id: 'proxy-corp-egress',
    profileCode: 'corp-egress',
    profileName: 'Corporate egress',
    proxyType: 'HTTP',
    proxyHost: 'proxy.internal',
    proxyPort: 8080,
    username: 'operator',
    credentialConfigured: true,
    enabled: true,
    deleted: false,
    createdAt: '2026-05-08T00:00:00Z',
    updatedAt: '2026-05-08T00:00:00Z',
  },
  {
    id: 'proxy-disabled',
    profileCode: 'disabled-lab',
    profileName: 'Disabled lab proxy',
    proxyType: 'HTTP',
    proxyHost: 'lab-proxy.internal',
    proxyPort: 18080,
    username: null,
    credentialConfigured: false,
    enabled: false,
    deleted: false,
    createdAt: '2026-05-07T00:00:00Z',
    updatedAt: '2026-05-07T00:00:00Z',
  },
]

const assetBindings = new Map<string, AssetProxyBindingRespDto>([
  [
    'baidu-search',
    {
      apiCode: 'baidu-search',
      proxyProfileId: 'proxy-corp-egress',
      proxyProfileCode: 'corp-egress',
      proxyProfileName: 'Corporate egress',
    },
  ],
])

const assetCandidates: PlatformProxyAssetCandidateRespDto[] = [
  {
    apiCode: 'baidu-search',
    assetName: 'Baidu Search',
    assetType: 'STANDARD_API',
    status: 'PUBLISHED',
    publisherDisplayName: 'Search Platform',
    proxyProfileId: 'proxy-corp-egress',
    proxyProfileCode: 'corp-egress',
    proxyProfileName: 'Corporate egress',
    createdAt: '2026-05-06T00:00:00Z',
    updatedAt: '2026-05-08T00:00:00Z',
  },
  {
    apiCode: 'weather-api',
    assetName: 'Weather API',
    assetType: 'STANDARD_API',
    status: 'PUBLISHED',
    publisherDisplayName: 'Operations Team',
    proxyProfileId: null,
    proxyProfileCode: null,
    proxyProfileName: null,
    createdAt: '2026-05-05T00:00:00Z',
    updatedAt: '2026-05-07T00:00:00Z',
  },
  {
    apiCode: 'deepseek-v3',
    assetName: 'DeepSeek V3',
    assetType: 'AI_API',
    status: 'DRAFT',
    publisherDisplayName: 'AI Lab',
    proxyProfileId: null,
    proxyProfileCode: null,
    proxyProfileName: null,
    createdAt: '2026-05-04T00:00:00Z',
    updatedAt: '2026-05-04T00:00:00Z',
  },
  {
    apiCode: 'legacy-stock',
    assetName: 'Legacy Stock Quote',
    assetType: 'STANDARD_API',
    status: 'UNPUBLISHED',
    publisherDisplayName: 'Finance Team',
    proxyProfileId: 'proxy-disabled',
    proxyProfileCode: 'disabled-lab',
    proxyProfileName: 'Disabled lab proxy',
    createdAt: '2026-05-03T00:00:00Z',
    updatedAt: '2026-05-03T00:00:00Z',
  },
]

function ok<T>(data: T): AxiosResponse<T> {
  return {
    data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: { headers: {} } as InternalAxiosRequestConfig,
  }
}

function notFound(): never {
  const err = Object.assign(new Error('Not Found'), {
    response: { status: 404, data: { message: 'Not Found' } },
  })
  throw err
}

function page(items: PlatformProxyProfileRespDto[], params: Record<string, string>) {
  const pageNumber = Number(params.page ?? 1)
  const size = Number(params.size ?? 20)
  const keyword = (params.keyword ?? '').toLowerCase()
  const enabled = params.enabled
  const filtered = items.filter((profile) => {
    if (profile.deleted) return false
    if (enabled === 'true' && !profile.enabled) return false
    if (enabled === 'false' && profile.enabled) return false
    if (!keyword) return true
    return [profile.profileCode, profile.profileName, profile.proxyHost]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword))
  })
  return {
    items: filtered.slice((pageNumber - 1) * size, pageNumber * size),
    total: filtered.length,
    page: pageNumber,
    size,
  }
}

function assetCandidatePage(
  items: PlatformProxyAssetCandidateRespDto[],
  params: Record<string, string>,
) {
  const pageNumber = Number(params.page ?? 1)
  const size = Number(params.size ?? 20)
  const keyword = (params.keyword ?? '').toLowerCase()
  const status = params.status
  const boundProfileId = params.boundProfileId
  const filtered = items.filter((candidate) => {
    if (status && candidate.status !== status) return false
    if (boundProfileId && candidate.proxyProfileId !== boundProfileId) return false
    if (!keyword) return true
    return [candidate.apiCode, candidate.assetName, candidate.publisherDisplayName]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword))
  })
  return {
    items: filtered.slice((pageNumber - 1) * size, pageNumber * size),
    total: filtered.length,
    page: pageNumber,
    size,
  }
}

function findProfile(profileId: string) {
  const profile = proxyProfiles.find((item) => item.id === profileId)
  if (!profile || profile.deleted) notFound()
  return profile
}

function profileBody(body: unknown): CreatePlatformProxyProfileReqDto {
  return body as CreatePlatformProxyProfileReqDto
}

export const platformProxyProfileMockRoutes: MockRoute[] = [
  {
    method: 'PUT',
    pattern: /^\/api\/v1\/platform\/proxy-profiles\/asset-bindings\/(.+)$/,
    handler: (_, body, match) => {
      const apiCode = match![1]
      const req = body as BindProxyProfileReqDto
      const profile = findProfile(req.profileId)
      if (!profile.enabled || profile.deleted) notFound()
      const binding: AssetProxyBindingRespDto = {
        apiCode,
        proxyProfileId: profile.id ?? null,
        proxyProfileCode: profile.profileCode ?? null,
        proxyProfileName: profile.profileName ?? null,
      }
      assetBindings.set(apiCode, binding)
      return ok(binding)
    },
  },
  {
    method: 'DELETE',
    pattern: /^\/api\/v1\/platform\/proxy-profiles\/asset-bindings\/(.+)$/,
    handler: (_, __, match) => {
      const apiCode = match![1]
      const binding: AssetProxyBindingRespDto = {
        apiCode,
        proxyProfileId: null,
        proxyProfileCode: null,
        proxyProfileName: null,
      }
      assetBindings.set(apiCode, binding)
      return ok(binding)
    },
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/platform\/proxy-profiles$/,
    handler: (params) => ok(page(proxyProfiles, params)),
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/platform\/proxy-profiles\/asset-binding-candidates$/,
    handler: (params) => ok(assetCandidatePage(assetCandidates, params)),
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/platform\/proxy-profiles$/,
    handler: (_, body) => {
      const req = profileBody(body)
      const now = new Date().toISOString()
      const profile: PlatformProxyProfileRespDto = {
        id: `proxy-${Date.now()}`,
        profileCode: req.profileCode,
        profileName: req.profileName,
        proxyType: req.proxyType,
        proxyHost: req.proxyHost,
        proxyPort: req.proxyPort,
        username: req.username ?? null,
        credentialConfigured: Boolean(req.password),
        enabled: req.enabled ?? true,
        deleted: false,
        createdAt: now,
        updatedAt: now,
      }
      proxyProfiles.unshift(profile)
      return ok(profile)
    },
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/platform\/proxy-profiles\/(.+)$/,
    handler: (_, __, match) => ok({ ...findProfile(match![1]) }),
  },
  {
    method: 'PUT',
    pattern: /^\/api\/v1\/platform\/proxy-profiles\/(.+)$/,
    handler: (_, body, match) => {
      const profile = findProfile(match![1])
      const req = profileBody(body)
      Object.assign(profile, {
        profileCode: req.profileCode,
        profileName: req.profileName,
        proxyType: req.proxyType,
        proxyHost: req.proxyHost,
        proxyPort: req.proxyPort,
        username: req.username ?? null,
        credentialConfigured: req.password ? true : profile.credentialConfigured,
        enabled: req.enabled ?? profile.enabled,
        updatedAt: new Date().toISOString(),
      })
      return ok({ ...profile })
    },
  },
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/platform\/proxy-profiles\/(.+)\/enable$/,
    handler: (_, __, match) => {
      const profile = findProfile(match![1])
      profile.enabled = true
      profile.updatedAt = new Date().toISOString()
      return ok({ ...profile })
    },
  },
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/platform\/proxy-profiles\/(.+)\/disable$/,
    handler: (_, __, match) => {
      const profile = findProfile(match![1])
      profile.enabled = false
      profile.updatedAt = new Date().toISOString()
      return ok({ ...profile })
    },
  },
  {
    method: 'DELETE',
    pattern: /^\/api\/v1\/platform\/proxy-profiles\/(.+)$/,
    handler: (_, __, match) => {
      const profile = findProfile(match![1])
      profile.deleted = true
      profile.updatedAt = new Date().toISOString()
      return ok({ ...profile })
    },
  },
]
