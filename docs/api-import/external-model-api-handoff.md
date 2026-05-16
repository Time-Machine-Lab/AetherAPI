# 外部平台原生 API 调用说明

本文按当前接入代码整理外部平台自身的调用方式、鉴权、原生入参、轮询和响应读取方式。文档只说明供应商 API，不定义 API 中台的抽象字段、路由、响应结构或业务对象。

真实 API Key 不在本文档中提供，均以 `<api_key>` 表示。

## 1. 外部平台清单

| 平台 | 能力 | Base URL | 鉴权 |
|---|---|---|---|
| Wuyin | GPT Image 2.0、Grok Imagine 图片、NanoBanana Pro、NanoBanana2、Grok Imagine 视频、Sora 2 视频 | `https://api.wuyinkeji.com` | URL query `key=<api_key>`，同时 Header `Authorization: <api_key>` |
| BLTCY | Jimeng Seedream 5.0、Midjourney v7 | `https://api.bltcy.top` | Header `Authorization: Bearer <api_key>` |
| ModelMaster | SeedVR2 图片增强 | `http://101.126.81.73:6008` | 当前接口无鉴权 |
| ABLAI | Veo 3.1 Fast 视频 | `https://api.ablai.top` | Header `Authorization: Bearer <api_key>` |
| MiniMax / QNAIGC 等 OpenAI-compatible 网关 | 文本、视觉理解 | 见各模型配置 | Header `Authorization: Bearer <api_key>` |
| Qiniu AIToken Search | Web / Image Search | `https://api.qnaigc.com/v1/search/web` | Header `Authorization: Bearer <api_key>` |

## 2. Wuyin 异步图片 API

Wuyin 图片接口均为异步任务。提交接口不同，轮询接口相同。

### 2.1 通用鉴权与轮询

提交请求均使用：

```http
POST {base_url}{submit_endpoint}?key=<api_key>
Authorization: <api_key>
Content-Type: application/json
Accept: application/json
```

轮询请求：

```http
GET {base_url}/api/async/detail?id=<task_id>&key=<api_key>
Authorization: <api_key>
Accept: application/json
```

任务 ID 可能出现在以下字段：

- `data.id`
- `data.task_id`
- `data.taskId`
- `id`
- `task_id`
- `taskId`

状态读取字段：

- `data.status`
- `status`
- `data.state`
- `state`

图片任务状态含义：

| 原始状态 | 含义 |
|---|---|
| `2`、`success`、`succeed`、`succeeded`、`completed`、`complete`、`done`、`finish`、`finished` | 成功 |
| `3`、`fail`、`failed`、`failure`、`error`、`errored`、`canceled`、`cancelled` | 失败 |
| 其他值 | 等待或处理中，继续轮询 |

成功后图片 URL 可能出现在：

- `data.result`
- `data.results`
- `data.output`
- `data.outputs`
- `data.output_url`
- `data.output_urls`
- `data.image_url`
- `data.imageUrl`
- `data.url`
- 根级同名字段

如果返回体包含 `code`，则 `0`、`200` 或空值表示非错误；其他值按供应商错误处理。

### 2.2 GPT Image 2.0

```yaml
base_url: https://api.wuyinkeji.com
submit_endpoint: /api/async/image_gpt
model_name: gpt-image-2
request_timeout_sec: 60
poll_interval_sec: 5
poll_timeout_sec: 600
```

原生请求体：

