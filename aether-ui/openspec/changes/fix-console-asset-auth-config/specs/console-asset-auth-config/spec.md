## ADDED Requirements

### Requirement: Workspace SHALL expose upstream auth configuration
The console asset workspace SHALL provide an editable `authConfig` field for owned asset upstream authentication configuration alongside the documented `authScheme` field.

#### Scenario: Load existing auth config
- **WHEN** the user loads an owned asset detail whose response contains `authConfig`
- **THEN** the workspace configuration form displays that `authConfig` value without dropping it

#### Scenario: Edit token auth config
- **WHEN** the user selects `HEADER_TOKEN` or `QUERY_TOKEN` and enters an auth configuration value
- **THEN** the workspace keeps that value in the asset configuration form until the user saves, reloads, or clears it

### Requirement: Workspace SHALL save auth configuration through the documented revision contract
The console asset workspace SHALL send `authConfig` to `PUT v1/current-user/assets/{apiCode}` when saving an owned asset configuration.

#### Scenario: Save header token config
- **WHEN** the user saves an owned asset with `authScheme` set to `HEADER_TOKEN` and a non-empty `authConfig`
- **THEN** the frontend calls the asset revision API with both `authScheme: "HEADER_TOKEN"` and the normalized `authConfig` value

#### Scenario: Save query token config
- **WHEN** the user saves an owned asset with `authScheme` set to `QUERY_TOKEN` and a non-empty `authConfig`
- **THEN** the frontend calls the asset revision API with both `authScheme: "QUERY_TOKEN"` and the normalized `authConfig` value

#### Scenario: Clear unnecessary auth config
- **WHEN** the user saves an owned asset with `authScheme` set to `NONE` and the auth configuration field empty
- **THEN** the frontend sends `authConfig: null` through the asset revision API

### Requirement: Token-auth assets SHALL be publishable after complete configuration
The console asset workspace SHALL allow token-auth assets to use the existing publish action after users have saved the required upstream auth configuration.

#### Scenario: Publish after saving token auth configuration
- **WHEN** an owned asset has saved `requestMethod`, `upstreamUrl`, `authScheme` of `HEADER_TOKEN` or `QUERY_TOKEN`, and a non-empty `authConfig`
- **THEN** the workspace publish action uses the existing `PATCH v1/current-user/assets/{apiCode}/publish` endpoint and does not block publication due to a frontend-missing auth configuration field

#### Scenario: Publish validation still fails for incomplete token auth configuration
- **WHEN** the publish endpoint rejects a token-auth asset because required configuration is still incomplete
- **THEN** the workspace shows the existing i18n-backed publish failure feedback and keeps the current asset detail visible
