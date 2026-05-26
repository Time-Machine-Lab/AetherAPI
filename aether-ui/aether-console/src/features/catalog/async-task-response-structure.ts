import type { AsyncTaskConfig } from '@/api/catalog/catalog.types'

export interface AsyncTaskResponseStructureLabels {
  status: string
  result: string
  error: string
}

export function buildAsyncTaskResponseStructure(
  config: AsyncTaskConfig | null | undefined,
  labels: AsyncTaskResponseStructureLabels,
): string | undefined {
  void labels
  if (!config) {
    return undefined
  }

  return config.queryResponseJsonSchema ?? undefined
}
