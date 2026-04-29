<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useUnifiedAccessPlayground } from '@/composables/useUnifiedAccessPlayground'
import type { UnifiedAccessMethod } from '@/api/unified-access/unified-access.types'
import type { DiscoveryAsset } from '@/api/catalog/catalog.types'
import UnifiedAccessGuidance from './UnifiedAccessGuidance.vue'
import CodeBlock from '@/components/console/CodeBlock.vue'
import CopyableField from '@/components/console/CopyableField.vue'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Label } from '@/components/ui/label'
import {
  Eye,
  EyeOff,
  Info,
  Loader2,
  Play,
  RotateCcw,
  ShieldAlert,
  Trash2,
  Download,
  Copy,
  CheckCircle2,
  XCircle,
  AlertTriangle,
} from 'lucide-vue-next'
import { buildUnifiedAccessAddress } from '@/utils/platform-url'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const {
  apiCode,
  method,
  apiKey,
  requestBody,
  extraHeaders,
  discoveryAssets,
  discoveryLoading,
  discoveryError,
  selectedAssetDetail,
  detailLoading,
  loadDiscoveryAssets,
  selectDiscoveryAsset,
  loadSelectedAssetDetail,
  invoking,
  result,
  invokeError,
  elapsedMs,
  methodSupportsBody,
  canInvoke,
  invoke,
  resetForm,
  clearApiKey,
  clearResult,
} = useUnifiedAccessPlayground()

const showApiKey = ref(false)
const showDiscoveryPicker = ref(false)
const showGuidance = ref(false)
const responseCopied = ref(false)

const METHODS: UnifiedAccessMethod[] = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE']

function handleSelectAsset(asset: DiscoveryAsset) {
  selectDiscoveryAsset(asset)
  showDiscoveryPicker.value = false
}

function toggleDiscoveryPicker() {
  showDiscoveryPicker.value = !showDiscoveryPicker.value
  if (showDiscoveryPicker.value && discoveryAssets.value.length === 0) {
    loadDiscoveryAssets()
  }
}

async function handleCopyResponse() {
  if (!result.value) return
  let text = ''
  if (result.value.kind === 'json') {
    text = JSON.stringify(result.value.jsonBody, null, 2)
  } else if (result.value.kind === 'text') {
    text = result.value.textBody ?? ''
  } else if (result.value.kind === 'platform-failure' && result.value.platformFailure) {
    text = JSON.stringify(result.value.platformFailure, null, 2)
  }
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    responseCopied.value = true
    setTimeout(() => {
      responseCopied.value = false
    }, 2000)
  } catch {
    // clipboard may not be available
  }
}

