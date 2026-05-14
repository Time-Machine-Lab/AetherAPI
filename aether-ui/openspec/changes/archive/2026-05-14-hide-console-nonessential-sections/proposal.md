## Why

`aether-console` currently still exposes temporary sections that the product now wants to take out of the standard operator flow: category management, usage statistics, order center, billing management, and documentation center. These entries appear in the sidebar, top utility area, and supporting shell surfaces, which makes the console look broader than the set of capabilities that should currently be available.

The change needs to be handled as a visibility adjustment rather than a backend or contract redesign. The goal is to keep the console focused on the remaining active paths while avoiding dead-end hashes, misleading helper cards, or partially visible management UI.

## What Changes

- Hide the temporary console entries for `category-manage`, `usage`, `orders`, `billing`, and `docs` from the standard `aether-console` navigation surfaces.
- Remove related supporting exposure for these sections from shell-adjacent UI, including helper or preview surfaces that currently advertise category management.
- Define fallback behavior so hidden hashes or hidden section states do not leave the operator on a misleading or partially hidden destination.
- Keep the change limited to frontend visibility in `aether-console`; it does not remove backend capabilities or redesign retained areas such as asset management, API Key, API call logs, and playground.

## Capabilities

### New Capabilities
- `console-section-visibility`: Define which console sections remain visible in the current release and how the shell behaves when temporarily hidden sections are addressed.

### Modified Capabilities
- None.

## Impact

- Affected app: `aether-console`
- Affected frontend areas: console shell navigation, layout rendering, workspace composition, sign-in helper/previews, locale usage, and shell/workspace tests
- Boundary note: this proposal is intentionally limited to temporary UI hiding and route/hash behavior; it does not change backend API contracts or delete dormant copy/configuration unless implementation chooses to clean up unused references
- Context gap noted during proposal: `aether-ui/openspec/project.md` is not present, so this proposal is grounded in `openspec/config.yaml`, existing specs, and current `aether-console` code
