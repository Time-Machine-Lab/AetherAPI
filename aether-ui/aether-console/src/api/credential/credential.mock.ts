import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type {
  CurrentUserApiKeyDto,
  CurrentUserApiKeyPageDto,
  IssuedCurrentUserApiKeyDto,
} from './credential.dto'

// ── Seed data ────────────────────────────────────────────────

const apiKeys: CurrentUserApiKeyDto[] = [
  {
    credentialId: '550e8400-e29b-41d4-a716-446655440000',
    credentialCode: 'cred_20260417_0001',
    credentialName: '控制台默认密钥',
    credentialDescription: '用于本地开发环境联调',
    maskedKey: 'ak_live_****A9K2',
    keyPrefix: 'ak_live',
    status: 'ENABLED',
    expireAt: '2026-12-31T23:59:59Z',
    revokedAt: null,
    createdAt: '2026-04-17T09:00:00Z',
    updatedAt: '2026-04-17T09:00:00Z',
    lastUsedSnapshot: {
      lastUsedAt: '2026-04-17T10:15:00Z',
      lastUsedChannel: 'UNIFIED_ACCESS',
      lastUsedResult: 'SUCCESS',
    },
  },
  {
    credentialId: '660e8400-e29b-41d4-a716-446655440001',
    credentialCode: 'cred_20260417_0002',
    credentialName: 'CI 流水线专用密钥',
    credentialDescription: '持续集成自动化测试',
    maskedKey: 'ak_live_****B7X1',
    keyPrefix: 'ak_live',
    status: 'DISABLED',
    expireAt: null,
    revokedAt: null,
    createdAt: '2026-04-15T14:30:00Z',
    updatedAt: '2026-04-16T08:00:00Z',
    lastUsedSnapshot: null,
  },
  {
    credentialId: '770e8400-e29b-41d4-a716-446655440002',
    credentialCode: 'cred_20260410_0003',
    credentialName: '已吊销的旧密钥',
    credentialDescription: null,
    maskedKey: 'ak_live_****C3P0',
    keyPrefix: 'ak_live',
    status: 'REVOKED',
    expireAt: '2026-06-01T00:00:00Z',
    revokedAt: '2026-04-10T11:00:00Z',
    createdAt: '2026-03-01T10:00:00Z',
    updatedAt: '2026-04-10T11:00:00Z',
    lastUsedSnapshot: {
      lastUsedAt: '2026-04-09T22:00:00Z',
      lastUsedChannel: 'UNIFIED_ACCESS',
      lastUsedResult: 'SUCCESS',
    },
  },
]

// ── Helpers ──────────────────────────────────────────────────

let counter = apiKeys.length

function ok<T>(data: T, status = 200): AxiosResponse<T> {
  return {
    data,
    status,
    statusText: 'OK',
    headers: {},
    config: { headers: {} } as InternalAxiosRequestConfig,
  }
}

function notFound(): never {
  throw Object.assign(new Error('Not Found'), {
    response: { status: 404, data: { message: 'API Key not found' } },
  })
}

function conflict(message: string): never {
  throw Object.assign(new Error(message), {
    response: { status: 409, data: { message } },
  })
}

// ── Route table ──────────────────────────────────────────────

type MockHandler = (
  params: Record<string, string>,
  body?: unknown,
  match?: RegExpMatchArray,
) => AxiosResponse

export const credentialMockRoutes: { method: string; pattern: RegExp; handler: MockHandler }[] = [
  // List
  {
    method: 'GET',
    pattern: /^\/api\/v1\/current-user\/api-keys$/,
    handler: (params) => {
      const statusFilter = params.status
      let filtered = [...apiKeys]
      if (statusFilter) {
        filtered = filtered.filter((k) => k.status === statusFilter)
      }
      const page = Number(params.page ?? 1)
      const size = Number(params.size ?? 20)
      const start = (page - 1) * size
      const items = filtered.slice(start, start + size)
      const result: CurrentUserApiKeyPageDto = { items, page, size, total: filtered.length }
      return ok(result)
    },
  },
  // Detail
  {
    method: 'GET',
    pattern: /^\/api\/v1\/current-user\/api-keys\/([^/]+)$/,
    handler: (_, __, match) => {
      const key = apiKeys.find((k) => k.credentialId === match![1])
      if (!key) notFound()
      return ok({ ...key })
    },
  },
  // Create
  {
    method: 'POST',
    pattern: /^\/api\/v1\/current-user\/api-keys$/,
    handler: (_, body) => {
      const b = body as { credentialName: string; credentialDescription?: string; expireAt?: string }
      counter++
      const id = `mock-${Date.now()}-${counter}`
      const now = new Date().toISOString()
      const plaintext = `ak_live_${Math.random().toString(36).slice(2, 14)}`
      const newKey: CurrentUserApiKeyDto = {
        credentialId: id,
        credentialCode: `cred_mock_${String(counter).padStart(4, '0')}`,
        credentialName: b.credentialName,
        credentialDescription: b.credentialDescription ?? null,
        maskedKey: `ak_live_****${plaintext.slice(-4).toUpperCase()}`,
        keyPrefix: 'ak_live',
        status: 'ENABLED',
        expireAt: b.expireAt ?? null,
        revokedAt: null,
        createdAt: now,
        updatedAt: now,
        lastUsedSnapshot: null,
      }
      apiKeys.unshift(newKey)
      const issued: IssuedCurrentUserApiKeyDto = { ...newKey, plaintextKey: plaintext }
      return ok(issued, 201)
    },
  },
  // Enable
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/current-user\/api-keys\/([^/]+)\/enable$/,
    handler: (_, __, match) => {
      const key = apiKeys.find((k) => k.credentialId === match![1])
      if (!key) notFound()
      if (key.status !== 'DISABLED') conflict('Only DISABLED keys can be enabled')
      key.status = 'ENABLED'
      key.updatedAt = new Date().toISOString()
      return ok({ ...key })
    },
  },
  // Disable
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/current-user\/api-keys\/([^/]+)\/disable$/,
    handler: (_, __, match) => {
      const key = apiKeys.find((k) => k.credentialId === match![1])
      if (!key) notFound()
      if (key.status !== 'ENABLED') conflict('Only ENABLED keys can be disabled')
      key.status = 'DISABLED'
      key.updatedAt = new Date().toISOString()
      return ok({ ...key })
    },
  },
  // Revoke
  {
    method: 'PATCH',
    pattern: /^\/api\/v1\/current-user\/api-keys\/([^/]+)\/revoke$/,
    handler: (_, __, match) => {
      const key = apiKeys.find((k) => k.credentialId === match![1])
      if (!key) notFound()
      if (key.status === 'REVOKED') conflict('Key is already revoked')
      key.status = 'REVOKED'
      key.revokedAt = new Date().toISOString()
      key.updatedAt = new Date().toISOString()
      return ok({ ...key })
    },
  },
]
