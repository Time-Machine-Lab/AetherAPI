<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Check, Copy } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { copyTextToClipboard } from '@/utils/code-display'

const props = defineProps<{
  label: string
  value: string
  hint?: string
}>()

const { t } = useI18n()
const feedback = ref<'idle' | 'success' | 'failed'>('idle')

async function copy() {
  feedback.value = (await copyTextToClipboard(props.value)) ? 'success' : 'failed'
  window.setTimeout(() => {
    feedback.value = 'idle'
  }, 1800)
}
</script>

<template>
  <div class="rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-secondary/50 p-3">
    <div class="mb-2 flex items-start justify-between gap-3">
      <div class="min-w-0">
        <p class="text-xs font-semibold text-foreground">{{ label }}</p>
        <p v-if="hint" class="mt-1 text-xs leading-5 text-muted-foreground">{{ hint }}</p>
      </div>
      <Button
        type="button"
        size="icon-xs"
        variant="outline"
        :title="t('console.shared.copy')"
        @click="copy"
      >
        <Check v-if="feedback === 'success'" class="size-3.5 text-primary" />
        <Copy v-else class="size-3.5" />
      </Button>
    </div>
    <code class="block break-all rounded-[8px] bg-white px-3 py-2 text-xs text-foreground">
      {{ value }}
    </code>
    <p
      v-if="feedback !== 'idle'"
      class="mt-2 text-xs"
      :class="feedback === 'success' ? 'text-primary' : 'text-destructive'"
    >
      {{
        feedback === 'success' ? t('console.shared.copySuccess') : t('console.shared.copyFailed')
      }}
    </p>
  </div>
</template>
