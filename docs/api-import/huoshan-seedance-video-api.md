# 火山 Seedance 视频生成 API 调用说明

本文基于火山官方“创建视频生成任务 API”“查询视频生成任务 API”以及模型列表页面整理，统一将平台名称写为“火山”。

本文覆盖火山当前文档中列出的全部 Seedance 系列视频生成模型，并使用便于 AI 读取和程序接入的结构描述接口、模型能力、字段约束和轮询方式。

真实 API Key 不在本文中提供，统一以 `<huoshan_api_key>` 表示。

## AI 可读摘要

```yaml
platform: 火山
product: Seedance 视频生成
protocol: native-http-json
base_url: https://ark.cn-beijing.volces.com/api/v3
auth:
  type: bearer
  header: Authorization
  value_format: Bearer <huoshan_api_key>
task_mode: async
task_id_prefix: cgt-
endpoints:
  create_task:
    method: POST
    path: /contents/generations/tasks
  get_task:
    method: GET
    path: /contents/generations/tasks/{id}
supported_models:
  - id: doubao-seedance-2-0-260128
    name: Seedance 2.0
    family: Seedance 2.0
    scenes:
      - text_to_video
      - image_to_video_first_frame
      - image_to_video_first_last_frame
      - multimodal_reference
      - video_edit
      - video_extend
    input_modalities:
      text: true
      image: true
      video: true
      audio: true
      draft_task: false
    output_audio: true
    web_search: true
    draft_mode: false
    service_tiers:
      - default
    resolutions:
      - 480p
      - 720p
      - 1080p
    ratios:
      - 21:9
      - 16:9
      - 4:3
      - 1:1
      - 3:4
      - 9:16
      - adaptive
    duration_seconds: 4-15 or -1
  - id: doubao-seedance-2-0-fast-260128
    name: Seedance 2.0 Fast
    family: Seedance 2.0
    scenes:
      - text_to_video
      - image_to_video_first_frame
      - image_to_video_first_last_frame
      - multimodal_reference
      - video_edit
      - video_extend
    input_modalities:
      text: true
      image: true
      video: true
      audio: true
      draft_task: false
    output_audio: true
    web_search: true
    draft_mode: false
    service_tiers:
      - default
    resolutions:
      - 480p
      - 720p
    ratios:
      - 21:9
      - 16:9
      - 4:3
      - 1:1
      - 3:4
      - 9:16
      - adaptive
    duration_seconds: 4-15 or -1
  - id: doubao-seedance-1-5-pro-251215
    name: Seedance 1.5 Pro
    family: Seedance 1.5
    scenes:
      - text_to_video
      - image_to_video_first_frame
      - image_to_video_first_last_frame
    input_modalities:
      text: true
      image: true
      video: false
      audio: false
      draft_task: true
    output_audio: true
    web_search: false
    draft_mode: true
    service_tiers:
      - default
      - flex
    resolutions:
      - 480p
      - 720p
      - 1080p
    ratios:
      - 21:9
      - 16:9
      - 4:3
      - 1:1
      - 3:4
      - 9:16
      - adaptive
    duration_seconds: 4-12 or -1
  - id: doubao-seedance-1-0-pro-250528
    name: Seedance 1.0 Pro
    family: Seedance 1.0
    scenes:
      - text_to_video
      - image_to_video_first_frame
      - image_to_video_first_last_frame
    input_modalities:
      text: true
      image: true
      video: false
      audio: false
      draft_task: false
    output_audio: false
    web_search: false
    draft_mode: false
    service_tiers:
      - default
      - flex
    resolutions:
      - 480p
      - 720p
      - 1080p
    ratios:
      - 21:9
      - 16:9
      - 4:3
      - 1:1
      - 3:4
      - 9:16
      - adaptive_for_image_to_video_only
    duration_seconds: 2-12
  - id: doubao-seedance-1-0-pro-fast-251015
    name: Seedance 1.0 Pro Fast
    family: Seedance 1.0
    scenes:
      - text_to_video
      - image_to_video_first_frame
    input_modalities:
      text: true
      image: true
      video: false
      audio: false
      draft_task: false
    output_audio: false
    web_search: false
    draft_mode: false
    service_tiers:
      - default
      - flex
    resolutions:
      - 480p
      - 720p
      - 1080p
    ratios:
      - 21:9
      - 16:9
      - 4:3
      - 1:1
      - 3:4
      - 9:16
      - adaptive_for_image_to_video_only
    duration_seconds: 2-12
request_body_shape:
  type: object
  required:
    - model
    - content
  properties:
    model: string
    content: array
    resolution: string
    ratio: string
    duration: integer
    frames: integer
    seed: integer
    camera_fixed: boolean
    watermark: boolean
    return_last_frame: boolean
    service_tier: string
    execution_expires_after: integer
    generate_audio: boolean
    draft: boolean
    tools: array
    safety_identifier: string
query_response_keys:
  - id
  - model
  - status
  - error
  - content.video_url
  - created_at
  - updated_at
  - seed
  - resolution
  - ratio
  - duration
  - framespersecond
  - service_tier
  - execution_expires_after
  - generate_audio
  - draft
  - usage.completion_tokens
  - usage.total_tokens
```

