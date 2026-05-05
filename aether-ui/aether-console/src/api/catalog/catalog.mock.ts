import type { AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type {
  CategoryDto,
  AssetDto,
  DiscoveryAssetDto,
  DiscoveryAssetDetailDto,
  PageDto,
} from './catalog.dto'
import type {
  ApiSubscriptionDto,
  ApiSubscriptionPageDto,
  ApiSubscriptionStatusDto,
} from '@/api/subscription/subscription.dto'
import { credentialMockRoutes } from '@/api/credential/credential.mock'
import { apiCallLogMockRoutes } from '@/api/api-call-log/api-call-log.mock'

// ── Seed data ────────────────────────────────────────────────

const categories: CategoryDto[] = [
  {
    categoryCode: 'cat-ai',
    name: 'AI 接口',
    status: 'ENABLED',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  {
    categoryCode: 'cat-search',
    name: '搜索增强',
    status: 'ENABLED',
    createdAt: '2026-01-02T00:00:00Z',
    updatedAt: '2026-01-02T00:00:00Z',
  },
  {
    categoryCode: 'cat-data',
    name: '数据服务',
    status: 'DISABLED',
    createdAt: '2026-01-03T00:00:00Z',
    updatedAt: '2026-01-03T00:00:00Z',
  },
]

const assets: AssetDto[] = [
  {
    apiCode: 'deepseek-v3',
    assetName: 'DeepSeek V3',
    displayName: 'DeepSeek V3',
    assetType: 'AI_API',
    categoryCode: 'cat-ai',
    status: 'PUBLISHED',
    publisherDisplayName: 'Aether Labs',
    publishedAt: '2026-04-20T10:00:00Z',
    requestMethod: 'POST',
    upstreamUrl: 'https://upstream.example.com/deepseek/chat',
    description: '通用推理与工具调用旗舰模型。',
    authScheme: 'HEADER_TOKEN',
    authConfig: 'Authorization: Bearer deepseek-token',
    aiProfile: {
      provider: 'DeepSeek',
      model: 'deepseek-v3',
      streaming: true,
      tags: ['reasoning', 'tool-call'],
    },
  },
  {
    apiCode: 'kimi-k2',
    assetName: 'Kimi K2',
    displayName: 'Kimi K2',
    assetType: 'AI_API',
    categoryCode: 'cat-ai',
    status: 'PUBLISHED',
    publisherDisplayName: 'Moonshot Team',
    publishedAt: '2026-04-21T10:00:00Z',
    requestMethod: 'POST',
    upstreamUrl: 'https://upstream.example.com/kimi/chat',
    description: '长上下文与 Agent 场景旗舰模型。',
    authScheme: 'HEADER_TOKEN',
    authConfig: 'Authorization: Bearer kimi-token',
    aiProfile: { provider: 'Moonshot', model: 'kimi-k2', streaming: true, tags: ['reasoning'] },
  },
  {
    apiCode: 'baidu-search',
    assetName: 'Baidu Search API',
    displayName: 'Baidu Search API',
    assetType: 'STANDARD_API',
    categoryCode: 'cat-search',
    status: 'PUBLISHED',
    publisherDisplayName: 'Search Studio',
    publishedAt: '2026-04-22T10:00:00Z',
    requestMethod: 'GET',
    upstreamUrl: 'https://upstream.example.com/search',
    description: '联网检索与引用增强接口。',
    authScheme: 'QUERY_TOKEN',
    authConfig: 'access_token=search-token',
  },
  {
    apiCode: 'weather-api',
    assetName: 'Weather Data API',
    displayName: 'Weather Data API',
    assetType: 'STANDARD_API',
    categoryCode: 'cat-data',
    status: 'UNPUBLISHED',
    publisherDisplayName: 'Weather Studio',
    publishedAt: null,
    requestMethod: 'GET',
    upstreamUrl: 'https://upstream.example.com/weather',
    description: '实时天气与预报数据服务。',
    authScheme: 'QUERY_TOKEN',
    authConfig: 'access_token=weather-token',
  },
]

const ownerAccessApiCodes = new Set(['kimi-k2'])

const subscriptions: ApiSubscriptionDto[] = [
  {
    subscriptionId: 'sub-baidu-search',
    apiCode: 'baidu-search',
    assetName: 'Baidu Search API',
    assetOwnerUserId: 'user-search-studio',
    subscriptionStatus: 'ACTIVE',
    subscribed: true,
    ownerAccess: false,
    createdAt: '2026-05-02T08:00:00Z',
    updatedAt: '2026-05-02T08:00:00Z',
    cancelledAt: null,
  },
  {
    subscriptionId: 'sub-weather-cancelled',
    apiCode: 'weather-api',
    assetName: 'Weather Data API',
    assetOwnerUserId: 'user-weather-studio',
    subscriptionStatus: 'CANCELLED',
    subscribed: false,
    ownerAccess: false,
    createdAt: '2026-05-01T08:00:00Z',
    updatedAt: '2026-05-02T09:00:00Z',
    cancelledAt: '2026-05-02T09:00:00Z',
  },
]

// ── Helpers ──────────────────────────────────────────────────

function page<T>(items: T[], params: Record<string, string>): PageDto<T> {
  const p = Number(params.page ?? 1)
  const size = Number(params.size ?? params.pageSize ?? 20)
  const keyword = (params.keyword ?? '').toLowerCase()
  const filtered = keyword
    ? ((items as Record<string, unknown>[]).filter((item) =>
        Object.values(item).some((v) => String(v).toLowerCase().includes(keyword)),
      ) as T[])
    : items
  return {
    items: filtered.slice((p - 1) * size, p * size),
    total: filtered.length,
    page: p,
    pageSize: size,
  }
}

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

// ── Route table ──────────────────────────────────────────────

type MockHandler = (
  params: Record<string, string>,
  body?: unknown,
  match?: RegExpMatchArray,
) => AxiosResponse

const routes: { method: string; pattern: RegExp; handler: MockHandler }[] = [
  // Discovery
  {
    method: 'GET',
    pattern: /^\/api\/v1\/discovery\/assets$/,
    handler: (params) => {
      const list: DiscoveryAssetDto[] = assets
        .filter((a) => a.status === 'PUBLISHED')
        .map((a) => ({
          apiCode: a.apiCode,
          assetName: a.assetName ?? a.displayName ?? null,
          assetType: a.assetType,
          category: {
            categoryCode: a.categoryCode ?? null,
            categoryName: categories.find((c) => c.categoryCode === a.categoryCode)?.name ?? null,
          },
          publisher: { displayName: a.publisherDisplayName ?? null },
          publishedAt: a.publishedAt ?? null,
        }))
      return ok(page(list, params))
    },
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/discovery\/assets\/(.+)$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset || asset.status !== 'PUBLISHED' || asset.deleted) notFound()
      const aiProfile = asset.aiCapabilityProfile
        ? {
            provider: asset.aiCapabilityProfile.provider,
            model: asset.aiCapabilityProfile.model,
            streamingSupported: asset.aiCapabilityProfile.streamingSupported,
            capabilityTags: asset.aiCapabilityProfile.capabilityTags,
          }
        : asset.aiProfile
          ? {
              provider: asset.aiProfile.provider,
              model: asset.aiProfile.model,
              streamingSupported: asset.aiProfile.streaming,
              capabilityTags: asset.aiProfile.tags,
            }
          : null
      const detail: DiscoveryAssetDetailDto = {
        apiCode: asset.apiCode,
        assetName: asset.assetName ?? asset.displayName ?? null,
        assetType: asset.assetType,
        category: {
          categoryCode: asset.categoryCode ?? null,
          categoryName: categories.find((c) => c.categoryCode === asset.categoryCode)?.name ?? null,
        },
        publisher: { displayName: asset.publisherDisplayName ?? null },
        publishedAt: asset.publishedAt ?? null,
        description: asset.description,
        authScheme: (asset.authScheme as 'NONE' | 'HEADER_TOKEN' | 'QUERY_TOKEN' | null) ?? null,
        requestMethod: asset.assetType === 'AI_API' ? 'POST' : 'GET',
        requestTemplate:
          asset.assetType === 'AI_API'
            ? '{"model":"' +
              (aiProfile?.model ?? '') +
              '","messages":[{"role":"user","content":"Hello"}]}'
            : undefined,
        exampleSnapshot: { requestExample: '{"code":0,"data":{}}' },
        aiCapabilityProfile: aiProfile,
      }
      return ok(detail)
    },
  },
  // Current-user API subscriptions
  {
    method: 'GET',
    pattern: /^\/api\/v1\/current-user\/api-subscriptions$/,
    handler: (params) => {
      const result = page([...subscriptions], params)
      const data: ApiSubscriptionPageDto = {
        items: result.items,
        total: result.total,
        page: result.page,
        size: Number(params.size ?? params.pageSize ?? 20),
      }
      return ok(data)
    },
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/current-user\/api-subscriptions$/,
    handler: (_, body) => {
      const apiCode = (body as { apiCode?: string }).apiCode
      const asset = assets.find((a) => a.apiCode === apiCode)
      if (!apiCode || !asset || asset.status !== 'PUBLISHED' || asset.deleted) notFound()
      if (ownerAccessApiCodes.has(apiCode)) {
        return ok({
          subscriptionId: null,
          apiCode,
          assetName: asset.assetName ?? asset.displayName ?? null,
          assetOwnerUserId: 'current-user',
          subscriptionStatus: 'OWNER',
          subscribed: false,
          ownerAccess: true,
          createdAt: null,
          updatedAt: null,
          cancelledAt: null,
        } satisfies ApiSubscriptionDto)
      }
      const active = subscriptions.find(
        (s) => s.apiCode === apiCode && s.subscriptionStatus === 'ACTIVE',
      )
      if (active) return ok({ ...active })
      const now = new Date().toISOString()
      const subscription: ApiSubscriptionDto = {
        subscriptionId: `sub-${apiCode}-${Date.now()}`,
        apiCode,
        assetName: asset.assetName ?? asset.displayName ?? null,
        assetOwnerUserId: `owner-${apiCode}`,
        subscriptionStatus: 'ACTIVE',
        subscribed: true,
        ownerAccess: false,
        createdAt: now,
        updatedAt: now,
        cancelledAt: null,
      }
      subscriptions.unshift(subscription)
      return ok({ ...subscription })
    },
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/current-user\/api-subscriptions\/status$/,
    handler: (params) => {
      const apiCode = params.apiCode
      if (ownerAccessApiCodes.has(apiCode)) {
        return ok({
          apiCode,
          accessStatus: 'OWNER',
          subscriptionId: null,
          subscriptionStatus: null,
          subscribed: false,
          ownerAccess: true,
        } satisfies ApiSubscriptionStatusDto)
      }
      const active = subscriptions.find(
        (s) => s.apiCode === apiCode && s.subscriptionStatus === 'ACTIVE',
      )
      if (active) {
        return ok({
          apiCode,
          accessStatus: 'SUBSCRIBED',
          subscriptionId: active.subscriptionId,
          subscriptionStatus: 'ACTIVE',
          subscribed: true,
          ownerAccess: false,
        } satisfies ApiSubscriptionStatusDto)
      }
      return ok({
        apiCode,
        accessStatus: 'NOT_SUBSCRIBED',
        subscriptionId: null,
        subscriptionStatus: null,
        subscribed: false,
        ownerAccess: false,
      } satisfies ApiSubscriptionStatusDto)
    },
  },
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/current-user\/api-subscriptions\/(.+)\/cancel$/,
    handler: (_, __, match) => {
      const subscription = subscriptions.find((s) => s.subscriptionId === match![1])
      if (!subscription) notFound()
      if (subscription.subscriptionStatus !== 'ACTIVE') return ok({ ...subscription })
      subscription.subscriptionStatus = 'CANCELLED'
      subscription.subscribed = false
      subscription.updatedAt = new Date().toISOString()
      subscription.cancelledAt = subscription.updatedAt
      return ok({ ...subscription })
    },
  },
  // Categories
  {
    method: 'GET',
    pattern: /^\/api\/v1\/categories$/,
    handler: (params) => ok(page([...categories], params)),
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/categories$/,
    handler: (_, body) => {
      const b = body as { name: string }
      const cat: CategoryDto = {
        categoryCode: `cat-${Date.now()}`,
        name: b.name,
        status: 'ENABLED',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }
      categories.unshift(cat)
      return ok(cat)
    },
  },
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/categories\/(.+)$/,
    handler: (_, body, match) => {
      const cat = categories.find((c) => c.categoryCode === match![1])
      if (!cat) notFound()
      cat.name = (body as { name: string }).name
      cat.updatedAt = new Date().toISOString()
      return ok({ ...cat })
    },
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/categories\/(.+)\/enable$/,
    handler: (_, __, match) => {
      const cat = categories.find((c) => c.categoryCode === match![1])
      if (!cat) notFound()
      cat.status = 'ENABLED'
      return ok({ ...cat })
    },
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/categories\/(.+)\/disable$/,
    handler: (_, __, match) => {
      const cat = categories.find((c) => c.categoryCode === match![1])
      if (!cat) notFound()
      cat.status = 'DISABLED'
      return ok({ ...cat })
    },
  },
  // Assets
  {
    method: 'GET',
    pattern: /^\/api\/v1\/current-user\/assets$/,
    handler: (params) => {
      const status = params.status
      const categoryCode = params.categoryCode
      const list = assets.filter(
        (asset) =>
          !asset.deleted &&
          (!status || asset.status === status) &&
          (!categoryCode || asset.categoryCode === categoryCode),
      )
      return ok(page(list, params))
    },
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/current-user\/assets$/,
    handler: (_, body) => {
      const b = body as AssetDto
      const now = new Date().toISOString()
      const asset: AssetDto = {
        ...b,
        categoryCode: b.categoryCode ?? null,
        status: 'DRAFT',
        publisherDisplayName: 'Current User',
        publishedAt: null,
        deleted: false,
        createdAt: now,
        updatedAt: now,
      }
      assets.push(asset)
      return ok(asset)
    },
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/current-user\/assets\/(.+)$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset || asset.deleted) notFound()
      return ok({ ...asset })
    },
  },
  {
    method: 'PUT',
    pattern: /^\/api\/v1\/current-user\/assets\/(.+)$/,
    handler: (_, body, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset || asset.deleted) notFound()
      Object.assign(asset, body)
      if (asset.status === 'PUBLISHED') {
        asset.status = 'UNPUBLISHED'
      }
      asset.updatedAt = new Date().toISOString()
      return ok({ ...asset })
    },
  },
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/current-user\/assets\/(.+)\/publish$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset || asset.deleted) notFound()
      asset.status = 'PUBLISHED'
      asset.publishedAt = new Date().toISOString()
      asset.updatedAt = asset.publishedAt
      return ok({ ...asset })
    },
  },
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/current-user\/assets\/(.+)\/unpublish$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset || asset.deleted) notFound()
      asset.status = 'UNPUBLISHED'
      asset.updatedAt = new Date().toISOString()
      return ok({ ...asset })
    },
  },
  {
    method: 'DELETE',
    pattern: /^\/api\/v1\/current-user\/assets\/(.+)$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset || asset.deleted) notFound()
      asset.deleted = true
      asset.updatedAt = new Date().toISOString()
      return ok({ ...asset })
    },
  },
  {
    method: 'PUT',
    pattern: /^\/api\/v1\/current-user\/assets\/(.+)\/ai-profile$/,
    handler: (_, body, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset || asset.deleted) notFound()
      asset.aiCapabilityProfile = body as AssetDto['aiCapabilityProfile']
      asset.updatedAt = new Date().toISOString()
      return ok({ ...asset })
    },
  },
]

