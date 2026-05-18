# Design System Inspired by Airbnb

## 1. Visual Theme & Atmosphere

Airbnb's website is a warm, photography-forward marketplace that feels like flipping through a travel magazine where every page invites you to book. The design operates on a foundation of pure white (`#ffffff`) with the iconic Rausch Red (`#ff385c`) — named after Airbnb's first street address — serving as the singular brand accent. The result is a clean, airy canvas where listing photography, category icons, and the red CTA button are the only sources of color.

The typography uses Airbnb Cereal VF — a custom variable font that's warm and approachable, with rounded terminals that echo the brand's "belong anywhere" philosophy. The font operates in a tight weight range: 500 (medium) for most UI, 600 (semibold) for emphasis, and 700 (bold) for primary headings. Slight negative letter-spacing (-0.18px to -0.44px) on headings creates a cozy, intimate reading experience rather than the compressed efficiency of tech companies.

What distinguishes Airbnb is its palette-based token system (`--palette-*`) and multi-layered shadow approach. The primary card shadow uses a three-layer stack (`rgba(0,0,0,0.02) 0px 0px 0px 1px, rgba(0,0,0,0.04) 0px 2px 6px, rgba(0,0,0,0.1) 0px 4px 8px`) that creates a subtle, warm lift. Combined with generous border-radius (8px–32px), circular navigation controls (50%), and a category pill bar with horizontal scrolling, the interface feels tactile and inviting — designed for browsing, not commanding.

**Key Characteristics:**

- Pure white canvas with Rausch Red (`#ff385c`) as singular brand accent
- Airbnb Cereal VF — custom variable font with warm, rounded terminals
- Palette-based token system (`--palette-*`) for systematic color management
- Three-layer card shadows: border ring + soft blur + stronger blur
- Generous border-radius: 8px buttons, 14px badges, 20px cards, 32px large elements
- Circular navigation controls (50% radius)
- Photography-first listing cards — images are the hero content
- Near-black text (`#222222`) — warm, not cold
- Luxe Purple (`#460479`) and Plus Magenta (`#92174d`) for premium tiers

## 2. Color Palette & Roles

### Primary Brand

- **Rausch Red** (`#ff385c`): `--palette-bg-primary-core`, primary CTA, brand accent, active states
- **Deep Rausch** (`#e00b41`): `--palette-bg-tertiary-core`, pressed/dark variant of brand red
- **Error Red** (`#c13515`): `--palette-text-primary-error`, error text on light
- **Error Dark** (`#b32505`): `--palette-text-secondary-error-hover`, error hover

### Premium Tiers

- **Luxe Purple** (`#460479`): `--palette-bg-primary-luxe`, Airbnb Luxe tier branding
- **Plus Magenta** (`#92174d`): `--palette-bg-primary-plus`, Airbnb Plus tier branding

### Text Scale

- **Near Black** (`#222222`): `--palette-text-primary`, primary text — warm, not cold
- **Focused Gray** (`#3f3f3f`): `--palette-text-focused`, focused state text
- **Secondary Gray** (`#6a6a6a`): Secondary text, descriptions
- **Disabled** (`rgba(0,0,0,0.24)`): `--palette-text-material-disabled`, disabled state
- **Link Disabled** (`#929292`): `--palette-text-link-disabled`, disabled links

### Interactive

- **Legal Blue** (`#428bff`): `--palette-text-legal`, legal links, informational
- **Border Gray** (`#c1c1c1`): Border color for cards and dividers
- **Light Surface** (`#f2f2f2`): Circular navigation buttons, secondary surfaces

### Surface & Shadows

- **Pure White** (`#ffffff`): Page background, card surfaces
- **Card Shadow** (`rgba(0,0,0,0.02) 0px 0px 0px 1px, rgba(0,0,0,0.04) 0px 2px 6px, rgba(0,0,0,0.1) 0px 4px 8px`): Three-layer warm lift
- **Hover Shadow** (`rgba(0,0,0,0.08) 0px 4px 12px`): Button hover elevation

