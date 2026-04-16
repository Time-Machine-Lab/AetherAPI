<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  listCategories,
  createCategory,
  renameCategory,
  enableCategory,
  disableCategory,
} from '@/api/catalog/category.api'
import {
  getAsset,
  registerAsset,
  enableAsset,
  disableAsset,
  bindAiProfile,
} from '@/api/catalog/asset.api'
import type { ApiCategory, ApiAsset } from '@/api/catalog/catalog.types'
import type { RegisterAssetBody, BindAiProfileBody } from '@/api/catalog/catalog.dto'
import { getRecentAssets } from '@/features/catalog/catalog-helpers'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'

const { t } = useI18n()

// ── Categories ──────────────────────────────────────────────
const categories = ref<ApiCategory[]>([])
const catLoading = ref(false)
const catError = ref(false)
const newCatName = ref('')
const renamingCat = ref<string | null>(null)
const renameValue = ref('')

async function loadCategories() {
  catLoading.value = true
  catError.value = false
  try {
    const result = await listCategories()
    categories.value = result.items
  } catch {
    catError.value = true
  } finally {
    catLoading.value = false
  }
}

async function handleCreateCategory() {
  if (!newCatName.value.trim()) return
  const cat = await createCategory({ name: newCatName.value.trim() })
  categories.value.unshift(cat)
  newCatName.value = ''
}

async function handleRenameCategory(code: string) {
  if (!renameValue.value.trim()) return
  const updated = await renameCategory(code, { name: renameValue.value.trim() })
  const idx = categories.value.findIndex((c) => c.categoryCode === code)
  if (idx !== -1) categories.value[idx] = updated
  renamingCat.value = null
}

async function handleToggleCategory(cat: ApiCategory) {
  const updated =
    cat.status === 'ENABLED' ? await disableCategory(cat.categoryCode) : await enableCategory(cat.categoryCode)
  const idx = categories.value.findIndex((c) => c.categoryCode === cat.categoryCode)
  if (idx !== -1) categories.value[idx] = updated
}

// ── Asset management ─────────────────────────────────────────
const assetCodeInput = ref('')
const currentAsset = ref<ApiAsset | null>(null)
const assetLoading = ref(false)
const assetError = ref('')

const registerForm = ref<RegisterAssetBody>({
  apiCode: '',
  displayName: '',
  assetType: 'STANDARD_API',
  categoryCode: '',
})

const aiProfileForm = ref<BindAiProfileBody>({
  provider: '',
  model: '',
  streaming: false,
  tags: [],
})
const aiTagInput = ref('')

async function handleLoadAsset() {
  if (!assetCodeInput.value.trim()) return
  assetLoading.value = true
  assetError.value = ''
  try {
    currentAsset.value = await getAsset(assetCodeInput.value.trim())
  } catch {
    assetError.value = t('console.workspace.assetNotFound')
  } finally {
    assetLoading.value = false
  }
}

async function handleRegisterAsset() {
  assetLoading.value = true
  assetError.value = ''
  try {
    currentAsset.value = await registerAsset(registerForm.value)
    registerForm.value = { apiCode: '', displayName: '', assetType: 'STANDARD_API', categoryCode: '' }
  } catch {
    assetError.value = t('console.workspace.registerFailed')
  } finally {
    assetLoading.value = false
  }
}

async function handleToggleAsset() {
  if (!currentAsset.value) return
  const updated =
    currentAsset.value.status === 'ENABLED'
      ? await disableAsset(currentAsset.value.apiCode)
      : await enableAsset(currentAsset.value.apiCode)
  currentAsset.value = updated
}

async function handleBindAiProfile() {
  if (!currentAsset.value) return
  currentAsset.value = await bindAiProfile(currentAsset.value.apiCode, aiProfileForm.value)
}

function addAiTag() {
  const tag = aiTagInput.value.trim()
  if (tag && !aiProfileForm.value.tags.includes(tag)) {
    aiProfileForm.value.tags.push(tag)
  }
  aiTagInput.value = ''
}

// ── Recent assets ─────────────────────────────────────────────
const recentAssets = ref(getRecentAssets())

