# sufy OpenAI 风格视频 API 调用说明

本文只收录 sufy 平台当前已验证可用的 Kling 和 Sora 视频模型，并统一使用 OpenAI-compatible 的请求与响应格式描述。

真实 API Key 不在本文中提供，统一以 `<sufy_api_key>` 表示。

## AI 可读摘要

```yaml
platform: sufy
protocol: openai-compatible
base_url: https://openai.sufy.com/v1
auth:
  type: bearer
  header: Authorization
  value_format: Bearer <sufy_api_key>
supported_models:
  - sora-2
  - kling-v2-1
  - kling-v2-5-turbo
  - kling-v2-6
  - kling-v3
  - kling-v3-omni
  - kling-video-o1
endpoints:
  create_video:
    method: POST
    path: /videos
  get_video:
    method: GET
    path: /videos/{video_id}
task_id_prefix: qvideo-
request_format: openai
response_format: openai
request_body_schema:
  type: object
  required:
    - model
    - prompt
    - duration
  additionalProperties: true
verified_request_fields:
  - model
  - prompt
  - duration
```

## 1. 平台概览

| 项目 | 值 |
|---|---|
| 平台名 | sufy |
| Base URL | `https://openai.sufy.com/v1` |
| 鉴权 | `Authorization: Bearer <sufy_api_key>` |
| 协议风格 | OpenAI-compatible |
| 任务 ID 前缀 | `qvideo-` |

说明：

- sufy 当前已验证可用的视频提交接口是 `POST /v1/videos`。
- sufy 当前已验证可用的任务查询接口是 `GET /v1/videos/{video_id}`。
- 当前 `/v1/videos` 实测仅支持模型名以 `sora` 或 `kling` 开头的模型。
- 本文里的 `OpenAI-compatible` 指接口风格兼容 OpenAI，不等于“本文已拿到 sufy 视频接口的完整字段全集”。

## 2. 已验证可用模型

| 模型 ID | 模型族 | 状态 | 备注 |
|---|---|---|---|
| `sora-2` | Sora | 已验证可提交 | 返回 `status: queued` |
| `kling-v2-1` | Kling | 按同一 OpenAI 风格接入 | 请求与响应格式与其他 Kling 模型一致 |
| `kling-v2-5-turbo` | Kling | 按同一 OpenAI 风格接入 | 请求与响应格式与其他 Kling 模型一致 |
| `kling-v2-6` | Kling | 按同一 OpenAI 风格接入 | 请求与响应格式与其他 Kling 模型一致 |
| `kling-v3` | Kling | 已验证可提交 | 返回 `status: queued` |
| `kling-v3-omni` | Kling | 已验证可提交 | 返回 `status: queued` |
| `kling-video-o1` | Kling | 已验证可提交 | 返回 `status: queued` |

不在本文范围内的模型：

- `veo-3.1-fast-generate-001`
- `veo-3.1-generate-001`
- `viduq3-turbo`
- `viduq3-pro`

这些模型虽然在平台页面可见，但当前 `POST /v1/videos` 返回的错误明确指出仅支持 `sora` 或 `kling` vendor，因此不视为当前可用模型。

## 3. 通用鉴权

所有请求统一使用：

```http
Authorization: Bearer <sufy_api_key>
Content-Type: application/json
Accept: application/json
```

## 4. 创建视频任务

### 4.1 端点

```http
POST https://openai.sufy.com/v1/videos
Authorization: Bearer <sufy_api_key>
Content-Type: application/json
Accept: application/json
```

### 4.2 OpenAI-compatible 最小可用请求体

```json
{
  "model": "sora-2",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5
}
```

这里展示的是当前已经实测打通的最小请求体，不是完整参数上限。

### 4.3 OpenAI-compatible 在这里具体指什么

当前能确认的兼容点是：

- 路径兼容：`POST /v1/videos`、`GET /v1/videos/{video_id}`
- 鉴权兼容：`Authorization: Bearer <sufy_api_key>`
- 请求风格兼容：顶层 JSON body，使用 `model` 指定模型
- 响应风格兼容：成功返回任务对象，失败返回 `error.message` 和 `error.type`

当前不能确认的是：sufy 是否完整公开或完整支持某个固定版本的“OpenAI 视频参数全集”。

### 4.4 OpenAI 风格请求字段总表

