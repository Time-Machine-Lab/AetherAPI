import js from '@eslint/js'
import globals from 'globals'
import pluginVue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'
import vueParser from 'vue-eslint-parser'

const autoImportGlobals = {
  computed: 'readonly',
  createApp: 'readonly',
  createPinia: 'readonly',
  defineStore: 'readonly',
  nextTick: 'readonly',
  onMounted: 'readonly',
  onUnmounted: 'readonly',
  reactive: 'readonly',
  ref: 'readonly',
  storeToRefs: 'readonly',
  toRef: 'readonly',
  toRefs: 'readonly',
  useI18n: 'readonly',
  useLink: 'readonly',
  useRoute: 'readonly',
  useRouter: 'readonly',
  watch: 'readonly',
  watchEffect: 'readonly',
}

export default [
  {
    ignores: [
      'dist',
      'coverage',
      'node_modules',
      '.eslint-auto-import.json',
      'src/auto-imports.d.ts',
      'src/typed-router.d.ts',
    ],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    files: ['**/*.{ts,tsx,vue}'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tseslint.parser,
        ecmaVersion: 'latest',
        sourceType: 'module',
        extraFileExtensions: ['.vue'],
      },
      globals: {
        ...globals.browser,
        ...globals.es2024,
        ...autoImportGlobals,
      },
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      'vue/block-lang': ['error', { script: { lang: 'ts' } }],
      'vue/html-closing-bracket-newline': 'off',
      'vue/html-indent': 'off',
      'vue/html-self-closing': 'off',
      'vue/max-attributes-per-line': 'off',
      'vue/multiline-html-element-content-newline': 'off',
      'vue/multi-word-component-names': 'off',
      'vue/require-default-prop': 'off',
      'vue/singleline-html-element-content-newline': 'off',
    },
  },
]
