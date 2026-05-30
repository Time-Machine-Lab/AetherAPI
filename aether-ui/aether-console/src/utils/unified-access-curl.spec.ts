import { describe, expect, it } from 'vitest'
import { buildUnifiedAccessCurlCommand, getUnifiedAccessCurlIssue } from './unified-access-curl'

describe('unified-access-curl', () => {
  it('builds a Linux curl command from manual playground fields', () => {
    const command = buildUnifiedAccessCurlCommand({
      format: 'linux',
      apiCode: 'weather-api',
      method: 'POST',
      apiKey: 'ak_live_valid',
      address: '/api/v1/access/weather-api',
      requestBody: '{"city":"Shanghai"}',
      extraHeaders: {
        Accept: 'application/json',
      },
    })

    expect(command).toBe(
      [
        'curl -X POST \\',
        "  '/api/v1/access/weather-api' \\",
        "  -H 'X-Aether-Api-Key: ak_live_valid' \\",
        "  -H 'Accept: application/json' \\",
        "  -H 'Content-Type: application/json' \\",
        `  --data '{"city":"Shanghai"}'`,
      ].join('\n'),
    )
  })

  it('builds a Windows curl command with cmd-safe quoting', () => {
    const command = buildUnifiedAccessCurlCommand({
      format: 'windows',
      apiCode: 'quote-api',
      method: 'PATCH',
      apiKey: 'ak"valid',
      address: '/api/v1/access/quote-api',
      requestBody: '{"text":"hello & goodbye"}',
      extraHeaders: {
        'X-Trace': 'a&b',
      },
    })

    expect(command).toBe(
      [
        'curl -X PATCH ^',
        '  "/api/v1/access/quote-api" ^',
        '  -H "X-Aether-Api-Key: ak\\"valid" ^',
        '  -H "X-Trace: a^&b" ^',
        '  -H "Content-Type: application/json" ^',
        '  --data "{\\"text\\":\\"hello ^& goodbye\\"}"',
      ].join('\n'),
    )
  })

  it('omits stale body content for no-body methods', () => {
    const command = buildUnifiedAccessCurlCommand({
      format: 'linux',
      apiCode: 'weather-api',
      method: 'GET',
      apiKey: 'ak_live_valid',
      address: '/api/v1/access/weather-api',
      requestBody: '{"stale":true}',
      extraHeaders: {
        'X-Debug': 'true',
      },
    })

    expect(command).not.toContain('--data')
    expect(command).not.toContain('stale')
    expect(command).toContain("  -H 'X-Debug: true'")
  })

  it('escapes single quotes in Linux commands', () => {
    const command = buildUnifiedAccessCurlCommand({
      format: 'linux',
      apiCode: 'quote-api',
      method: 'POST',
      apiKey: "ak'valid",
      address: '/api/v1/access/quote-api',
      requestBody: `{"text":"can't stop"}`,
    })

    expect(command).toContain(`-H 'X-Aether-Api-Key: ak'\\''valid'`)
    expect(command).toContain(`--data '{"text":"can'\\''t stop"}'`)
  })

  it('reports missing required inputs before copy', () => {
    expect(
      getUnifiedAccessCurlIssue({
        apiCode: '',
        apiKey: 'ak_live_valid',
      }),
    ).toBe('apiCode')

    expect(
      getUnifiedAccessCurlIssue({
        apiCode: 'weather-api',
        apiKey: '',
      }),
    ).toBe('apiKey')
  })
})