onMounted(loadCategories)
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
  <div class="space-y-8">
    <!-- Header -->
    <section>
      <p class="console-kicker">{{ t('console.navigation.catalogManage') }}</p>
      <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
        {{ t('console.workspace.title') }}
      </h2>
      <p class="mt-3 text-sm leading-6 text-muted-foreground">{{ t('console.workspace.description') }}</p>
    </section>

    <div class="grid gap-8 2xl:grid-cols-2">
      <!-- Category management -->
      <Card id="category-manage" class="scroll-mt-24">
        <CardHeader>
          <CardTitle>{{ t('console.workspace.categoryTitle') }}</CardTitle>
          <CardDescription>{{ t('console.workspace.categoryDescription') }}</CardDescription>
        </CardHeader>
        <CardContent class="space-y-4">
          <div class="flex gap-2">
            <Input v-model="newCatName" :placeholder="t('console.workspace.categoryNamePlaceholder')" class="flex-1" />
            <Button size="sm" @click="handleCreateCategory">{{ t('console.workspace.categoryCreate') }}</Button>
          </div>

          <div v-if="catLoading" class="py-6 text-center text-sm text-muted-foreground">
            {{ t('console.workspace.loading') }}
          </div>
          <div v-else-if="catError" class="py-6 text-center text-sm text-destructive">
            {{ t('console.workspace.loadError') }}
          </div>
          <div v-else-if="categories.length === 0" class="py-6 text-center text-sm text-muted-foreground">
            {{ t('console.workspace.categoryEmpty') }}
          </div>
          <div v-else class="space-y-2">
            <div
              v-for="cat in categories"
              :key="cat.categoryCode"
              class="flex items-center gap-3 rounded-[14px] bg-secondary px-4 py-3"
            >
              <div class="min-w-0 flex-1">
                <template v-if="renamingCat === cat.categoryCode">
                  <div class="flex gap-2">
                    <Input v-model="renameValue" class="h-7 flex-1 text-sm" />
                    <Button size="sm" variant="outline" @click="handleRenameCategory(cat.categoryCode)">
                      {{ t('console.workspace.save') }}
                    </Button>
                    <Button size="sm" variant="ghost" @click="renamingCat = null">
                      {{ t('console.workspace.cancel') }}
                    </Button>
                  </div>
                </template>
                <template v-else>
                  <p class="truncate text-sm font-medium text-foreground">{{ cat.name }}</p>
                  <p class="text-xs text-muted-foreground">{{ cat.categoryCode }}</p>
                </template>
              </div>
              <Badge :variant="cat.status === 'ENABLED' ? 'default' : 'outline'" class="shrink-0 text-[11px]">
                {{ cat.status === 'ENABLED' ? t('console.workspace.enabled') : t('console.workspace.disabled') }}
              </Badge>
              <div class="flex shrink-0 gap-1">
                <Button
                  size="sm"
                  variant="ghost"
                  @click="renamingCat = cat.categoryCode; renameValue = cat.name"
                >
                  {{ t('console.workspace.rename') }}
                </Button>
                <Button size="sm" variant="ghost" @click="handleToggleCategory(cat)">
                  {{ cat.status === 'ENABLED' ? t('console.workspace.disable') : t('console.workspace.enable') }}
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Asset management -->
      <div id="catalog-manage" class="space-y-5">
        <Card class="scroll-mt-24">
          <CardHeader>
            <CardTitle>{{ t('console.workspace.assetTitle') }}</CardTitle>
            <CardDescription>{{ t('console.workspace.assetDescription') }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <!-- Load existing asset -->
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

            <!-- Register new asset -->
            <div class="space-y-2 rounded-[14px] bg-secondary p-4">
              <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                {{ t('console.workspace.registerTitle') }}
              </p>
              <Input v-model="registerForm.apiCode" :placeholder="t('console.workspace.fieldApiCode')" />
              <Input v-model="registerForm.displayName" :placeholder="t('console.workspace.fieldDisplayName')" />
              <Input v-model="registerForm.categoryCode" :placeholder="t('console.workspace.fieldCategoryCode')" />
              <select
                v-model="registerForm.assetType"
                class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="STANDARD_API">STANDARD_API</option>
                <option value="AI_API">AI_API</option>
              </select>
              <Button size="sm" :disabled="assetLoading" @click="handleRegisterAsset">
                {{ t('console.workspace.registerAction') }}
              </Button>
            </div>

            <p v-if="assetError" class="text-sm text-destructive">{{ assetError }}</p>

            <!-- Current asset snapshot -->
            <div v-if="currentAsset" class="space-y-3 rounded-[14px] border px-4 py-4">
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="font-semibold text-foreground">{{ currentAsset.displayName }}</p>
                  <p class="text-xs text-muted-foreground">{{ currentAsset.apiCode }}</p>
                </div>
                <Badge :variant="currentAsset.status === 'ENABLED' ? 'default' : 'outline'" class="shrink-0">
                  {{ currentAsset.status }}
                </Badge>
              </div>
              <div class="flex gap-2">
                <Button size="sm" variant="outline" @click="handleToggleAsset">
                  {{ currentAsset.status === 'ENABLED' ? t('console.workspace.disable') : t('console.workspace.enable') }}
                </Button>
              </div>

              <!-- AI profile form — only for AI_API -->
              <div v-if="currentAsset.assetType === 'AI_API'" class="space-y-2 rounded-[14px] bg-secondary p-4">
                <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  {{ t('console.workspace.aiProfileTitle') }}
                </p>
                <Input v-model="aiProfileForm.provider" :placeholder="t('console.workspace.fieldProvider')" />
                <Input v-model="aiProfileForm.model" :placeholder="t('console.workspace.fieldModel')" />
                <label class="flex items-center gap-2 text-sm">
                  <input v-model="aiProfileForm.streaming" type="checkbox" />
                  {{ t('console.workspace.fieldStreaming') }}
                </label>
                <div class="flex gap-2">
                  <Input v-model="aiTagInput" :placeholder="t('console.workspace.fieldTagPlaceholder')" class="flex-1" @keydown.enter.prevent="addAiTag" />
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
                <Button size="sm" @click="handleBindAiProfile">{{ t('console.workspace.bindAiProfile') }}</Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Recently opened assets — local shortcut only -->
        <Card v-if="recentAssets.length">
          <CardHeader>
            <CardTitle class="text-sm">{{ t('console.workspace.recentTitle') }}</CardTitle>
            <CardDescription class="text-xs">{{ t('console.workspace.recentNote') }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-2">
            <button
              v-for="asset in recentAssets"
              :key="asset.apiCode"
              type="button"
              class="flex w-full items-center justify-between rounded-[14px] bg-secondary px-4 py-3 text-left hover:bg-white hover:shadow-console"
              @click="assetCodeInput = asset.apiCode; handleLoadAsset()"
            >
              <span class="truncate text-sm font-medium text-foreground">{{ asset.displayName }}</span>
              <span class="ml-3 shrink-0 text-xs text-muted-foreground">{{ asset.apiCode }}</span>
            </button>
          </CardContent>
        </Card>
      </div>
    </div>
  </div>
</template>
