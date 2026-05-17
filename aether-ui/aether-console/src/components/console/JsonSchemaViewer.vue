<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CodeBlock from '@/components/console/CodeBlock.vue'
import StateBlock from '@/components/console/StateBlock.vue'

const props = withDefaults(
  defineProps<{
    value?: string | null
    label: string
    emptyTitle?: string
    emptyDescription?: string
    maxHeightClass?: string
  }>(),
  {
    value: null,
    maxHeightClass: 'max-h-[300px]',
  },
)

const { t } = useI18n()
const schema = computed(() => props.value?.trim() ?? '')
</script>

<template>
  <div data-console-json-schema-viewer>
    <CodeBlock
      v-if="schema"
      :label="label"
      :value="schema"
      :max-height-class="maxHeightClass"
      collapsible
    />
    <StateBlock
      v-else
      tone="empty"
      :title="emptyTitle ?? t('console.shared.schemaEmpty')"
      :description="emptyDescription"
    />
  </div>
</template>
