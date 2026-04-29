import { describe, expect, it } from 'vitest'
import { assetTypeTone, displayToneClasses, methodTone, successTone } from './visual-system'

describe('visual system helpers', () => {
  it('maps HTTP methods to stable read-only tones', () => {
    expect(methodTone('GET')).toBe('success')
    expect(methodTone('POST')).toBe('info')
    expect(methodTone('PATCH')).toBe('warning')
    expect(methodTone('DELETE')).toBe('danger')
    expect(methodTone(undefined)).toBe('neutral')
  })

  it('maps asset and result state to non-action tag tones', () => {
    expect(assetTypeTone('AI_API')).toBe('ai')
    expect(assetTypeTone('STANDARD_API')).toBe('api')
    expect(successTone(true)).toBe('success')
    expect(successTone(false)).toBe('danger')
  })

  it('returns class strings without interactive affordances', () => {
    expect(displayToneClasses('neutral')).toContain('bg-secondary')
    expect(displayToneClasses('danger')).not.toContain('shadow')
    expect(displayToneClasses('success')).not.toContain('cursor-pointer')
  })
})
