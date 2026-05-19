import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'
import { formatCodeContent } from '@/utils/code-display'
import { parseSchemaDocument, summarizeSchemaTree } from '@/utils/schema-visualization'

const componentSource = readFileSync(
  resolve(dirname(fileURLToPath(import.meta.url)), 'JsonSchemaViewer.vue'),
  'utf8',
)

describe('JsonSchemaViewer', () => {
  it('supports overlay-based visual inspection on top of raw code display', () => {
    const parsed = parseSchemaDocument(
      '{"type":"object","required":["city"],"properties":{"city":{"type":"string"}}}',
    )
    const summary = summarizeSchemaTree(parsed.tree)

    expect(componentSource).toContain('<CodeBlock')
    expect(componentSource).toContain('<Teleport to="body">')
    expect(componentSource).toContain("presentation === 'overlay'")
    expect(componentSource).toContain('<JsonSchemaTreeNode')
    expect(componentSource).toContain('cursor-pointer')
    expect(componentSource).toContain('group-hover:border-[rgb(34_34_34_/_0.14)]')
    expect(summary).toEqual({
      typeLabel: 'object',
      fieldCount: 1,
      requiredCount: 1,
      enumCount: 0,
    })
  })

  it('keeps invalid schema text displayable as plain text', () => {
    const formatted = formatCodeContent('{invalid-schema')
    const parsed = parseSchemaDocument('{invalid-schema')

    expect(formatted.language).toBe('text')
    expect(formatted.formatted).toBe(false)
    expect(formatted.display).toBe('{invalid-schema')
    expect(parsed.visualizable).toBe(false)
    expect(componentSource).toContain('schemaVisualUnavailableTitle')
  })

  it('uses an empty state instead of an empty code block for absent schemas', () => {
    const formatted = formatCodeContent('   ')

    expect(componentSource).toContain('<StateBlock')
    expect(componentSource).toContain("t('console.shared.schemaEmpty')")
    expect(formatted.source).toBe('')
  })
})