## 1. 平台概览

| 项目 | 值 |
|---|---|
| 平台名 | 火山 |
| Base URL | `https://ark.cn-beijing.volces.com/api/v3` |
| 鉴权 | `Authorization: Bearer <huoshan_api_key>` |
| 接口风格 | 原生 HTTP JSON 异步任务接口 |
| 创建任务 | `POST /contents/generations/tasks` |
| 查询任务 | `GET /contents/generations/tasks/{id}` |
| 任务 ID 前缀 | `cgt-` |
| 任务结果形式 | 先返回任务 ID，成功后在查询接口返回 `content.video_url` |

说明：

- 视频生成是异步任务接口，不会在创建接口同步返回最终视频。
- 创建成功后通常只拿到任务 ID，必须继续轮询查询接口。
- 查询接口仅支持查询最近 7 天的历史任务。
- 成功返回的视频 URL 有效期为 24 小时，建议及时转存。

## 2. Seedance 全量模型清单

| 模型 ID | 模型名 | 支持场景 | 是否支持有声视频 | 是否支持视频/音频输入 | 是否支持联网搜索 | 是否支持 Draft | 服务等级 |
|---|---|---|---|---|---|---|---|
| `doubao-seedance-2-0-260128` | Seedance 2.0 | 文生视频、首帧图生视频、首尾帧图生视频、多模态参考、编辑视频、延长视频 | 是 | 是 | 是 | 否 | `default` |
| `doubao-seedance-2-0-fast-260128` | Seedance 2.0 Fast | 文生视频、首帧图生视频、首尾帧图生视频、多模态参考、编辑视频、延长视频 | 是 | 是 | 是 | 否 | `default` |
| `doubao-seedance-1-5-pro-251215` | Seedance 1.5 Pro | 文生视频、首帧图生视频、首尾帧图生视频 | 是 | 否 | 否 | 是 | `default`、`flex` |
| `doubao-seedance-1-0-pro-250528` | Seedance 1.0 Pro | 文生视频、首帧图生视频、首尾帧图生视频 | 否 | 否 | 否 | 否 | `default`、`flex` |
| `doubao-seedance-1-0-pro-fast-251015` | Seedance 1.0 Pro Fast | 文生视频、首帧图生视频 | 否 | 否 | 否 | 否 | `default`、`flex` |

补充差异：

- Seedance 2.0 与 Seedance 2.0 Fast 的能力集合相同，但 Fast 不支持 `1080p`。
- Seedance 1.5 Pro 支持 `generate_audio` 和 `draft`，但不支持视频输入、音频输入和联网搜索。
- Seedance 1.0 Pro Fast 不支持首尾帧模式。
- `ratio=adaptive`：Seedance 2.0 系列与 Seedance 1.5 Pro 在更多场景可用；Seedance 1.0 系列仅图生视频场景支持。

## 3. 通用鉴权

所有请求统一使用：

```http
Authorization: Bearer <huoshan_api_key>
Content-Type: application/json
Accept: application/json
```

## 4. 创建视频生成任务

### 4.1 端点

```http
POST https://ark.cn-beijing.volces.com/api/v3/contents/generations/tasks
Authorization: Bearer <huoshan_api_key>
Content-Type: application/json
Accept: application/json
```

