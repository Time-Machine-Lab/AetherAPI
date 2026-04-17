<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  listCurrentUserApiKeys,
  createCurrentUserApiKey,
  getCurrentUserApiKeyDetail,
  enableCurrentUserApiKey,
  disableCurrentUserApiKey,
  revokeCurrentUserApiKey,
} from '@/api/credential/credential.api'
import type { CreateCurrentUserApiKeyBody } from '@/api/credential/credential.dto'
import type { ApiKey, IssuedApiKey, CredentialStatus } from '@/api/credential/credential.types'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  Copy,
  Eye,
  Info,
  KeyRound,
  Loader2,
  Plus,
  ShieldAlert,
  XCircle,
} from 'lucide-vue-next'

const { t } = useI18n()

// ── List state ──────────────────────────────────────────────
const apiKeys = ref<ApiKey[]>([])
const listLoading = ref(false)
const listError = ref(false)
const listTotal = ref(0)
const listPage = ref(1)
const listPageSize = ref(20)
const statusFilter = ref<CredentialStatus | ''>('')

// ── Detail state ────────────────────────────────────────────
const selectedKey = ref<ApiKey | null>(null)
const detailLoading = ref(false)

// ── Create state ────────────────────────────────────────────
const showCreateForm = ref(false)
const createForm = ref({
  credentialName: '',
  credentialDescription: '',
  expireAt: '',
})
const createLoading = ref(false)
const createError = ref('')

// ── Issued key (one-time display) ───────────────────────────
const issuedKey = ref<IssuedApiKey | null>(null)
const plaintextCopied = ref(false)

// ── Lifecycle operation state ───────────────────────────────
const operationLoading = ref<string | null>(null)

// ── Computed ────────────────────────────────────────────────
const statusOptions: Array<{ value: CredentialStatus | ''; labelKey: string }> = [
  { value: '', labelKey: 'console.credentials.filterAll' },
  { value: 'ENABLED', labelKey: 'console.credentials.statusEnabled' },
  { value: 'DISABLED', labelKey: 'console.credentials.statusDisabled' },
  { value: 'REVOKED', labelKey: 'console.credentials.statusRevoked' },
  { value: 'EXPIRED', labelKey: 'console.credentials.statusExpired' },
]

const canCreateKey = computed(() => createForm.value.credentialName.trim().length > 0)

