<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <section
    data-console-field-group
    class="space-y-4 rounded-[14px] border border-[rgb(34_34_34_/_0.06)] bg-white p-4"
  >
    <div class="border-l-2 border-primary pl-3">
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0">
          <h3 class="text-sm font-semibold text-foreground">{{ title }}</h3>
          <p v-if="description" class="mt-1 text-xs leading-5 text-muted-foreground">
            {{ description }}
          </p>
        </div>
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </section>
</template>