### 4.2 请求体骨架

```json
{
  "model": "doubao-seedance-2-0-260128",
  "content": [
    {
      "type": "text",
      "text": "A cinematic shot of a cat walking in the rain."
    }
  ],
  "resolution": "720p",
  "ratio": "16:9",
  "duration": 5,
  "watermark": false
}
```

### 4.3 核心字段说明

| 字段 | 类型 | 必填 | 适用模型 | 说明 |
|---|---|---|---|---|
| `model` | string | 是 | 全部 | 目标模型 ID。 |
| `content` | array | 是 | 全部 | 输入内容数组，至少包含 1 个内容项。 |
| `resolution` | string | 否 | 全部 | `480p`、`720p`、`1080p`。Seedance 2.0 Fast 不支持 `1080p`。 |
| `ratio` | string | 否 | 全部 | `21:9`、`16:9`、`4:3`、`1:1`、`3:4`、`9:16`、`adaptive`。 |
| `duration` | integer | 否 | 全部 | 整数秒；与 `frames` 二选一，`frames` 优先级更高。 |
| `frames` | integer | 否 | 仅 Seedance 1.0 系列 | 用帧数控制时长；Seedance 2.0 系列和 1.5 Pro 暂不支持。 |
| `seed` | integer | 否 | 全部 | `-1` 到 `2^32-1`。`-1` 表示随机种子。 |
| `camera_fixed` | boolean | 否 | 非 Seedance 2.0 参考图场景 | 固定镜头。Seedance 2.0 系列暂不支持。 |
| `watermark` | boolean | 否 | 全部 | 是否添加 AI 水印。 |
| `return_last_frame` | boolean | 否 | 支持尾帧返回的场景 | 为 `true` 时可在查询接口拿到尾帧信息。 |
| `service_tier` | string | 否 | 全部 | `default` 或 `flex`。Seedance 2.0 系列不支持 `flex`。 |
| `execution_expires_after` | integer | 否 | 全部 | 任务超时阈值，单位秒，范围 `[3600, 259200]`。默认 `172800`。 |
| `generate_audio` | boolean | 否 | Seedance 2.0 系列、Seedance 1.5 Pro | 是否生成同步音频。 |
| `draft` | boolean | 否 | 仅 Seedance 1.5 Pro | 是否生成 Draft 样片。 |
| `tools` | array | 否 | 仅 Seedance 2.0 系列 | 当前仅公开 `web_search`。 |
| `safety_identifier` | string | 否 | 全部 | 终端用户唯一标识，长度不超过 64。 |

### 4.4 content 内容项类型

#### 文本

```json
{
  "type": "text",
  "text": "描述期望生成的视频内容"
}
```

说明：

- 全模型支持中英文提示词。
- Seedance 2.0 与 Seedance 2.0 Fast 额外支持日语、印尼语、西班牙语、葡萄牙语。
- 建议中文提示词不超过 500 字，英文提示词不超过 1000 词。

#### 图片

```json
{
  "type": "image_url",
  "image_url": {
    "url": "https://example.com/first-frame.png"
  },
  "role": "first_frame"
}
```

`image_url.url` 支持三种格式：

- 公网 URL
- Base64：`data:image/<format>;base64,<data>`
- 素材 ID：`asset://<ASSET_ID>`

图片约束：

- 格式：`jpeg`、`png`、`webp`、`bmp`、`tiff`、`gif`
- Seedance 1.5 Pro 和 Seedance 2.0 系列额外支持 `heic`、`heif`
- 单图大小小于 30 MB
- 请求体总大小不超过 64 MB

图片角色：

- `first_frame`：首帧图生视频
- `last_frame`：尾帧图生视频
- `reference_image`：Seedance 2.0 系列多模态参考图

#### 视频

```json
{
  "type": "video_url",
  "video_url": {
    "url": "https://example.com/reference.mp4"
  },
  "role": "reference_video"
}
```

说明：

- 仅 Seedance 2.0 系列支持输入视频。
- `video_url.url` 支持公网 URL 或素材 ID `asset://<ASSET_ID>`。
- 支持 `mp4`、`mov`。
- 单个视频时长 `[2, 15]` 秒。
- 最多传入 3 个参考视频，且总时长不超过 15 秒。
- 单个视频不超过 50 MB。

