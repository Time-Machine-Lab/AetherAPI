import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import type { CategoryDto, AssetDto, DiscoveryAssetDto, DiscoveryAssetDetailDto, PageDto } from './catalog.dto'

// ── Seed data ────────────────────────────────────────────────

const categories: CategoryDto[] = [
  { categoryCode: 'cat-ai', name: 'AI 接口', status: 'ENABLED', createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' },
  { categoryCode: 'cat-search', name: '搜索增强', status: 'ENABLED', createdAt: '2026-01-02T00:00:00Z', updatedAt: '2026-01-02T00:00:00Z' },
  { categoryCode: 'cat-data', name: '数据服务', status: 'DISABLED', createdAt: '2026-01-03T00:00:00Z', updatedAt: '2026-01-03T00:00:00Z' },
]

const assets: AssetDto[] = [
  {
    apiCode: 'deepseek-v3',
    displayName: 'DeepSeek V3',
    assetType: 'AI_API',
    categoryCode: 'cat-ai',
    status: 'ENABLED',
    description: '通用推理与工具调用旗舰模型。',
    authScheme: 'Bearer Token',
    aiProfile: { provider: 'DeepSeek', model: 'deepseek-v3', streaming: true, tags: ['reasoning', 'tool-call'] },
  },
  {
    apiCode: 'kimi-k2',
    displayName: 'Kimi K2',
    assetType: 'AI_API',
    categoryCode: 'cat-ai',
    status: 'ENABLED',
    description: '长上下文与 Agent 场景旗舰模型。',
    authScheme: 'Bearer Token',
    aiProfile: { provider: 'Moonshot', model: 'kimi-k2', streaming: true, tags: ['reasoning'] },
  },
  {
    apiCode: 'baidu-search',
    displayName: 'Baidu Search API',
    assetType: 'STANDARD_API',
    categoryCode: 'cat-search',
    status: 'ENABLED',
    description: '联网检索与引用增强接口。',
    authScheme: 'API Key',
  },
  {
    apiCode: 'weather-api',
    displayName: 'Weather Data API',
    assetType: 'STANDARD_API',
    categoryCode: 'cat-data',
    status: 'DISABLED',
    description: '实时天气与预报数据服务。',
    authScheme: 'API Key',
  },
]

// ── Helpers ──────────────────────────────────────────────────

function page<T>(items: T[], params: Record<string, string>): PageDto<T> {
  const p = Number(params.page ?? 1)
  const size = Number(params.pageSize ?? 20)
  const keyword = (params.keyword ?? '').toLowerCase()
  const filtered = keyword
    ? (items as Record<string, unknown>[]).filter((item) =>
        Object.values(item).some((v) => String(v).toLowerCase().includes(keyword)),
      ) as T[]
    : items
  return { items: filtered.slice((p - 1) * size, p * size), total: filtered.length, page: p, pageSize: size }
}

function ok<T>(data: T): AxiosResponse<T> {
  return { data, status: 200, statusText: 'OK', headers: {}, config: {} as AxiosRequestConfig }
}

function notFound(): never {
  const err = Object.assign(new Error('Not Found'), { response: { status: 404, data: { message: 'Not Found' } } })
  throw err
}

// ── Route table ──────────────────────────────────────────────

type MockHandler = (params: Record<string, string>, body?: unknown, match?: RegExpMatchArray) => AxiosResponse

const routes: { method: string; pattern: RegExp; handler: MockHandler }[] = [
  // Discovery
  {
    method: 'GET',
    pattern: /^\/api\/v1\/discovery\/assets$/,
    handler: (params) => {
      const list: DiscoveryAssetDto[] = assets
        .filter((a) => a.status === 'ENABLED')
        .map((a) => ({ apiCode: a.apiCode, displayName: a.displayName, assetType: a.assetType, categoryCode: a.categoryCode, categoryName: categories.find((c) => c.categoryCode === a.categoryCode)?.name }))
      return ok(page(list, params))
    },
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/discovery\/assets\/(.+)$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset) notFound()
      const detail: DiscoveryAssetDetailDto = {
        apiCode: asset.apiCode,
        displayName: asset.displayName,
        assetType: asset.assetType,
        categoryCode: asset.categoryCode,
        categoryName: categories.find((c) => c.categoryCode === asset.categoryCode)?.name,
        description: asset.description,
        authScheme: asset.authScheme,
        methods: asset.assetType === 'AI_API' ? ['POST /chat/completions'] : ['GET', 'POST'],
        requestTemplate: asset.assetType === 'AI_API' ? '{"model":"' + asset.aiProfile?.model + '","messages":[{"role":"user","content":"Hello"}]}' : undefined,
        exampleSnapshot: '{"code":0,"data":{}}',
        aiProfile: asset.aiProfile,
      }
      return ok(detail)
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
      const cat: CategoryDto = { categoryCode: `cat-${Date.now()}`, name: b.name, status: 'ENABLED', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() }
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
    method: 'POST',
    pattern: /^\/api\/v1\/assets$/,
    handler: (_, body) => {
      const b = body as AssetDto
      const asset: AssetDto = { ...b, status: 'DRAFT' }
      assets.push(asset)
      return ok(asset)
    },
  },
  {
    method: 'GET',
    pattern: /^\/api\/v1\/assets\/(.+)$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset) notFound()
      return ok({ ...asset })
    },
  },
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/assets\/(.+)$/,
    handler: (_, body, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset) notFound()
      Object.assign(asset, body)
      return ok({ ...asset })
    },
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/assets\/(.+)\/enable$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset) notFound()
      asset.status = 'ENABLED'
      return ok({ ...asset })
    },
  },
  {
    method: 'POST',
    pattern: /^\/api\/v1\/assets\/(.+)\/disable$/,
    handler: (_, __, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset) notFound()
      asset.status = 'DISABLED'
      return ok({ ...asset })
    },
  },
  {
    method: 'PUT',
    pattern: /^\/api\/v1\/assets\/(.+)\/ai-profile$/,
    handler: (_, body, match) => {
      const asset = assets.find((a) => a.apiCode === match![1])
      if (!asset) notFound()
      asset.aiProfile = body as AssetDto['aiProfile']
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

  for (const route of routes) {
    if (route.method !== method) continue
    const match = url.match(route.pattern)
    if (!match) continue
    const body = config.data != null
      ? (typeof config.data === 'string' ? JSON.parse(config.data) : config.data)
      : undefined
    return Promise.resolve(route.handler(params, body, match))
  }

  return Promise.reject(
    Object.assign(new Error(`[mock] No handler: ${method} ${url}`), {
      response: { status: 404, data: { message: 'Mock: route not found' } },
    }),
  )
}
