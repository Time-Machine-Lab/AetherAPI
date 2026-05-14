## Design

The export helper continues to consume only `DiscoveryAssetDetail`. Once backend Discovery exposes `asyncTaskConfig`, the frontend DTO mapper carries it into the document generator.

The Markdown async task section documents the platform query endpoint, query method, auth mode, optional auth scheme, and configured extraction paths. It does not print `authConfig`.

The response structure generator accepts simple JSONPath-style paths such as `$.data.status` and folds all configured paths into one illustrative JSON object:

```json
{
  "data": {
    "status": "<task status>",
    "result": "<task result>",
    "error": "<task error>"
  }
}
```

Unsupported path syntax is ignored for the generated structure, while the raw path table remains visible.