## 3. Typography Rules

### Font Family

- **Primary**: `Airbnb Cereal VF`, fallbacks: `Circular, -apple-system, system-ui, Roboto, Helvetica Neue`
- **OpenType Features**: `"salt"` (stylistic alternates) on specific caption elements

### Hierarchy

| Role                | Font             | Size           | Weight  | Line Height  | Letter Spacing | Notes                       |
| ------------------- | ---------------- | -------------- | ------- | ------------ | -------------- | --------------------------- |
| Section Heading     | Airbnb Cereal VF | 28px (1.75rem) | 700     | 1.43         | normal         | Primary headings            |
| Card Heading        | Airbnb Cereal VF | 22px (1.38rem) | 600     | 1.18 (tight) | -0.44px        | Category/card titles        |
| Card Heading Medium | Airbnb Cereal VF | 22px (1.38rem) | 500     | 1.18 (tight) | -0.44px        | Lighter variant             |
| Sub-heading         | Airbnb Cereal VF | 21px (1.31rem) | 700     | 1.43         | normal         | Bold sub-headings           |
| Feature Title       | Airbnb Cereal VF | 20px (1.25rem) | 600     | 1.20 (tight) | -0.18px        | Feature headings            |
| UI Medium           | Airbnb Cereal VF | 16px (1.00rem) | 500     | 1.25 (tight) | normal         | Nav, emphasized text        |
| UI Semibold         | Airbnb Cereal VF | 16px (1.00rem) | 600     | 1.25 (tight) | normal         | Strong emphasis             |
| Button              | Airbnb Cereal VF | 16px (1.00rem) | 500     | 1.25 (tight) | normal         | Button labels               |
| Body / Link         | Airbnb Cereal VF | 14px (0.88rem) | 400     | 1.43         | normal         | Standard body               |
| Body Medium         | Airbnb Cereal VF | 14px (0.88rem) | 500     | 1.29 (tight) | normal         | Medium body                 |
| Caption Salt        | Airbnb Cereal VF | 14px (0.88rem) | 600     | 1.43         | normal         | `"salt"` feature            |
| Small               | Airbnb Cereal VF | 13px (0.81rem) | 400     | 1.23 (tight) | normal         | Descriptions                |
| Tag                 | Airbnb Cereal VF | 12px (0.75rem) | 400–700 | 1.33         | normal         | Tags, prices                |
| Badge               | Airbnb Cereal VF | 11px (0.69rem) | 600     | 1.18 (tight) | normal         | `"salt"` feature            |
| Micro Uppercase     | Airbnb Cereal VF | 8px (0.50rem)  | 700     | 1.25 (tight) | 0.32px         | `text-transform: uppercase` |

### Principles

- **Warm weight range**: 500–700 dominate. No weight 300 or 400 for headings — Airbnb's type is always at least medium weight, creating a warm, confident voice.
- **Negative tracking on headings**: -0.18px to -0.44px letter-spacing on display creates intimate, cozy headings rather than cold, compressed ones.
- **"salt" OpenType feature**: Stylistic alternates on specific UI elements (badges, captions) create subtle glyph variations that add visual interest.
- **Variable font precision**: Cereal VF enables continuous weight interpolation, though the design system uses discrete stops at 500, 600, and 700.

## 4. Component Stylings

### Buttons

**Primary Dark**

- Background: `#222222` (near-black, not pure black)
- Text: `#ffffff`
- Padding: 0px 24px
- Radius: 8px
- Hover: transitions to error/brand accent via `var(--accent-bg-error)`
- Focus: `0 0 0 2px var(--palette-grey1000)` ring + scale(0.92)

**Circular Nav**

- Background: `#f2f2f2`
- Text: `#222222`
- Radius: 50% (circle)
- Hover: shadow `rgba(0,0,0,0.08) 0px 4px 12px` + translateX(50%)
- Active: 4px white border ring + focus shadow
- Focus: scale(0.92) shrink animation

