export default {
  app: {
    name: 'AetherAPI',
    subtitle: '开发者门户',
  },
  common: {
    locale: {
      zhCn: '中文',
      enUs: 'English',
    },
    actions: {
      continue: '继续',
    },
  },
  portal: {
    nav: {
      home: '门户首页',
      workspace: '工作台',
      signIn: '登录',
      signOut: '退出',
    },
    shell: {
      session: '当前会话',
    },
    home: {
      metaTitle: '开发者门户',
      badge: '开发者门户骨架',
      headline: '面向 API 提供者与集成者的统一入口',
      description:
        '这里预置了门户首页、登录页与受保护工作台，方便后续继续扩展上架管理、收益查看、文档生成与密钥管理能力。',
      primaryAction: '进入登录页',
      secondaryAction: '查看工作台',
      sections: {
        onboarding: {
          title: '上架准备',
          description: '后续可以在这里补上 API 上架流程、定价、文档转换和测试工具。',
        },
        governance: {
          title: '开发约束',
          description: '页面文案走 i18n，请求统一走 API 层，工作台路由通过命名路由与守卫控制访问。',
        },
      },
    },
    signIn: {
      metaTitle: '登录',
      eyebrow: '门户访问控制',
      title: '使用演示身份进入开发者工作台',
      description:
        '当前为初始化阶段，登录动作会创建本地演示会话，便于验证守卫、状态管理与页面流转。',
      nameLabel: '显示名称',
      emailLabel: '邮箱',
      submit: '进入工作台',
    },
    workspace: {
      metaTitle: '工作台',
      title: '开发者工作台已初始化',
      description: '接下来可以在此基础上接入 API 上架、调用统计、定价与收益管理能力。',
      cards: {
        app: {
          title: '应用标识',
          description: '当前应用与 API 基地址来自环境变量，避免在业务代码中写死地址。',
        },
        auth: {
          title: '鉴权状态',
          description: '当前页面通过路由守卫保护，只允许带会话的用户访问。',
        },
        next: {
          title: '下一步建议',
          description: '继续补充 API 列表、文档解析流水线与密钥管理页面。',
        },
      },
      actions: {
        signOut: '退出当前会话',
      },
    },
  },
} as const
