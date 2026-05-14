## ADDED Requirements

### Requirement: API Market MUST present discovery cards with sufficient summary density
`aether-console` MUST render the API Market card list with enough contract-backed summary information that published APIs do not appear visually empty when browsing the marketplace.

#### Scenario: User browses the API Market list
- **WHEN** the API Market list successfully returns one or more published assets
- **THEN** each card MUST present the asset identity and additional summary metadata using documented discovery fields rather than leaving large empty regions with only a title
- **THEN** the summary treatment MUST remain within the existing console card and badge grammar defined by `aether-console/DESIGN.md`

### Requirement: API Market detail MUST align with the discovery authority contract
The API Market detail panel MUST align with `../docs/api/api-catalog-discovery.yaml` and present documented discovery detail fields, including request-method information and structured request/response example snapshots when they are available.

#### Scenario: User opens a standard API detail panel
- **WHEN** the user selects a published API whose discovery detail includes `requestMethod`, `authScheme`, `requestTemplate`, or `exampleSnapshot`
- **THEN** the detail panel MUST display the available documented fields with clear labels
- **THEN** `exampleSnapshot` MUST be interpreted according to the authority contract instead of being rendered as an opaque single-string blob when request and response snapshots are separately available

#### Scenario: Discovery detail fields are partially populated
- **WHEN** the selected API returns only a subset of the documented discovery detail fields
- **THEN** the detail panel MUST show the available fields and omit absent fields without inventing placeholder values or undocumented metadata

### Requirement: Discovery frontend mapping MUST respect authority drift boundaries
The frontend discovery DTO/type mapping MUST stay consistent with the authority discovery contract, and implementation MUST treat contract drift as an authority-alignment issue rather than papering over it in page-level UI code.

#### Scenario: Frontend implementation detects a mismatch with the authority discovery contract
- **WHEN** the team finds that the runtime discovery payload does not match `api-catalog-discovery.yaml`
- **THEN** implementation MUST resolve or explicitly block on that contract mismatch before finalizing the UI behavior
- **THEN** the page layer MUST NOT hard-code undocumented fallback structures that contradict the authority document