### Cards & Containers

- Background: `#ffffff`
- Radius: 14px (badges), 20px (cards/buttons), 32px (large)
- Shadow: `rgba(0,0,0,0.02) 0px 0px 0px 1px, rgba(0,0,0,0.04) 0px 2px 6px, rgba(0,0,0,0.1) 0px 4px 8px` (three-layer)
- Listing cards: full-width photography on top, details below
- Carousel controls: circular 50% buttons

### Inputs

- Search: `#222222` text
- Focus: `var(--palette-bg-primary-error)` background tint + `0 0 0 2px` ring
- Radius: depends on context (search bar uses pill-like rounding)

### Navigation

- White sticky header with search bar centered
- Airbnb logo (Rausch Red) left-aligned
- Category filter pills: horizontal scroll below search
- Circular nav controls for carousel navigation
- "Become a Host" text link, avatar/menu right-aligned

### Image Treatment

- Listing photography fills card top with generous height
- Image carousel with dot indicators
- Heart/wishlist icon overlay on images
- 8px–14px radius on contained images

## 5. Layout Principles

### Spacing System

- Base unit: 8px
- Scale: 2px, 3px, 4px, 6px, 8px, 10px, 11px, 12px, 15px, 16px, 22px, 24px, 32px

### Grid & Container

- Full-width header with centered search
- Category pill bar: horizontal scrollable row
- Listing grid: responsive multi-column (3–5 columns on desktop)
- Full-width footer with link columns

### Whitespace Philosophy

- **Travel-magazine spacing**: Generous vertical padding between sections creates a leisurely browsing pace — you're meant to scroll slowly, like browsing a magazine.
- **Photography density**: Listing cards are packed relatively tightly, but each image is large enough to feel immersive.
- **Search bar prominence**: The search bar gets maximum vertical space in the header — finding your destination is the primary action.

### Border Radius Scale

- Subtle (4px): Small links
- Standard (8px): Buttons, tabs, search elements
- Badge (14px): Status badges, labels
- Card (20px): Feature cards, large buttons
- Large (32px): Large containers, hero elements
- Circle (50%): Nav controls, avatars, icons

## 6. Depth & Elevation

| Level                  | Treatment                                                                                     | Use                            |
| ---------------------- | --------------------------------------------------------------------------------------------- | ------------------------------ |
| Flat (Level 0)         | No shadow                                                                                     | Page background, text blocks   |
| Card (Level 1)         | `rgba(0,0,0,0.02) 0px 0px 0px 1px, rgba(0,0,0,0.04) 0px 2px 6px, rgba(0,0,0,0.1) 0px 4px 8px` | Listing cards, search bar      |
| Hover (Level 2)        | `rgba(0,0,0,0.08) 0px 4px 12px`                                                               | Button hover, interactive lift |
| Active Focus (Level 3) | `rgb(255,255,255) 0px 0px 0px 4px` + focus ring                                               | Active/focused elements        |

**Shadow Philosophy**: Airbnb's three-layer shadow system creates a warm, natural lift. Layer 1 (`0px 0px 0px 1px` at 0.02 opacity) is an ultra-subtle border. Layer 2 (`0px 2px 6px` at 0.04) provides soft ambient shadow. Layer 3 (`0px 4px 8px` at 0.1) adds the primary lift. This graduated approach creates shadows that feel like natural light rather than CSS effects.

## 7. Do's and Don'ts

### Do

- Use `#222222` (warm near-black) for text — never pure `#000000`
- Apply Rausch Red (`#ff385c`) only for primary CTAs and brand moments — it's the singular accent
- Use Airbnb Cereal VF at weight 500–700 — the warm weight range is intentional
- Apply the three-layer card shadow for all elevated surfaces
- Use generous border-radius: 8px for buttons, 20px for cards, 50% for controls
- Use photography as the primary visual content — listings are image-first
- Apply negative letter-spacing (-0.18px to -0.44px) on headings for intimacy
- Use circular (50%) buttons for carousel/navigation controls

