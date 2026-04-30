import { fileURLToPath, URL } from 'node:url'

import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import AutoImport from 'unplugin-auto-import/vite'
import VueRouter from 'vue-router/vite'
import { VueRouterAutoImports } from 'vue-router/unplugin'
import { defineConfig } from 'vitest/config'

export default defineConfig({
  plugins: [
    VueRouter({
      dts: 'src/typed-router.d.ts',
      routeBlockLang: 'json5',
    }),
    AutoImport({
      imports: ['vue', VueRouterAutoImports, 'pinia', { 'vue-i18n': ['useI18n'] }],
      dirs: ['src/composables', 'src/stores'],
      dts: 'src/auto-imports.d.ts',
      vueTemplate: true,
      eslintrc: {
        enabled: true,
        filepath: './.eslint-auto-import.json',
        globalsPropValue: 'readonly',
      },
    }),
    vue(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  test: {
    include: ['src/**/*.spec.ts'],
  },
  server: {
    port: 80,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://61.184.13.101:8090',
        changeOrigin: true,
        secure: false,
        // rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
    allowedHosts: ['ai.cybernomads.cn'],
  },
})
