<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import {
  CalendarClock,
  Folder,
  Network,
  Pencil,
  Plus,
  Settings,
  Trash2,
} from 'lucide-vue-next'
import { useWorkspaceCatalog } from '@/composables/useWorkspaceCatalog'
import CredentialWorkspace from '@/features/credential/CredentialWorkspace.vue'
import ApiCallLogWorkspace from '@/features/api-call-log/ApiCallLogWorkspace.vue'
import { defaultConsoleWorkspaceHash, isHiddenConsoleNavId } from '@/features/console/console-shell'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import MetaItem from '@/components/console/MetaItem.vue'
import CopyableField from '@/components/console/CopyableField.vue'
import { buildUnifiedAccessAddress } from '@/utils/platform-url'
import type { AssetStatus } from '@/api/catalog/catalog.types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const isCredentialsSection = computed(() => route.hash === '#credentials')
const isApiCallLogsSection = computed(() => route.hash === '#api-call-logs')

watch(
  () => route.hash,
  async (hash) => {
    if (!hash) return
    const hashId = hash.replace('#', '')
    if (!isHiddenConsoleNavId(hashId)) return
    await router.replace({ name: 'console-workspace', hash: defaultConsoleWorkspaceHash })
  },
  { immediate: true },
)

const {
  assetCodeInput,
  currentAsset,
  assetLoading,
  assetError,
  assetCreateOpen,
  assetEditorOpen,
  openCreateAsset,
  closeCreateAsset,
  openAssetEditor,
  closeAssetEditor,
  registerForm,
  assetConfigForm,
  aiProfileForm,
  aiTagInput,
  handleLoadAsset,
  handleRegisterAsset,
  handleSaveAssetConfig,
  handleToggleAsset,
  handleDeleteAsset,
  handleBindAiProfile,
  addAiTag,
  recentAssets,
  assetListItems,
  assetListTotal,
  assetListPage,
  assetListLoading,
  assetListError,
  assetListFilterKeyword,
  assetListFilterStatus,
  handleListAssets,
  handleListSelectAsset,
  assetListTotalPages,
} = useWorkspaceCatalog({ t })

function assetStatusLabel(status: AssetStatus) {
  if (status === 'PUBLISHED') return t('console.workspace.published')
  if (status === 'UNPUBLISHED') return t('console.workspace.unpublished')
  return t('console.workspace.draft')
}

function assetStatusBadgeVariant(status: AssetStatus) {
  return status === 'PUBLISHED' ? 'status-enabled' : 'status-disabled'
}

function assetStatusBarClass(status: AssetStatus) {
  if (status === 'PUBLISHED') return 'bg-primary'
  if (status === 'UNPUBLISHED') return 'bg-muted-foreground/35'
  return 'bg-[var(--palette-text-legal)]'
}

function formatDateTime(value?: string | null) {
  if (!value) return ''
  try {
    return new Date(value).toLocaleString()
  } catch {
    return value
  }
}

async function selectAsset(apiCode: string) {
  await handleListSelectAsset(apiCode)
}

async function editAsset(apiCode: string) {
  await handleListSelectAsset(apiCode)
  openAssetEditor()
}

function openRecentAsset(apiCode: string) {
  assetCodeInput.value = apiCode
  handleLoadAsset()
}

function confirmDeleteAsset() {
  if (!window.confirm(t('console.workspace.assetDeleteConfirm'))) return
  handleDeleteAsset()
}
</script>

<route lang="json5">
{
  name: 'console-workspace',
  meta: {
    layout: 'ConsoleLayout',
    titleKey: 'console.workspace.metaTitle',
    requiresAuth: true,
  },
}
</route>