// ── Adapter ──────────────────────────────────────────────────

export function mockAdapter(config: AxiosRequestConfig): Promise<AxiosResponse> {
  const method = (config.method ?? 'GET').toUpperCase()
  // axios 1.x: buildFullPath is called inside built-in adapters, not in dispatchRequest.
  // Custom adapters receive raw config.url without baseURL, so we resolve it here.
  const base = (config.baseURL ?? '').replace(/\/+$/, '')
  const raw = (config.url ?? '').replace(/\?.*$/, '')
  const url = base && !raw.startsWith(base) ? base + '/' + raw.replace(/^\/+/, '') : raw
  const params: Record<string, string> = {}
  if (config.params) {
    for (const [k, v] of Object.entries(config.params)) {
      if (v !== undefined && v !== null) params[k] = String(v)
    }
  }

  const allRoutes = [...routes, ...credentialMockRoutes, ...apiCallLogMockRoutes]
  for (const route of allRoutes) {
    if (route.method !== method) continue
    const match = url.match(route.pattern)
    if (!match) continue
    const body =
      config.data != null
        ? typeof config.data === 'string'
          ? JSON.parse(config.data)
          : config.data
        : undefined
    return Promise.resolve(route.handler(params, body, match))
  }

  return Promise.reject(
    Object.assign(new Error(`[mock] No handler: ${method} ${url}`), {
      response: { status: 404, data: { message: 'Mock: route not found' } },
    }),
  )
}
