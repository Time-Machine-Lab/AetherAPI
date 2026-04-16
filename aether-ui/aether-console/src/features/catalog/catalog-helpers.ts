import type { AiProfile, AssetType, DiscoveryAsset } from '@/api/catalog/catalog.types'

const AI_CAPABILITY_LABEL_MAP: Record<string, string> = {
  streaming: '流式输出',
  'tool-call': '工具调用',
  vision: '视觉理解',
  reasoning: '推理增强',
}

export function formatAssetType(assetType: AssetType): string {
  return assetType === 'AI_API' ? 'AI 接口' : '标准接口'
}

export function getAiCapabilityLabels(aiProfile: AiProfile): string[] {
  const labels: string[] = []
  if (aiProfile.streaming) labels.push(AI_CAPABILITY_LABEL_MAP['streaming'])
  for (const tag of aiProfile.tags) {
    labels.push(AI_CAPABILITY_LABEL_MAP[tag] ?? tag)
  }
  return labels
}

export function formatAuthScheme(authScheme?: string): string {
  if (!authScheme) return '无鉴权'
  return authScheme
}

const RECENT_ASSETS_KEY = 'catalog:recent-assets'
const MAX_RECENT = 10

export function getRecentAssets(): DiscoveryAsset[] {
  try {
    const raw = localStorage.getItem(RECENT_ASSETS_KEY)
    return raw ? (JSON.parse(raw) as DiscoveryAsset[]) : []
  } catch {
    return []
  }
}

export function pushRecentAsset(asset: DiscoveryAsset): void {
  const list = getRecentAssets().filter((a) => a.apiCode !== asset.apiCode)
  list.unshift(asset)
  localStorage.setItem(RECENT_ASSETS_KEY, JSON.stringify(list.slice(0, MAX_RECENT)))
}