#### 音频

```json
{
  "type": "audio_url",
  "audio_url": {
    "url": "https://example.com/reference.mp3"
  },
  "role": "reference_audio"
}
```

说明：

- 仅 Seedance 2.0 系列支持输入音频。
- 不允许纯音频输入，也不允许仅“文本 + 音频”。
- `audio_url.url` 支持公网 URL、Base64、素材 ID `asset://<ASSET_ID>`。
- 支持 `wav`、`mp3`。
- 单段音频时长 `[2, 15]` 秒。
- 最多传入 3 段，且总时长不超过 15 秒。

#### Draft 任务引用

```json
{
  "type": "draft_task",
  "draft_task": {
    "id": "cgt-2026xxxxxxxxxxxx"
  }
}
```

说明：

- 仅 Seedance 1.5 Pro 支持。
- 适用于先生成 Draft 样片，再基于样片生成正式视频。

### 4.5 组合规则

火山官方文档可确认的组合规则如下：

- 支持 `text`
- 支持 `text(可选) + image`
- 支持 `text(可选) + video`，仅限 Seedance 2.0 系列
- 支持 `text(可选) + image + audio`，仅限 Seedance 2.0 系列
- 支持 `text(可选) + image + video`，仅限 Seedance 2.0 系列
- 支持 `text(可选) + video + audio`，仅限 Seedance 2.0 系列
- 支持 `text(可选) + image + video + audio`，仅限 Seedance 2.0 系列
- 支持 `draft_task`，仅限 Seedance 1.5 Pro
- 不支持纯音频
- 不支持 `text + audio`
- 首帧图生、首尾帧图生、多模态参考三类场景互斥，不可混用

### 4.6 关键参数差异

| 参数 | Seedance 2.0 / 2.0 Fast | Seedance 1.5 Pro | Seedance 1.0 Pro | Seedance 1.0 Pro Fast |
|---|---|---|---|---|
| `resolution` 默认值 | `720p` | `720p` | `1080p` | `1080p` |
| `1080p` 支持 | 2.0 支持，2.0 Fast 不支持 | 支持 | 支持 | 支持 |
| `ratio=adaptive` | 支持 | 支持 | 仅图生视频支持 | 仅图生视频支持 |
| `duration` | `4-15` 或 `-1` | `4-12` 或 `-1` | `2-12` | `2-12` |
| `frames` | 不支持 | 不支持 | 支持 | 支持 |
| `generate_audio` | 支持 | 支持 | 不支持 | 不支持 |
| `draft` | 不支持 | 支持 | 不支持 | 不支持 |
| `tools.web_search` | 支持 | 不支持 | 不支持 | 不支持 |
| `service_tier=flex` | 不支持 | 支持 | 支持 | 支持 |

### 4.7 最小文生视频示例

```json
{
  "model": "doubao-seedance-1-0-pro-250528",
  "content": [
    {
      "type": "text",
      "text": "A slow cinematic shot of waves hitting black volcanic rocks at sunset."
    }
  ],
  "resolution": "1080p",
  "ratio": "16:9",
  "duration": 5,
  "watermark": false
}
```

### 4.8 Seedance 2.0 多模态参考示例

```json
{
  "model": "doubao-seedance-2-0-260128",
  "content": [
    {
      "type": "text",
      "text": "全程参考视频1的运镜和节奏，保留图片1的人物造型，并使用音频1作为背景音乐，生成一段 8 秒广告视频。"
    },
    {
      "type": "image_url",
      "image_url": {
        "url": "https://example.com/ref-image.png"
      },
      "role": "reference_image"
    },
    {
      "type": "video_url",
      "video_url": {
        "url": "https://example.com/ref-video.mp4"
      },
      "role": "reference_video"
    },
    {
      "type": "audio_url",
      "audio_url": {
        "url": "https://example.com/ref-audio.mp3"
      },
      "role": "reference_audio"
    }
  ],
  "generate_audio": true,
  "ratio": "16:9",
  "duration": 8,
  "watermark": true
}
```

### 4.9 Seedance 2.0 联网搜索示例

