// @vitest-environment happy-dom
import { computed, defineComponent, nextTick, ref, type Ref } from 'vue'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/useAuthStore'
import { createConsoleSession } from '@/test/console-test-kit'
import type {
  ImportAgentClarificationItem,
  ImportAgentPlan,
  ImportAgentSession,
} from '@/api/import-agent/import-agent.types'
import ImportAgentWorkspace from './ImportAgentWorkspace.vue'

const mocks = vi.hoisted(() => ({
  useImportAgentWorkspace: vi.fn(),
}))

vi.mock('@/composables/useImportAgentWorkspace', () => ({
  useImportAgentWorkspace: mocks.useImportAgentWorkspace,
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, values?: Record<string, unknown>) => {
      const messages: Record<string, string> = {
        'console.importAgent.clarificationTitle': '待回答问题',
        'console.importAgent.clarificationPlanGroup': '整体计划',
        'console.importAgent.clarificationSelectPlaceholder': '请选择',
        'console.importAgent.clarificationTextPlaceholder': '填写答案',
        'console.importAgent.clarificationDefaultTitle': 'Agent 推荐值',
        'console.importAgent.useClarificationDefault': '采用推荐值',
        'console.importAgent.clarificationDefaultSource.DOCUMENT': '来自文档',
        'console.importAgent.clarificationDefaultSource.INFERRED_FROM_URL': '根据 URL 推断',
        'console.importAgent.clarificationDefaultSource.CURRENT_PLAN': '来自当前计划',
        'console.importAgent.clarificationDefaultSource.AGENT_HEURISTIC': 'Agent 推断',
        'console.importAgent.clarificationDefaultConfidence.HIGH': '置信度高',
        'console.importAgent.clarificationDefaultConfidence.MEDIUM': '置信度中',
        'console.importAgent.clarificationDefaultConfidence.LOW': '置信度低',
        'console.importAgent.planTitle': '导入计划',
        'console.importAgent.planExecutable': '可执行',
        'console.importAgent.planNeedsClarification': '需要补充信息',
        'console.importAgent.planConfirmed': '已确认',
        'console.importAgent.planUnconfirmed': '未确认',
        'console.importAgent.planSummaryTitle': '计划摘要',
        'console.importAgent.planVersionLabel': `版本 ${values?.version ?? ''}`,
        'console.importAgent.refreshSession': '刷新会话',
        'console.importAgent.startFresh': '重新开始',
        'console.importAgent.confirmPlan': '确认计划',
        'console.importAgent.startRun': '开始导入',
        'console.importAgent.turnMessagePlaceholder': '补充说明',
        'console.importAgent.actor.USER': '用户',
        'console.importAgent.actor.AGENT': 'Agent',
        'console.shared.yes': '是',
        'console.shared.no': '否',
      }
      return messages[key] ?? key
    },
  }),
}))

interface MockWorkspace {
  clarificationDrafts: Ref<Record<string, string>>
  messageDraft: Ref<string>
  currentPlan: Ref<ImportAgentPlan | null>
  currentClarificationItems: Ref<ImportAgentClarificationItem[]>
  pendingTurn: Ref<{ message: string } | null>
  adoptClarificationDefault: ReturnType<typeof vi.fn>
  sendMessage: ReturnType<typeof vi.fn>
}

function createSession(plan: ImportAgentPlan): ImportAgentSession {
  return {
    sessionId: 'session-001',
    status: plan.executable ? 'WAITING_FOR_CONFIRMATION' : 'WAITING_FOR_CLARIFICATION',
    documentSource: 'https://docs.example.com/video',
    documentSummary: 'HappyHorse video API',
    importIntent: '导入 HappyHorse 视频 API',
    publisherDisplayName: 'Console Operator',
    currentPlanVersion: plan.version,
    confirmedPlanVersion: undefined,
    latestRunId: undefined,
    currentPlan: plan,
    turns: [],
    createdAt: '2026-05-20T10:00:00Z',
    updatedAt: '2026-05-20T10:05:00Z',
  }
}

