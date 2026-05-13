import type { DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import { buildUnifiedAccessPath, buildUnifiedAccessTaskPath } from '@/utils/platform-url'

export interface CatalogDocLabels {
  exportTitle: string
  generatedAt: string
  exportSummary: string
  successCount: string
  failedCount: string
  failedItems: string
  basicInfo: string
  apiCode: string
  displayName: string
  assetType: string
  category: string
  publisher: string
  publishedAt: string
  platformUnifiedAccessUrl: string
  description: string
  request: string
  requestMethod: string
  authScheme: string
  requestTemplate: string
  requestExample: string
  responseExample: string
  asyncTaskQuery: string
  asyncTaskQueryEndpoint: string
  asyncTaskAuthMode: string
  asyncTaskStatusPath: string
  asyncTaskResultPath: string
  asyncTaskErrorPath: string
  asyncTaskResponseStructure: string
  aiCapability: string
  provider: string
  model: string
  streaming: string
  tags: string
  unavailable: string
  yes: string
  no: string
  detailLoadFailed: string
}

export interface BuildSingleApiMarkdownOptions {
  labels?: CatalogDocLabels
  generatedAt?: Date
  headingLevel?: 1 | 2
}

export interface CatalogDocExportFailure {
  apiCode: string
  reason?: string
}

export interface BuildMarketplaceDocsMarkdownOptions {
  details: DiscoveryAssetDetail[]
  failures?: CatalogDocExportFailure[]
  labels?: CatalogDocLabels
  generatedAt?: Date
}

interface MarkdownDownloadAnchor {
  href?: string
  download?: string
  click: () => void
}

export interface DownloadMarkdownDeps {
  createObjectURL?: (blob: Blob) => string
  revokeObjectURL?: (url: string) => void
  createElement?: (tagName: 'a') => MarkdownDownloadAnchor
  appendChild?: (node: MarkdownDownloadAnchor) => unknown
  removeChild?: (node: MarkdownDownloadAnchor) => unknown
}

export const defaultCatalogDocLabels: CatalogDocLabels = {
  exportTitle: 'AetherAPI Marketplace Documentation Export',
  generatedAt: 'Generated At',
  exportSummary: 'Export Summary',
  successCount: 'Success',
  failedCount: 'Failed',
  failedItems: 'Failed Items',
  basicInfo: 'Basic Information',
  apiCode: 'API Code',
  displayName: 'Display Name',
  assetType: 'Asset Type',
  category: 'Category',
  publisher: 'Publisher',
  publishedAt: 'Published At',
  platformUnifiedAccessUrl: 'Platform Unified Access URL',
  description: 'Description',
  request: 'Request',
  requestMethod: 'Request Method',
  authScheme: 'Auth Scheme',
  requestTemplate: 'Request Template',
  requestExample: 'Request Example',
  responseExample: 'Response Example',
  asyncTaskQuery: 'Async Task Query',
  asyncTaskQueryEndpoint: 'Task Query Endpoint',
  asyncTaskAuthMode: 'Task Query Auth Mode',
  asyncTaskStatusPath: 'Task Status Path',
  asyncTaskResultPath: 'Task Result Path',
  asyncTaskErrorPath: 'Task Error Path',
  asyncTaskResponseStructure: 'Task Query Response Structure',
  aiCapability: 'AI Capability',
  provider: 'Provider',
  model: 'Model',
  streaming: 'Streaming',
  tags: 'Tags',
  unavailable: 'Unavailable',
  yes: 'Yes',
  no: 'No',
  detailLoadFailed: 'Detail failed',
}

function valueOrUnavailable(value: unknown, labels: CatalogDocLabels): string {
  if (value === null || value === undefined || value === '') {
    return labels.unavailable
  }
  return String(value)
}

function tableRow(label: string, value: unknown, labels: CatalogDocLabels): string {
  return `| ${escapeTableCell(label)} | ${escapeTableCell(valueOrUnavailable(value, labels))} |`
}

function escapeTableCell(value: string): string {
  return value.replace(/\|/g, '\\|').replace(/\r?\n/g, '<br>')
}

function formatCodeFence(value: string): string {
  const language = isJsonLike(value) ? 'json' : ''
  return ['```' + language, value, '```'].join('\n')
}

function isJsonLike(value: string): boolean {
  try {
    JSON.parse(value)
    return true
  } catch {
    return false
  }
}

function heading(level: 1 | 2, text: string): string {
  return `${'#'.repeat(level)} ${text}`
}

function subsection(level: 1 | 2, text: string): string {
  return `${'#'.repeat(level + 1)} ${text}`
}

type JsonExample = Record<string, unknown>

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

function buildAsyncTaskResponseStructure(
  detail: DiscoveryAssetDetail,
  labels: CatalogDocLabels,
): string | undefined {
  const config = detail.asyncTaskConfig
  if (!config) {
    return undefined
  }

  const example: JsonExample = {}
  const assigned = [
    assignPathValue(example, pathSegments(config.statusPath), `<${labels.asyncTaskStatusPath}>`),
    assignPathValue(example, pathSegments(config.resultPath), `<${labels.asyncTaskResultPath}>`),
    assignPathValue(example, pathSegments(config.errorPath), `<${labels.asyncTaskErrorPath}>`),
  ].some(Boolean)

  return assigned ? JSON.stringify(example, null, 2) : undefined
}

function buildApiMarkdownSection(
  detail: DiscoveryAssetDetail,
  labels: CatalogDocLabels,
  generatedAt: Date,
  headingLevel: 1 | 2,
): string {
  const lines: string[] = [
    heading(headingLevel, detail.displayName || detail.apiCode),
    '',
    `${labels.generatedAt}: ${generatedAt.toISOString()}`,
    '',
    subsection(headingLevel, labels.basicInfo),
    '',
    '| Field | Value |',
    '| --- | --- |',
    tableRow(labels.apiCode, detail.apiCode, labels),
    tableRow(labels.displayName, detail.displayName, labels),
    tableRow(labels.assetType, detail.assetType, labels),
    tableRow(labels.category, detail.categoryName || detail.categoryCode, labels),
    tableRow(labels.publisher, detail.publisherDisplayName, labels),
    tableRow(labels.publishedAt, detail.publishedAt, labels),
    tableRow(labels.platformUnifiedAccessUrl, buildUnifiedAccessPath(detail.apiCode), labels),
    '',
  ]

  if (detail.description) {
    lines.push(subsection(headingLevel, labels.description), '', detail.description, '')
  }

  lines.push(
    subsection(headingLevel, labels.request),
    '',
    '| Field | Value |',
    '| --- | --- |',
    tableRow(labels.requestMethod, detail.requestMethod, labels),
    tableRow(labels.authScheme, detail.authScheme, labels),
    '',
  )

  if (detail.requestTemplate) {
    lines.push(
      subsection(headingLevel, labels.requestTemplate),
      '',
      formatCodeFence(detail.requestTemplate),
      '',
    )
  }

  if (detail.exampleSnapshot?.requestExample) {
    lines.push(
      subsection(headingLevel, labels.requestExample),
      '',
      formatCodeFence(detail.exampleSnapshot.requestExample),
      '',
    )
  }

  if (detail.exampleSnapshot?.responseExample) {
    lines.push(
      subsection(headingLevel, labels.responseExample),
      '',
      formatCodeFence(detail.exampleSnapshot.responseExample),
      '',
    )
  }

  if (detail.asyncTaskConfig?.enabled) {
    lines.push(
      subsection(headingLevel, labels.asyncTaskQuery),
      '',
      '| Field | Value |',
      '| --- | --- |',
      tableRow(labels.asyncTaskQueryEndpoint, buildUnifiedAccessTaskPath(detail.apiCode), labels),
      tableRow(labels.requestMethod, detail.asyncTaskConfig.queryMethod, labels),
      tableRow(labels.asyncTaskAuthMode, detail.asyncTaskConfig.authMode, labels),
      tableRow(labels.authScheme, detail.asyncTaskConfig.authScheme, labels),
      tableRow(labels.asyncTaskStatusPath, detail.asyncTaskConfig.statusPath, labels),
      tableRow(labels.asyncTaskResultPath, detail.asyncTaskConfig.resultPath, labels),
      tableRow(labels.asyncTaskErrorPath, detail.asyncTaskConfig.errorPath, labels),
      '',
    )

    const responseStructure = buildAsyncTaskResponseStructure(detail, labels)
    if (responseStructure) {
      lines.push(
        subsection(headingLevel, labels.asyncTaskResponseStructure),
        '',
        formatCodeFence(responseStructure),
        '',
      )
    }
  }

  if (detail.assetType === 'AI_API' && detail.aiProfile) {
    lines.push(
      subsection(headingLevel, labels.aiCapability),
      '',
      '| Field | Value |',
      '| --- | --- |',
      tableRow(labels.provider, detail.aiProfile.provider, labels),
      tableRow(labels.model, detail.aiProfile.model, labels),
      tableRow(labels.streaming, detail.aiProfile.streaming ? labels.yes : labels.no, labels),
      tableRow(labels.tags, detail.aiProfile.tags.join(', '), labels),
      '',
    )
  }

  return lines.join('\n').trimEnd()
}

export function buildSingleApiMarkdown(
  detail: DiscoveryAssetDetail,
  options: BuildSingleApiMarkdownOptions = {},
): string {
  return buildApiMarkdownSection(
    detail,
    options.labels ?? defaultCatalogDocLabels,
    options.generatedAt ?? new Date(),
    options.headingLevel ?? 1,
  )
}

export function buildMarketplaceDocsMarkdown({
  details,
  failures = [],
  labels = defaultCatalogDocLabels,
  generatedAt = new Date(),
}: BuildMarketplaceDocsMarkdownOptions): string {
  const lines: string[] = [
    `# ${labels.exportTitle}`,
    '',
    `${labels.generatedAt}: ${generatedAt.toISOString()}`,
    '',
    `## ${labels.exportSummary}`,
    '',
    `${labels.successCount}: ${details.length}, ${labels.failedCount}: ${failures.length}`,
    '',
  ]

  if (failures.length > 0) {
    lines.push(`## ${labels.failedItems}`, '')
    for (const failure of failures) {
      lines.push(`- \`${failure.apiCode}\`: ${failure.reason || labels.detailLoadFailed}`)
    }
    lines.push('')
  }

  for (const detail of details) {
    lines.push(
      '---',
      '',
      buildSingleApiMarkdown(detail, { labels, generatedAt, headingLevel: 2 }),
      '',
    )
  }

  return lines.join('\n').trimEnd()
}

function safeFileNameSegment(value: string): string {
  return value
    .trim()
    .replace(/[^a-zA-Z0-9_-]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .toLowerCase()
}

function dateStamp(date: Date): string {
  return date.toISOString().slice(0, 10)
}

export function buildSingleApiDocFileName(apiCode: string): string {
  return `aetherapi-${safeFileNameSegment(apiCode) || 'api'}-doc.md`
}

export function buildBatchDocFileName(date = new Date()): string {
  return `aetherapi-market-docs-${dateStamp(date)}.md`
}

export function downloadMarkdownFile(
  content: string,
  fileName: string,
  deps: DownloadMarkdownDeps = {},
): void {
  const createObjectURL = deps.createObjectURL ?? URL.createObjectURL.bind(URL)
  const revokeObjectURL = deps.revokeObjectURL ?? URL.revokeObjectURL.bind(URL)
  const createElement =
    deps.createElement ??
    ((tagName: 'a') => document.createElement(tagName) as MarkdownDownloadAnchor)
  const appendChild =
    deps.appendChild ??
    ((node: MarkdownDownloadAnchor) => document.body.appendChild(node as unknown as Node))
  const removeChild =
    deps.removeChild ??
    ((node: MarkdownDownloadAnchor) => document.body.removeChild(node as unknown as Node))

  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = createObjectURL(blob)
  const anchor = createElement('a')
  anchor.href = url
  anchor.download = fileName
  appendChild(anchor)
  anchor.click()
  removeChild(anchor)
  revokeObjectURL(url)
}
