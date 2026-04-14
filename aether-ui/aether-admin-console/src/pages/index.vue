<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { appConfig } from '@/app/app-config'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const router = useRouter()

onMounted(async () => {
  await router.replace({
    name: authStore.isAuthenticated ? appConfig.protectedHomeRouteName : appConfig.signInRouteName,
  })
})
</script>

<route lang="json5">
{
  name: 'admin-home',
  meta: {
    layout: 'AdminLayout',
    requiresAuth: false,
  },
}
</route>

<template>
  <div class="sr-only" />
</template>
