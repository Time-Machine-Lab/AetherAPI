<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useWorkspaceCatalog } from '@/composables/useWorkspaceCatalog'
import CredentialWorkspace from '@/features/credential/CredentialWorkspace.vue'
import ApiCallLogWorkspace from '@/features/api-call-log/ApiCallLogWorkspace.vue'
import { defaultConsoleWorkspaceHash, isHiddenConsoleNavId } from '@/features/console/console-shell'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const assetDetailCardRef = ref<HTMLElement | null>(null)

const isCredentialsSection = computed(() => route.hash === '#credentials')
const isApiCallLogsSection = computed(() => route.hash === '#api-call-logs')

watch(
  () => route.hash,
  async (hash) => {
    if (!hash) {
      return
    }

    const hashId = hash.replace('#', '')

    if (!isHiddenConsoleNavId(hashId)) {
      return
    }

    await router.replace({
      name: 'console-workspace',
      hash: defaultConsoleWorkspaceHash,
    })
  },
  { immediate: true },
)

async function focusAssetDetailCard() {
  await nextTick()
  assetDetailCardRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'start',
  })
}

// ── Asset workspace state ───────────────────────────────────────────────────────────────────────
const {
  assetCodeInput,
  currentAsset,
  assetLoading,
  assetError,
  registerForm,
  assetConfigForm,
  aiProfileForm,
  aiTagInput,
  handleLoadAsset,
  handleRegisterAsset,
  handleSaveAssetConfig,
  handleToggleAsset,
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
} = useWorkspaceCatalog({ t, onAssetSelected: focusAssetDetailCard })

