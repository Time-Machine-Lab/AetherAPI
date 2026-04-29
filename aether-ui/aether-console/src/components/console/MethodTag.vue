<script setup lang="ts">
import { Network } from 'lucide-vue-next'
import DisplayTag from './DisplayTag.vue'
import { methodTone } from '@/utils/visual-system'

defineProps<{
  method?: string | null
}>()
</script>

<template>
  <DisplayTag
    v-if="method"
    data-console-method-tag
    :icon="Network"
    :label="method"
    :tone="methodTone(method)"
  />
</template>
