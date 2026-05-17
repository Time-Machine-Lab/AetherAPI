import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'
import { formatCodeContent } from '@/utils/code-display'

const componentSource = readFileSync(
  resolve(dirname(fileURLToPath(import.meta.url)), 'JsonSchemaViewer.vue'),
  'utf8',
)

describe('JsonSchemaViewer', () => {
  it('uses the console code display for present schema content', () => {
    const formatted = formatCodeContent('{"type":"object","required":["city"]}')

    expect(componentSource).toContain('<CodeBlock')
    expect(componentSource).toContain('v-if="schema"')
    expect(formatted.language).toBe('json')
    expect(formatted.display).toContain('"required": [')
  })

  it('keeps invalid schema text displayable as plain text', () => {
    const formatted = formatCodeContent('{invalid-schema')

    expect(formatted.language).toBe('text')
    expect(formatted.formatted).toBe(false)
    expect(formatted.display).toBe('{invalid-schema')
  })

  it('uses an empty state instead of an empty code block for absent schemas', () => {
    const formatted = formatCodeContent('   ')

    expect(componentSource).toContain('<StateBlock')
    expect(componentSource).toContain("t('console.shared.schemaEmpty')")
    expect(formatted.source).toBe('')
  })
})
