export default {
  app: {
    name: 'AetherAPI',
    subtitle: 'Admin Console',
  },
  common: {
    locale: {
      zhCn: 'Chinese',
      enUs: 'English',
    },
  },
  admin: {
    nav: {
      dashboard: 'Dashboard',
      signIn: 'Sign in',
      signOut: 'Sign out',
    },
    shell: {
      operator: 'Current operator',
    },
    signIn: {
      metaTitle: 'Admin Sign In',
      eyebrow: 'Protected back office',
      title: 'Enter the AetherAPI admin console',
      description:
        'Signing in lets us validate the admin route guards, persisted session state, and shared layout conventions.',
      nameLabel: 'Operator name',
      emailLabel: 'Email',
      submit: 'Enter console',
    },
    dashboard: {
      metaTitle: 'Dashboard',
      title: 'Admin console initialized',
      description:
        'The admin shell is ready for review queues, operations workflows, monitoring, and alerting screens.',
      cards: {
        governance: {
          title: 'Governance and release',
          description:
            'CI, formatting, type checking, and route guards are already wired at the project layer to support ongoing delivery.',
        },
        telemetry: {
          title: 'Monitoring and alerting',
          description:
            'This shell is ready for white-screen tracking, request failure reporting, and critical action monitoring.',
        },
        config: {
          title: 'Environment config',
          description:
            'The app id and API base URL come from environment variables so admin code avoids hardcoded targets.',
        },
      },
      actions: {
        signOut: 'Sign out current session',
      },
    },
  },
} as const
