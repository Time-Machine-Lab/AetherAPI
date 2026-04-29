<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{
  label: string
  hint?: string
  required?: boolean
  optional?: boolean
}>()

const { t } = useI18n()
</script>

<template>
  <div class="space-y-1">
    <div class="flex flex-wrap items-center gap-2">
      <label class="text-xs font-semibold text-foreground">{{ label }}</label>
      <span
        v-if="required"
        class="rounded-full bg-[color-mix(in_srgb,var(--destructive)_8%,white)] px-2 py-0.5 text-[10px] font-semibold text-destructive"
      >
        {{ t('console.shared.required') }}
      </span>
      <span
        v-else-if="optional"
        class="rounded-full bg-secondary px-2 py-0.5 text-[10px] font-semibold text-muted-foreground"
      >
        {{ t('console.shared.optional') }}
      </span>
    </div>
    <p v-if="hint" class="text-xs leading-5 text-muted-foreground">{{ hint }}</p>
  </div>
</template>
