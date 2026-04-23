import { computed, onMounted, ref } from 'vue'
import {
  createCurrentUserApiKey,
  disableCurrentUserApiKey,
  enableCurrentUserApiKey,
  getCurrentUserApiKeyDetail,
  listCurrentUserApiKeys,
  revokeCurrentUserApiKey,
} from '@/api/credential/credential.api'
import type { CreateCurrentUserApiKeyBody } from '@/api/credential/credential.dto'
import type { ApiKey, CredentialStatus, IssuedApiKey } from '@/api/credential/credential.types'

interface CredentialWorkspaceDeps {
  t: (key: string) => string
  listKeys: typeof listCurrentUserApiKeys
  createKey: typeof createCurrentUserApiKey
  getDetail: typeof getCurrentUserApiKeyDetail
  enableKey: typeof enableCurrentUserApiKey
  disableKey: typeof disableCurrentUserApiKey
  revokeKey: typeof revokeCurrentUserApiKey
  writeClipboard: (text: string) => Promise<void>
  autoLoad?: boolean
}

type CredentialWorkspaceOptions =
  Partial<Omit<CredentialWorkspaceDeps, 't'>> &
  Pick<CredentialWorkspaceDeps, 't'>

function buildDeps(options: CredentialWorkspaceOptions): CredentialWorkspaceDeps {
  return {
    listKeys: listCurrentUserApiKeys,
    createKey: createCurrentUserApiKey,
    getDetail: getCurrentUserApiKeyDetail,
    enableKey: enableCurrentUserApiKey,
    disableKey: disableCurrentUserApiKey,
    revokeKey: revokeCurrentUserApiKey,
    writeClipboard: (text: string) => navigator.clipboard.writeText(text),
    autoLoad: true,
    ...options,
  }
}

export function useCredentialWorkspace(options: CredentialWorkspaceOptions) {
  const deps = buildDeps(options)

  const apiKeys = ref<ApiKey[]>([])
  const listLoading = ref(false)
  const listError = ref(false)
  const listTotal = ref(0)
  const listPage = ref(1)
  const listPageSize = ref(20)
  const statusFilter = ref<CredentialStatus | ''>('')

  const selectedKey = ref<ApiKey | null>(null)
  const detailLoading = ref(false)

  const showCreateForm = ref(false)
  const createForm = ref({
    credentialName: '',
    credentialDescription: '',
    expireAt: '',
  })
  const createLoading = ref(false)
  const createError = ref('')

  const issuedKey = ref<IssuedApiKey | null>(null)
  const plaintextCopied = ref(false)
  const operationLoading = ref<string | null>(null)

  const canCreateKey = computed(() => createForm.value.credentialName.trim().length > 0)

  async function loadApiKeys() {
    listLoading.value = true
    listError.value = false
    try {
      const params: { status?: CredentialStatus; page?: number; size?: number } = {
        page: listPage.value,
        size: listPageSize.value,
      }
      if (statusFilter.value) params.status = statusFilter.value
      const result = await deps.listKeys(params)
      apiKeys.value = result.items
      listTotal.value = result.total
    } catch {
      listError.value = true
    } finally {
      listLoading.value = false
    }
  }

  async function handleFilterChange(status: CredentialStatus | '') {
    statusFilter.value = status
    listPage.value = 1
    selectedKey.value = null
    await loadApiKeys()
  }

  async function handleSelectKey(key: ApiKey) {
    if (selectedKey.value?.credentialId === key.credentialId) {
      selectedKey.value = null
      return
    }
    detailLoading.value = true
    try {
      selectedKey.value = await deps.getDetail(key.credentialId)
    } catch {
      selectedKey.value = key
    } finally {
      detailLoading.value = false
    }
  }

  function openCreateForm() {
    showCreateForm.value = true
    issuedKey.value = null
    createError.value = ''
    createForm.value = { credentialName: '', credentialDescription: '', expireAt: '' }
  }

  function closeCreateForm() {
    showCreateForm.value = false
    createError.value = ''
  }

  async function handleCreate() {
    if (!canCreateKey.value) return
    createLoading.value = true
    createError.value = ''
    try {
      const body: CreateCurrentUserApiKeyBody = {
        credentialName: createForm.value.credentialName.trim(),
      }
      if (createForm.value.credentialDescription.trim()) {
        body.credentialDescription = createForm.value.credentialDescription.trim()
      }
      if (createForm.value.expireAt) {
        body.expireAt = new Date(createForm.value.expireAt).toISOString()
      }
      issuedKey.value = await deps.createKey(body)
      showCreateForm.value = false
      plaintextCopied.value = false
      await loadApiKeys()
    } catch {
      createError.value = deps.t('console.credentials.createFailed')
    } finally {
      createLoading.value = false
    }
  }

  async function handleCopyPlaintext() {
    if (!issuedKey.value) return
    try {
      await deps.writeClipboard(issuedKey.value.plaintextKey)
      plaintextCopied.value = true
    } catch {
      // clipboard API may fail in some environments
    }
  }

  function dismissIssuedKey() {
    issuedKey.value = null
  }

  async function handleEnable(key: ApiKey) {
    await runLifecycle(key, deps.enableKey)
  }

  async function handleDisable(key: ApiKey) {
    await runLifecycle(key, deps.disableKey)
  }

  async function handleRevoke(key: ApiKey) {
    await runLifecycle(key, deps.revokeKey)
  }

  async function runLifecycle(key: ApiKey, action: (credentialId: string) => Promise<ApiKey>) {
    operationLoading.value = key.credentialId
    try {
      const updated = await action(key.credentialId)
      replaceKeyInList(updated)
      if (selectedKey.value?.credentialId === key.credentialId) selectedKey.value = updated
    } finally {
      operationLoading.value = null
    }
  }

  function replaceKeyInList(updated: ApiKey) {
    const idx = apiKeys.value.findIndex((k) => k.credentialId === updated.credentialId)
    if (idx !== -1) apiKeys.value[idx] = updated
  }

  function canEnable(key: ApiKey) {
    return key.status === 'DISABLED'
  }

  function canDisable(key: ApiKey) {
    return key.status === 'ENABLED'
  }

  function canRevoke(key: ApiKey) {
    return key.status === 'ENABLED' || key.status === 'DISABLED'
  }

  if (deps.autoLoad) {
    onMounted(loadApiKeys)
  }

  return {
    apiKeys,
    listLoading,
    listError,
    listTotal,
    listPage,
    listPageSize,
    statusFilter,
    selectedKey,
    detailLoading,
    showCreateForm,
    createForm,
    createLoading,
    createError,
    issuedKey,
    plaintextCopied,
    operationLoading,
    canCreateKey,
    loadApiKeys,
    handleFilterChange,
    handleSelectKey,
    openCreateForm,
    closeCreateForm,
    handleCreate,
    handleCopyPlaintext,
    dismissIssuedKey,
    handleEnable,
    handleDisable,
    handleRevoke,
    canEnable,
    canDisable,
    canRevoke,
  }
}
