<script setup lang="ts">
import type { Component } from 'vue'
import { AlertTriangle, Ban, CheckCircle2, Inbox, Loader2 } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    tone?: 'empty' | 'loading' | 'error' | 'unavailable' | 'success'
    title: string
    description?: string
    icon?: Component
  }>(),
  {
    tone: 'empty',
  },
)

const defaultIcons = {
  empty: Inbox,
  loading: Loader2,
  error: AlertTriangle,
  unavailable: Ban,
  success: CheckCircle2,
}
</script>

<template>
  <div
    data-console-state-block
    class="flex flex-col items-center justify-center gap-3 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white px-5 py-9 text-center"
  >
    <component
      :is="icon ?? defaultIcons[tone]"
      class="size-6"
      :class="[
        tone === 'error' ? 'text-destructive' : '',
        tone === 'success' ? 'text-primary' : '',
        tone === 'loading' ? 'animate-spin text-muted-foreground' : '',
        tone === 'empty' || tone === 'unavailable' ? 'text-muted-foreground/60' : '',
      ]"
    />
    <div class="space-y-1">
      <p
        class="text-sm font-medium"
        :class="tone === 'error' ? 'text-destructive' : 'text-foreground'"
      >
        {{ title }}
      </p>
      <p v-if="description" class="text-xs leading-5 text-muted-foreground">
        {{ description }}
      </p>
    </div>
    <slot />
  </div>
</template>