function handleDownloadBlob() {
  if (result.value?.kind !== 'binary' || !result.value.blobBody) return
  const url = URL.createObjectURL(result.value.blobBody)
  const a = document.createElement('a')
  a.href = url
  a.download = `response_${apiCode.value || 'data'}`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function getMethodColor(m: UnifiedAccessMethod): string {
  const colors: Record<UnifiedAccessMethod, string> = {
    GET: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    POST: 'bg-blue-50 text-blue-700 border-blue-200',
    PUT: 'bg-amber-50 text-amber-700 border-amber-200',
    PATCH: 'bg-purple-50 text-purple-700 border-purple-200',
    DELETE: 'bg-red-50 text-red-700 border-red-200',
  }
  return colors[m]
}

function getFailureTypeColor(failureType: string): string {
  const map: Record<string, string> = {
    INVALID_API_CODE: 'bg-amber-50 text-amber-700',
    INVALID_CREDENTIAL: 'bg-red-50 text-red-700',
    TARGET_NOT_FOUND: 'bg-orange-50 text-orange-700',
    TARGET_UNAVAILABLE: 'bg-slate-50 text-slate-700',
  }
  return map[failureType] ?? 'bg-muted text-foreground'
}

function routeApiCode() {
  return typeof route.query.apiCode === 'string' ? route.query.apiCode : ''
}

function openApiKeyManagement() {
  router.push({ name: 'console-workspace', hash: '#credentials' })
}

onMounted(async () => {
  await loadDiscoveryAssets()
  const queryApiCode = routeApiCode()
  if (queryApiCode) {
    await loadSelectedAssetDetail(queryApiCode)
  }
})

watch(
  () => route.query.apiCode,
  async () => {
    const queryApiCode = routeApiCode()
    if (queryApiCode && queryApiCode !== apiCode.value) {
      await loadSelectedAssetDetail(queryApiCode)
    }
  },
)
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <section>
      <p class="console-kicker">Unified Access</p>
      <h2 class="console-display mt-3 text-[1.75rem] font-bold text-foreground">
        {{ t('console.playground.title') }}
      </h2>
      <p class="mt-3 text-sm leading-6 text-muted-foreground">
        {{ t('console.playground.description') }}
      </p>
    </section>

    <!-- Security notice -->
    <div
      class="flex items-start gap-3 rounded-[14px] border border-amber-200 bg-amber-50 px-5 py-4"
    >
      <ShieldAlert class="mt-0.5 h-4 w-4 shrink-0 text-amber-600" />
      <p class="text-sm leading-6 text-amber-800">{{ t('console.playground.securityNotice') }}</p>
    </div>

    <div class="grid gap-5 xl:grid-cols-[1fr_380px]">
      <!-- Left: Request Form -->
      <div class="space-y-5">
        <!-- Target API card -->
        <Card>
          <CardHeader>
            <CardTitle class="flex items-center justify-between">
              {{ t('console.playground.targetApi') }}
              <Button variant="ghost" size="sm" @click="toggleDiscoveryPicker">
                {{
                  showDiscoveryPicker
                    ? t('console.playground.hideDiscovery')
                    : t('console.playground.browseDiscovery')
                }}
              </Button>
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-4">
            <!-- Discovery picker -->
            <div
              v-if="showDiscoveryPicker"
              class="rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-muted/40 p-4"
            >
              <div v-if="discoveryLoading" class="py-4 text-center text-sm text-muted-foreground">
                <Loader2 class="mx-auto mb-2 h-4 w-4 animate-spin" />
                {{ t('console.playground.discoveryLoading') }}
              </div>
              <div v-else-if="discoveryError" class="py-4 text-center text-sm text-destructive">
                {{ t('console.playground.discoveryError') }}
              </div>
              <div
                v-else-if="discoveryAssets.length === 0"
                class="py-4 text-center text-sm text-muted-foreground"
              >
                {{ t('console.playground.discoveryEmpty') }}
              </div>
              <div v-else class="grid gap-2 sm:grid-cols-2">
                <button
                  v-for="asset in discoveryAssets"
                  :key="asset.apiCode"
                  class="flex items-center gap-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white px-4 py-3 text-left shadow-console transition-[box-shadow,transform] duration-200 hover:-translate-y-px hover:shadow-console-hover"
                  @click="handleSelectAsset(asset)"
                >
                  <div class="min-w-0 flex-1">
                    <p class="truncate text-sm font-medium text-foreground">
                      {{ asset.displayName || asset.apiCode }}
                    </p>
                    <p class="mt-0.5 truncate text-xs text-muted-foreground">{{ asset.apiCode }}</p>
                  </div>
                  <Badge variant="secondary" class="shrink-0 text-xs">{{
                    asset.assetType === 'AI_API' ? 'AI' : 'API'
                  }}</Badge>
                </button>
              </div>
            </div>

            <!-- apiCode input -->
            <div class="space-y-2">
              <Label>{{ t('console.playground.fieldApiCode') }}</Label>
              <Input v-model="apiCode" :placeholder="t('console.playground.apiCodePlaceholder')" />
            </div>

            <CopyableField
              v-if="apiCode.trim()"
              :label="t('console.shared.platformCallAddress')"
              :hint="t('console.playground.platformAddressHint')"
              :value="buildUnifiedAccessAddress(apiCode.trim())"
            />

            <!-- Detail loading indicator -->
            <div v-if="detailLoading" class="flex items-center gap-2 text-sm text-muted-foreground">
              <Loader2 class="h-3.5 w-3.5 animate-spin" />
              {{ t('console.playground.loadingDetail') }}
            </div>

            <!-- Pre-fill hint from discovery -->
            <div
              v-if="selectedAssetDetail && !detailLoading"
              class="flex flex-wrap items-center gap-2 text-sm text-muted-foreground"
            >
              <Info class="h-3.5 w-3.5" />
              <span
                >{{ t('console.playground.prefilledFrom') }}
                <strong>{{
                  selectedAssetDetail.displayName || selectedAssetDetail.apiCode
                }}</strong></span
              >
              <Badge
                v-if="selectedAssetDetail.aiProfile?.streaming"
                variant="outline"
                class="text-xs"
              >
                {{ t('console.playground.streamingSupported') }}
              </Badge>
            </div>

            <!-- Method selector -->
            <div class="space-y-2">
              <Label>{{ t('console.playground.fieldMethod') }}</Label>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="m in METHODS"
                  :key="m"
                  :class="[
                    'rounded-[8px] border px-3 py-1.5 text-xs font-semibold transition-all duration-150',
                    method === m
                      ? getMethodColor(m) + ' ring-1 ring-current'
                      : 'border-[rgb(34_34_34_/_0.08)] bg-white text-muted-foreground hover:bg-muted',
                  ]"
                  @click="method = m"
                >
                  {{ m }}
                </button>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- API Key card -->
        <Card>
          <CardHeader>
            <CardTitle>{{ t('console.playground.fieldApiKey') }}</CardTitle>
            <CardDescription>{{ t('console.playground.apiKeyHint') }}</CardDescription>
          </CardHeader>
          <CardContent class="space-y-3">
            <div class="flex gap-2">
              <div class="relative flex-1">
                <Input
                  v-model="apiKey"
                  :type="showApiKey ? 'text' : 'password'"
                  :placeholder="t('console.playground.apiKeyPlaceholder')"
                  autocomplete="off"
                />
                <button
                  class="absolute inset-y-0 right-3 flex items-center text-muted-foreground hover:text-foreground"
                  @click="showApiKey = !showApiKey"
                >
                  <Eye v-if="!showApiKey" class="h-4 w-4" />
                  <EyeOff v-else class="h-4 w-4" />
                </button>
              </div>
              <Button variant="outline" size="sm" :disabled="!apiKey" @click="clearApiKey">
                <Trash2 class="h-3.5 w-3.5" />
              </Button>
            </div>
            <Button size="sm" variant="link" @click="openApiKeyManagement">
              {{ t('console.playground.apiKeyManage') }}
            </Button>
          </CardContent>
        </Card>

        <!-- Request body card -->
        <Card v-if="methodSupportsBody">
          <CardHeader>
            <CardTitle>{{ t('console.playground.fieldRequestBody') }}</CardTitle>
            <CardDescription>{{ t('console.playground.requestBodyHint') }}</CardDescription>
          </CardHeader>
          <CardContent>
            <textarea
              v-model="requestBody"
              rows="8"
              class="w-full resize-y rounded-[8px] border border-[rgb(34_34_34_/_0.12)] bg-white p-4 font-mono text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              :placeholder="t('console.playground.requestBodyPlaceholder')"
              spellcheck="false"
            />
          </CardContent>
        </Card>

        <!-- Extra headers card (optional, collapsed by default) -->
        <Card>
          <CardHeader>
            <CardTitle>{{ t('console.playground.fieldExtraHeaders') }}</CardTitle>
            <CardDescription>{{ t('console.playground.extraHeadersHint') }}</CardDescription>
          </CardHeader>
          <CardContent>
            <textarea
              v-model="extraHeaders"
              rows="3"
              class="w-full resize-y rounded-[8px] border border-[rgb(34_34_34_/_0.12)] bg-white p-4 font-mono text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              :placeholder="`{ &quot;Accept&quot;: &quot;application/json&quot; }`"
              spellcheck="false"
            />
          </CardContent>
        </Card>

        <!-- Actions -->
        <div class="flex items-center gap-3">
          <Button :disabled="!canInvoke || invoking" @click="invoke">
            <Loader2 v-if="invoking" class="mr-2 h-4 w-4 animate-spin" />
            <Play v-else class="mr-2 h-4 w-4" />
            {{ invoking ? t('console.playground.invoking') : t('console.playground.invoke') }}
          </Button>
          <Button variant="outline" @click="resetForm">
            <RotateCcw class="mr-2 h-4 w-4" />
            {{ t('console.playground.reset') }}
          </Button>
          <Button variant="ghost" size="sm" class="ml-auto" @click="showGuidance = !showGuidance">
            <Info class="mr-2 h-4 w-4" />
            {{ t('console.playground.usageGuide') }}
          </Button>
        </div>

        <!-- Response -->
        <Card v-if="result || invokeError">
          <CardHeader>
            <CardTitle class="flex items-center gap-3">
              {{ t('console.playground.responseTitle') }}
              <Badge
                v-if="result"
                :variant="result.status >= 200 && result.status < 300 ? 'default' : 'destructive'"
                class="text-xs"
              >
                {{ result.status }}
              </Badge>
              <span v-if="elapsedMs" class="text-xs font-normal text-muted-foreground"
                >{{ elapsedMs }}ms</span
              >
              <span class="flex-1" />
              <Button
                v-if="result && result.kind !== 'binary'"
                variant="ghost"
                size="sm"
                @click="handleCopyResponse"
              >
                <Copy v-if="!responseCopied" class="mr-1 h-3.5 w-3.5" />
                <CheckCircle2 v-else class="mr-1 h-3.5 w-3.5 text-emerald-600" />
                {{ responseCopied ? t('console.playground.copied') : t('console.playground.copy') }}
              </Button>
              <Button v-if="result" variant="ghost" size="sm" @click="clearResult">
                <Trash2 class="h-3.5 w-3.5" />
              </Button>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <!-- Client-side error -->
            <div
              v-if="invokeError"
              class="flex items-start gap-3 rounded-[14px] bg-red-50 px-5 py-4"
            >
              <XCircle class="mt-0.5 h-4 w-4 shrink-0 text-red-600" />
              <p class="text-sm text-red-800">{{ invokeError }}</p>
            </div>

            <!-- Platform failure -->
            <div
              v-else-if="result?.kind === 'platform-failure' && result.platformFailure"
              class="space-y-4"
            >
              <div class="flex items-start gap-3 rounded-[14px] bg-amber-50 px-5 py-4">
                <AlertTriangle class="mt-0.5 h-4 w-4 shrink-0 text-amber-600" />
                <div class="min-w-0 flex-1 space-y-2">
                  <p class="text-sm font-medium text-amber-800">
                    {{ t('console.playground.platformFailure') }}
                  </p>
                  <div class="flex flex-wrap items-center gap-2">
                    <Badge
                      :class="getFailureTypeColor(result.platformFailure.failureType)"
                      class="text-xs"
                    >
                      {{ result.platformFailure.failureType }}
                    </Badge>
                    <code class="text-xs text-amber-700">{{ result.platformFailure.code }}</code>
                  </div>
                  <p class="text-sm text-amber-700">{{ result.platformFailure.message }}</p>
                  <div v-if="result.platformFailure.traceId" class="mt-2 text-xs text-amber-600">
                    <span class="font-medium">traceId:</span> {{ result.platformFailure.traceId }}
                  </div>
                  <div v-if="result.platformFailure.apiCode" class="text-xs text-amber-600">
                    <span class="font-medium">apiCode:</span> {{ result.platformFailure.apiCode }}
                  </div>
                </div>
              </div>
              <!-- Failure explanation -->
              <p class="text-sm text-muted-foreground">
                {{ t(`console.playground.failureExplain.${result.platformFailure.failureType}`) }}
              </p>
            </div>

            <!-- JSON success -->
            <div v-else-if="result?.kind === 'json'">
              <div class="flex items-center gap-2 mb-3">
                <CheckCircle2 class="h-4 w-4 text-emerald-600" />
                <span class="text-sm text-emerald-700">{{
                  t('console.playground.passthroughSuccess')
                }}</span>
                <Badge variant="outline" class="text-xs">{{ result.contentType }}</Badge>
              </div>
              <CodeBlock :value="result.jsonBody" max-height-class="max-h-[500px]" />
            </div>

            <!-- Text success -->
            <div v-else-if="result?.kind === 'text'">
              <div class="flex items-center gap-2 mb-3">
                <CheckCircle2 class="h-4 w-4 text-emerald-600" />
                <span class="text-sm text-emerald-700">{{
                  t('console.playground.passthroughSuccess')
                }}</span>
                <Badge variant="outline" class="text-xs">{{ result.contentType }}</Badge>
              </div>
              <CodeBlock :value="result.textBody" max-height-class="max-h-[500px]" />
            </div>

            <!-- Binary -->
            <div
              v-else-if="result?.kind === 'binary'"
              class="flex flex-col items-center gap-4 py-6"
            >
              <Download class="h-8 w-8 text-muted-foreground" />
              <p class="text-sm text-muted-foreground">
                {{ t('console.playground.binaryResponse') }}
              </p>
              <Badge variant="outline" class="text-xs">{{ result.contentType }}</Badge>
              <Button variant="outline" @click="handleDownloadBlob">
                <Download class="mr-2 h-4 w-4" />
                {{ t('console.playground.download') }}
              </Button>
            </div>

            <!-- Raw headers -->
            <details v-if="result" class="mt-4">
              <summary
                class="cursor-pointer text-xs font-medium text-muted-foreground hover:text-foreground"
              >
                {{ t('console.playground.rawHeaders') }}
              </summary>
              <CodeBlock
                class="mt-2"
                :value="result.rawHeaders"
                max-height-class="max-h-[200px]"
              />
            </details>
          </CardContent>
        </Card>
      </div>

      <!-- Right: Guidance Panel -->
      <div class="space-y-5">
        <UnifiedAccessGuidance
          v-if="showGuidance"
          :selected-asset-detail="selectedAssetDetail"
          @close="showGuidance = false"
        />
        <UnifiedAccessGuidance v-else :selected-asset-detail="selectedAssetDetail" compact />
      </div>
    </div>
  </div>
</template>
