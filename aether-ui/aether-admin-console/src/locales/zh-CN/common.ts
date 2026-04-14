export default {
  app: {
    name: 'AetherAPI',
    subtitle: '管理后台',
  },
  common: {
    locale: {
      zhCn: '中文',
      enUs: 'English',
    },
  },
  admin: {
    nav: {
      dashboard: '总览',
      signIn: '登录',
      signOut: '退出',
    },
    shell: {
      operator: '当前操作员',
    },
    signIn: {
      metaTitle: '后台登录',
      eyebrow: '受保护后台',
      title: '进入 AetherAPI 管理后台',
      description: '登录后即可验证后台路由守卫、状态持久化和统一布局是否按规范工作。',
      nameLabel: '操作员名称',
      emailLabel: '邮箱',
      submit: '进入后台',
    },
    dashboard: {
      metaTitle: '后台总览',
      title: '管理后台骨架已初始化',
      description:
        '这里已经预置了后台首页、登录页和统一导航，可继续接入审核、运营、监控和告警页面。',
      cards: {
        governance: {
          title: '治理与发布',
          description: 'CI、格式化、类型检查与路由守卫已经在项目层落好，便于后续持续交付。',
        },
        telemetry: {
          title: '监控与告警',
          description: '后续可以在此基础上接入白屏、接口错误、关键操作失败率等监控能力。',
        },
        config: {
          title: '环境配置',
          description: '应用标识和 API 基地址统一来自环境变量，避免后台代码里写死目标地址。',
        },
      },
      actions: {
        signOut: '退出当前会话',
      },
    },
  },
} as const
