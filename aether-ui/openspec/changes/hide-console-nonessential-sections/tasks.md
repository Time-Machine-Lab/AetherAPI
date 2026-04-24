## 1. Shell Visibility Alignment

- [x] 1.1 Update the console shell source of truth so `category-manage`, `usage`, `orders`, `billing`, and `docs` are no longer exposed through the standard sidebar and top utility models
- [x] 1.2 Re-check sign-in/helper preview surfaces and any other shell-derived entry models so hidden sections are no longer advertised there

## 2. Workspace And Route Fallback

- [x] 2.1 Update `console-workspace` rendering so temporarily hidden section UI is not shown as part of the normal workspace flow
- [x] 2.2 Normalize stale hidden hashes to a visible default management destination and verify active navigation state remains consistent, including `#billing`

## 3. Verification

- [x] 3.1 Update shell/workspace tests to cover the retained visible entries and the newly hidden entries
- [x] 3.2 Run the relevant frontend test command(s) and confirm the hidden-section release scope does not regress visible console paths