function createPlan(overrides: Partial<ImportAgentPlan> = {}): ImportAgentPlan {
  return {
    version: 2,
    executable: false,
    summary: '需要补充导入信息',
    clarificationQuestions: [],
    clarificationItems: [],
    categoryPlans: [{ categoryCode: 'video', categoryName: '视频分类', action: 'CREATE_IF_MISSING' }],
    assetPlans: [
      {
        apiCode: 'happyhorse-t2v',
        assetName: 'HappyHorse 文生视频',
        assetType: 'AI_API',
        categoryCode: 'video',
        requestMethod: 'POST',
        upstreamUrl: 'https://dashscope.aliyuncs.com/api/v1/video',
        authScheme: undefined,
        authConfig: undefined,
        publishAfterImport: true,
        asyncTaskConfig: null,
        aiProfile: null,
      },
    ],
    ...overrides,
  }
}

function createWorkspace(plan = createPlan(), options: Partial<MockWorkspace> = {}) {
  const currentPlan = ref<ImportAgentPlan | null>(plan)
  const clarificationDrafts = ref<Record<string, string>>({})
  const workspace: MockWorkspace & Record<string, unknown> = {
    documentSource: ref(''),
    documentSummary: ref(''),
    publisherDisplayName: ref('Console Operator'),
    clarificationDrafts,
    messageDraft: ref(''),
    draftAttachments: ref([]),
    activeSession: ref(createSession(plan)),
    activeRun: ref(null),
    pendingTurn: ref(null),
    streamingReply: ref(''),
    streamingPhase: ref(null),
    streamingStatusMessage: ref(''),
    currentPlan,
    currentClarificationItems: computed(() => currentPlan.value?.clarificationItems ?? []),
    restoring: ref(false),
    creating: ref(false),
    refreshingSession: ref(false),
    appending: ref(false),
    confirming: ref(false),
    startingRun: ref(false),
    refreshingRun: ref(false),
    attachingFiles: ref(false),
    sessionError: ref(null),
    turnError: ref(null),
    runError: ref(null),
    attachmentError: ref(null),
    hasActiveSession: ref(true),
    canSendMessage: ref(true),
    canConfirmPlan: ref(false),
    canStartRun: ref(false),
    refreshSession: vi.fn(),
    restoreActiveSession: vi.fn(),
    adoptClarificationDefault: vi.fn((item: ImportAgentClarificationItem) => {
      const value = item.defaultValue?.trim()
      if (!value) {
        return
      }
      clarificationDrafts.value = {
        ...clarificationDrafts.value,
        [item.id]: value,
      }
    }),
    sendMessage: vi.fn(),
    confirmPlan: vi.fn(),
    startRun: vi.fn(),
    refreshRun: vi.fn(),
    addDraftFiles: vi.fn(),
    removeDraftAttachment: vi.fn(),
    resetDraft: vi.fn(),
    ...options,
  }
  mocks.useImportAgentWorkspace.mockReturnValue(workspace)
  return workspace
}

function mountWorkspace(workspace: MockWorkspace & Record<string, unknown>) {
  mocks.useImportAgentWorkspace.mockReturnValue(workspace)
  return mount(ImportAgentWorkspace, {
    global: {
      plugins: [createPinia()],
      stubs: {
        Button: defineComponent({
          props: {
            disabled: { type: Boolean, default: false },
          },
          template: '<button type="button" :disabled="disabled"><slot /></button>',
        }),
        Input: defineComponent({
          props: {
            modelValue: { type: [String, Number], default: '' },
            placeholder: { type: String, default: '' },
          },
          emits: ['update:modelValue'],
          template:
            '<input data-test-id="stub-input" :value="modelValue" :placeholder="placeholder" @input="$emit(\'update:modelValue\', $event.target.value)" />',
        }),
        FieldLabel: defineComponent({
          props: {
            label: { type: String, required: true },
            optional: { type: Boolean, default: false },
          },
          template: '<label>{{ label }}<span v-if="optional"> optional</span></label>',
        }),
        DisplayTag: defineComponent({
          props: {
            label: { type: String, required: true },
          },
          template: '<span>{{ label }}</span>',
        }),
        CodeBlock: true,
        JsonSchemaViewer: true,
        StateBlock: true,
      },
    },
  })
}