<template>
  <CredentialWorkspace v-if="isCredentialsSection" />
  <ApiCallLogWorkspace v-else-if="isApiCallLogsSection" />
  <div v-else class="space-y-6">
    <section>
      <p class="console-kicker">{{ t('console.navigation.catalogManage') }}</p>
      <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
        {{ t('console.workspace.title') }}
      </h2>
      <p class="mt-3 text-sm leading-6 text-muted-foreground">
        {{ t('console.workspace.description') }}
      </p>
    </section>

    <div class="grid gap-5 2xl:grid-cols-2">
      <div id="catalog-manage" class="space-y-5">
        <Card class="scroll-mt-24">
          <CardHeader>
            <div class="flex flex-wrap items-start justify-between gap-3">
              <div>
                <CardTitle>{{ t('console.workspace.assetListTitle') }}</CardTitle>
                <CardDescription>{{ t('console.workspace.assetListDescription') }}</CardDescription>
              </div>
              <Button size="sm" @click="openCreateAsset">
                <Plus class="size-4" />
                {{ t('console.workspace.createAsset') }}
              </Button>
            </div>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="flex flex-wrap gap-2">
              <Input
                v-model="assetListFilterKeyword"
                :placeholder="t('console.workspace.assetListFilterKeyword')"
                class="min-w-[140px] flex-1"
                @keydown.enter.prevent="handleListAssets(1)"
              />
              <select
                v-model="assetListFilterStatus"
                class="h-11 cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
              >
                <option value="">{{ t('console.workspace.assetListFilterAll') }}</option>
                <option value="DRAFT">DRAFT</option>
                <option value="PUBLISHED">PUBLISHED</option>
                <option value="UNPUBLISHED">UNPUBLISHED</option>
              </select>
              <Button size="sm" :disabled="assetListLoading" @click="handleListAssets(1)">
                {{ t('console.workspace.assetListSearch') }}
              </Button>
            </div>

            <div v-if="assetListLoading" class="py-6 text-center text-sm text-muted-foreground">
              {{ t('console.workspace.loading') }}
            </div>
            <div v-else-if="assetListError" class="py-6 text-center text-sm text-destructive">
              {{ t('console.workspace.assetListError') }}
            </div>
            <div
              v-else-if="
                assetListItems.length === 0 &&
                assetListTotal === 0 &&
                assetListPage === 1 &&
                !assetListLoading
              "
              class="py-6 text-center text-sm text-muted-foreground"
            >
              {{ t('console.workspace.assetListEmpty') }}
            </div>
            <div v-else-if="assetListItems.length > 0" class="space-y-2">
              <div
                v-for="item in assetListItems"
                :key="item.apiCode"
                class="group relative flex min-h-[72px] w-full items-center gap-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white px-4 py-3 text-left shadow-console transition-[box-shadow,transform] duration-200 hover:-translate-y-px hover:shadow-console-hover"
              >
                <span
                  class="absolute bottom-3 left-3 top-3 w-[3px] rounded-full"
                  :class="assetStatusBarClass(item.status)"
                />
                <button
                  type="button"
                  class="min-w-0 flex-1 pl-2 text-left"
                  :disabled="assetLoading"
                  @click="selectAsset(item.apiCode)"
                >
                  <p class="truncate text-sm font-medium text-foreground">
                    {{ item.assetName ?? item.apiCode }}
                  </p>
                  <p class="text-xs text-muted-foreground">{{ item.apiCode }}</p>
                  <div class="mt-2 flex flex-wrap gap-1.5">
                    <Badge :variant="item.assetType === 'AI_API' ? 'type-ai' : 'type-api'">
                      {{ item.assetType === 'AI_API' ? 'AI' : 'API' }}
                    </Badge>
                    <Badge :variant="assetStatusBadgeVariant(item.status)">
                      {{ assetStatusLabel(item.status) }}
                    </Badge>
                    <MetaItem
                      :icon="Folder"
                      :label="t('console.workspace.listMetaCategory')"
                      :value="item.categoryName ?? item.categoryCode"
                    />
                    <MetaItem
                      :icon="CalendarClock"
                      :label="t('console.workspace.listMetaUpdatedAt')"
                      :value="formatDateTime(item.updatedAt)"
                    />
                    <MetaItem
                      :icon="CalendarClock"
                      :label="t('console.workspace.listMetaPublishedAt')"
                      :value="formatDateTime(item.publishedAt)"
                    />
                  </div>
                </button>
                <Button size="xs" variant="outline" :disabled="assetLoading" @click="editAsset(item.apiCode)">
                  <Pencil class="size-3.5" />
                  {{ t('console.shared.edit') }}
                </Button>
              </div>

              <div class="flex items-center justify-between pt-2 text-xs text-muted-foreground">
                <span>{{
                  t('console.workspace.assetListPageSummary', {
                    page: assetListPage,
                    totalPages: assetListTotalPages(),
                    total: assetListTotal,
                  })
                }}</span>
                <div class="flex gap-1">
                  <Button
                    size="xs"
                    variant="outline"
                    :disabled="assetListPage <= 1 || assetListLoading"
                    @click="handleListAssets(assetListPage - 1)"
                  >
                    {{ t('console.workspace.assetListPrev') }}
                  </Button>
                  <Button
                    size="xs"
                    variant="outline"
                    :disabled="assetListPage >= assetListTotalPages() || assetListLoading"
                    @click="handleListAssets(assetListPage + 1)"
                  >
                    {{ t('console.workspace.assetListNext') }}
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card class="scroll-mt-24">
          <CardHeader>
            <CardTitle>{{ t('console.workspace.assetTitle') }}</CardTitle>
            <CardDescription>{{ t('console.workspace.assetDescription') }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">
                {{ t('console.workspace.assetCodePlaceholder') }}
              </label>
              <div class="flex gap-2">
                <Input
                  v-model="assetCodeInput"
                  :placeholder="t('console.workspace.assetCodePlaceholder')"
                  class="flex-1"
                />
                <Button size="sm" variant="outline" :disabled="assetLoading" @click="handleLoadAsset">
                  {{ t('console.workspace.assetLoad') }}
                </Button>
              </div>
            </div>

            <p v-if="assetError" class="text-sm text-destructive">{{ assetError }}</p>

            <div
              v-if="currentAsset"
              class="space-y-4 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/40 px-4 py-4"
            >
              <div class="flex min-h-[44px] items-start justify-between gap-3">
                <div>
                  <p class="font-semibold text-foreground">{{ currentAsset.displayName }}</p>
                  <p class="text-xs text-muted-foreground">{{ currentAsset.apiCode }}</p>
                </div>
                <Badge :variant="assetStatusBadgeVariant(currentAsset.status)" class="shrink-0">
                  {{ assetStatusLabel(currentAsset.status) }}
                </Badge>
              </div>
              <div class="flex flex-wrap gap-2">
                <Badge :variant="currentAsset.assetType === 'AI_API' ? 'type-ai' : 'type-api'">
                  {{ currentAsset.assetType === 'AI_API' ? 'AI' : 'API' }}
                </Badge>
                <MetaItem
                  :icon="Network"
                  :label="t('console.workspace.listMetaMethod')"
                  :value="currentAsset.requestMethod"
                />
                <MetaItem
                  :icon="Folder"
                  :label="t('console.workspace.listMetaCategory')"
                  :value="currentAsset.categoryCode"
                />
                <MetaItem
                  :icon="CalendarClock"
                  :label="t('console.workspace.listMetaUpdatedAt')"
                  :value="formatDateTime(currentAsset.updatedAt)"
                />
              </div>
              <CopyableField
                :label="t('console.shared.platformCallAddress')"
                :hint="t('console.shared.platformCallAddressHint')"
                :value="buildUnifiedAccessAddress(currentAsset.apiCode)"
              />
              <div class="flex flex-wrap gap-2">
                <Button size="xs" variant="outline" @click="openAssetEditor">
                  <Settings class="size-3.5" />
                  {{ t('console.workspace.openEditor') }}
                </Button>
                <Button size="xs" variant="outline" @click="handleToggleAsset">
                  {{
                    currentAsset.status === 'PUBLISHED'
                      ? t('console.workspace.unpublish')
                      : t('console.workspace.publish')
                  }}
                </Button>
                <Button size="xs" variant="destructive" @click="confirmDeleteAsset">
                  <Trash2 class="size-3.5" />
                  {{ t('console.workspace.deleteAsset') }}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card v-if="recentAssets.length">
          <CardHeader>
            <CardTitle class="text-sm">{{ t('console.workspace.recentTitle') }}</CardTitle>
            <CardDescription class="text-xs">
              {{ t('console.workspace.recentNote') }}
            </CardDescription>
          </CardHeader>
          <CardContent class="space-y-2">
            <button
              v-for="asset in recentAssets"
              :key="asset.apiCode"
              type="button"
              class="group flex min-h-[44px] w-full cursor-pointer items-center gap-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white px-4 py-3 text-left shadow-console transition-[box-shadow,transform] duration-200 hover:-translate-y-px hover:shadow-console-hover active:scale-[0.995]"
              @click="openRecentAsset(asset.apiCode)"
            >
              <span class="min-w-0 flex-1 truncate text-sm font-medium text-foreground">
                {{ asset.displayName }}
              </span>
              <span class="shrink-0 text-xs text-muted-foreground">{{ asset.apiCode }}</span>
            </button>
          </CardContent>
        </Card>
      </div>
    </div>

    <div
      v-if="assetCreateOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/35 p-4"
      @click.self="closeCreateAsset"
    >
      <Card class="w-full max-w-xl">
        <CardHeader>
          <CardTitle>{{ t('console.workspace.createAsset') }}</CardTitle>
          <CardDescription>{{ t('console.workspace.createAssetDescription') }}</CardDescription>
        </CardHeader>
        <CardContent class="space-y-4">
          <div class="grid gap-3 sm:grid-cols-2">
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">
                {{ t('console.workspace.fieldApiCode') }}
              </label>
              <Input v-model="registerForm.apiCode" :placeholder="t('console.workspace.fieldApiCode')" />
            </div>
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">
                {{ t('console.workspace.fieldDisplayName') }}
              </label>
              <Input
                v-model="registerForm.assetName"
                :placeholder="t('console.workspace.fieldDisplayName')"
              />
            </div>
          </div>
          <div class="space-y-1">
            <label class="text-xs font-medium text-muted-foreground">
              {{ t('console.workspace.fieldAssetType') }}
            </label>
            <select
              v-model="registerForm.assetType"
              class="h-11 w-full cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:bg-[color-mix(in_srgb,var(--primary)_4%,white)] focus-visible:ring-2 focus-visible:ring-primary/15"
            >
              <option value="STANDARD_API">STANDARD_API</option>
              <option value="AI_API">AI_API</option>
            </select>
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <Button size="sm" variant="outline" @click="closeCreateAsset">
              {{ t('console.workspace.cancel') }}
            </Button>
            <Button size="sm" :disabled="assetLoading" @click="handleRegisterAsset">
              <Plus class="size-4" />
              {{ t('console.workspace.registerAction') }}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>

    <div
      v-if="assetEditorOpen && currentAsset"
      class="fixed inset-0 z-50 bg-black/35"
      @click.self="closeAssetEditor"
    >
      <aside class="ml-auto flex h-full w-full max-w-3xl flex-col overflow-y-auto bg-white shadow-console">
        <div
          class="sticky top-0 z-10 flex items-start justify-between gap-3 border-b bg-white px-6 py-5"
        >
          <div>
            <p class="console-kicker">{{ t('console.workspace.assetConfigTitle') }}</p>
            <h3 class="mt-2 text-xl font-semibold text-foreground">
              {{ currentAsset.displayName }}
            </h3>
            <p class="mt-1 text-xs text-muted-foreground">{{ currentAsset.apiCode }}</p>
          </div>
          <Button size="sm" variant="outline" @click="closeAssetEditor">
            {{ t('console.shared.close') }}
          </Button>
        </div>

        <div class="space-y-5 p-6">
          <CopyableField
            :label="t('console.shared.platformCallAddress')"
            :hint="t('console.shared.platformCallAddressHint')"
            :value="buildUnifiedAccessAddress(currentAsset.apiCode)"
          />

          <div class="space-y-3 rounded-[14px] bg-secondary/60 p-4">
            <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              {{ t('console.workspace.assetConfigTitle') }}
            </p>
            <div class="grid gap-3 md:grid-cols-2">
              <div class="space-y-1">
                <label class="text-xs font-medium text-muted-foreground">
                  {{ t('console.workspace.fieldDisplayName') }}
                </label>
                <Input
                  v-model="assetConfigForm.displayName"
                  :placeholder="t('console.workspace.fieldDisplayName')"
                />
              </div>
              <div class="space-y-1">
                <label class="text-xs font-medium text-muted-foreground">
                  {{ t('console.workspace.fieldCategoryCode') }}
                </label>
                <Input
                  v-model="assetConfigForm.categoryCode"
                  :placeholder="t('console.workspace.fieldCategoryCode')"
                />
                <p class="text-xs leading-5 text-muted-foreground">
                  {{ t('console.workspace.categoryDependencyHint') }}
                </p>
              </div>
              <div class="space-y-1">
                <label class="text-xs font-medium text-muted-foreground">
                  {{ t('console.workspace.fieldRequestMethodPlaceholder') }}
                </label>
                <select
                  v-model="assetConfigForm.requestMethod"
                  class="h-11 w-full cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
                >
                  <option value="">{{ t('console.workspace.fieldRequestMethodPlaceholder') }}</option>
                  <option value="GET">GET</option>
                  <option value="POST">POST</option>
                  <option value="PUT">PUT</option>
                  <option value="PATCH">PATCH</option>
                  <option value="DELETE">DELETE</option>
                </select>
                <p class="text-xs leading-5 text-muted-foreground">
                  {{ t('console.workspace.fieldRequestMethodHint') }}
                </p>
              </div>
              <div class="space-y-1">
                <label class="text-xs font-medium text-muted-foreground">
                  {{ t('console.workspace.fieldUpstreamUrl') }}
                </label>
                <Input
                  v-model="assetConfigForm.upstreamUrl"
                  :placeholder="t('console.workspace.fieldUpstreamUrl')"
                />
                <p class="text-xs leading-5 text-muted-foreground">
                  {{ t('console.workspace.fieldUpstreamUrlHint') }}
                </p>
              </div>
              <div class="space-y-1 md:col-span-2">
                <label class="text-xs font-medium text-muted-foreground">
                  {{ t('console.workspace.fieldAuthSchemePlaceholder') }}
                </label>
                <select
                  v-model="assetConfigForm.authScheme"
                  class="h-11 w-full cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
                >
                  <option value="">{{ t('console.workspace.fieldAuthSchemePlaceholder') }}</option>
                  <option value="NONE">NONE</option>
                  <option value="HEADER_TOKEN">HEADER_TOKEN</option>
                  <option value="QUERY_TOKEN">QUERY_TOKEN</option>
                </select>
                <p class="text-xs leading-5 text-muted-foreground">
                  {{ t('console.workspace.fieldAuthSchemeHint') }}
                </p>
              </div>
              <div class="space-y-1 md:col-span-2">
                <label class="text-xs font-medium text-muted-foreground">
                  {{ t('console.workspace.fieldAuthConfig') }}
                </label>
                <Input
                  v-model="assetConfigForm.authConfig"
                  :placeholder="t('console.workspace.fieldAuthConfigPlaceholder')"
                />
                <p class="text-xs leading-5 text-muted-foreground">
                  {{ t('console.workspace.fieldAuthConfigHint') }}
                </p>
              </div>
            </div>
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">
                {{ t('console.workspace.fieldRequestTemplate') }}
              </label>
              <textarea
                v-model="assetConfigForm.requestTemplate"
                :placeholder="t('console.workspace.fieldRequestTemplate')"
                class="min-h-[96px] w-full rounded-[12px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
              />
              <p class="text-xs leading-5 text-muted-foreground">
                {{ t('console.workspace.fieldRequestTemplateHint') }}
              </p>
            </div>
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">
                {{ t('console.workspace.fieldRequestExample') }}
              </label>
              <textarea
                v-model="assetConfigForm.requestExample"
                :placeholder="t('console.workspace.fieldRequestExample')"
                class="min-h-[96px] w-full rounded-[12px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
              />
              <p class="text-xs leading-5 text-muted-foreground">
                {{ t('console.workspace.fieldRequestExampleHint') }}
              </p>
            </div>
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">
                {{ t('console.workspace.fieldResponseExample') }}
              </label>
              <textarea
                v-model="assetConfigForm.responseExample"
                :placeholder="t('console.workspace.fieldResponseExample')"
                class="min-h-[96px] w-full rounded-[12px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
              />
              <p class="text-xs leading-5 text-muted-foreground">
                {{ t('console.workspace.fieldResponseExampleHint') }}
              </p>
            </div>
            <div class="rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white px-4 py-3">
              <p class="text-xs font-semibold text-foreground">
                {{ t('console.workspace.categoryDependencyTitle') }}
              </p>
              <p class="mt-1 text-xs leading-5 text-muted-foreground">
                {{ t('console.workspace.categoryDependencyHint') }}
              </p>
            </div>
            <div class="flex justify-end">
              <Button size="sm" :disabled="assetLoading" @click="handleSaveAssetConfig">
                {{ t('console.workspace.assetConfigSave') }}
              </Button>
            </div>
          </div>

          <div v-if="currentAsset.assetType === 'AI_API'" class="space-y-2 rounded-[14px] bg-secondary p-4">
            <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              {{ t('console.workspace.aiProfileTitle') }}
            </p>
            <p class="text-xs leading-5 text-muted-foreground">
              {{ t('console.workspace.aiProfileHint') }}
            </p>
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">
                {{ t('console.workspace.fieldProvider') }}
              </label>
              <Input v-model="aiProfileForm.provider" :placeholder="t('console.workspace.fieldProvider')" />
            </div>
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">
                {{ t('console.workspace.fieldModel') }}
              </label>
              <Input v-model="aiProfileForm.model" :placeholder="t('console.workspace.fieldModel')" />
            </div>
            <label class="flex items-center gap-2 text-sm">
              <input v-model="aiProfileForm.streamingSupported" type="checkbox" />
              {{ t('console.workspace.fieldStreaming') }}
            </label>
            <div class="flex gap-2">
              <Input
                v-model="aiTagInput"
                :placeholder="t('console.workspace.fieldTagPlaceholder')"
                class="flex-1"
                @keydown.enter.prevent="addAiTag"
              />
              <Button size="sm" variant="outline" @click="addAiTag">+</Button>
            </div>
            <div v-if="aiProfileForm.capabilityTags.length" class="flex flex-wrap gap-2">
              <span
                v-for="tag in aiProfileForm.capabilityTags"
                :key="tag"
                class="rounded-full bg-white px-2.5 py-1 text-[11px] shadow-console"
              >
                {{ tag }}
              </span>
            </div>
            <Button size="sm" :disabled="assetLoading" @click="handleBindAiProfile">
              {{ t('console.workspace.bindAiProfile') }}
            </Button>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>
