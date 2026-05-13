import { describe, expect, it } from 'vitest'
import { buildAsyncTaskResponseStructure } from './async-task-response-structure'

const labels = {
  status: 'task status',
  result: 'task result',
  error: 'task error',
}

describe('async task response structure', () => {
  it('builds a JSON response structure from configured simple paths', () => {
    const structure = buildAsyncTaskResponseStructure(
      {
        statusPath: '$.data.status',
        resultPath: '$.data.result',
        errorPath: "$['data']['error']",
      },
      labels,
    )

    expect(structure).toBe(
      JSON.stringify(
        {
          data: {
            status: '<task status>',
            result: '<task result>',
            error: '<task error>',
          },
        },
        null,
        2,
      ),
    )
  })

  it('returns undefined when paths cannot be represented as a simple object', () => {
    expect(
      buildAsyncTaskResponseStructure(
        {
          statusPath: '$.items[*].status',
        },
        labels,
      ),
    ).toBeUndefined()
  })
})
