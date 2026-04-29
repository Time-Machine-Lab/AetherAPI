import { env } from '@/utils/env'

function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '')
}

export function buildUnifiedAccessPath(apiCode: string): string {
  return `/api/v1/access/${encodeURIComponent(apiCode)}`
}

export function buildUnifiedAccessAddress(apiCode: string): string {
  const base = trimTrailingSlash(env.apiBaseUrl || '/api')
  return `${base}/v1/access/${encodeURIComponent(apiCode)}`
}