// ── Load list ───────────────────────────────────────────────
async function loadApiKeys() {
  listLoading.value = true
  listError.value = false
  try {
    const params: { status?: CredentialStatus; page?: number; size?: number } = {
      page: listPage.value,
      size: listPageSize.value,
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const result = await listCurrentUserApiKeys(params)
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

// ── Select detail ───────────────────────────────────────────
async function handleSelectKey(key: ApiKey) {
  if (selectedKey.value?.credentialId === key.credentialId) {
    selectedKey.value = null
    return
  }
  detailLoading.value = true
  try {
    selectedKey.value = await getCurrentUserApiKeyDetail(key.credentialId)
  } catch {
    selectedKey.value = key
  } finally {
    detailLoading.value = false
  }
}

// ── Create flow ─────────────────────────────────────────────
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
    const issued = await createCurrentUserApiKey(body)
    issuedKey.value = issued
    showCreateForm.value = false
    plaintextCopied.value = false
    await loadApiKeys()
  } catch {
    createError.value = t('console.credentials.createFailed')
  } finally {
    createLoading.value = false
  }
}

async function handleCopyPlaintext() {
  if (!issuedKey.value) return
  try {
    await navigator.clipboard.writeText(issuedKey.value.plaintextKey)
    plaintextCopied.value = true
  } catch {
    // clipboard API may fail in some environments
  }
}

function dismissIssuedKey() {
  issuedKey.value = null
}

// ── Lifecycle operations ────────────────────────────────────
async function handleEnable(key: ApiKey) {
  operationLoading.value = key.credentialId
  try {
    const updated = await enableCurrentUserApiKey(key.credentialId)
    replaceKeyInList(updated)
    if (selectedKey.value?.credentialId === key.credentialId) {
      selectedKey.value = updated
    }
  } finally {
    operationLoading.value = null
  }
}

async function handleDisable(key: ApiKey) {
  operationLoading.value = key.credentialId
  try {
    const updated = await disableCurrentUserApiKey(key.credentialId)
    replaceKeyInList(updated)
    if (selectedKey.value?.credentialId === key.credentialId) {
      selectedKey.value = updated
    }
  } finally {
    operationLoading.value = null
  }
}

async function handleRevoke(key: ApiKey) {
  operationLoading.value = key.credentialId
  try {
    const updated = await revokeCurrentUserApiKey(key.credentialId)
    replaceKeyInList(updated)
    if (selectedKey.value?.credentialId === key.credentialId) {
      selectedKey.value = updated
    }
  } finally {
    operationLoading.value = null
  }
}

function replaceKeyInList(updated: ApiKey) {
  const idx = apiKeys.value.findIndex((k) => k.credentialId === updated.credentialId)
  if (idx !== -1) apiKeys.value[idx] = updated
}

// ── Helpers ─────────────────────────────────────────────────
function statusBadgeVariant(status: CredentialStatus) {
  switch (status) {
    case 'ENABLED':
      return 'status-enabled' as const
    case 'DISABLED':
      return 'status-disabled' as const
    case 'REVOKED':
    case 'EXPIRED':
      return 'destructive' as const
  }
}

function statusLabel(status: CredentialStatus) {
  const keyMap: Record<CredentialStatus, string> = {
    ENABLED: 'console.credentials.statusEnabled',
    DISABLED: 'console.credentials.statusDisabled',
    REVOKED: 'console.credentials.statusRevoked',
    EXPIRED: 'console.credentials.statusExpired',
  }
  return t(keyMap[status])
}

function formatDateTime(iso: string | null): string {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
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

onMounted(loadApiKeys)
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <section>
      <p class="console-kicker">{{ t('console.navigation.credentials') }}</p>
      <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
        {{ t('console.credentials.title') }}
      </h2>
      <p class="mt-3 text-sm leading-6 text-muted-foreground">
        {{ t('console.credentials.description') }}
      </p>
    </section>

    <!-- Issued key — one-time plaintext display -->
    <div
      v-if="issuedKey"
      class="rounded-[20px] border-2 border-primary/30 bg-[color-mix(in_srgb,var(--primary)_4%,white)] p-5"
    >
      <div class="flex items-start gap-3">
        <ShieldAlert class="mt-0.5 size-5 shrink-0 text-primary" />
        <div class="min-w-0 flex-1 space-y-3">
          <div>
            <p class="text-sm font-semibold text-foreground">
              {{ t('console.credentials.issuedTitle') }}
            </p>
            <p class="mt-1 text-sm text-destructive font-medium">
              {{ t('console.credentials.issuedWarning') }}
            </p>
          </div>
          <div
            class="flex items-center gap-2 rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3"
          >
            <code class="min-w-0 flex-1 break-all text-sm font-mono text-foreground">{{
              issuedKey.plaintextKey
            }}</code>
            <Button size="xs" variant="outline" @click="handleCopyPlaintext">
              <Copy class="size-3.5" />
              <span>{{ plaintextCopied ? t('console.credentials.copied') : t('console.credentials.copy') }}</span>
            </Button>
          </div>
          <div class="flex items-center gap-2 text-xs text-muted-foreground">
            <span>{{ issuedKey.credentialName }}</span>
            <span>·</span>
            <span>{{ issuedKey.maskedKey }}</span>
          </div>
          <Button size="sm" variant="outline" @click="dismissIssuedKey">
            {{ t('console.credentials.issuedDismiss') }}
          </Button>
        </div>
      </div>
    </div>

    <div class="grid gap-5 xl:grid-cols-[1fr_380px]">
      <!-- API Key list -->
      <Card id="credentials" class="scroll-mt-24">
        <CardHeader>
          <div class="flex items-center justify-between gap-3">
            <div>
              <CardTitle>{{ t('console.credentials.listTitle') }}</CardTitle>
              <CardDescription>{{ t('console.credentials.listDescription') }}</CardDescription>
            </div>
            <Button size="sm" @click="openCreateForm">
              <Plus class="size-4" />
              <span>{{ t('console.credentials.create') }}</span>
            </Button>
          </div>
        </CardHeader>
        <CardContent class="space-y-4">
          <!-- Status filter -->
          <div class="flex flex-wrap gap-2">
            <button
              v-for="opt in statusOptions"
              :key="opt.value"
              type="button"
              class="rounded-full px-3 py-1.5 text-xs font-medium transition-colors"
              :class="
                statusFilter === opt.value
                  ? 'bg-foreground text-white'
                  : 'bg-secondary text-muted-foreground hover:bg-secondary/80 hover:text-foreground'
              "
              @click="handleFilterChange(opt.value)"
            >
              {{ t(opt.labelKey) }}
            </button>
          </div>

          <!-- Create form -->
          <div
            v-if="showCreateForm"
            class="space-y-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/60 p-4"
          >
            <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              {{ t('console.credentials.createTitle') }}
            </p>
            <Input
              v-model="createForm.credentialName"
              :placeholder="t('console.credentials.fieldName')"
            />
            <Input
              v-model="createForm.credentialDescription"
              :placeholder="t('console.credentials.fieldDescription')"
            />
            <Input
              v-model="createForm.expireAt"
              type="datetime-local"
              :placeholder="t('console.credentials.fieldExpireAt')"
            />
            <p v-if="createError" class="text-sm text-destructive">{{ createError }}</p>
            <div class="flex justify-end gap-2 pt-1">
              <Button size="sm" variant="outline" @click="closeCreateForm">
                {{ t('console.workspace.cancel') }}
              </Button>
              <Button size="sm" :disabled="!canCreateKey || createLoading" @click="handleCreate">
                <Loader2 v-if="createLoading" class="size-4 animate-spin" />
                <span>{{ t('console.credentials.createAction') }}</span>
              </Button>
            </div>
          </div>

          <!-- Loading / error / empty / list -->
          <div v-if="listLoading" class="py-6 text-center text-sm text-muted-foreground">
            {{ t('console.workspace.loading') }}
          </div>
          <div v-else-if="listError" class="py-6 text-center text-sm text-destructive">
            {{ t('console.credentials.listError') }}
          </div>
          <div v-else-if="apiKeys.length === 0" class="py-6 text-center text-sm text-muted-foreground">
            {{ t('console.credentials.listEmpty') }}
          </div>
          <div v-else class="space-y-2">
            <button
              v-for="key in apiKeys"
              :key="key.credentialId"
              type="button"
              class="group flex w-full min-h-[44px] items-center gap-3 rounded-[14px] border bg-white py-3 pl-7 pr-4 text-left transition-[box-shadow,transform] duration-200 hover:-translate-y-px hover:shadow-console-hover"
              :class="
                selectedKey?.credentialId === key.credentialId
                  ? 'border-primary/30 ring-2 ring-primary/30 shadow-console'
                  : 'border-[rgb(34_34_34_/_0.06)] shadow-console'
              "
              @click="handleSelectKey(key)"
            >
              <span
                class="absolute left-3 top-3 bottom-3 w-[3px] rounded-full"
                :class="key.status === 'ENABLED' ? 'bg-primary' : key.status === 'DISABLED' ? 'bg-muted-foreground/25' : 'bg-destructive/40'"
              />
              <KeyRound class="size-4 shrink-0 text-muted-foreground" />
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ key.credentialName }}</p>
                <p class="text-xs text-muted-foreground">{{ key.maskedKey }}</p>
              </div>
              <Badge :variant="statusBadgeVariant(key.status)" class="shrink-0 text-[11px]">
                {{ statusLabel(key.status) }}
              </Badge>
            </button>
          </div>
        </CardContent>
      </Card>

      <!-- Detail panel + guidance -->
      <div class="space-y-5">
        <!-- Selected key detail -->
        <Card v-if="selectedKey" class="scroll-mt-24">
          <CardHeader>
            <CardTitle class="text-sm">{{ t('console.credentials.detailTitle') }}</CardTitle>
          </CardHeader>
          <CardContent class="space-y-4">
            <div v-if="detailLoading" class="py-4 text-center text-sm text-muted-foreground">
              {{ t('console.workspace.loading') }}
            </div>
            <template v-else>
              <!-- Key info -->
              <div class="space-y-3">
                <div>
                  <p class="text-xs text-muted-foreground">{{ t('console.credentials.fieldName') }}</p>
                  <p class="text-sm font-medium text-foreground">{{ selectedKey.credentialName }}</p>
                </div>
                <div v-if="selectedKey.credentialDescription">
                  <p class="text-xs text-muted-foreground">{{ t('console.credentials.fieldDescription') }}</p>
                  <p class="text-sm text-foreground">{{ selectedKey.credentialDescription }}</p>
                </div>
                <div>
                  <p class="text-xs text-muted-foreground">{{ t('console.credentials.fieldMaskedKey') }}</p>
                  <p class="text-sm font-mono text-foreground">{{ selectedKey.maskedKey }}</p>
                </div>
                <div>
                  <p class="text-xs text-muted-foreground">{{ t('console.credentials.fieldStatus') }}</p>
                  <Badge :variant="statusBadgeVariant(selectedKey.status)" class="text-[11px]">
                    {{ statusLabel(selectedKey.status) }}
                  </Badge>
                </div>
                <div>
                  <p class="text-xs text-muted-foreground">{{ t('console.credentials.fieldCreatedAt') }}</p>
                  <p class="text-sm text-foreground">{{ formatDateTime(selectedKey.createdAt) }}</p>
                </div>
                <div>
                  <p class="text-xs text-muted-foreground">{{ t('console.credentials.fieldExpireAt') }}</p>
                  <p class="text-sm text-foreground">
                    {{ selectedKey.expireAt ? formatDateTime(selectedKey.expireAt) : t('console.credentials.neverExpires') }}
                  </p>
                </div>
                <div v-if="selectedKey.revokedAt">
                  <p class="text-xs text-muted-foreground">{{ t('console.credentials.fieldRevokedAt') }}</p>
                  <p class="text-sm text-foreground">{{ formatDateTime(selectedKey.revokedAt) }}</p>
                </div>
              </div>

              <!-- Last used snapshot -->
              <div class="rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/60 p-4 space-y-2">
                <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  {{ t('console.credentials.lastUsedTitle') }}
                </p>
                <template v-if="selectedKey.lastUsedSnapshot?.lastUsedAt">
                  <div class="flex items-center gap-2 text-sm text-foreground">
                    <Clock class="size-3.5 text-muted-foreground" />
                    <span>{{ formatDateTime(selectedKey.lastUsedSnapshot.lastUsedAt) }}</span>
                  </div>
                  <div v-if="selectedKey.lastUsedSnapshot.lastUsedChannel" class="text-xs text-muted-foreground">
                    {{ t('console.credentials.lastUsedChannel') }}: {{ selectedKey.lastUsedSnapshot.lastUsedChannel }}
                  </div>
                  <div v-if="selectedKey.lastUsedSnapshot.lastUsedResult" class="flex items-center gap-1 text-xs">
                    <CheckCircle2
                      v-if="selectedKey.lastUsedSnapshot.lastUsedResult === 'SUCCESS'"
                      class="size-3.5 text-primary"
                    />
                    <XCircle v-else class="size-3.5 text-destructive" />
                    <span>{{ selectedKey.lastUsedSnapshot.lastUsedResult }}</span>
                  </div>
                </template>
                <p v-else class="text-sm text-muted-foreground">
                  {{ t('console.credentials.lastUsedEmpty') }}
                </p>
              </div>

              <!-- Lifecycle actions -->
              <div class="flex flex-wrap gap-2">
                <Button
                  v-if="canEnable(selectedKey)"
                  size="xs"
                  variant="outline"
                  :disabled="operationLoading === selectedKey.credentialId"
                  @click="handleEnable(selectedKey)"
                >
                  {{ t('console.workspace.enable') }}
                </Button>
                <Button
                  v-if="canDisable(selectedKey)"
                  size="xs"
                  variant="outline"
                  :disabled="operationLoading === selectedKey.credentialId"
                  @click="handleDisable(selectedKey)"
                >
                  {{ t('console.workspace.disable') }}
                </Button>
                <Button
                  v-if="canRevoke(selectedKey)"
                  size="xs"
                  variant="destructive"
                  :disabled="operationLoading === selectedKey.credentialId"
                  @click="handleRevoke(selectedKey)"
                >
                  <AlertTriangle class="size-3.5" />
                  <span>{{ t('console.credentials.revoke') }}</span>
                </Button>
              </div>
            </template>
          </CardContent>
        </Card>

        <!-- Plaintext reminder (persistent after creation) -->
        <div
          class="flex items-start gap-3 rounded-[14px] bg-[color-mix(in_srgb,var(--palette-text-legal)_6%,white)] border-b-2 border-[var(--palette-text-legal)] px-4 py-3"
        >
          <Eye class="mt-0.5 size-4 shrink-0 text-[var(--palette-text-legal)]" />
          <p class="text-sm font-medium text-foreground">
            {{ t('console.credentials.plaintextReminder') }}
          </p>
        </div>

        <!-- Guidance card -->
        <Card>
          <CardHeader>
            <CardTitle class="text-sm">{{ t('console.credentials.guidanceTitle') }}</CardTitle>
          </CardHeader>
          <CardContent class="space-y-3 text-sm text-muted-foreground">
            <div class="flex items-start gap-2">
              <Info class="mt-0.5 size-4 shrink-0 text-[var(--palette-text-legal)]" />
              <p>{{ t('console.credentials.guidanceModel') }}</p>
            </div>
            <div class="flex items-start gap-2">
              <Info class="mt-0.5 size-4 shrink-0 text-[var(--palette-text-legal)]" />
              <p>{{ t('console.credentials.guidanceConsumer') }}</p>
            </div>
            <div class="flex items-start gap-2">
              <Info class="mt-0.5 size-4 shrink-0 text-[var(--palette-text-legal)]" />
              <p>{{ t('console.credentials.guidanceDocs') }}</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  </div>
</template>
