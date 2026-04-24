## 1. Contract And Design Alignment

- [ ] 1.1 Wait for or update `../docs/api/api-asset-management.yaml` so the management asset list contract is authoritative before frontend code work begins
- [ ] 1.2 Re-check `aether-console/DESIGN.md` and confirm the asset list presentation stays within the existing workspace card and row grammar

## 2. Frontend Data Flow

- [ ] 2.1 Add the asset-management list API wrapper, DTO/type mapping, and contract-aligned request parameters in `aether-console/src/api`
- [ ] 2.2 Extend the workspace asset composable with page/filter/list/error state for managed asset browsing

## 3. Workspace UI

- [ ] 3.1 Update `console-workspace` asset management UI to render the asset list, filters, pagination, and selection flow alongside the existing register/detail behavior
- [ ] 3.2 Add or update i18n copy and UI tests for loading, empty, error, filtering, paging, and selection behavior
