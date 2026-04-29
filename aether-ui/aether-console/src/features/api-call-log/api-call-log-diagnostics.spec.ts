import { describe, expect, it } from 'vitest'
import { contractLimitedDiagnosticFieldKeys } from './api-call-log-diagnostics'

describe('api call log diagnostics contract boundary', () => {
  it('lists only contract-limited request and response diagnostic placeholders', () => {
    expect(contractLimitedDiagnosticFieldKeys).toEqual([
      'console.apiCallLogs.fields.upstreamUrl',
      'console.apiCallLogs.fields.requestBody',
      'console.apiCallLogs.fields.responseBody',
      'console.apiCallLogs.fields.requestHeaders',
      'console.apiCallLogs.fields.responseHeaders',
    ])
  })
})
