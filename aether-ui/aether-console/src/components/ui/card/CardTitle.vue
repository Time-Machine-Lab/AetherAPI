<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{
  class?: HTMLAttributes['class']
}>()
</script>

<template>
  <div
    data-slot="card-title"
    :class="cn('console-display text-[1.375rem] leading-[1.18] font-semibold', props.class)"
  >
    <slot />
  </div>
</template>
