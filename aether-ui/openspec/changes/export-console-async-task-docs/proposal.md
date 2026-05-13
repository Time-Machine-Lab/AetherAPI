## Why

Marketplace API document export currently includes request, examples, and AI capability sections, but omits async task query details. Users cannot tell from the exported documentation how to query an async task or how to interpret status/result/error paths.

## What Changes

- Map Discovery detail `asyncTaskConfig` into the console model.
- Add an async task query section to exported Markdown when the asset declares an enabled task query channel.
- Generate a best-effort response structure example from `statusPath`, `resultPath`, and `errorPath`.

## Non-Goals

- No new page route or background polling UI.
- No inference of fields beyond the configured paths.
- No export of private auth override payloads.
