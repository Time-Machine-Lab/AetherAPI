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
    :class="
      cn(
        'text-lg leading-snug font-semibold group-data-[size=sm]/card:text-base cn-font-heading',
        props.class,
      )
    "
  >
    <slot />
  </div>
</template>
