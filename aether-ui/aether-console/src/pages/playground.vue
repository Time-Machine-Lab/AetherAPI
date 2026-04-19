<script setup lang="ts">
import UnifiedAccessPlayground from '@/features/unified-access/UnifiedAccessPlayground.vue'
</script>

<route lang="json5">
{
  name: 'console-playground',
  meta: {
    layout: 'ConsoleLayout',
    titleKey: 'console.playground.metaTitle',
    requiresAuth: true,
  },
}
</route>

<template>
  <UnifiedAccessPlayground />
</template>