| 字段 | 类型 | 级别 | 当前状态 | 说明 |
|---|---|---|---|---|
| `model` | string | 核心字段 | 已验证 | 只能填写当前文档收录的 sufy 模型 ID。 |
| `prompt` | string | 核心字段 | 已验证 | 文生视频提示词。 |
| `duration` | number | 核心字段 | 已验证 | 视频秒数，当前实测 `5` 可用。 |
| `image` | string 或 array | OpenAI 风格可选字段 | 待验证 | 用于图生视频或首帧控制，常见传 URL、data URL 或图片数组。 |
| `n` | number | OpenAI 风格可选字段 | 待验证 | 批量生成数量，常见默认值为 `1`。 |
| `size` | string | OpenAI 风格可选字段 | 待验证 | 输出尺寸或分辨率，例如 `1280x720`、`720x1280`。 |
| `response_format` | string | OpenAI 风格可选字段 | 待验证 | 常见值为 `url` 或 `b64_json`。 |
| `seed` | number | OpenAI 风格可选字段 | 待验证 | 固定随机种子，便于复现。 |
| `metadata` | object | OpenAI 风格可选字段 | 待验证 | 透传业务标签或调用侧附加信息。 |

### 4.5 字段分层说明

不是说请求体只有 `model`、`prompt`、`duration` 这几个参数，而是：

- sufy 公共文档目前没有公开 `/v1/videos` 的完整 schema
- 当前仓库里的真实探测脚本只验证了 `model`、`prompt`、`duration`
- 但为了方便接入，文档现在已经把常见 OpenAI 风格可选字段一起列出来，并明确标注为“待验证”

因此当前文档的表达方式是：

- 已验证字段：可以直接按当前文档发请求
- 待验证字段：可作为 OpenAI 风格兼容候选字段，但不能当作 sufy 已官方确认支持

### 4.6 OpenAI 风格扩展请求体草案

下面这个请求体不是“已全部验证成功”，而是给接入方的 OpenAI 风格扩展草案：

```json
{
  "model": "kling-v3",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5,
  "image": "https://example.com/first-frame.png",
  "n": 1,
  "size": "1280x720",
  "response_format": "url",
  "seed": 123456,
  "metadata": {
    "project": "demo",
    "scene": "rain-cat"
  }
}
```

### 4.7 各模型最小请求示例

#### `sora-2`

```json
{
  "model": "sora-2",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5
}
```

#### `kling-v3`

```json
{
  "model": "kling-v3",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5
}
```

#### `kling-v2-1`

```json
{
  "model": "kling-v2-1",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5
}
```

#### `kling-v2-5-turbo`

```json
{
  "model": "kling-v2-5-turbo",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5
}
```

#### `kling-v2-6`

```json
{
  "model": "kling-v2-6",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5
}
```

#### `kling-v3-omni`

```json
{
  "model": "kling-v3-omni",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5
}
```

#### `kling-video-o1`

```json
{
  "model": "kling-video-o1",
  "prompt": "A cat walking in the rain, cinematic lighting.",
  "duration": 5
}
```

### 4.8 OpenAI 风格成功响应

#### `sora-2` 响应示例

```json
{
  "id": "qvideo-1382943071-1778933334837556823",
  "object": "video",
  "model": "sora-2",
  "status": "queued",
  "created_at": 1778933334,
  "updated_at": 1778933334,
  "seconds": "4",
  "job_type_description": "OpenAI 文生视频",
  "billing_type_description": "Sora 有声视频"
}
```

#### `kling-v3` 响应示例

```json
{
  "id": "qvideo-1382943071-1778933051224197911",
  "object": "video",
  "model": "kling-v3",
  "mode": "std",
  "status": "queued",
  "created_at": 1778933051,
  "updated_at": 1778933051,
  "seconds": "5",
  "job_type_description": "可灵文生视频",
  "billing_type_description": "std x 无参考视频 x 无声"
}
```

#### `kling-v3-omni` 响应示例

```json
{
  "id": "qvideo-1382943071-1778933051582906805",
  "object": "video",
  "model": "kling-v3-omni",
  "mode": "std",
  "status": "queued",
  "created_at": 1778933051,
  "updated_at": 1778933051,
  "seconds": "5",
  "job_type_description": "可灵 Omni-Video",
  "billing_type_description": "std x 无参考视频 x 无声"
}
```

#### `kling-video-o1` 响应示例

