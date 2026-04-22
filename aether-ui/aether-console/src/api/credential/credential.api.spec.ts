import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
  },
}))

import { http } from '@/api/http'
import {
  createCurrentUserApiKey,
  disableCurrentUserApiKey,
  enableCurrentUserApiKey,
  getCurrentUserApiKeyDetail,
  listCurrentUserApiKeys,
  revokeCurrentUserApiKey,
} from './credential.api'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPatch = vi.mocked(http.patch)

describe('credential api', () => {
  beforeEach(() => {
    mockedGet.mockReset()
    mockedPost.mockReset()
    mockedPatch.mockReset()
  })

  it('maps current user api key list response', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        items: [
          {
            credentialId: 'cred-1',
            credentialCode: 'cred_code_001',
            credentialName: 'Console Key',
            credentialDescription: 'For integration verification',
            maskedKey: 'ak_live_****1234',
            keyPrefix: 'ak_live',
            status: 'ENABLED',
            expireAt: null,
            revokedAt: null,
            createdAt: '2026-04-22T14:00:00Z',
            updatedAt: '2026-04-22T14:00:00Z',
            lastUsedSnapshot: null,
          },
        ],
        page: 1,
        size: 20,
        total: 1,
      },
    })

    const result = await listCurrentUserApiKeys({
      status: 'ENABLED',
      page: 1,
      size: 20,
    })

    expect(mockedGet).toHaveBeenCalledWith('v1/current-user/api-keys', {
      params: {
        status: 'ENABLED',
        page: 1,
        size: 20,
      },
    })
    expect(result.items[0].credentialName).toBe('Console Key')
    expect(result.pageSize).toBe(20)
  })

  it('maps api key detail response', async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        credentialId: 'cred-1',
        credentialCode: 'cred_code_001',
        credentialName: 'Console Key',
        credentialDescription: 'For integration verification',
        maskedKey: 'ak_live_****1234',
        keyPrefix: 'ak_live',
        status: 'ENABLED',
        expireAt: null,
        revokedAt: null,
        createdAt: '2026-04-22T14:00:00Z',
        updatedAt: '2026-04-22T14:00:00Z',
        lastUsedSnapshot: {
          lastUsedAt: '2026-04-22T14:05:00Z',
          lastUsedChannel: 'UNIFIED_ACCESS',
          lastUsedResult: 'SUCCESS',
        },
      },
    })

    const result = await getCurrentUserApiKeyDetail('cred-1')

    expect(mockedGet).toHaveBeenCalledWith('v1/current-user/api-keys/cred-1')
    expect(result.lastUsedSnapshot?.lastUsedChannel).toBe('UNIFIED_ACCESS')
  })

  it('maps create api key response including plaintext key', async () => {
    mockedPost.mockResolvedValueOnce({
      data: {
        credentialId: 'cred-1',
        credentialCode: 'cred_code_001',
        credentialName: 'Console Key',
        credentialDescription: 'For integration verification',
        maskedKey: 'ak_live_****1234',
        keyPrefix: 'ak_live',
        status: 'ENABLED',
        expireAt: null,
        revokedAt: null,
        createdAt: '2026-04-22T14:00:00Z',
        updatedAt: '2026-04-22T14:00:00Z',
        lastUsedSnapshot: null,
        plaintextKey: 'ak_live_plaintext_1234',
      },
    })

    const result = await createCurrentUserApiKey({
      credentialName: 'Console Key',
      credentialDescription: 'For integration verification',
    })

    expect(mockedPost).toHaveBeenCalledWith('v1/current-user/api-keys', {
      credentialName: 'Console Key',
      credentialDescription: 'For integration verification',
    })
    expect(result.plaintextKey).toBe('ak_live_plaintext_1234')
  })

  it('maps enable, disable and revoke operation responses', async () => {
    mockedPatch
      .mockResolvedValueOnce({
        data: {
          credentialId: 'cred-1',
          credentialCode: 'cred_code_001',
          credentialName: 'Console Key',
          credentialDescription: null,
          maskedKey: 'ak_live_****1234',
          keyPrefix: 'ak_live',
          status: 'ENABLED',
          expireAt: null,
          revokedAt: null,
          createdAt: '2026-04-22T14:00:00Z',
          updatedAt: '2026-04-22T14:01:00Z',
          lastUsedSnapshot: null,
        },
      })
      .mockResolvedValueOnce({
        data: {
          credentialId: 'cred-1',
          credentialCode: 'cred_code_001',
          credentialName: 'Console Key',
          credentialDescription: null,
          maskedKey: 'ak_live_****1234',
          keyPrefix: 'ak_live',
          status: 'DISABLED',
          expireAt: null,
          revokedAt: null,
          createdAt: '2026-04-22T14:00:00Z',
          updatedAt: '2026-04-22T14:02:00Z',
          lastUsedSnapshot: null,
        },
      })
      .mockResolvedValueOnce({
        data: {
          credentialId: 'cred-1',
          credentialCode: 'cred_code_001',
          credentialName: 'Console Key',
          credentialDescription: null,
          maskedKey: 'ak_live_****1234',
          keyPrefix: 'ak_live',
          status: 'REVOKED',
          expireAt: null,
          revokedAt: '2026-04-22T14:03:00Z',
          createdAt: '2026-04-22T14:00:00Z',
          updatedAt: '2026-04-22T14:03:00Z',
          lastUsedSnapshot: null,
        },
      })

    const enabled = await enableCurrentUserApiKey('cred-1')
    const disabled = await disableCurrentUserApiKey('cred-1')
    const revoked = await revokeCurrentUserApiKey('cred-1')

    expect(enabled.status).toBe('ENABLED')
    expect(disabled.status).toBe('DISABLED')
    expect(revoked.status).toBe('REVOKED')
    expect(mockedPatch).toHaveBeenNthCalledWith(
      1,
      'v1/current-user/api-keys/cred-1/enable',
    )
    expect(mockedPatch).toHaveBeenNthCalledWith(
      2,
      'v1/current-user/api-keys/cred-1/disable',
    )
    expect(mockedPatch).toHaveBeenNthCalledWith(
      3,
      'v1/current-user/api-keys/cred-1/revoke',
    )
  })
})
