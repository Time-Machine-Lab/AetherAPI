<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{
  class?: HTMLAttributes['class']
}>()
</script>

<template>
  <div
    data-slot="card-header"
    :class="
      cn(
        'grid auto-rows-min items-start gap-3 px-6 pt-6 group-data-[size=sm]/card:px-5 group-data-[size=sm]/card:pt-5',
        props.class,
      )
    "
  >
    <slot />
  </div>
</template>