```json
{
  "id": "qvideo-1382943071-1778933052931327540",
  "object": "video",
  "model": "kling-video-o1",
  "mode": "std",
  "status": "queued",
  "created_at": 1778933052,
  "updated_at": 1778933052,
  "seconds": "5",
  "job_type_description": "可灵 Omni-Video",
  "billing_type_description": "std x 无参考视频 x 无声"
}
```

### 4.9 通用响应字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | string | 视频任务 ID，格式通常为 `qvideo-*`。 |
| `object` | string | 固定为 `video`。 |
| `model` | string | 本次使用的模型 ID。 |
| `status` | string | 任务状态，例如 `queued`。 |
| `created_at` | number | 创建时间戳。 |
| `updated_at` | number | 更新时间戳。 |
| `seconds` | string | 输出视频秒数，当前实测返回 string。 |
| `mode` | string | Kling 系列可能返回，例如 `std`。 |
| `job_type_description` | string | 平台内部任务类型描述。 |
| `billing_type_description` | string | 平台内部计费类型描述。 |

## 5. 查询视频任务

### 5.1 端点

```http
GET https://openai.sufy.com/v1/videos/{video_id}
Authorization: Bearer <sufy_api_key>
Accept: application/json
```

其中 `{video_id}` 使用创建任务接口返回的 `id`，例如 `qvideo-1382943071-1778933334837556823`。

### 5.2 OpenAI 风格查询响应示例

```json
{
  "id": "qvideo-1382943071-1778933334837556823",
  "object": "video",
  "model": "sora-2",
  "status": "queued",
  "created_at": 1778933334,
  "updated_at": 1778933334,
  "seconds": "4",
  "job_type_description": "OpenAI 文生视频",
  "billing_type_description": "Sora 有声视频"
}
```

### 5.3 状态读取建议

当前已观测到：

- `queued`: 已入队，继续轮询

工程上建议把以下状态视为终态：

- `completed`
- `failed`
- `error`
- `cancelled`
- `canceled`

## 6. OpenAI 风格错误响应

当模型不可用或 vendor 不受支持时，sufy 返回的错误对象也是 OpenAI 风格：

```json
{
  "error": {
    "message": "failed to determine job type: unsupported model vendor: veo-3.1-fast-generate-001 (model must start with 'sora' or 'kling')",
    "type": "invalid_request_error"
  }
}
```

常见判断：

| HTTP 状态 | 含义 |
|---|---|
| `200` | 提交成功或查询成功 |
| `400` | 请求体错误、模型不支持或 vendor 不支持 |
| `401` | API Key 缺失或无效 |
| `404` | 路径错误或任务不存在 |

## 7. cURL 示例

### 7.1 提交 Sora 任务

```bash
curl --request POST "https://openai.sufy.com/v1/videos" \
  --header "Authorization: Bearer <sufy_api_key>" \
  --header "Content-Type: application/json" \
  --data '{
    "model": "sora-2",
    "prompt": "A cat walking in the rain, cinematic lighting.",
    "duration": 5
  }'
```

### 7.2 提交 Kling 任务

```bash
curl --request POST "https://openai.sufy.com/v1/videos" \
  --header "Authorization: Bearer <sufy_api_key>" \
  --header "Content-Type: application/json" \
  --data '{
    "model": "kling-v3",
    "prompt": "A cat walking in the rain, cinematic lighting.",
    "duration": 5
  }'
```

### 7.3 查询任务

```bash
curl --request GET "https://openai.sufy.com/v1/videos/qvideo-1382943071-1778933334837556823" \
  --header "Authorization: Bearer <sufy_api_key>"
```

## 8. 给 AI 或接入程序的最小结论

- 平台统一命名为 `sufy`
- 仅使用 `https://openai.sufy.com/v1`
- 仅使用 OpenAI-compatible 的 Bearer 鉴权
- 当前文档收录模型为 `sora-2`、`kling-v2-1`、`kling-v2-5-turbo`、`kling-v2-6`、`kling-v3`、`kling-v3-omni`、`kling-video-o1`
- 创建任务用 `POST /videos`
- 查询任务用 `GET /videos/{video_id}`
- 请求字段分为“已验证字段”和“OpenAI 风格待验证字段”两层
- 返回任务 ID 形如 `qvideo-*`
- 不要把 `veo-*` 或 `viduq3-*` 写进当前可用模型清单