### Don't

- Don't use pure black (`#000000`) for text — always `#222222` (warm)
- Don't apply Rausch Red to backgrounds or large surfaces — it's an accent only
- Don't use thin font weights (300, 400) for headings — 500 minimum
- Don't use heavy shadows (>0.1 opacity as primary layer) — keep them warm and graduated
- Don't use sharp corners (0–4px) on cards — the generous rounding (20px+) is core
- Don't introduce additional brand colors beyond the Rausch/Luxe/Plus system
- Don't override the palette token system — use `--palette-*` variables consistently

## 8. Responsive Behavior

### Breakpoints

| Name          | Width       | Key Changes                   |
| ------------- | ----------- | ----------------------------- |
| Mobile Small  | <375px      | Single column, compact search |
| Mobile        | 375–550px   | Standard mobile listing grid  |
| Tablet Small  | 550–744px   | 2-column listings             |
| Tablet        | 744–950px   | Search bar expansion          |
| Desktop Small | 950–1128px  | 3-column listings             |
| Desktop       | 1128–1440px | 4-column grid, full header    |
| Large Desktop | 1440–1920px | 5-column grid                 |
| Ultra-wide    | >1920px     | Maximum grid width            |

_Note: Airbnb has 61 detected breakpoints — one of the most granular responsive systems observed, reflecting their obsession with layout at every possible screen size._

### Touch Targets

- Circular nav buttons: adequate 50% radius sizing
- Listing cards: full-card tap target on mobile
- Search bar: prominently sized for thumb interaction
- Category pills: horizontally scrollable with generous padding

### Collapsing Strategy

- Listing grid: 5 → 4 → 3 → 2 → 1 columns
- Search: expanded bar → compact bar → overlay
- Category pills: horizontal scroll at all sizes
- Navigation: full header → mobile simplified
- Map: side panel → overlay/toggle

### Image Behavior

- Listing photos: carousel with swipe on mobile
- Responsive image sizing with aspect ratio maintained
- Heart overlay positioned consistently across sizes
- Photo quality adjusts based on viewport

## 9. Agent Prompt Guide

### Quick Color Reference

- Background: Pure White (`#ffffff`)
- Text: Near Black (`#222222`)
- Brand accent: Rausch Red (`#ff385c`)
- Secondary text: `#6a6a6a`
- Disabled: `rgba(0,0,0,0.24)`
- Card border: `rgba(0,0,0,0.02) 0px 0px 0px 1px`
- Card shadow: full three-layer stack
- Button surface: `#f2f2f2`

### Example Component Prompts

- "Create a listing card: white background, 20px radius. Three-layer shadow: rgba(0,0,0,0.02) 0px 0px 0px 1px, rgba(0,0,0,0.04) 0px 2px 6px, rgba(0,0,0,0.1) 0px 4px 8px. Photo area on top (16:10 ratio), details below: 16px Airbnb Cereal VF weight 600 title, 14px weight 400 description in #6a6a6a."
- "Design search bar: white background, full card shadow, 32px radius on container. Search text at 14px Cereal VF weight 400. Red search button (#ff385c, 50% radius, white icon)."
- "Build category pill bar: horizontal scrollable row. Each pill: 14px Cereal VF weight 600, #222222 text, bottom border on active. Circular prev/next arrows (#f2f2f2 bg, 50% radius)."
- "Create a CTA button: #222222 background, white text, 8px radius, 16px Cereal VF weight 500, 0px 24px padding. Hover: brand red accent."
- "Design a heart/wishlist button: transparent background, 50% radius, white heart icon with dark shadow outline."

### Iteration Guide