function openRecentAsset(apiCode: string) {
  assetCodeInput.value = apiCode
  handleLoadAsset()
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
    <!-- Header -->
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
      <!-- Asset management -->
      <div id="catalog-manage" class="space-y-5">
        <!-- Asset list browse -->
        <Card class="scroll-mt-24">
          <CardHeader>
            <CardTitle>{{ t('console.workspace.assetListTitle') }}</CardTitle>
            <CardDescription>{{ t('console.workspace.assetListDescription') }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <!-- Filters -->
            <div class="flex flex-wrap gap-2">
              <Input
                v-model="assetListFilterKeyword"
                :placeholder="t('console.workspace.assetListFilterKeyword')"
                class="flex-1 min-w-[140px]"
                @keydown.enter.prevent="handleListAssets(1)"
              />
              <select
                v-model="assetListFilterStatus"
                class="h-11 cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
              >
                <option value="">{{ t('console.workspace.assetListFilterAll') }}</option>
                <option value="DRAFT">DRAFT</option>
                <option value="ENABLED">ENABLED</option>
                <option value="DISABLED">DISABLED</option>
              </select>
              <Button size="sm" :disabled="assetListLoading" @click="handleListAssets(1)">
                {{ t('console.workspace.assetListSearch') }}
              </Button>
            </div>

            <!-- Loading state -->
            <div v-if="assetListLoading" class="py-6 text-center text-sm text-muted-foreground">
              {{ t('console.workspace.loading') }}
            </div>
            <!-- Error state -->
            <div v-else-if="assetListError" class="py-6 text-center text-sm text-destructive">
              {{ t('console.workspace.assetListError') }}
            </div>
            <!-- Empty state -->
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
            <!-- List rows -->
            <div v-else-if="assetListItems.length > 0" class="space-y-2">
              <button
                v-for="item in assetListItems"
                :key="item.apiCode"
                type="button"
                class="group flex min-h-[44px] w-full cursor-pointer items-center gap-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white px-4 py-3 text-left shadow-console transition-[box-shadow,transform] duration-200 hover:-translate-y-px hover:shadow-console-hover active:scale-[0.995]"
                :disabled="assetLoading"
                @click="handleListSelectAsset(item.apiCode)"
              >
                <span
                  class="absolute left-3 top-3 bottom-3 hidden w-[3px] rounded-full"
                  :class="item.status === 'ENABLED' ? 'bg-primary' : 'bg-muted-foreground/25'"
                />
                <div class="min-w-0 flex-1">
                  <p class="truncate text-sm font-medium text-foreground">
                    {{ item.assetName ?? item.apiCode }}
                  </p>
                  <p class="text-xs text-muted-foreground">{{ item.apiCode }}</p>
                </div>
                <Badge
                  :variant="item.status === 'ENABLED' ? 'status-enabled' : 'status-disabled'"
                  class="shrink-0 text-[11px]"
                >
                  {{
                    item.status === 'ENABLED'
                      ? t('console.workspace.enabled')
                      : item.status === 'DRAFT'
                        ? 'DRAFT'
                        : t('console.workspace.disabled')
                  }}
                </Badge>
                <span
                  class="shrink-0 text-muted-foreground/40 transition-transform duration-200 group-hover:translate-x-0.5 group-hover:text-foreground/60"
                  >&gt;</span
                >
              </button>

              <!-- Pagination -->
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

        <!-- Asset detail card (code lookup + register form + current asset detail) -->
        <Card class="scroll-mt-24">
          <CardHeader>
            <CardTitle>{{ t('console.workspace.assetTitle') }}</CardTitle>
            <CardDescription>{{ t('console.workspace.assetDescription') }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <!-- Load existing asset -->
            <div class="space-y-1">
              <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.assetCodePlaceholder') }}</label>
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

            <!-- Register new asset -->
            <div
              class="space-y-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/60 p-4"
            >
              <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                {{ t('console.workspace.registerTitle') }}
              </p>
              <div class="grid gap-3 sm:grid-cols-2">
                <div class="space-y-1">
                  <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldApiCode') }}</label>
                  <Input
                    v-model="registerForm.apiCode"
                    :placeholder="t('console.workspace.fieldApiCode')"
                  />
                </div>
                <div class="space-y-1">
                  <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldCategoryCode') }}</label>
                  <Input
                    v-model="registerForm.categoryCode"
                    :placeholder="t('console.workspace.fieldCategoryCode')"
                  />
                </div>
              </div>
              <div class="space-y-1">
                <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldDisplayName') }}</label>
                <Input
                  v-model="registerForm.displayName"
                  :placeholder="t('console.workspace.fieldDisplayName')"
                />
              </div>
              <div class="space-y-1">
                <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldAssetType') }}</label>
                <select
                  v-model="registerForm.assetType"
                  class="h-11 w-full cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:bg-[color-mix(in_srgb,var(--primary)_4%,white)] focus-visible:ring-2 focus-visible:ring-primary/15"
                >
                  <option value="STANDARD_API">STANDARD_API</option>
                  <option value="AI_API">AI_API</option>
                </select>
              </div>
              <div class="flex justify-end pt-1">
                <Button size="sm" :disabled="assetLoading" @click="handleRegisterAsset">
                  {{ t('console.workspace.registerAction') }}
                </Button>
              </div>
            </div>

            <p v-if="assetError" class="text-sm text-destructive">{{ assetError }}</p>

            <!-- Current asset snapshot -->
            <div
              v-if="currentAsset"
              ref="assetDetailCardRef"
              class="space-y-3 rounded-[14px] border px-4 py-4"
            >
              <div class="flex min-h-[44px] items-start justify-between gap-3">
                <div>
                  <p class="font-semibold text-foreground">{{ currentAsset.displayName }}</p>
                  <p class="text-xs text-muted-foreground">{{ currentAsset.apiCode }}</p>
                </div>
                <Badge
                  :variant="
                    currentAsset.status === 'ENABLED' ? 'status-enabled' : 'status-disabled'
                  "
                  class="shrink-0"
                >
                  {{ currentAsset.status }}
                </Badge>
              </div>
              <div class="flex gap-2">
                <Button size="xs" variant="outline" @click="handleToggleAsset">
                  {{
                    currentAsset.status === 'ENABLED'
                      ? t('console.workspace.disable')
                      : t('console.workspace.enable')
                  }}
                </Button>
              </div>

              <div class="space-y-3 rounded-[14px] bg-secondary/60 p-4">
                <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  {{ t('console.workspace.assetConfigTitle') }}
                </p>
                <div class="grid gap-3 md:grid-cols-2">
                  <div class="space-y-1">
                    <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldDisplayName') }}</label>
                    <Input
                      v-model="assetConfigForm.displayName"
                      :placeholder="t('console.workspace.fieldDisplayName')"
                    />
                  </div>
                  <div class="space-y-1">
                    <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldCategoryCode') }}</label>
                    <Input
                      v-model="assetConfigForm.categoryCode"
                      :placeholder="t('console.workspace.fieldCategoryCode')"
                    />
                  </div>
                  <div class="space-y-1">
                    <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldRequestMethodPlaceholder') }}</label>
                    <select
                      v-model="assetConfigForm.requestMethod"
                      class="h-11 w-full cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
                    >
                      <option value="">
                        {{ t('console.workspace.fieldRequestMethodPlaceholder') }}
                      </option>
                      <option value="GET">GET</option>
                      <option value="POST">POST</option>
                      <option value="PUT">PUT</option>
                      <option value="PATCH">PATCH</option>
                      <option value="DELETE">DELETE</option>
                    </select>
                  </div>
                  <div class="space-y-1">
                    <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldUpstreamUrl') }}</label>
                    <Input
                      v-model="assetConfigForm.upstreamUrl"
                      :placeholder="t('console.workspace.fieldUpstreamUrl')"
                    />
                  </div>
                  <div class="space-y-1 md:col-span-2">
                    <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldAuthSchemePlaceholder') }}</label>
                    <select
                      v-model="assetConfigForm.authScheme"
                      class="h-11 w-full cursor-pointer appearance-none rounded-[8px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
                    >
                      <option value="">
                        {{ t('console.workspace.fieldAuthSchemePlaceholder') }}
                      </option>
                      <option value="NONE">NONE</option>
                      <option value="HEADER_TOKEN">HEADER_TOKEN</option>
                      <option value="QUERY_TOKEN">QUERY_TOKEN</option>
                    </select>
                  </div>
                </div>
                <div class="space-y-1">
                  <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldRequestTemplate') }}</label>
                  <textarea
                    v-model="assetConfigForm.requestTemplate"
                    :placeholder="t('console.workspace.fieldRequestTemplate')"
                    class="min-h-[96px] w-full rounded-[12px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
                  />
                </div>
                <div class="space-y-1">
                  <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldRequestExample') }}</label>
                  <textarea
                    v-model="assetConfigForm.requestExample"
                    :placeholder="t('console.workspace.fieldRequestExample')"
                    class="min-h-[96px] w-full rounded-[12px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
                  />
                </div>
                <div class="space-y-1">
                  <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldResponseExample') }}</label>
                  <textarea
                    v-model="assetConfigForm.responseExample"
                    :placeholder="t('console.workspace.fieldResponseExample')"
                    class="min-h-[96px] w-full rounded-[12px] border border-[rgb(34_34_34_/_0.08)] bg-white px-4 py-3 text-sm text-foreground outline-none transition-[background-color,box-shadow,border-color] focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15"
                  />
                </div>
                <div class="flex justify-end">
                  <Button size="sm" :disabled="assetLoading" @click="handleSaveAssetConfig">
                    {{ t('console.workspace.assetConfigSave') }}
                  </Button>
                </div>
              </div>

              <!-- AI profile form — only for AI_API -->
              <div
                v-if="currentAsset.assetType === 'AI_API'"
                class="space-y-2 rounded-[14px] bg-secondary p-4"
              >
                <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  {{ t('console.workspace.aiProfileTitle') }}
                </p>
                <div class="space-y-1">
                  <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldProvider') }}</label>
                  <Input
                    v-model="aiProfileForm.provider"
                    :placeholder="t('console.workspace.fieldProvider')"
                  />
                </div>
                <div class="space-y-1">
                  <label class="text-xs font-medium text-muted-foreground">{{ t('console.workspace.fieldModel') }}</label>
                  <Input
                    v-model="aiProfileForm.model"
                    :placeholder="t('console.workspace.fieldModel')"
                  />
                </div>
                <label class="flex items-center gap-2 text-sm">
                  <input v-model="aiProfileForm.streaming" type="checkbox" />
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
                <div v-if="aiProfileForm.tags.length" class="flex flex-wrap gap-2">
                  <span
                    v-for="tag in aiProfileForm.tags"
                    :key="tag"
                    class="rounded-full bg-white px-2.5 py-1 text-[11px] shadow-console"
                  >
                    {{ tag }}
                  </span>
                </div>
                <Button size="sm" @click="handleBindAiProfile">{{
                  t('console.workspace.bindAiProfile')
                }}</Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Recently opened assets 鈥?local shortcut only -->
        <Card v-if="recentAssets.length">
          <CardHeader>
            <CardTitle class="text-sm">{{ t('console.workspace.recentTitle') }}</CardTitle>
            <CardDescription class="text-xs">{{
              t('console.workspace.recentNote')
            }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-2">
            <button
              v-for="asset in recentAssets"
              :key="asset.apiCode"
              type="button"
              class="group flex min-h-[44px] w-full cursor-pointer items-center gap-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white px-4 py-3 text-left shadow-console transition-[box-shadow,transform] duration-200 hover:-translate-y-px hover:shadow-console-hover active:scale-[0.995]"
              @click="openRecentAsset(asset.apiCode)"
            >
              <span class="min-w-0 flex-1 truncate text-sm font-medium text-foreground">{{
                asset.displayName
              }}</span>
              <span class="shrink-0 text-xs text-muted-foreground">{{ asset.apiCode }}</span>
              <span
                class="shrink-0 text-muted-foreground/40 transition-transform duration-200 group-hover:translate-x-0.5 group-hover:text-foreground/60"
                >&gt;</span
              >
            </button>
          </CardContent>
        </Card>
      </div>
    </div>
  </div>
</template>