describe('ImportAgentWorkspace', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    useAuthStore().setSession(createConsoleSession())
    mocks.useImportAgentWorkspace.mockReset()
  })

  it('renders grouped guided clarification controls for every input type and submits the turn', async () => {
    const plan = createPlan({
      clarificationItems: [
        {
          id: 'plan-2:/assetPlans/0/authScheme:authScheme',
          targetPath: '/assetPlans/0/authScheme',
          fieldKey: 'authScheme',
          label: '鉴权方式',
          description: '请选择上游鉴权方式。',
          inputType: 'SELECT',
          required: true,
          options: [{ value: 'HEADER_TOKEN', label: 'Header Token' }],
          defaultValue: 'HEADER_TOKEN',
          defaultLabel: 'Header Token',
          defaultSource: 'DOCUMENT',
          defaultConfidence: 'HIGH',
        },
        {
          id: 'plan-2:/assetPlans/0/authConfig:authConfig',
          targetPath: '/assetPlans/0/authConfig',
          fieldKey: 'authConfig',
          label: '上游鉴权信息',
          description: '请提供 Header 名称或凭证来源，Agent 会据此生成配置。',
          inputType: 'MULTILINE',
          required: true,
          options: [],
        },
        {
          id: 'plan-2:/categoryPlans/0/categoryCode:categoryCode',
          targetPath: '/categoryPlans/0/categoryCode',
          fieldKey: 'categoryCode',
          label: '分类编码',
          inputType: 'TEXT',
          required: true,
          options: [],
          currentValue: 'video',
        },
        {
          id: 'plan-2:/publishAfterImport:publishAfterImport',
          targetPath: '/publishAfterImport',
          fieldKey: 'publishAfterImport',
          label: '导入后发布',
          inputType: 'BOOLEAN',
          required: true,
          options: [],
        },
      ],
    })
    const workspace = createWorkspace(plan)
    const wrapper = mountWorkspace(workspace)

    expect(wrapper.text()).toContain('HappyHorse 文生视频')
    expect(wrapper.text()).toContain('视频分类')
    expect(wrapper.text()).toContain('整体计划')
    expect(wrapper.text()).toContain('上游鉴权信息')
    expect(wrapper.text()).toContain('Agent 会据此生成配置')
    expect(wrapper.text()).toContain('Agent 推荐值')
    expect(wrapper.text()).toContain('Header Token')
    expect(wrapper.text()).toContain('来自文档')
    expect(wrapper.text()).toContain('置信度高')

    const recommendationButton = wrapper
      .findAll('button')
      .find((button) => button.text() === '采用推荐值')
    expect(recommendationButton).toBeDefined()
    await recommendationButton?.trigger('click')
    expect(workspace.adoptClarificationDefault).toHaveBeenCalledWith(plan.clarificationItems[0])
    expect(workspace.clarificationDrafts.value['plan-2:/assetPlans/0/authScheme:authScheme']).toBe(
      'HEADER_TOKEN',
    )

    await wrapper.findAll('select')[0].setValue('HEADER_TOKEN')
    await wrapper.find('textarea').setValue('Authorization Header，凭证来自用户自己的阿里云百炼 API Key')
    await wrapper.find('[data-test-id="stub-input"]').setValue('video-ai')
    await wrapper.findAll('select')[1].setValue('true')

    expect(workspace.clarificationDrafts.value).toEqual({
      'plan-2:/assetPlans/0/authScheme:authScheme': 'HEADER_TOKEN',
      'plan-2:/assetPlans/0/authConfig:authConfig':
        'Authorization Header，凭证来自用户自己的阿里云百炼 API Key',
      'plan-2:/categoryPlans/0/categoryCode:categoryCode': 'video-ai',
      'plan-2:/publishAfterImport:publishAfterImport': 'true',
    })

    const buttons = wrapper.findAll('button')
    await buttons[buttons.length - 1].trigger('click')

    expect(workspace.sendMessage).toHaveBeenCalledTimes(1)
  })

  it('falls back to legacy clarification question cards', () => {
    const workspace = createWorkspace(
      createPlan({
        clarificationItems: [],
        clarificationQuestions: ['请补充上游地址。'],
      }),
    )
    const wrapper = mountWorkspace(workspace)

    expect(wrapper.text()).toContain('待回答问题')
    expect(wrapper.text()).toContain('请补充上游地址。')
  })

  it('shows answer-only pending summaries while waiting for the final session snapshot', async () => {
    const workspace = createWorkspace(createPlan(), {
      pendingTurn: ref({ message: '已提交 2 项澄清信息' }),
    })
    const wrapper = mountWorkspace(workspace)

    expect(wrapper.text()).toContain('已提交 2 项澄清信息')

    workspace.pendingTurn.value = null
    await nextTick()

    expect(wrapper.text()).not.toContain('已提交 2 项澄清信息')
  })
})
