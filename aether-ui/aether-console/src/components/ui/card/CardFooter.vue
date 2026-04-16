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
        'bg-muted/65 rounded-b-xl p-6 group-data-[size=sm]/card:p-4 flex items-center',
        props.class,
      )
    "
  >
    <slot />
  </div>
</template>
