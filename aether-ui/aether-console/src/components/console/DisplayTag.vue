<script setup lang="ts">
import type { Component } from 'vue'
import { computed } from 'vue'
import { displayToneClasses, type DisplayTone } from '@/utils/visual-system'

const props = withDefaults(
  defineProps<{
    tone?: DisplayTone
    icon?: Component
    label?: string | number | null
  }>(),
  {
    tone: 'neutral',
  },
)

const toneClass = computed(() => displayToneClasses(props.tone))
</script>

<template>
  <span
    v-if="label !== null && label !== undefined && label !== ''"
    data-console-display-tag
    class="inline-flex w-fit shrink-0 cursor-default items-center justify-center gap-1.5 rounded-full border border-transparent px-3 py-1 text-[0.69rem] font-semibold uppercase tracking-[0.18em] whitespace-nowrap"
    :class="toneClass"
  >
    <component :is="icon" v-if="icon" class="size-3" />
    <span>{{ label }}</span>
  </span>
</template>
