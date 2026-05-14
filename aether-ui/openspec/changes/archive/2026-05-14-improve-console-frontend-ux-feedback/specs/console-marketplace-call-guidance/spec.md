## ADDED Requirements

### Requirement: 市场卡片 MUST 展示有用的 API 元信息
控制台 API 市场 MUST 使用可扫读的元信息呈现 API 资产，帮助用户在打开详情前理解资产内容。

#### Scenario: 用户查看市场资产卡片
- **WHEN** 市场网格展示已发布资产
- **THEN** 每张卡片 MUST 在字段可用时展示资产名称或编码、API 类型、发布者、发布时间、分类和请求方法
- **THEN** 元信息 MUST 使用图标化或组件化表达，而不是全部使用无差别纯文本

#### Scenario: 市场资产元信息部分不可用
- **WHEN** Discovery API 未返回某个可选元信息字段
- **THEN** 卡片 MUST 省略该元信息或展示中性的不可用状态
- **THEN** 卡片 MUST NOT 从无关数据推断不可用字段

### Requirement: 平台调用地址 MUST 可见且可复制
控制台 MUST 在用户从发现进入调用的自然路径中展示 AetherAPI 平台统一调用地址。

#### Scenario: 用户打开市场资产详情
- **WHEN** 用户选择一个带有 `apiCode` 的市场资产
- **THEN** 详情面板 MUST 基于已契约化的 Unified Access 路径 `/api/v1/access/{apiCode}` 展示可复制的平台调用地址
- **THEN** 该字段 MUST 标注为平台调用地址，而不是上游地址

#### Scenario: 用户复制调用地址
- **WHEN** 用户触发平台调用地址的复制操作
- **THEN** 浏览器允许时，系统 MUST 将展示的地址复制到剪贴板
- **THEN** 系统 MUST 展示国际化的成功或失败反馈

### Requirement: Playground MUST 接收所选资产上下文
控制台 MUST 允许用户从资产详情进入 Unified Access Playground，并在可行时预填资产上下文。

#### Scenario: 用户从资产打开 Playground
- **WHEN** 用户选择在 Playground 中试用某个市场资产
- **THEN** Playground MUST 接收或重建所选资产的 `apiCode`
- **THEN** 在 Discovery 详情字段可用时，Playground MUST 预填请求方法和请求示例

#### Scenario: 用户尚未准备 API Key
- **WHEN** Playground 调用所选资产需要 API Key
- **THEN** 系统 MUST 说明 Unified Access 使用 `X-Aether-Api-Key`
- **THEN** 系统 MUST 为已登录用户提供进入 API Key 管理的清晰路径

### Requirement: 无契约前 MUST NOT 展示可用订阅能力
在订阅 API 契约和产品流程存在之前，控制台 MUST NOT 将 API 订阅呈现为可工作的真实功能。

#### Scenario: 用户期待从市场订阅 API
- **WHEN** 市场渲染资产发现或详情操作
- **THEN** 系统 MUST NOT 展示无法通过文档化 API 完成的可用订阅操作
- **THEN** 如果展示占位入口，该入口 MUST 在视觉上禁用或明确标记为暂不可用/未来能力
