import { describe, expect, it, vi } from 'vitest'
import { useCredentialWorkspace } from './useCredentialWorkspace'
import type { ApiKey, IssuedApiKey } from '@/api/credential/credential.types'
import type { PageResult } from '@/api/catalog/catalog.types'

vi.mock('@/api/credential/credential.api', () => ({
  listCurrentUserApiKeys: vi.fn(),
  createCurrentUserApiKey: vi.fn(),
  getCurrentUserApiKeyDetail: vi.fn(),
  enableCurrentUserApiKey: vi.fn(),
  disableCurrentUserApiKey: vi.fn(),
  revokeCurrentUserApiKey: vi.fn(),
}))

function t(key: string) {
  return key
}

function key(overrides: Partial<ApiKey> = {}): ApiKey {
  return {
    credentialId: 'cred-id-1',
    credentialCode: 'cred_code_1',
    credentialName: 'Default key',
    credentialDescription: null,
    maskedKey: 'ak_live_****1234',
    keyPrefix: 'ak_live',
    status: 'ENABLED',
    expireAt: null,
    revokedAt: null,
    createdAt: '2026-04-23T09:00:00Z',
    updatedAt: '2026-04-23T09:00:00Z',
    lastUsedSnapshot: null,
    ...overrides,
  }
}

function issued(overrides: Partial<IssuedApiKey> = {}): IssuedApiKey {
  return {
    ...key(),
    plaintextKey: 'ak_live_plaintext_once',
    ...overrides,
  }
}

function page(items: ApiKey[]): PageResult<ApiKey> {
  return { items, total: items.length, page: 1, pageSize: 20 }
}

describe('useCredentialWorkspace', () => {
  it('loads list with status filter and clears selected detail on filter change', async () => {
    const listKeys = vi.fn().mockResolvedValue(page([key({ status: 'DISABLED' })]))
    const workspace = useCredentialWorkspace({ t, autoLoad: false, listKeys })
    workspace.selectedKey.value = key()

    await workspace.handleFilterChange('DISABLED')

    expect(listKeys).toHaveBeenCalledWith({ status: 'DISABLED', page: 1, size: 20 })
    expect(workspace.selectedKey.value).toBeNull()
    expect(workspace.apiKeys.value[0].status).toBe('DISABLED')
  })

  it('loads detail and falls back to list snapshot when detail request fails', async () => {
    const snapshot = key({ credentialName: 'Snapshot' })
    const workspace = useCredentialWorkspace({
      t,
      autoLoad: false,
      getDetail: vi.fn().mockRejectedValueOnce(new Error('detail failed')),
    })

    await workspace.handleSelectKey(snapshot)

    expect(workspace.selectedKey.value).toEqual(snapshot)
    expect(workspace.detailLoading.value).toBe(false)
  })

  it('creates a key, exposes plaintext once, reloads list, and supports clipboard copy', async () => {
    const listKeys = vi.fn().mockResolvedValue(page([key()]))
    const writeClipboard = vi.fn().mockResolvedValue(undefined)
    const workspace = useCredentialWorkspace({
      t,
      autoLoad: false,
      listKeys,
      createKey: vi.fn().mockResolvedValueOnce(issued()),
      writeClipboard,
    })
    workspace.openCreateForm()
    workspace.createForm.value = {
      credentialName: ' New Key ',
      credentialDescription: ' Integration ',
      expireAt: '2026-12-31T23:59',
    }

    await workspace.handleCreate()
    await workspace.handleCopyPlaintext()

    expect(workspace.issuedKey.value?.plaintextKey).toBe('ak_live_plaintext_once')
    expect(workspace.showCreateForm.value).toBe(false)
    expect(writeClipboard).toHaveBeenCalledWith('ak_live_plaintext_once')
    expect(workspace.plaintextCopied.value).toBe(true)
  })

  it('updates list and selected detail through disable and revoke lifecycle actions', async () => {
    const disabled = key({ status: 'DISABLED' })
    const revoked = key({ status: 'REVOKED' })
    const workspace = useCredentialWorkspace({
      t,
      autoLoad: false,
      disableKey: vi.fn().mockResolvedValueOnce(disabled),
      revokeKey: vi.fn().mockResolvedValueOnce(revoked),
    })
    workspace.apiKeys.value = [key()]
    workspace.selectedKey.value = key()

    await workspace.handleDisable(key())
    expect(workspace.apiKeys.value[0].status).toBe('DISABLED')
    expect(workspace.selectedKey.value?.status).toBe('DISABLED')

    await workspace.handleRevoke(disabled)
    expect(workspace.apiKeys.value[0].status).toBe('REVOKED')
    expect(workspace.operationLoading.value).toBeNull()
  })

  it('maps create failure to the existing i18n feedback key', async () => {
    const workspace = useCredentialWorkspace({
      t,
      autoLoad: false,
      createKey: vi.fn().mockRejectedValueOnce(new Error('create failed')),
    })
    workspace.createForm.value.credentialName = 'broken'

    await workspace.handleCreate()

    expect(workspace.createError.value).toBe('console.credentials.createFailed')
    expect(workspace.createLoading.value).toBe(false)
  })
})
