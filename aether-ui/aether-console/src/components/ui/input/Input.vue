<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { useVModel } from '@vueuse/core'
import { cn } from '@/lib/utils'

const props = defineProps<{
  defaultValue?: string | number
  modelValue?: string | number
  class?: HTMLAttributes['class']
}>()

const emits = defineEmits<{
  (e: 'update:modelValue', payload: string | number): void
}>()

const modelValue = useVModel(props, 'modelValue', emits, {
  passive: true,
  defaultValue: props.defaultValue,
})
</script>

<template>
  <input
    v-model="modelValue"
    data-slot="input"
    :class="
      cn(
        'bg-input text-foreground placeholder:text-muted-foreground h-12 w-full min-w-0 rounded-xl border border-transparent px-4 py-3 text-sm transition-[background-color,box-shadow,border-color] outline-none file:inline-flex file:border-0 file:bg-transparent file:text-foreground file:text-sm file:font-medium focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/15 aria-invalid:text-destructive aria-invalid:ring-2 aria-invalid:ring-destructive/15 disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 disabled:bg-muted',
        props.class,
      )
    "
  />
</template>
