import axios from 'axios'
import { env } from '@/utils/env'
import { appConfig } from '@/app/app-config'
import type { UnifiedAccessPlatformFailureDto } from './unified-access.dto'
import type {
  UnifiedAccessMethod,
  UnifiedAccessPlatformFailure,
  UnifiedAccessResult,
} from './unified-access.types'

/**
 * Standalone axios instance for Unified Access calls.
 *
 * Uses X-Aether-Api-Key (not the console bearer token) and must
 * handle arbitrary upstream responses without TML-SDK normalisation.
 */
const uaHttp = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: env.requestTimeoutMs,
  headers: { 'X-App-Id': appConfig.appId },
  // Prevent axios from rejecting non-2xx so we can classify ourselves
  validateStatus: () => true,
  // Receive binary-safe data
  responseType: 'arraybuffer',
})

function isPlatformFailure(
  status: number,
  contentType: string,
  body: unknown,
): body is UnifiedAccessPlatformFailureDto {
  if (![400, 401, 404, 503].includes(status)) return false
  if (!contentType.includes('application/json')) return false
  if (typeof body !== 'object' || body === null) return false
  return 'failureType' in body
}

function parseContentType(raw?: string): string {
  return (raw ?? 'application/octet-stream').split(';')[0].trim().toLowerCase()
}

function collectHeaders(headers: Record<string, unknown>): Record<string, string> {
  const out: Record<string, string> = {}
  for (const [k, v] of Object.entries(headers)) {
    if (typeof v === 'string') out[k] = v
  }
  return out
}

export async function invokeUnifiedAccess(
  apiCode: string,
  method: UnifiedAccessMethod,
  apiKey: string,
  payload?: string,
  extraHeaders?: Record<string, string>,
): Promise<UnifiedAccessResult> {
  const url = `v1/access/${encodeURIComponent(apiCode)}`
  const hasBody = !['GET', 'DELETE'].includes(method)

  const headers: Record<string, string> = {
    'X-Aether-Api-Key': apiKey,
    ...extraHeaders,
  }

  if (hasBody && payload) {
    headers['Content-Type'] = headers['Content-Type'] ?? 'application/json'
  }

  const response = await uaHttp.request({
    url,
    method: method.toLowerCase(),
    headers,
    data: hasBody && payload ? payload : undefined,
  })

  const status: number = response.status
  const ct = parseContentType(response.headers['content-type'])
  const rawHeaders = collectHeaders(response.headers)
  const buffer: ArrayBuffer = response.data

  // Try to decode as text first for JSON / text classification
  let textDecoded: string | undefined
  try {
    textDecoded = new TextDecoder('utf-8').decode(buffer)
  } catch {
    // binary - leave undefined
  }

  // Check platform failure
  if (textDecoded && ct.includes('application/json')) {
    try {
      const parsed = JSON.parse(textDecoded)
      if (isPlatformFailure(status, ct, parsed)) {
        const failure: UnifiedAccessPlatformFailure = {
          code: parsed.code,
          message: parsed.message,
          failureType: parsed.failureType,
          traceId: parsed.traceId,
          apiCode: parsed.apiCode,
        }
        return { kind: 'platform-failure', status, contentType: ct, platformFailure: failure, rawHeaders }
      }
      // Non-failure JSON
      return { kind: 'json', status, contentType: ct, jsonBody: parsed, rawHeaders }
    } catch {
      // malformed JSON - fall through to text
    }
  }

  if (textDecoded && ct.startsWith('text/')) {
    return { kind: 'text', status, contentType: ct, textBody: textDecoded, rawHeaders }
  }

  // Binary fallback
  return {
    kind: 'binary',
    status,
    contentType: ct,
    blobBody: new Blob([buffer], { type: ct }),
    rawHeaders,
  }
}
