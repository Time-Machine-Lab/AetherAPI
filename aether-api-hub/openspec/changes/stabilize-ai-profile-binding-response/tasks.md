## 1. Reproduction And Authority Check

- [ ] 1.1 Reproduce BUG-002 with an AI asset that has category, upstream config, auth config, request template, request example, and response example before AI profile binding.
- [ ] 1.2 Confirm whether `docs/api/api-asset-management.yaml` already documents AI profile binding as returning a full `ApiAssetResp`; if not, update it with `tml-docs-spec-generate` using the API template.
- [ ] 1.3 Confirm `docs/sql/api-asset.sql` needs no change because the required fields already exist in `api_asset`.

## 2. Backend Preservation Fix

- [ ] 2.1 Add or update domain/service tests proving `attachAiCapabilityProfile` preserves non-AI asset fields.
- [ ] 2.2 Add or update persistence converter/repository tests proving AI profile binding does not write nulls over existing asset configuration.
- [ ] 2.3 Add or update adapter/delegate tests proving the AI-profile binding response includes the complete asset view.
- [ ] 2.4 Fix aggregate, application-service, converter, repository, or DTO mapping code if any preservation or response completeness test fails.

## 3. Verification

- [ ] 3.1 Run focused Catalog asset tests in domain, service, infrastructure, and adapter modules.
- [ ] 3.2 If backend response is already complete, record the finding in the change notes so the remaining form-clearing issue can move to frontend state handling.