```json
{
  "prompt": "string",
  "size": "auto",
  "urls": ["https://example.com/input.png"]
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `prompt` | 是 | 生成提示词。 |
| `size` | 否 | 画面比例。支持 `auto`,`1:1`,`3:2`,`2:3`,`16:9`,`9:16`,`4:3`,`3:4`,`21:9`,`9:21`,`1:3`,`3:1`,`2:1`,`1:2`。默认 `auto`。 |
| `urls` | 否 | 参考图 URL 数组。传该字段为图生图；不传为文生图。 |

文生图最小示例：

```json
{
  "prompt": "a product photo of a red backpack, studio lighting",
  "size": "1:1"
}
```

图生图示例：

```json
{
  "prompt": "keep the product shape, change the background to a clean office desk",
  "size": "16:9",
  "urls": ["https://example.com/source.jpg"]
}
```

### 2.3 Grok Imagine 图片

```yaml
base_url: https://api.wuyinkeji.com
submit_endpoint: /api/async/image_grok_imagine
model_name: grok_imagine
request_timeout_sec: 60
poll_interval_sec: 5
poll_timeout_sec: 600
```

原生请求体：

```json
{
  "prompt": "string",
  "aspect_ratio": "2:3",
  "image_urls": ["https://example.com/input.png"]
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `prompt` | 是 | 生成提示词。 |
| `aspect_ratio` | 否 | 支持 `2:3`,`3:2`,`1:1`,`16:9`,`9:16`。默认 `2:3`。 |
| `image_urls` | 否 | 参考图 URL 数组。传该字段为图生图；不传为文生图。 |

示例：

```json
{
  "prompt": "cinematic realistic portrait, soft sunlight, shallow depth of field",
  "aspect_ratio": "9:16"
}
```

### 2.4 NanoBanana Pro

```yaml
base_url: https://api.wuyinkeji.com
submit_endpoint: /api/async/image_nanoBanana_pro
model_name: nano-banana-pro
request_timeout_sec: 60
poll_interval_sec: 5
poll_timeout_sec: 600
```

原生请求体：

```json
{
  "prompt": "string",
  "aspectRatio": "auto",
  "size": "2K",
  "urls": ["https://example.com/input.png"]
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `prompt` | 是 | 生成提示词。 |
| `aspectRatio` | 否 | 支持 `auto`,`1:1`,`16:9`,`9:16`,`4:3`,`3:4`,`3:2`,`2:3`,`5:4`,`4:5`,`21:9`。默认 `auto`。 |
| `size` | 否 | 输出规格。支持 `1K`,`2K`,`4K`。默认 `2K`。 |
| `urls` | 否 | 参考图 URL 数组。传该字段为图生图；不传为文生图。 |

示例：

```json
{
  "prompt": "a premium skincare product poster with clear Chinese typography",
  "aspectRatio": "4:5",
  "size": "4K"
}
```

### 2.5 NanoBanana2

```yaml
base_url: https://api.wuyinkeji.com
submit_endpoint: /api/async/image_nanoBanana2
model_name: nano-banana2
request_timeout_sec: 60
poll_interval_sec: 5
poll_timeout_sec: 600
```

原生请求体同 NanoBanana Pro：

```json
{
  "prompt": "string",
  "aspectRatio": "auto",
  "size": "2K",
  "urls": ["https://example.com/input.png"]
}
```

字段取值同 NanoBanana Pro。

## 3. BLTCY 图片 API

### 3.1 Jimeng Seedream 5.0

```yaml
base_url: https://api.bltcy.top
endpoint: /v1/images/generations
model: doubao-seedream-5-0-260128
request_timeout_sec: 600
```

请求：

```http
POST /v1/images/generations
Authorization: Bearer <api_key>
Content-Type: application/json
```

原生请求体：

```json
{
  "model": "doubao-seedream-5-0-260128",
  "prompt": "string",
  "response_format": "url",
  "size": "2048x2048",
  "n": 1,
  "watermark": false,
  "image": ["data:image/jpeg;base64,..."]
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `model` | 是 | 固定 `doubao-seedream-5-0-260128`。 |
| `prompt` | 是 | 生成提示词。 |
| `response_format` | 否 | `url` 或 `b64_json`，默认 `url`。 |
| `size` | 否 | 原生尺寸字段，格式 `<width>x<height>`，也可由调用方按比例计算后传入。默认 `2048x2048`。 |
| `n` | 否 | 当前按 `1` 调用。 |
| `watermark` | 否 | 当前按 `false` 调用。 |
| `image` | 否 | 参考图 data URI 数组。传该字段为图生图；不传为文生图。 |

尺寸限制：

- 宽高范围：`64~4096`。
- 当前接入代码按供应商返回约束处理，要求像素数不低于 `3,686,400`。如果尺寸过小，需要按比例放大后再传。

常用比例换算参考：

| 规格 | `size` 示例 |
|---|---|
| `1:1` 2K | `2048x2048` |
| `16:9` 2K | `2048x1152` |
| `9:16` 2K | `1152x2048` |
| `4:3` 2K | `2048x1536` |
| `3:4` 2K | `1536x2048` |

文生图示例：

```json
{
  "model": "doubao-seedream-5-0-260128",
  "prompt": "modern Chinese tea brand poster, elegant packaging, soft daylight",
  "response_format": "url",
  "size": "2048x2048",
  "n": 1,
  "watermark": false
}
```

图生图示例：

```json
{
  "model": "doubao-seedream-5-0-260128",
  "prompt": "keep the same character, change outfit to a white business suit",
  "response_format": "url",
  "size": "2048x2048",
  "n": 1,
  "watermark": false,
  "image": ["data:image/jpeg;base64,..."]
}
```

响应读取：

- `data[].url`
- `data[].b64_json`
- `data[].revised_prompt`

### 3.2 Midjourney v7

```yaml
base_url: https://api.bltcy.top
submit_endpoint: /fast/mj/submit/imagine
poll_endpoint: /fast/mj/task/{task_id}/fetch
default_version: "7"
request_timeout_sec: 120
poll_interval_sec: 3
poll_timeout_sec: 600
```

提交：

```http
POST /fast/mj/submit/imagine
Authorization: Bearer <api_key>
Content-Type: application/json
```

原生请求体：

```json
{
  "prompt": "string --ar 1:1 --v 7",
  "base64Array": ["data:image/jpeg;base64,..."]
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `prompt` | 是 | MJ 原生 prompt。比例和版本通过 prompt 参数传，例如 `--ar 16:9 --v 7`。 |
| `base64Array` | 否 | 参考图 data URI 数组。传该字段为图生图；不传或空数组为文生图。 |

当前支持的 `--ar`：

- `3:4`
- `16:9`
- `9:16`
- `1:1`
- `4:3`

文生图示例：

```json
{
  "prompt": "high fashion editorial photo, dramatic studio light --ar 3:4 --v 7",
  "base64Array": []
}
```

图生图示例：

```json
{
  "prompt": "turn the reference into a luxury perfume advertising visual --ar 4:3 --v 7",
  "base64Array": ["data:image/jpeg;base64,..."]
}
```

提交响应任务 ID 可能出现在：

- `result` 字符串
- `result.taskId`
- `result.task_id`
- `result.id`
- `taskId`
- `task_id`
- `id`

轮询：

```http
GET /fast/mj/task/{task_id}/fetch
Authorization: Bearer <api_key>
```

任务状态：

| 原始状态 | 含义 |
|---|---|
| `SUCCESS`、`DONE` 或 `progress=100%` | 成功 |
| `FAIL`、`FAILURE`、`CANCEL`、`CANCELLED` | 失败 |
| 其他 | 继续轮询 |

失败原因可能在：

- `failReason`
- `fail_reason`
- `description`
- `message`

成功图片读取：

- `imageUrl` / `image_url`：通常为网格图。
- `imageUrls` / `image_urls`：单图数组，数组元素可能是 URL 字符串，也可能是 `{ "url": "..." }`。

## 4. ModelMaster SeedVR2 图片增强 API

```yaml
base_url: http://101.126.81.73:6008
submit_endpoint: /tasks
poll_endpoint: /tasks/{task_id}
modelname: seedvr2_image
request_timeout_sec: 600
poll_interval_ms: 5000
auth: none
```

### 4.1 提交任务

```http
POST /tasks
User-Agent: api-client/1.0
Content-Type: application/json
Accept: application/json
```

原生请求体：

```json
{
  "modelname": "seedvr2_image",
  "url": "https://example.com/source.png",
  "type": "image",
  "resolution": 1707
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `modelname` | 是 | 固定 `seedvr2_image`。 |
| `url` | 是 | 待增强图片 URL，必须可被供应商访问。 |
| `type` | 是 | 固定 `image`。 |
| `resolution` | 是 | 供应商使用的目标短边数值。 |

`resolution` 计算方式：

```text
目标长边：2K = 2560，4K = 3840
resolution = round(目标长边 * 原图短边 / 原图长边)
```

示例：原图 `1200x1800`，目标 2K，短边 `1200`，长边 `1800`：

```text
round(2560 * 1200 / 1800) = 1707
```

### 4.2 轮询任务

```http
GET /tasks/{task_id}
User-Agent: api-client/1.0
Accept: application/json
```

提交响应读取：

- `task_id`

轮询响应字段：

| 字段 | 说明 |
|---|---|
| `task_id` | 任务 ID。 |
| `status` | 任务状态。 |
| `output.url` | 成功后的图片 URL。 |
| `error` | 失败原因。 |
| `stages` | 阶段信息。 |

状态含义：

| 原始状态 | 含义 |
|---|---|
| `completed` | 成功 |
| `failed` | 失败 |
| 其他 | 继续轮询 |

## 5. Wuyin 视频 API

### 5.1 Grok Imagine 视频

```yaml
base_url: https://api.wuyinkeji.com
submit_endpoint: /api/async/video_grok_imagine
poll_endpoint: /api/async/detail
model_name: grok_imagine
request_timeout_sec: 180
poll_interval_sec: 10
poll_timeout_sec: 1200
```

提交：

```http
POST /api/async/video_grok_imagine?key=<api_key>
Authorization: <api_key>
Content-Type: application/json
Accept: application/json
```

原生请求体：

```json
{
  "prompt": "string",
  "duration": "10",
  "aspect_ratio": "2:3",
  "image_urls": ["https://example.com/first-frame.png"]
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `prompt` | 是 | 视频提示词。 |
| `duration` | 否 | 字符串形式。支持 `"6"`、`"10"`、`"15"`、`"20"`。默认 `"10"`。 |
| `aspect_ratio` | 否 | 支持 `2:3`,`3:2`,`1:1`,`16:9`,`9:16`。默认 `2:3`。 |
| `image_urls` | 否 | 首帧/参考图 URL 数组，当前按最多 1 张使用。传该字段为图生视频；不传为文生视频。 |

比例对应的实际输出规格：

| `aspect_ratio` | 规格 |
|---|---|
| `2:3` | `784x1168` |
| `3:2` | `1168x784` |
| `1:1` | `960x960` |
| `16:9` | `1280x720` |
| `9:16` | `720x1280` |

轮询：

```http
GET /api/async/detail?id=<task_id>&key=<api_key>
Authorization: <api_key>
Accept: application/json
```

状态含义：

| 原始状态 | 含义 |
|---|---|
| `0`、`waiting`、`queued`、`queue`、`pending`、`created` | 等待 |
| `1`、`processing`、`running`、`in_progress`、`generating` | 处理中 |
| `2`、`success`、`succeed`、`succeeded`、`completed`、`complete`、`done`、`finish`、`finished` | 成功 |
| `3`、`fail`、`failed`、`failure`、`error`、`errored`、`canceled`、`cancelled` | 失败 |

成功视频 URL 可能在：

- `data.result`
- `result`
- `data.video_url`
- `data.url`
- `video_url`
- `url`

### 5.2 Sora 2

```yaml
base_url: https://api.wuyinkeji.com
submit_endpoint: /api/sora2-new/submit
poll_endpoint: /api/sora2/detail
model_name: sora2-new
request_timeout_sec: 300
poll_interval_sec: 10
poll_timeout_sec: 2500
```

提交：

```http
POST /api/sora2-new/submit?key=<api_key>
Authorization: <api_key>
Content-Type: application/json
Accept: application/json
```

原生请求体：

```json
{
  "prompt": "string",
  "aspectRatio": "16:9",
  "duration": "8",
  "size": "small",
  "url": "https://example.com/first-frame.png"
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `prompt` | 是 | 视频提示词。 |
| `aspectRatio` | 否 | 支持 `16:9`、`9:16`。默认 `16:9`。 |
| `duration` | 否 | 字符串形式。支持 `"8"`、`"12"`。默认 `"8"`。 |
| `size` | 否 | 当前按 `small` 调用。 |
| `url` | 否 | 单张首帧/参考图 URL。传该字段为图生视频；不传为文生视频。 |

规格对应关系：

| `aspectRatio` | 输出规格 |
|---|---|
| `16:9` | `1280x720` |
| `9:16` | `720x1280` |

轮询：

```http
GET /api/sora2/detail?id=<task_id>&key=<api_key>
Authorization: <api_key>
Accept: application/json
```

状态含义：

| 原始状态 | 含义 |
|---|---|
| `0`、`waiting`、`queued`、`queue`、`pending`、`created` | 等待 |
| `3`、`processing`、`running`、`in_progress`、`generating` | 处理中 |
| `1`、`success`、`succeed`、`succeeded`、`completed`、`complete`、`done`、`finish`、`finished` | 成功 |
| `2`、`fail`、`failed`、`failure`、`error`、`errored`、`canceled`、`cancelled` | 失败 |

成功视频 URL 优先读取：

- `data.remote_url`
- `data.video_url`
- `data.url`
- `remote_url`
- `video_url`

## 6. ABLAI Veo 3.1 Fast 视频 API

```yaml
base_url: https://api.ablai.top
submit_endpoint: /v2/videos/generations
poll_endpoint: /v2/videos/generations/{task_id}
model: veo3.1-fast
request_timeout_sec: 180
poll_interval_sec: 10
poll_timeout_sec: 900
```

### 6.1 提交任务

```http
POST /v2/videos/generations
Authorization: Bearer <api_key>
Content-Type: application/json
Accept: application/json
```

原生请求体：

```json
{
  "prompt": "string",
  "model": "veo3.1-fast",
  "aspect_ratio": "16:9",
  "enhance_prompt": true,
  "images": ["https://example.com/first-frame.png"]
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `prompt` | 是 | 视频提示词。 |
| `model` | 是 | 固定 `veo3.1-fast`。 |
| `aspect_ratio` | 是 | 当前固定 `16:9`。 |
| `enhance_prompt` | 否 | 当前按 `true` 调用。 |
| `images` | 否 | 首帧/参考图 URL 数组，当前按最多 1 张使用。传该字段为图生视频；不传为文生视频。 |

当前规格固定为：

- 时长：8 秒
- 比例：`16:9`
- 输出规格：`1280x720`

提交响应任务 ID 可能在：

- `task_id`
- `taskId`
- `id`

### 6.2 轮询任务

```http
GET /v2/videos/generations/{task_id}
Authorization: Bearer <api_key>
Accept: application/json
```

状态含义：

| 原始状态 | 含义 |
|---|---|
| `SUCCESS` | 成功 |
| `FAILURE`、`CANCELLED`、`REJECTED` | 失败 |
| 其他 | 继续轮询 |

失败原因可能在：

- `fail_reason`
- `message`
- `detail`
- `error`

成功视频 URL 读取：

- `data.output` 为字符串
- `data.output` 为字符串数组时取第一个
- `data.output` 为对象时读取 `url` 或 `video_url`
- `data.output` 为对象数组时读取第一个对象的 `url` 或 `video_url`

## 7. OpenAI-compatible 文本与视觉模型

这些模型按 OpenAI Chat Completions 兼容协议调用。

### 7.1 模型列表

| 用途 | Base URL | Model |
|---|---|---|
| 文本 | `https://api.minimaxi.com/v1` | `MiniMax-M2.7-highspeed` |
| 文本 | `https://api.qnaigc.com/v1` | `deepseek/deepseek-v4-flash` |
| 文本，当前未启用 | `https://117.72.211.46/v1` | `gpt-5.5-mini` |
| 视觉 | `https://api.qnaigc.com/v1` | `doubao-seed-1.6-flash` |
| 视觉 | `https://api.qnaigc.com/v1` | `moonshotai/kimi-k2.5` |

### 7.2 文本调用

```http
POST {base_url}/chat/completions
Authorization: Bearer <api_key>
Content-Type: application/json
```

原生请求体示例：

```json
{
  "model": "MiniMax-M2.7-highspeed",
  "messages": [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "Write a short product description."}
  ],
  "stream": true
}
```

### 7.3 视觉调用

```http
POST {base_url}/chat/completions
Authorization: Bearer <api_key>
Content-Type: application/json
```

原生请求体示例：

```json
{
  "model": "doubao-seed-1.6-flash",
  "messages": [
    {
      "role": "user",
      "content": [
        {"type": "text", "text": "请识别图片内容，并返回 JSON。"},
        {"type": "image_url", "image_url": {"url": "https://example.com/image.png"}}
      ]
    }
  ],
  "stream": false,
  "response_format": {"type": "json_object"},
  "max_tokens": 4096
}
```

当前视觉调用使用非流式请求，并附加：

```json
{
  "response_format": {"type": "json_object"}
}
```

## 8. Qiniu AIToken Search API

```yaml
base_url: https://api.qnaigc.com/v1/search/web
request_timeout_sec: 20
```

请求：

```http
POST /v1/search/web
Authorization: Bearer <api_key>
Content-Type: application/json
```

原生请求体：

```json
{
  "query": "string",
  "search_type": "web",
  "max_results": 5,
  "time_filter": "month"
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `query` | 是 | 搜索词。 |
| `search_type` | 是 | `web` 或 `image`。 |
| `max_results` | 是 | 返回数量。当前调用默认 `5`。 |
| `time_filter` | 否 | 时间过滤。支持 `day`,`week`,`month`,`year`；不需要时不传。 |

Web 搜索示例：

```json
{
  "query": "2026 春节营销视觉趋势",
  "search_type": "web",
  "max_results": 5,
  "time_filter": "month"
}
```

图片搜索示例：

```json
{
  "query": "minimal skincare packaging product photography",
  "search_type": "image",
  "max_results": 5
}
```

响应列表可能在：

- `data`
- `results`
- `items`
- `data.items`
- `data.results`
- `data.list`

单条结果字段兼容读取：

| 语义 | 字段 |
|---|---|
| 标题 | `title`、`name` |
| 链接 | `url`、`link`、`source_url` |
| 来源 | `source`、`site`、`domain` |
| 时间 | `date`、`published_time`、`time` |
| 摘要 | `snippet`、`summary`、`content`、`description` |
| 图片 URL | `image_url`、`thumbnail`、`image.url`、`image.image_url`、`image.src` |
| 图片尺寸 | `width`、`height`、`image.width`、`image.height` |

错误识别：

| 条件 | 含义 |
|---|---|
| HTTP `401/403`，或响应包含 `access_denied`、`invalid user`、`invalid api key`、`unauthorized` | 鉴权失败 |
| HTTP `429`，或响应包含 `rate_limit`、`too many requests` | 限流 |
| 请求超时 | 超时 |
| HTTP 非 2xx | 供应商错误 |
| 响应无法解析 | 供应商响应结构异常 |

## 9. HTTP 重试参考

当前接入代码通常将以下 HTTP 状态视为可重试：

- `408`
- `425`
- `429`
- `500`
- `502`
- `503`
- `504`

明确参数错误、鉴权失败、请求体过大、模型不可用、任务失败状态，不按可重试处理。
