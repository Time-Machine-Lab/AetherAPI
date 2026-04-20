## ADDED Requirements

### Requirement: In-scope controllers MUST support deterministic parameter binding
The system MUST stably bind path variables and query parameters for the affected category, asset, and current-user API credential endpoints, and MUST scan same-pattern high-risk controllers so these requests do not depend on implicit runtime inference of Java parameter names.

#### Scenario: Category list request binds paging parameters
- **WHEN** the client requests `GET /api/v1/categories` with optional `status`, `page`, and `size` parameters
- **THEN** the system binds those parameters successfully and executes the category list flow instead of failing with a framework-level `400`

#### Scenario: Asset detail request binds apiCode
- **WHEN** the client requests `GET /api/v1/assets/{apiCode}` with a concrete asset code such as `deepseek-v3`
- **THEN** the system binds `apiCode` successfully and enters the asset detail flow instead of failing before delegate execution

#### Scenario: Current-user API key list request binds paging parameters
- **WHEN** the client requests `GET /api/v1/current-user/api-keys?page=1&size=20`
- **THEN** the system binds paging parameters successfully and executes the current-user API credential list flow

#### Scenario: Same-pattern high-risk controllers are scanned
- **WHEN** the project fixes the known parameter-binding failures
- **THEN** it also scans other controllers using the same implicit `@PathVariable` or `@RequestParam` style and brings high-risk endpoints into regression coverage

### Requirement: Framework-level binding failures MUST map to the correct interface-family error
The system MUST represent framework-level request parameter binding failures as `400` responses aligned with the target interface family, instead of mapping unrelated endpoints to category error codes.

#### Scenario: Category endpoint binding failure returns category invalid-request error
- **WHEN** a category management endpoint fails during request parameter binding
- **THEN** the response uses the category interface-family invalid-request error code

#### Scenario: Asset endpoint binding failure returns asset invalid-request error
- **WHEN** an asset management endpoint fails during request parameter binding
- **THEN** the response uses the asset interface-family invalid-request error code and does not return `CATEGORY_CODE_INVALID`

#### Scenario: API credential endpoint binding failure returns credential invalid-request error
- **WHEN** a current-user API credential endpoint fails during request parameter binding
- **THEN** the response uses the credential interface-family invalid-request error code and does not return a category error code

#### Scenario: Spring Web binding exceptions are classified by endpoint family
- **WHEN** Spring Web raises a missing-parameter, path-variable-binding, or type-mismatch exception before business logic execution
- **THEN** the system maps that failure to the correct endpoint family instead of falling through a generic category fallback

### Requirement: Binding-failure responses MUST NOT leak compiler or reflection diagnostics
The system MUST NOT expose compiler flags, reflection hints, or similar internal framework diagnostics in client-visible parameter-binding failure messages.

#### Scenario: Client response strips compiler and reflection hints
- **WHEN** an in-scope endpoint fails during framework-level parameter binding
- **THEN** the client-visible error message describes the request as invalid without including `-parameters` or reflection metadata hints

### Requirement: Parameter-binding behavior changes MUST be reflected in controller-level API authority docs
The system MUST record client-visible parameter-binding and invalid-request behavior changes in controller-level repo-root `docs/api/` authority documents before implementation.

#### Scenario: Category API authority doc is updated
- **WHEN** the project records category interface parameter-binding stability changes
- **THEN** it uses `tml-docs-spec-generate` with the API template to update the repo-root authority document mapped to `CategoryController.java`

#### Scenario: Asset API authority doc is updated
- **WHEN** the project records asset interface parameter-binding stability changes
- **THEN** it uses `tml-docs-spec-generate` with the API template to update the repo-root authority document mapped to `ApiAssetController.java`

#### Scenario: API credential API authority doc is updated
- **WHEN** the project records current-user API credential interface parameter-binding stability changes
- **THEN** it uses `tml-docs-spec-generate` with the API template to maintain `docs/api/api-credential.yaml` mapped to `ApiCredentialController.java`
