<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(
  defineProps<{
    class?: HTMLAttributes['class']
    size?: 'default' | 'sm'
  }>(),
  {
    size: 'default',
  },
)
</script>

<template>
  <div
    data-slot="card"
    :data-size="size"
    :class="
      cn(
        'group/card flex flex-col overflow-hidden rounded-[20px] border border-[rgb(34_34_34_/_0.04)] bg-white text-sm text-card-foreground shadow-console data-[size=sm]:rounded-[18px]',
        props.class,
      )
    "
  >
    <slot />
  </div>
</template>
