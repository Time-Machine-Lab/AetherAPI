export default {
  app: {
    name: 'AetherAPI',
    subtitle: 'Console',
  },
  common: {
    locale: {
      zhCn: '中文',
      enUs: 'English',
    },
    actions: {
      continue: 'Continue',
    },
  },
  console: {
    nav: {
      home: 'Marketplace',
      workspace: 'Workspace',
      signIn: 'Sign in',
      signOut: 'Sign out',
      searchPlaceholder: 'Search models, agents, API keys, docs',
    },
    sidebar: {
      modelSuite: 'AI model operations',
      operations: 'Operations center',
    },
    navigation: {
      overview: 'Overview',
      marketplace: 'Model marketplace',
      agents: 'Agent hub',
      credentials: 'API key',
      usage: 'Usage analytics',
      orders: 'Order center',
      billing: 'Billing',
      docs: 'Docs center',
    },
    topbar: {
      new: 'New',
      messages: 'Messages',
      docs: 'Docs',
      workorder: 'Work order',
      usage: 'Usage',
      cloud: 'Cloud market',
    },
    notices: {
      tokenCampaign:
        'New user campaign: claim the starter token pack and complete the first integration quickly.',
      agentTrial: 'Agent beta is now live. Open the workspace to configure your first automation flow.',
    },
    filters: {
      all: 'All',
      new: 'New',
      free: 'Free trial',
      vision: 'Search & context',
      reasoning: 'Reasoning',
    },
    metrics: {
      models: 'Available models',
      vendors: 'Connected vendors',
      new: 'New this week',
    },
    metricsHints: {
      models: 'The marketplace is structured for large-scale inventory growth.',
      vendors: 'Current upstream channels already mapped in the shell.',
      new: 'New arrivals are surfaced first for operator review.',
    },
    home: {
      metaTitle: 'Model Marketplace',
      title: 'Model marketplace',
      description: 'A full-screen operator console with left navigation, toolbar filters, and model cards.',
      banner:
        'Use the search bar, filters, and sorting controls to curate the model shelf before deeper channel management work lands.',
      searchPlaceholder: 'Search model name or vendor',
      toolbarExpand: 'Advanced filters',
      toolbarSort: 'Sort',
      toolbarExport: 'Export list',
      promoBadge: 'Featured',
      promoTitle: 'Token bundles and launch-ready offers',
      promoDescription:
        'Keep the first screen focused on supply discovery: promotion, new arrivals, and pricing visibility all live in the same workspace.',
      promoAction: 'Launch now',
      inputPrice: 'Input',
      outputPrice: 'Output',
    },
    signIn: {
      metaTitle: 'Console Sign In',
      eyebrow: 'Demo access',
      title: 'Enter the operator console',
      description:
        'A local session is enough for now so we can keep the layout, navigation, and protected routes moving while the real identity layer is still pending.',
      note: 'This sign-in only creates a local demo session in the current app sandbox.',
      nameLabel: 'Operator name',
      emailLabel: 'Work email',
      submit: 'Enter console',
      helperTitle: 'What is already prepared',
      helperDescription:
        'The full-screen shell, sidebar navigation, and working surfaces are already in place for follow-up delivery.',
    },
    workspace: {
      metaTitle: 'Console Workspace',
      title: 'Operations overview',
      description: 'Track agents, credentials, usage, orders, billing, and documentation in one shell.',
      primaryAction: 'Create workflow',
      secondaryAction: 'Review pricing',
      statLabel: 'Sidebar entries',
      statHint: 'The left menu is already structured for scale.',
      readyLabel: 'Active work panels',
      readyHint: 'Each panel already has a stable place in the console.',
      timelineLabel: 'Planned follow-ups',
      timelineHint: 'Upcoming slices can attach without reworking layout.',
      envLabel: 'Runtime environment',
      panelsTitle: 'Operations panels',
      panelsDescription: 'Each panel is a delivery surface anchored to the left navigation.',
      timelineTitle: 'Execution timeline',
      timelineDescription: 'Use this rail to decide what ships next.',
      environmentTitle: 'Environment snapshot',
    },
    panels: {
      agents: {
        title: 'Agent workflows',
        description: 'Prepare agent templates, linked tools, and release status for assisted automation.',
      },
      credentials: {
        title: 'Credential management',
        description: 'Track active keys, ownership, and environment segmentation in one place.',
      },
      usage: {
        title: 'Usage analytics',
        description: 'Surface call volume, consumption trends, and model-level traffic signals.',
      },
      orders: {
        title: 'Order processing',
        description: 'Keep settlement, contract review, and exception handling inside the same workspace.',
      },
      billing: {
        title: 'Billing strategy',
        description: 'Prepare pricing packages, margins, and customer-facing billing controls.',
      },
      docs: {
        title: 'Documentation delivery',
        description: 'Stage doc publishing, release notes, and integration guides from one queue.',
      },
    },
    panelStatus: {
      beta: 'Beta',
      ready: 'Ready',
      stable: 'Stable',
      attention: 'Attention',
      planned: 'Planned',
      inProgress: 'In progress',
    },
    timeline: {
      launch: {
        title: 'Marketplace stays on the first screen',
        description: 'Operators should be able to browse supply immediately after entering the console.',
      },
      pricing: {
        title: 'Billing lands next',
        description: 'The same shell will host pricing drafts, settlements, and bundle strategy.',
      },
      docs: {
        title: 'Docs center plugs into the sidebar',
        description: 'Documentation publishing should attach to this shell instead of forking a new UI.',
      },
    },
  },
} as const
