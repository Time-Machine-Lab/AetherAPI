import { describe, expect, it } from 'vitest'
import { buildAsyncTaskResponseStructure } from './async-task-response-structure'

const labels = {
  status: 'task status',
  result: 'task result',
  error: 'task error',
}

describe('async task response structure', () => {
  it('returns the configured task query response schema', () => {
    const structure = buildAsyncTaskResponseStructure(
      {
        queryResponseJsonSchema: '{"type":"object"}',
      },
      labels,
    )

    expect(structure).toBe('{"type":"object"}')
  })

  it('returns undefined when no response schema is configured', () => {
    expect(
      buildAsyncTaskResponseStructure(
        {
          enabled: true,
        },
        labels,
      ),
    ).toBeUndefined()
  })
})
