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
        'card-ethereal py-4 text-sm data-[size=sm]:gap-3 data-[size=sm]:py-3 has-data-[slot=card-footer]:pb-0 data-[size=sm]:has-data-[slot=card-footer]:pb-0 *:[img:first-child]:rounded-t-lg *:[img:last-child]:rounded-b-lg group/card',
        props.class,
      )
    "
  >
    <slot />
  </div>
</template>
