# 外部平台 API Key 明文清单

> 内部临时文档，请勿对外公开。

## 媒体生成

| 分组 | 覆盖接口 | 明文 API Key |
|---|---|---|
| Wuyin Media Key | GPT Image 2.0、Grok Imagine 图片、NanoBanana Pro、NanoBanana2、Grok Imagine 视频、Sora 2 视频 | `BatcRNoLHPpAt9Dj42LNDJfQio` |
| BLTCY Image / MJ Key | Jimeng Seedream 5.0、Midjourney v7 | `sk-TBNkV70KJE4sdsLNskVwuGlTiMoWo3kuFwB9qk25P65kBfcy` |
| ABLAI Veo Key | Veo 3.1 Fast 视频 | `sk-TBNkV70KJE4sdsLNskVwuGlTiMoWo3kuFwB9qk25P65kBfcy` |
| ModelMaster SeedVR2 | SeedVR2 图片增强 | 当前无 API Key |

## LLM / Vision / Search

| 分组 | 覆盖接口 | 明文 API Key |
|---|---|---|
| MiniMax Key | MiniMax M2.7 文本模型 | `sk-cp-YLks6HcFPfn526OjBT3ab_Y6MWb2n7KSvklDAv4PXdyK8uHlUMbwewFs6q7CtzfqKTzYTZnwdQ0rMiI7lrzise7NFAlAjgB9VcBnV0aIAkXGTWIp8RrQ8SI` |
| QNAIGC LLM / Vision Key | DeepSeek 文本、Doubao Vision、Doubao Magic Vision、Kimi Magic Vision | `sk-6a669def65df45a7039ffcb75f78b095f7375f0599dbc15ff787a4435033bbeb` |
| GPT-5.5 Mini Key | GPT-5.5-mini 文本模型，当前 disabled | `sk-d2de936e89f13bb2cb1592277aab50f2225a745bbf1b843316c58ccb1ad25d5a` |
| QNAIGC Search Key | Web / Image Search | `sk-14e6c2a19ad81c35cd05be13f5c8ccdf88804f5eab037190f33f525a5236ba57` |

## 备注

1. Wuyin 图片和视频当前复用同一份 key。
2. Jimeng、Midjourney、Veo 3.1 Fast 当前配置中的明文 key 相同，但接口分属 BLTCY 与 ABLAI 两个平台。
3. QNAIGC LLM / Vision Key 与 QNAIGC Search Key 是两份不同 key，不要混用。