```json
{
  "model": "doubao-seedance-2-0-260128",
  "content": [
    {
      "type": "text",
      "text": "微距镜头拍摄玻璃蛙，联网搜索其外观特征并生成纪录片风格视频。"
    }
  ],
  "generate_audio": true,
  "ratio": "adaptive",
  "duration": 11,
  "watermark": true,
  "tools": [
    {
      "type": "web_search"
    }
  ]
}
```

## 5. 查询视频生成任务

### 5.1 端点

```http
GET https://ark.cn-beijing.volces.com/api/v3/contents/generations/tasks/{id}
Authorization: Bearer <huoshan_api_key>
Accept: application/json
```

### 5.2 任务状态

| 状态 | 含义 |
|---|---|
| `queued` | 排队中 |
| `running` | 运行中 |
| `cancelled` | 已取消，仅排队中任务可取消 |
| `succeeded` | 成功 |
| `failed` | 失败 |
| `expired` | 超时 |

### 5.3 查询响应示例

```json
{
  "id": "cgt-2025******-****",
  "model": "doubao-seedance-1-5-pro-251215",
  "status": "succeeded",
  "content": {
    "video_url": "https://ark-content-generation-cn-beijing.tos-cn-beijing.volces.com/xxx"
  },
  "usage": {
    "completion_tokens": 108900,
    "total_tokens": 108900
  },
  "created_at": 1743414619,
  "updated_at": 1743414673,
  "seed": 10,
  "resolution": "720p",
  "ratio": "16:9",
  "duration": 5,
  "framespersecond": 24,
  "service_tier": "default",
  "execution_expires_after": 172800,
  "generate_audio": true,
  "draft": false
}
```

### 5.4 查询接口关键信息

| 字段 | 说明 |
|---|---|
| `id` | 任务 ID |
| `model` | 实际执行的模型 ID |
| `status` | 当前任务状态 |
| `error.code` / `error.message` | 失败时的错误信息 |
| `content.video_url` | 成功后的视频 URL |
| `created_at` / `updated_at` | 任务创建和最后更新时间 |
| `usage.completion_tokens` | 可用于计费对账的输出 token |
| `usage.tool_usage.web_search` | 联网搜索实际调用次数，仅启用工具时返回 |

## 6. 推荐接入流程

```text
1. 使用 API Key 调用 POST /contents/generations/tasks
2. 取得返回的任务 ID，例如 cgt-xxxx
3. 轮询 GET /contents/generations/tasks/{id}
4. 当 status=succeeded 时读取 content.video_url
5. 立即将视频转存到自有存储，避免 24 小时后失效
```

## 7. 接入注意事项

- Seedance 2.0 系列不支持 `service_tier=flex`，只能在线推理。
- Seedance 2.0 Fast 不支持 `1080p`。
- Seedance 2.0 系列和 Seedance 1.5 Pro 支持 `duration=-1`，表示由模型自动选择时长。
- Seedance 1.5 Pro 开启 `draft=true` 时，将固定使用 `480p`，且不支持尾帧返回和离线推理。
- 若传入参数与所选模型不兼容，火山可能忽略该参数，或直接返回错误。
- 大文件不建议走 Base64，优先使用公网 URL 或 `asset://<ASSET_ID>`。
- 含人脸素材、虚拟人像、模型原始产物再编辑等高级能力，当前主要面向 Seedance 2.0 系列。

## 8. 适合 AI 代理的最小规则

```yaml
agent_rules:
  always_poll_after_create: true
  success_condition: status == succeeded and content.video_url exists
  failure_condition: status in [failed, expired, cancelled]
  use_generate_audio_only_for:
    - doubao-seedance-2-0-260128
    - doubao-seedance-2-0-fast-260128
    - doubao-seedance-1-5-pro-251215
  use_web_search_only_for:
    - doubao-seedance-2-0-260128
    - doubao-seedance-2-0-fast-260128
  use_draft_only_for:
    - doubao-seedance-1-5-pro-251215
  use_video_or_audio_input_only_for:
    - doubao-seedance-2-0-260128
    - doubao-seedance-2-0-fast-260128
  if_model_is_fast_2_0:
    forbid_resolution:
      - 1080p
  if_model_is_1_0_pro_fast:
    forbid_scene:
      - image_to_video_first_last_frame
```