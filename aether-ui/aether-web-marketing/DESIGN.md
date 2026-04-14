# Design System Strategy: The Luminous Exchange

## 1. Overview & Creative North Star: "Prismatic Clarity"

The North Star for this design system is **Prismatic Clarity**. In a world of complex API documentation and dense data, we position ourselves as the light through the prism—breaking down complexity into a clean, spectrum-rich, and organized experience.

We move beyond the "SaaS-in-a-box" look by embracing **High-End Editorial Minimalism**. This means rejecting the rigid 1px border grid in favor of breathing room, intentional asymmetry, and "The Layering Principle." We create professional energy not through heavy colors, but through the vibration between soft warm tones (`primary`) and expansive white space (`surface`). The UI should feel less like a software tool and more like a high-end digital gallery for technical assets.

---

## 2. Colors & Surface Philosophy

The palette is rooted in warmth and luminosity. We avoid "dead" grays, ensuring every neutral has a hint of warmth to keep the energy professional yet inviting.

### The "No-Line" Rule

**Explicit Instruction:** Solid 1px borders are prohibited for sectioning. To define boundaries, designers must use tonal shifts between surface tiers.

- *Example:* A `surface-container-low` sidebar against a `surface` background provides all the separation needed without adding visual "noise."

### Surface Hierarchy & Nesting

Treat the interface as a physical stack of semi-transparent layers.

- **Base:** `surface` (#f5f6f7) is your canvas.
- **Lowest Layer:** Use `surface-container-lowest` (#ffffff) for primary content cards to make them "pop" against the background.
- **Nesting:** When a component lives inside a card, move one tier up (e.g., a search bar inside a white card should use `surface-container-low`).

### The Glass & Gradient Rule

To achieve "The Luminous Exchange" tone, main CTAs and Hero moments should utilize subtle linear gradients:

- **Primary Gradient:** `primary` (#a0383b) to `primary_container` (#fe7f7f) at a 135° angle.
- **Glassmorphism:** For floating navigation or modal overlays, use `surface` at 80% opacity with a `24px` backdrop-blur. This keeps the marketplace feeling airy and interconnected.

---

## 3. Typography: Editorial Authority

We pair **Manrope** (Display/Headline) for its geometric, modern character with **Inter** (Body/Label) for its world-class legibility.

- **Display (Manrope):** Set with tight letter-spacing (-2%) and generous line-height. Use `display-lg` for hero statements to create an editorial feel.
- **Headlines (Manrope):** `headline-md` should be used to anchor sections. Don't be afraid of asymmetrical placement—try left-aligning headers while centering content to create visual interest.
- **Body & Labels (Inter):** These are your workhorses. Use `body-md` for documentation and `label-md` for API keys and technical metadata.
- **Contrast:** Always use `on_surface_variant` (#595c5d) for secondary text to maintain the "Luminous" quality—avoiding pure black keeps the interface soft.

---

## 4. Elevation & Depth

In this system, depth is felt, not seen. We favor **Tonal Layering** over shadows.

- **The Layering Principle:** Instead of a shadow, place a `surface-container-lowest` card on a `surface-container-low` background. The subtle 2% difference in luminosity creates a sophisticated "lift."
- **Ambient Shadows:** Where floating is required (e.g., a dropdown), use a "Sunlight Shadow":
  - `box-shadow: 0px 12px 32px rgba(160, 56, 59, 0.04);` (A shadow tinted with the primary hue).
- **The "Ghost Border":** If a container requires a boundary (like a code snippet block), use `outline_variant` at 15% opacity. Never use 100% opacity lines.

---

## 5. Components

### Buttons

- **Primary:** Gradient fill (`primary` to `primary_container`). `xl` rounded corners (1.5rem). High-energy, high-visibility.
- **Secondary:** `surface-container-high` background with `on_surface` text. No border.
- **Tertiary:** Pure text with `primary` color. Use for low-priority actions like "Learn More."

### Input Fields

- **Default State:** `surface-container-lowest` background with a `Ghost Border`.
- **Focus State:** Transition the border to `primary` at 40% opacity and add a subtle 4px "glow" using the primary color at 10% opacity.
- **Corner Radius:** Use `md` (0.75rem) to maintain the "soft" aesthetic.

### Cards (The API Marketplace Entry)

- **Rules:** No dividers. Use `spacing-lg` (vertical white space) to separate the API title, description, and tags.
- **Hover State:** Instead of a heavy shadow, shift the background color from `surface-container-lowest` to `surface-bright` and apply a subtle `sm` (0.25rem) lift.

### Chips (API Categories)

- **Visuals:** Use `secondary_container` (#ffc882) for backgrounds with `on_secondary_container` text.
- **Shape:** `full` (pill-shaped) to contrast against the `md` rounded corners of cards.

### Code Blocks (Unique to API Context)

- **Style:** Never use dark mode code blocks in this system. Use `surface-container-highest` with a soft `on_surface` mono font. This maintains the "Luminous" theme even in technical areas.

---

## 6. Do’s and Don'ts

### Do:

- **Do** use whitespace as a functional element. If a section feels crowded, add 24px of padding rather than a divider line.
- **Do** use the `primary_fixed_dim` (#ee7373) for interactive icons to give them a "soft-glow" appearance.
- **Do** lean into asymmetry. A large `display-lg` headline offset to the left with a primary CTA creates a premium, custom feel.

### Don’t:

- **Don't** use solid black (#000000) for text or backgrounds. It breaks the luminous, airy vibe.
- **Don't** use sharp 90-degree corners. Everything must feel "held" and "soft."
- **Don't** use standard "Material Design" blue for links. Use the `primary` soft red to maintain brand signature.
- **Don't** stack more than three levels of surface containers. It leads to "Visual Mud." Keep it shallow and bright.