<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { DiscoveryAssetDetail } from '@/api/catalog/catalog.types'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Info, ShieldCheck, Zap, ArrowRightLeft, X } from 'lucide-vue-next'

withDefaults(defineProps<{
  selectedAssetDetail?: DiscoveryAssetDetail | null
  compact?: boolean
}>(), {
  selectedAssetDetail: null,
  compact: false,
})

defineEmits<{
  close: []
}>()

const { t } = useI18n()
</script>

<template>
  <!-- Compact: minimal help link -->
  <Card v-if="compact" class="border-dashed">
    <CardContent class="flex items-center gap-3 py-4">
      <Info class="h-4 w-4 shrink-0 text-muted-foreground" />
      <p class="text-sm text-muted-foreground">{{ t('console.playground.guidanceCompactHint') }}</p>
    </CardContent>
  </Card>

  <!-- Full guidance panel -->
  <div v-else class="space-y-5">
    <!-- How it works -->
    <Card>
      <CardHeader>
        <CardTitle class="flex items-center justify-between text-base">
          {{ t('console.playground.guidanceTitle') }}
          <Button variant="ghost" size="sm" @click="$emit('close')">
            <X class="h-4 w-4" />
          </Button>
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-4 text-sm leading-6 text-muted-foreground">
        <!-- Request auth -->
        <div class="flex items-start gap-3">
          <ShieldCheck class="mt-0.5 h-4 w-4 shrink-0 text-foreground" />
          <div>
            <p class="font-medium text-foreground">{{ t('console.playground.guidanceAuthTitle') }}</p>
            <p>{{ t('console.playground.guidanceAuthBody') }}</p>
          </div>
        </div>

        <!-- Response passthrough -->
        <div class="flex items-start gap-3">
          <ArrowRightLeft class="mt-0.5 h-4 w-4 shrink-0 text-foreground" />
          <div>
            <p class="font-medium text-foreground">{{ t('console.playground.guidancePassthroughTitle') }}</p>
            <p>{{ t('console.playground.guidancePassthroughBody') }}</p>
          </div>
        </div>

        <!-- Streaming hint -->
        <div v-if="selectedAssetDetail?.aiProfile?.streaming" class="flex items-start gap-3">
          <Zap class="mt-0.5 h-4 w-4 shrink-0 text-amber-500" />
          <div>
            <p class="font-medium text-foreground">{{ t('console.playground.guidanceStreamingTitle') }}</p>
            <p>{{ t('console.playground.guidanceStreamingBody') }}</p>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Failure type reference -->
    <Card>
      <CardHeader>
        <CardTitle class="text-base">{{ t('console.playground.failureReferenceTitle') }}</CardTitle>
      </CardHeader>
      <CardContent class="space-y-3">
        <div class="flex items-start gap-3">
          <Badge class="shrink-0 bg-amber-50 text-amber-700 text-xs">INVALID_API_CODE</Badge>
          <p class="text-sm text-muted-foreground">{{ t('console.playground.failureExplain.INVALID_API_CODE') }}</p>
        </div>
        <div class="flex items-start gap-3">
          <Badge class="shrink-0 bg-red-50 text-red-700 text-xs">INVALID_CREDENTIAL</Badge>
          <p class="text-sm text-muted-foreground">{{ t('console.playground.failureExplain.INVALID_CREDENTIAL') }}</p>
        </div>
        <div class="flex items-start gap-3">
          <Badge class="shrink-0 bg-orange-50 text-orange-700 text-xs">TARGET_NOT_FOUND</Badge>
          <p class="text-sm text-muted-foreground">{{ t('console.playground.failureExplain.TARGET_NOT_FOUND') }}</p>
        </div>
        <div class="flex items-start gap-3">
          <Badge class="shrink-0 bg-slate-50 text-slate-700 text-xs">TARGET_UNAVAILABLE</Badge>
          <p class="text-sm text-muted-foreground">{{ t('console.playground.failureExplain.TARGET_UNAVAILABLE') }}</p>
        </div>
      </CardContent>
    </Card>

    <!-- Selected asset detail -->
    <Card v-if="selectedAssetDetail">
      <CardHeader>
        <CardTitle class="text-base">{{ t('console.playground.selectedAssetTitle') }}</CardTitle>
      </CardHeader>
      <CardContent class="space-y-2 text-sm">
        <div class="flex justify-between">
          <span class="text-muted-foreground">apiCode</span>
          <code class="text-foreground">{{ selectedAssetDetail.apiCode }}</code>
        </div>
        <div v-if="selectedAssetDetail.displayName" class="flex justify-between">
          <span class="text-muted-foreground">{{ t('console.playground.fieldDisplayName') }}</span>
          <span class="text-foreground">{{ selectedAssetDetail.displayName }}</span>
        </div>
        <div v-if="selectedAssetDetail.methods?.length" class="flex justify-between">
          <span class="text-muted-foreground">{{ t('console.playground.fieldRecommendedMethods') }}</span>
          <div class="flex gap-1">
            <Badge v-for="m in selectedAssetDetail.methods" :key="m" variant="outline" class="text-xs">{{ m }}</Badge>
          </div>
        </div>
        <div v-if="selectedAssetDetail.aiProfile" class="flex justify-between">
          <span class="text-muted-foreground">{{ t('console.playground.fieldModel') }}</span>
          <span class="text-foreground">{{ selectedAssetDetail.aiProfile.provider }} / {{ selectedAssetDetail.aiProfile.model }}</span>
        </div>
        <div v-if="selectedAssetDetail.requestTemplate" class="mt-3">
          <p class="mb-1 text-muted-foreground">{{ t('console.playground.fieldRequestTemplate') }}</p>
          <pre class="max-h-[200px] overflow-auto rounded-[8px] border border-[rgb(34_34_34_/_0.06)] bg-muted/40 p-3 font-mono text-xs leading-5 text-foreground">{{ selectedAssetDetail.requestTemplate }}</pre>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
