import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const source = readFileSync(
  resolve(dirname(fileURLToPath(import.meta.url)), 'workspace.vue'),
  'utf8',
)

describe('workspace layout composition', () => {
  it('renders the default asset workspace as left summary followed by right list', () => {
    const recentIndex = source.indexOf("t('console.workspace.recentTitle')")
    const selectedAssetIndex = source.indexOf("t('console.workspace.assetTitle')")
    const assetListIndex = source.indexOf("t('console.workspace.assetListTitle')")

    expect(recentIndex).toBeGreaterThan(-1)
    expect(selectedAssetIndex).toBeGreaterThan(recentIndex)
    expect(assetListIndex).toBeGreaterThan(selectedAssetIndex)
    expect(source).toContain('xl:grid-cols-[minmax(280px,0.8fr)_minmax(0,1.35fr)]')
  })

  it('keeps asset editing in the existing right-side drawer overlay', () => {
    expect(source).toContain('v-if="assetEditorOpen && currentAsset"')
    expect(source).toContain('class="fixed inset-0 z-50 bg-black/35"')
    expect(source).toContain('class="ml-auto flex h-full w-full max-w-3xl')
  })

  it('routes the import-agent hash to the dedicated workspace section', () => {
    expect(source).toContain(
      "const isImportAgentSection = computed(() => route.hash === '#import-agent')",
    )
    expect(source).toContain('<ImportAgentWorkspace v-else-if="isImportAgentSection" />')
  })
})
