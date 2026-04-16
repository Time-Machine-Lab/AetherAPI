<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{
  class?: HTMLAttributes['class']
}>()
</script>

<template>
  <div
    data-slot="card-footer"
    :class="
      cn(
        'flex items-center gap-3 px-6 pb-6 group-data-[size=sm]/card:px-5 group-data-[size=sm]/card:pb-5',
        props.class,
      )
    "
  >
    <slot />
  </div>
</template>
