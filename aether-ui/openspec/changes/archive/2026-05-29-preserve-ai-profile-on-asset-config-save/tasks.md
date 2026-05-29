## 1. Contract And Scope

- [x] 1.1 Confirm the existing asset management API response contract already includes AI capability profile fields and no authority document update is required.
- [x] 1.2 Confirm the fix stays inside `aether-console` asset workspace state handling without layout, i18n, or API adapter changes.

## 2. Regression Coverage

- [x] 2.1 Add a composable regression test for saving base asset configuration when the revision response omits AI profile data.
- [x] 2.2 Assert both `currentAsset.aiProfile` and `aiProfileForm` keep provider, model, streaming, and capability tags after the save.

## 3. Implementation

- [x] 3.1 Update `useWorkspaceCatalog` so base asset revision responses preserve the previous AI profile when omitted.
- [x] 3.2 Keep the existing AI profile binding merge behavior unchanged.

## 4. Verification

- [x] 4.1 Run the focused `useWorkspaceCatalog` Vitest suite.
- [x] 4.2 Validate the OpenSpec change artifacts.
