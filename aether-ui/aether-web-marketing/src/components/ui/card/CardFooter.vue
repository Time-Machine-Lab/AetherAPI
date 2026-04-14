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
      cn('card-ethereal-footer group-data-[size=sm]/card:px-4 group-data-[size=sm]/card:pb-4', props.class)
    "
  >
    <slot />
  </div>
</template>
