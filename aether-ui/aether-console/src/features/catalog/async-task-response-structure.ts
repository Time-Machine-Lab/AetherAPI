import type { AsyncTaskConfig } from '@/api/catalog/catalog.types'

type JsonExample = Record<string, unknown>

export interface AsyncTaskResponseStructureLabels {
  status: string
  result: string
  error: string
}

function pathSegments(path?: string | null): string[] {
  if (!path) {
    return []
  }

  let normalized = path.trim()
  if (!normalized) {
    return []
  }

  normalized = normalized.replace(/^\$/, '').replace(/^\./, '')
  normalized = normalized.replace(/\[['"]?([^'"\]]+)['"]?\]/g, '.$1')

  if (!normalized || /[*?()]/.test(normalized)) {
    return []
  }

  const segments = normalized
    .split('.')
    .map((segment) => segment.trim())
    .filter(Boolean)
  return segments.every((segment) => /^[A-Za-z_$][\w$-]*$/.test(segment)) ? segments : []
}

function assignPathValue(target: JsonExample, segments: string[], value: string): boolean {
  if (segments.length === 0) {
    return false
  }

  let cursor: JsonExample = target
  for (let index = 0; index < segments.length; index += 1) {
    const segment = segments[index]
    const isLast = index === segments.length - 1
    if (isLast) {
      cursor[segment] = value
      return true
    }
    if (typeof cursor[segment] !== 'object' || cursor[segment] === null) {
      cursor[segment] = {}
    }
    cursor = cursor[segment] as JsonExample
  }

  return false
}

export function buildAsyncTaskResponseStructure(
  config: AsyncTaskConfig | null | undefined,
  labels: AsyncTaskResponseStructureLabels,
): string | undefined {
  if (!config) {
    return undefined
  }

  const example: JsonExample = {}
  const assigned = [
    assignPathValue(example, pathSegments(config.statusPath), `<${labels.status}>`),
    assignPathValue(example, pathSegments(config.resultPath), `<${labels.result}>`),
    assignPathValue(example, pathSegments(config.errorPath), `<${labels.error}>`),
  ].some(Boolean)

  return assigned ? JSON.stringify(example, null, 2) : undefined
}
