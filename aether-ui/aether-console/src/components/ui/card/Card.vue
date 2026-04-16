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
        'bg-card text-card-foreground gap-6 overflow-hidden rounded-xl py-6 text-sm shadow-[0_24px_48px_-36px_rgba(28,39,56,0.28)] has-data-[slot=card-footer]:pb-0 has-[>img:first-child]:pt-0 data-[size=sm]:gap-4 data-[size=sm]:py-4 data-[size=sm]:has-data-[slot=card-footer]:pb-0 *:[img:first-child]:rounded-t-xl *:[img:last-child]:rounded-b-xl group/card flex flex-col',
        props.class,
      )
    "
  >
    <slot />
  </div>
</template>