1. Start with white — the photography provides all the color
2. Rausch Red (#ff385c) is the singular accent — use sparingly for CTAs only
3. Near-black (#222222) for text — the warmth matters
4. Three-layer shadows create natural, warm lift — always use all three layers
5. Generous radius: 8px buttons, 20px cards, 50% controls
6. Cereal VF at 500–700 weight — no thin weights for any heading
7. Photography is hero — every listing card is image-first

## 10. Console Semantic Roles & Page Hierarchy

This section defines component semantics, page-level hierarchy, and interaction feedback rules specific to the **control-panel (console)** context. All rules below extend (never contradict) Sections 1–9. When implementing console pages, this section takes precedence over generic component examples.

### 10.1 Semantic Role Taxonomy

Every visible element in the console belongs to exactly one of five semantic roles. The roles determine fill, border, elevation, and interaction affordance:

| Role        | Purpose                                                       | Fill                                                                                                                                               | Border                                                                              | Elevation                                  | Interaction Cue                                                                                                                            |
| ----------- | ------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------- | ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------ |
| **Action**  | Clickable operations (create, save, toggle, rename, sign-out) | Solid fill or transparent                                                                                                                          | 1 px `rgb(34 34 34 / 0.08)` for outline; none for primary                           | `shadow-console` on outline; none on ghost | Hover: lift / color shift; Focus: `ring-2 ring-ring/20`; Active: `scale-[0.98]`; Disabled: `opacity-50 pointer-events-none`                |
| **Status**  | Read-only state labels (Enabled / Disabled, AI / API type)    | Tinted background — `color-mix(in srgb, <role-color> 10%, white)`                                                                                  | None (`border-transparent`)                                                         | None                                       | No hover/focus affordance; cursor remains default                                                                                          |
| **Notice**  | System feedback or announcements (banners, alerts)            | Full-width tinted strip — info: `color-mix(in srgb, var(--palette-text-legal) 6%, white)`; success: `color-mix(in srgb, var(--primary) 6%, white)` | Bottom 2 px accent line: info `var(--palette-text-legal)`, success `var(--primary)` | None (flat)                                | Non-interactive; icon slot left, dismiss slot right (optional)                                                                             |
| **Field**   | Data-entry surfaces (inputs, selects, inline-edit)            | `bg-white`                                                                                                                                         | 1 px `rgb(34 34 34 / 0.08)` default; `border-primary` on focus                      | None                                       | Focus: `ring-2 ring-primary/15` + subtle primary bg tint; Disabled: `bg-muted opacity-60`; Min height: `h-9` (compact) / `h-11` (standard) |
| **Surface** | Passive containers (cards, panels, sections, detail panes)    | `bg-white`                                                                                                                                         | 1 px `rgb(34 34 34 / 0.04)`                                                         | `shadow-console`                           | Selected: `ring-2 ring-primary/30`; Hover (if clickable): `shadow-console-hover`                                                           |

#### Key Differentiation Rules

- **Action vs. Status**: Status elements NEVER use `shadow-console`, hover lift, or `cursor-pointer`. Actions ALWAYS show a cursor change and at least one visual state transition on hover.
- **Status badge fill colors**: Enabled/success → `color-mix(in srgb, var(--primary) 12%, white)` with `text-primary`; Disabled/neutral → `bg-secondary text-secondary-foreground`; Error/destructive → `color-mix(in srgb, var(--destructive) 12%, white)` with `text-destructive`.
- **Notice vs. Status badge**: Notices are full-width block-level strips with icon + text + optional dismiss; badges are inline pill tokens. They must never share the same container shape.

### 10.2 Notice Banner Specification

The console uses a **dedicated notice banner** (not a reused pill/badge) for system-level messages displayed below the shell header.

**Structure:**

```
┌────────────────────────────────────────────────────────┐
│ [icon]  Notice text content                   [dismiss?] │
│ ── 2 px accent bottom border ──────────────────────────│
└────────────────────────────────────────────────────────┘
```

**Styling:**

- Container: full width of header content area, `rounded-[14px]`, `px-4 py-3`
- Background: tone-dependent tinted fill (see role table)
- Bottom accent: `border-b-2` with tone color
- Icon slot: `size-4`, tone-colored, left-aligned
- Text: `text-sm font-medium text-foreground`
- No `shadow-console` — banners are flat informational strips
- Gap between multiple notices: `gap-3` vertical stack

### 10.3 Shell Search vs. Page Search Hierarchy

| Attribute         | Shell Search (header)                            | Page Search (in-content)                               |
| ----------------- | ------------------------------------------------ | ------------------------------------------------------ |
| **Visual weight** | Subordinate — blends into header                 | Primary — the page's main filter action                |
| **Container**     | No shadow, `bg-secondary` tinted, `rounded-full` | `shadow-console`, `bg-white`, `rounded-full`           |
| **Border**        | `border-transparent`                             | `border-transparent` (shadow provides edge)            |
| **Width**         | Fixed `w-[280px]`                                | Responsive `max-w-sm` minimum                          |
| **Icon**          | None — placeholder text only                     | `Search` icon in `left-4` position                     |
| **Focus**         | Subtle `ring-1 ring-ring/10`                     | Standard field focus (`ring-2 ring-primary/15` + tint) |

**Rationale:** The shell search is a global jump/quick-find utility; it must not compete with the page's primary browsing or query control.

### 10.4 Marketplace Page Layout Rules

```
┌─────────────────────────────────────────────────────────┐
│  Page header: kicker + display title + description      │
│  ───────────────────────  [Page search ⌕]               │
├─────────────────────────────────┬───────────────────────┤
│  Asset card grid                │  Detail panel (sticky) │
│  3-col on lg / 2-col sm        │  Fixed 360px on xl     │
│  gap-4                          │  top-24 sticky         │
│                                 │                        │
│  [empty / loading / error]      │  [empty / loading /    │
│                                 │   error / detail]      │
└─────────────────────────────────┴───────────────────────┘
```

- Asset cards and detail panel share the same top edge alignment.
- Selected card: `ring-2 ring-primary/30` (surface selected state).
- Detail panel uses the same card radius and shadow as asset cards, ensuring visual parity.
- Empty / loading / error states: centered text inside the region, `py-16` on list, `py-10` on detail, `text-sm text-muted-foreground` (or `text-destructive` for errors).

### 10.5 Workspace Alignment Grid

All workspace management panels (category management, asset management, recent assets) observe a shared alignment grid:

| Token                     | Value                                 | Applies to                                                |
| ------------------------- | ------------------------------------- | --------------------------------------------------------- |
| **Row height (standard)** | `min-h-[44px]` (`h-11`)               | Category rows, asset snapshot header, recent-asset rows   |
| **Row height (compact)**  | `min-h-[36px]` (`h-9`)                | Inline rename input + adjacent action buttons             |
| **Row inner padding**     | `px-4 py-3`                           | All list-item rows                                        |
| **Action button size**    | `size="xs"` (`h-8 rounded-full px-3`) | Row-level actions (rename, enable, disable, save, cancel) |
| **Input min height**      | `h-9`                                 | Inline rename input (never below this)                    |
| **Create row**            | Input `flex-1` + Button `size="sm"`   | Category create, asset lookup rows                        |
| **Section gap**           | `gap-5`                               | Between workspace cards                                   |

#### Inline Rename Rules

- When rename mode activates, the input replaces the text display but keeps `h-9` minimum height.
- Adjacent save/cancel buttons use `size="xs"` to stay within the row rhythm.
- The row container does NOT change its padding or background during rename mode.

#### API Asset Management Workspace Composition

The default API asset management workspace uses a two-region composition on wide screens:

- Left region: recent assets at the top, selected/API asset card below.
- Right region: API asset list, including filters, rows, pagination, and list states.
- Narrow viewports collapse into one column in this order: recent assets, selected/API asset card, API asset list.
- The asset editor opens in the existing right-side drawer overlay. The page layout must not reserve a blank right-side edit column.
- If there are no recent assets, do not render an empty recent-assets card; keep the selected/API asset card in the left region.

### 10.6 State Feedback Inventory

| State                         | Visual Treatment                                                                              |
| ----------------------------- | --------------------------------------------------------------------------------------------- |
| **Empty**                     | Centered `text-sm text-muted-foreground` message, `py-6` (workspace) or `py-16` (marketplace) |
| **Loading**                   | Same layout as empty, with `text-muted-foreground` loading message                            |
| **Error**                     | Same layout as empty, with `text-destructive` error message                                   |
| **Selected (card)**           | `ring-2 ring-primary/30` on the surface                                                       |
| **Hover (clickable surface)** | `shadow-console-hover` transition                                                             |
| **Focus (field)**             | `border-primary ring-2 ring-primary/15` + `bg-[color-mix(in srgb, var(--primary) 4%, white)]` |
| **Disabled (action)**         | `opacity-50 pointer-events-none`                                                              |
| **Disabled (field)**          | `bg-muted opacity-60 pointer-events-none cursor-not-allowed`                                  |
| **Active (action)**           | `scale-[0.98]` press feedback                                                                 |

### 10.7 Console-Specific Do's and Don'ts

#### Do

- Use the semantic role table to classify every new element before choosing its variant
- Use the dedicated notice banner for system-level messages — never reuse badge or pill
- Keep shell search visually subordinate to page-level search
- Maintain `h-9` minimum on all inline-edit inputs
- Use `size="xs"` for row-level compact actions inside list items
- Align category rows, asset rows, and recent-asset rows to the same baseline grid

#### Don't

- Don't apply `shadow-console` to status badges — shadows imply interactivity
- Don't use `rounded-full` on notice banners — banners use `rounded-[14px]`
- Don't let the shell search container use `shadow-console` — use `bg-secondary` instead
- Don't give inline-rename inputs a height below `h-9` — cramped inputs break row rhythm
- Don't mix action affordances (hover lift, cursor-pointer) into read-only status labels

### 10.8 Import-Agent Conversation Workspace

The import-agent workspace is a console-native conversation surface, not a generic form page. It should feel like an operator talking to an execution assistant inside the existing control panel.

- **Primary composition**: a large left conversation surface paired with a narrower right summary rail. The conversation surface owns the first-message composer, clarification turns, and file-attachment entry. The right rail owns session snapshot, plan status, and run status cards.
- **Primary composition**: a large left conversation surface paired with a narrower right summary rail. The conversation surface owns the first-message composer, clarification turns, latest plan card, and latest run-result card. The right rail is intentionally reduced to a session snapshot and must not compete with the conversation as a second primary workspace.
- **Conversation bubbles**: user messages are the only place allowed to use the strong Rausch Red fill. Agent messages stay on soft secondary surfaces with thin borders. This keeps the brand accent tied to user intent and send actions instead of passive content.
- **Composer hierarchy**: the message textarea is always the primary field. “Add file” and “Advanced context” are secondary outline actions beneath the input, never peers above it. Advanced context must stay collapsed by default and only appear before the first message is sent.
- **File attachments**: each attached file renders as a compact surface card with filename, size, truncated preview, and a remove affordance. Treat attached files as contextual evidence, not as a separate upload workflow. The UI must clearly state that raw files are not uploaded directly.
- **Session summary rail**: session metadata is supportive context and must not compete with the conversation composer. Use standard surface cards and compact status tags; avoid promotional notice styling.
- **Plan / run embedding**: latest plan and latest run result should appear as structured assistant responses inside the conversation stream, not as detached side panels. They may use card-like treatment, but they still belong to the main narrative column.
- **Motion and density**: keep bubble spacing relaxed (`gap-3` to `gap-4`), preserve 16px+ internal padding, and avoid dense enterprise-table rhythms inside the conversation column. The workspace should still feel warm and browseable, consistent with the rest of the console.

## 11. 控制台展示型组件视觉规范

本章定义 `aether-console` 中用于“展示信息而非触发操作”的组件模式。实现市场、资产、日志、Playground 等页面时，优先复用这些展示型组件；基础 `Button`、`Input`、`Card` 继续作为底层原子组件，不承载只读状态语义。

### 11.1 语义边界

| 类型       | 用途                                        | 视觉规则                                                                          | 禁止事项                                          |
| ---------- | ------------------------------------------- | --------------------------------------------------------------------------------- | ------------------------------------------------- |
| 只读状态   | 类型、状态、方法、状态码、结果类型          | 使用 `DisplayTag` / `MethodTag`，圆形胶囊、浅色底、无阴影                         | 不使用 Button、无 hover lift、无 `cursor-pointer` |
| 可点击操作 | 新建、编辑、复制、查询、调用、删除          | 使用 `Button` 或可点击 `DataListRow`，具备 hover / focus / disabled / active 状态 | 不伪装成只读标签                                  |
| 字段输入   | 表单输入、选择、JSON 文本域                 | 使用 `FieldLabel` + 输入控件，说明在字段附近展示                                  | 不依赖 placeholder 作为唯一说明                   |
| 系统反馈   | 空态、错误态、加载态、不可用态              | 使用 `StateBlock` 或同等结构，包含图标、标题、说明和可选行动入口                  | 不用空白区域代替反馈                              |
| 数据展示   | JSON、Header、Payload、示例、日志结构化字段 | 使用 `CodeBlock`，支持格式化、复制、纯文本兜底和必要折叠                          | 不在页面内散写 `<pre>` 样式                       |

### 11.2 元信息与标签

- `MetaItem` 用于发布者、时间、分类、鉴权方式、耗时等短信息，结构为图标 + 可选标签 + 值。
- `DisplayTag` 用于类型、状态、结果、状态码等只读短标签。颜色按语义选择：成功使用主色浅底，危险使用 destructive 浅底，信息使用 legal blue 浅底，AI 使用 chart-3 浅底。
- `MethodTag` 用于 HTTP 方法：GET 为成功色，POST 为信息色，PUT/PATCH 为警示色，DELETE 为危险色。
- 元信息缺失时可以省略，不从其他数据推导不可用字段。

### 11.3 字段组与增强 Label

- 复杂表单必须用 `FieldGroup` 分组，例如基础信息、上游与鉴权、请求与响应示例、AI 能力。
- 字段标题使用 `FieldLabel`，支持说明、必填/可选标记。关键字段说明必须出现在字段附近。
- 字段组使用白底、14px 圆角、轻边框，并在标题左侧使用 2px 主色强调线。

### 11.4 结构化列表行

- 资产、日志、API Key 等高密度列表优先使用 `DataListRow` 或同等结构。
- 行内分区为：标题/描述、元信息、只读标签、操作按钮。只读标签和操作按钮必须视觉分离。
- 只有整行可点击时才允许 hover lift 和 `cursor-pointer`；纯展示行保持静态。

### 11.5 JSON 与代码展示

- 请求模板、请求示例、响应示例、Header、Payload、调用结果、日志 `usageSnapshot` 均使用 `CodeBlock`。
- 合法 JSON 自动格式化；非法 JSON 或普通文本以原文展示，并给出非阻塞提示。
- 代码块可按场景提供复制与折叠；复制反馈使用 i18n 文案。
- API 市场中的请求体/响应体 Schema 不再只以内联原始 JSON 长块展示；应使用紧凑触发入口，并在独立检查层中提供结构化查看。
- Schema 检查层在桌面端使用大尺寸居中弹层，在窄屏端使用全高抽屉式布局；必须支持字段树展开/收缩、类型/必填/枚举等元数据展示，并保留原始 JSON 兜底入口。

### 11.6 状态反馈

- 列表加载、空结果、请求失败、契约不可用等状态使用 `StateBlock` 或同等结构。
- 空态应包含明确标题和说明；错误态使用 destructive 色；不可用态使用中性图标和说明。
- 不可用能力不得呈现为可工作的真实操作，可展示为 disabled Action 或不可用态说明。
