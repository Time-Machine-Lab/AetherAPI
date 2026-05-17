---
name: batch-import-api
description: 在 Aether API Hub 中批量新增或导入 API 资产的工作流技能。只要用户提到批量导入 API、批量新增接口、批量接入上游接口、批量创建 API 资产、导入 AI API、给 Aether API Hub 一次性加多条 API，就应该使用这个 skill。它会先整理导入所需的后端接口，再要求用户提供后端请求 API key 或 bearer token、接口信息和可选代理配置，然后调用内置 Node.js 脚本执行导入，并在项目内生成带接入示例和任务执行记录表的过程文档。
compatibility:
  tools: Read, Edit, Bash
  runtime: Node.js 18+
---

# Batch Import API

用这个 skill 在 Aether API Hub 中批量创建 API 资产，而不是手动逐条点控制台。

## 先确认项目闭环

新增 API 资产的最小闭环基于以下接口契约：

1. 分类生命周期：docs/api/api-category-lifecycle.yaml
2. 资产管理：docs/api/api-asset-management.yaml
3. 平台代理配置：docs/api/platform-proxy-profile.yaml
4. 订阅：docs/api/api-subscription.yaml
5. 当前用户 API Key：docs/api/api-credential.yaml
6. 统一接入调用：docs/api/unified-access.yaml

默认执行顺序如下：

1. 确保分类存在，必要时启用分类。
2. 注册 API 资产草稿。
3. 修订资产详情，补齐 categoryCode、requestMethod、upstreamUrl、authScheme、样例等信息。
4. 如果是 AI API，绑定 AI 能力档案。
5. 如果用户提供平台代理配置，创建或复用代理配置并绑定到资产。
6. 按需发布资产。
7. 按需为过程文档生成统一接入示例，示例默认走 /api/v1/access/{apiCode}。

## 用户输入

开始执行前，必须向用户收集这些信息：

1. 后端 base URL，例如 http://localhost:8080。
2. 管理接口鉴权信息。
说明：
默认用 Authorization: Bearer <token>。
如果用户明确要求用别的 header，再改成自定义 headerName 和 prefix。
3. 要导入的 API 列表。
说明：
每条至少需要 apiCode、categoryCode、assetName。
通常还要给出 categoryCode、requestMethod、upstreamUrl。
4. 是否需要自动创建分类。
5. 是否需要代理配置与绑定。
6. 是否需要发布资产。
7. 过程文档中的统一接入示例是否要携带真实 X-Aether-Api-Key。
说明：
如果用户没有提供调用侧 API Key，就在示例里保留占位符。

## 导入脚本

使用脚本：.codex/skills/batch-import-api-skill/scripts/import-apis.mjs

配置样例：.codex/skills/batch-import-api-skill/assets/import-config.example.json

执行方式：

```bash
node .codex/skills/batch-import-api-skill/scripts/import-apis.mjs --config <config-json-path>
```

可选参数：

1. --config <path>：导入配置文件路径。
2. --output-dir <path>：过程文档输出目录，默认 docs/api-import-runs。
3. --dry-run：只生成计划和过程文档，不真正发请求。

## 生成配置时的原则

1. 优先把密钥放进环境变量。管理侧鉴权使用 managementAuth.apiKeyEnv；上游鉴权建议在 authConfig 中写 `${env:ENV_NAME}` 占位符，脚本会在执行时解析，避免把明文 token 落盘。
2. 如果用户直接给了明文 token 且明确接受写入临时文件，也可以写到 managementAuth.apiKey。
3. 一次导入多条 API 时，把通用字段抽到 defaults，再让单条 API 按需覆盖。
4. 不要凭空创造后端字段；字段名必须和现有 Req DTO 或 OpenAPI 契约一致。
5. 分类编码是运行时业务配置，不应写死在 skill 中。每条 API 都必须显式提供 assetType，避免脚本对业务配置做隐式推导。
6. authConfig 必须按后端真实格式填写为纯字符串，不是 JSON。HEADER_TOKEN 用 `Header-Name: value`；QUERY_TOKEN 用 `paramName=value`；NONE 不填 authConfig。

## authConfig 正确格式

后端在统一接入代理里直接把 authConfig 当字符串解析：

1. HEADER_TOKEN：按第一个 `:` 分割，左边是 header 名，右边是 header 值。没有 `:` 时默认 header 名是 Authorization。
2. QUERY_TOKEN：按第一个 `=` 分割，左边是 query 参数名，右边是参数值。没有 `=` 时默认参数名是 access_token。
3. NONE：不需要 authConfig。

因此不要再写下面这种 JSON：

```json
{"headerName":"Authorization","prefix":"Bearer","secretRef":"OPENAI_KEY"}
```

应该写成这种纯字符串：

```json
"Authorization: Bearer ${env:OPENAI_KEY}"
```

或者：

```json
"key=${env:WUYIN_MEDIA_API_KEY}"
```

## 推荐配置结构

