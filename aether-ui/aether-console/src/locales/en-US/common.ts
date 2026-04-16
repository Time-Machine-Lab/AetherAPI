export default {
  app: {
    name: 'AetherAPI',
    subtitle: 'Developer Portal',
  },
  common: {
    locale: {
      zhCn: 'Chinese',
      enUs: 'English',
    },
    actions: {
      continue: 'Continue',
    },
  },
  portal: {
    nav: {
      home: 'Home',
      workspace: 'Workspace',
      signIn: 'Sign in',
      signOut: 'Sign out',
    },
    shell: {
      session: 'Active session',
    },
    home: {
      metaTitle: 'Developer Portal',
      badge: 'Developer portal shell',
      headline: 'A unified entry point for API publishers and integrators',
      description:
        'The portal now includes a public landing page, a sign-in screen, and a protected workspace so future features can be added on a stable base.',
      primaryAction: 'Open sign-in',
      secondaryAction: 'View workspace',
      sections: {
        onboarding: {
          title: 'Publishing prep',
          description:
            'This area is ready for API publishing flows, pricing, documentation transformation, and testing tools.',
        },
        governance: {
          title: 'Delivery guardrails',
          description:
            'User-facing copy goes through i18n, requests stay in the API layer, and named routes drive protected navigation.',
        },
      },
    },
    signIn: {
      metaTitle: 'Sign In',
      eyebrow: 'Portal access control',
      title: 'Enter the developer workspace with a demo identity',
      description:
        'During bootstrap, signing in creates a local demo session so we can validate guards, state management, and route transitions.',
      nameLabel: 'Display name',
      emailLabel: 'Email',
      submit: 'Enter workspace',
    },
    workspace: {
      metaTitle: 'Workspace',
      title: 'Developer workspace initialized',
      description:
        'The next step is to add API publishing, usage analytics, pricing, and revenue management on top of this shell.',
      cards: {
        app: {
          title: 'Application identity',
          description:
            'The app id and API base URL come from environment variables so business code avoids hardcoded addresses.',
        },
        auth: {
          title: 'Auth status',
          description:
            'This page is protected by route guards and only available to users with a session.',
        },
        next: {
          title: 'Recommended next step',
          description:
            'Add API inventory, documentation processing, and token management flows next.',
        },
      },
      actions: {
        signOut: 'Sign out current session',
      },
    },
  },
} as const
