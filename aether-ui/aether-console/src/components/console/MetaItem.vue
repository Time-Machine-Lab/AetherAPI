<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  icon?: Component
  label?: string
  value?: string | number | null
}>()
</script>

<template>
  <span
    v-if="value !== null && value !== undefined && value !== ''"
    class="inline-flex min-w-0 items-center gap-1.5 rounded-[8px] border border-[rgb(34_34_34_/_0.06)] bg-white px-2.5 py-1 text-xs text-muted-foreground"
  >
    <component :is="icon" v-if="icon" class="size-3.5 shrink-0 text-muted-foreground/75" />
    <span v-if="label" class="shrink-0">{{ label }}</span>
    <span class="truncate font-medium text-foreground/80">{{ value }}</span>
  </span>
</template>