```json
{
  "baseUrl": "http://localhost:8080",
  "managementAuth": {
    "headerName": "Authorization",
    "prefix": "Bearer",
    "apiKeyEnv": "AETHER_ADMIN_TOKEN"
  },
  "defaults": {
    "publish": true,
    "ensureCategoryEnabled": true
  },
  "categories": [
    {
      "categoryCode": "your-chat-category",
      "categoryName": "你的分类名称"
    }
  ],
  "proxyProfiles": [],
  "apis": [
    {
      "apiCode": "chat-completions",
      "categoryCode": "your-chat-category",
      "assetName": "Chat Completions",
      "assetType": "AI_API",
      "requestMethod": "POST",
      "upstreamUrl": "https://example.com/v1/chat/completions",
      "authScheme": "HEADER_TOKEN",
      "authConfig": "Authorization: Bearer ${env:OPENAI_KEY}",
      "requestExample": "{\"model\":\"gpt-4.1\",\"messages\":[{\"role\":\"user\",\"content\":\"Hello\"}]}",
      "responseExample": "{\"id\":\"demo\",\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Hi\"}}]}",
      "requestJsonSchema": {
        "type": "object",
        "required": ["model", "messages"],
        "properties": {
          "model": { "type": "string" },
          "messages": {
            "type": "array",
            "items": { "type": "object" }
          }
        }
      },
      "responseJsonSchema": {
        "type": "object",
        "properties": {
          "id": { "type": "string" },
          "choices": {
            "type": "array",
            "items": { "type": "object" }
          }
        }
      },
      "aiProfile": {
        "provider": "OpenAI",
        "model": "gpt-4.1",
        "streamingSupported": true,
        "capabilityTags": ["chat", "stream"]
      }
    }
  ],
  "exampleAccess": {
    "apiKey": "<optional-aether-api-key>",
    "contentType": "application/json"
  }
}
```

## 执行步骤

1. 根据用户信息生成配置 JSON。
2. 运行 Node.js 脚本。
3. 检查脚本输出的过程文档路径和失败项。
4. 如果有失败，先修复配置或接口顺序，再重新运行。
5. 最终向用户汇报：
说明：
给出已调用的核心接口、成功导入的 API 数量、失败 API 数量、过程文档路径。

## 输出要求

脚本必须在项目内生成 Markdown 过程文档，文档至少包含：

1. 本次导入概览。
2. 使用到的后端接口清单。
3. 至少一个统一接入示例。
4. 任务执行记录表。
5. 失败项与重试建议。

## 注意事项

1. 管理接口大多走 bearerAuth；统一接入示例走 X-Aether-Api-Key。
2. 分类在被资产引用前应处于 ENABLED。
3. 发布前至少应完成资产注册与必要字段修订。
4. 不要在 skill 中假设分类编码和资产类型的固定关系。每条 API 直接显式提供 assetType。
5. 代理绑定需要 profileId；如果只有 profileCode，脚本会优先尝试通过创建结果或列表查询解析 profileId。
6. 当前后端不支持在 authConfig 中写 `secretRef`、`prefix`、`headerName`、`queryParamName` 这类 JSON 字段。skill 已改为接受纯字符串，并支持 `${env:ENV_NAME}` 占位符。
## JSON Schema 字段补充

导入 API 时可以在每条 `apis[]` 配置中提供：

1. `requestJsonSchema`：请求体 JSON Schema 快照，可为空。
2. `responseJsonSchema`：响应体 JSON Schema 快照，可为空。

字段名必须与后端 `RegisterApiAssetReq` / `ReviseApiAssetReq` 保持一致，不要使用 `requestSchema`、`responseSchema`、`inputSchema`、`outputSchema` 等别名。

字段值支持三种写法：

1. JSON 字符串：脚本原样 trim 后发送。
2. JSON 对象：脚本会 `JSON.stringify` 后发送给后端保存。
3. 布尔 schema：脚本会序列化为 `"true"` 或 `"false"`。

`null` 或空字符串会发送为 `null`，用于清空已有 schema。脚本不做 JSON Schema 方言校验，只负责把快照传给后端。
## JSON Schema 详细度要求

生成 `requestJsonSchema` 和 `responseJsonSchema` 时要尽可能详细，不要只写字段名和基础类型。应优先从用户提供的接口文档中提取并写入：

1. `description`：字段说明、业务含义、限制条件。
2. `required`：必填字段。
3. `enum`：文档列出的可选值。
4. `default`：文档列出的默认值。
5. `examples`：文档中的示例值。
6. `items`：数组元素结构。
7. `properties`：嵌套对象字段。
8. `minimum`、`maximum`、`minLength`、`maxLength` 等明确约束。

如果某个接口是异步提交接口，结果查询接口不要单独作为一个 API 资产导入；应把查询地址放到该 API 的 `asyncTaskConfig.queryUrlTemplate`，并补齐 `statusPath`、`resultPath`、`errorPath`。只有当用户明确要求把查询接口单独暴露给调用方时，才为查询接口创建独立 API 资产。
