import type { UnifiedAccessMethod } from '@/api/unified-access/unified-access.types'

export type UnifiedAccessCurlFormat = 'linux' | 'windows'
export type UnifiedAccessCurlIssue = 'apiCode' | 'apiKey' | null

export interface UnifiedAccessCurlInput {
  format: UnifiedAccessCurlFormat
  apiCode: string
  method: UnifiedAccessMethod
  apiKey: string
  address: string
  requestBody?: string
  extraHeaders?: Record<string, string>
}

export interface UnifiedAccessCurlIssueInput {
  apiCode: string
  apiKey: string
}

const NO_BODY_METHODS: UnifiedAccessMethod[] = ['GET', 'DELETE']

function supportsBody(method: UnifiedAccessMethod): boolean {
  return !NO_BODY_METHODS.includes(method)
}

export function getUnifiedAccessCurlIssue(
  input: UnifiedAccessCurlIssueInput,
): UnifiedAccessCurlIssue {
  if (!input.apiCode.trim()) return 'apiCode'
  if (!input.apiKey.trim()) return 'apiKey'
  return null
}

function quoteLinux(value: string): string {
  return `'${value.replace(/'/g, `'\\''`)}'`
}

function quoteWindows(value: string): string {
  return `"${value
    .replace(/"/g, '\\"')
    .replace(/\^/g, '^^')
    .replace(/&/g, '^&')
    .replace(/\|/g, '^|')
    .replace(/</g, '^<')
    .replace(/>/g, '^>')
    .replace(/%/g, '%%')}"`
}

function quoteForFormat(format: UnifiedAccessCurlFormat, value: string): string {
  return format === 'windows' ? quoteWindows(value) : quoteLinux(value)
}

function joinLines(format: UnifiedAccessCurlFormat, lines: string[]): string {
  const continuation = format === 'windows' ? ' ^' : ' \\'
  return lines.map((line, index) => (index === lines.length - 1 ? line : `${line}${continuation}`)).join('\n')
}

function normalizeHeaderEntries(input: UnifiedAccessCurlInput): Array<[string, string]> {
  const headers = new Map<string, string>()
  headers.set('X-Aether-Api-Key', input.apiKey.trim())

  for (const [name, value] of Object.entries(input.extraHeaders ?? {})) {
    const normalizedName = name.trim()
    if (!normalizedName) continue
    headers.set(normalizedName, String(value))
  }

  const body = input.requestBody?.trim() ?? ''
  if (supportsBody(input.method) && body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  return [...headers.entries()]
}

export function buildUnifiedAccessCurlCommand(input: UnifiedAccessCurlInput): string {
  const quote = (value: string) => quoteForFormat(input.format, value)
  const lines = [`curl -X ${input.method}`, `  ${quote(input.address)}`]

  for (const [name, value] of normalizeHeaderEntries(input)) {
    lines.push(`  -H ${quote(`${name}: ${value}`)}`)
  }

  const body = input.requestBody?.trim() ?? ''
  if (supportsBody(input.method) && body) {
    lines.push(`  --data ${quote(body)}`)
  }

  return joinLines(input.format, lines)
}
