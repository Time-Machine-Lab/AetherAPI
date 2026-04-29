<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Check, Copy } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { copyTextToClipboard, formatCodeContent } from '@/utils/code-display'

const props = withDefaults(
  defineProps<{
    value: unknown
    label?: string
    copyable?: boolean
    editable?: boolean
    maxHeightClass?: string
  }>(),
  {
    copyable: true,
    editable: false,
    maxHeightClass: 'max-h-[360px]',
  },
)

const { t } = useI18n()
const formatted = computed(() => formatCodeContent(props.value))
const feedback = ref<'idle' | 'success' | 'failed'>('idle')

async function copy() {
  feedback.value = (await copyTextToClipboard(formatted.value.source)) ? 'success' : 'failed'
  window.setTimeout(() => {
    feedback.value = 'idle'
  }, 1800)
}
</script>

<template>
  <div
    class="overflow-hidden rounded-[14px] border"
    :class="
      editable
        ? 'border-primary/20 bg-white'
        : 'border-[rgb(34_34_34_/_0.06)] bg-[color-mix(in_srgb,var(--secondary)_64%,white)]'
    "
  >
    <div
      v-if="label || copyable"
      class="flex min-h-10 items-center justify-between gap-3 border-b border-[rgb(34_34_34_/_0.06)] px-3 py-2"
    >
      <div class="flex min-w-0 items-center gap-2">
        <p v-if="label" class="truncate text-xs font-semibold text-foreground">{{ label }}</p>
        <span class="rounded-full bg-white px-2 py-0.5 text-[10px] font-semibold uppercase text-muted-foreground">
          {{ formatted.language }}
        </span>
      </div>
      <Button
        v-if="copyable && formatted.source"
        type="button"
        size="icon-xs"
        variant="ghost"
        :title="t('console.shared.copy')"
        @click="copy"
      >
        <Check v-if="feedback === 'success'" class="size-3.5 text-primary" />
        <Copy v-else class="size-3.5" />
      </Button>
    </div>
    <pre
      class="overflow-auto px-4 py-3 font-mono text-xs leading-5 text-foreground"
      :class="maxHeightClass"
    ><code>{{ formatted.display || t('console.shared.emptyCode') }}</code></pre>
    <p
      v-if="feedback !== 'idle'"
      class="border-t border-[rgb(34_34_34_/_0.06)] px-3 py-2 text-xs"
      :class="feedback === 'success' ? 'text-primary' : 'text-destructive'"
    >
      {{ feedback === 'success' ? t('console.shared.copySuccess') : t('console.shared.copyFailed') }}
    </p>
  </div>
</template>
