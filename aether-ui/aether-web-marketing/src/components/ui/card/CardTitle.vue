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
    :class="cn('card-ethereal-title group-data-[size=sm]/card:text-lg', props.class)"
  >
    <slot />
  </div>
</template>
