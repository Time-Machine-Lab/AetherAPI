import { describe, expect, it, vi } from 'vitest'
import { copyTextToClipboard, formatCodeContent } from './code-display'

describe('code-display utilities', () => {
  it('formats JSON strings without changing the copy source', () => {
    const formatted = formatCodeContent('{"ok":true}')

    expect(formatted.language).toBe('json')
    expect(formatted.formatted).toBe(true)
    expect(formatted.display).toContain('"ok": true')
    expect(formatted.source).toBe('{"ok":true}')
  })

  it('falls back to plain text for invalid JSON', () => {
    const formatted = formatCodeContent('{invalid-json')

    expect(formatted.language).toBe('text')
    expect(formatted.formatted).toBe(false)
    expect(formatted.display).toBe('{invalid-json')
  })

  it('copies text through the browser clipboard when available', async () => {
    const writeText = vi.fn().mockResolvedValue(undefined)
    vi.stubGlobal('navigator', { clipboard: { writeText } })

    await expect(copyTextToClipboard('payload')).resolves.toBe(true)
    expect(writeText).toHaveBeenCalledWith('payload')

    vi.unstubAllGlobals()
  })
})
