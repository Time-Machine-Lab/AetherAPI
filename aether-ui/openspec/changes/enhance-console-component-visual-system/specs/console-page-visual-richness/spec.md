## ADDED Requirements

### Requirement: API 市场页面 MUST 使用丰富组件表达资产信息
API 市场页面 MUST 使用元信息、标签、图标和结构化详情组件表达资产信息，而不是只依赖普通文字和基础 Card。

#### Scenario: 用户浏览市场卡片
- **WHEN** 市场页面展示 API 资产卡片
- **THEN** 卡片 MUST 使用类型标签、方法标签、发布者元信息、发布时间元信息和分类元信息等组件化表达
- **THEN** 卡片 MUST 保持可扫读，不把所有字段堆叠为普通段落文本

#### Scenario: 用户查看市场详情面板
- **WHEN** 用户选中某个市场资产
- **THEN** 详情面板 MUST 使用字段组、元信息行、代码展示和状态标签组织详情
- **THEN** 请求示例和响应示例 MUST 使用统一代码展示组件

### Requirement: 资产管理页面 MUST 使用信息密度更高的列表和表单表达
资产管理页面 MUST 通过列表行、字段组、标签和操作区组件提升信息密度和编辑清晰度。

#### Scenario: 用户浏览资产列表
- **WHEN** 资产管理页面展示资产列表
- **THEN** 每一行 MUST 使用结构化列表行表达名称、编码、类型、状态、方法、分类、更新时间等可用信息
- **THEN** 操作按钮 MUST 与状态标签视觉分离

#### Scenario: 用户编辑资产配置
- **WHEN** 用户进入资产配置表单
- **THEN** 表单 MUST 使用字段组和增强 Label 组织基础信息、上游配置、鉴权配置、示例配置和 AI 能力配置
- **THEN** 页面 MUST 使用辅助说明解释复杂字段含义

### Requirement: 调用日志页面 MUST 使用诊断友好的组件表达
调用日志页面 MUST 使用状态标签、方法标签、状态码、耗时、错误摘要和代码展示组件提升诊断效率。

#### Scenario: 用户浏览日志列表
- **WHEN** 调用日志页面展示日志列表
- **THEN** 每条日志 MUST 使用方法标签、结果标签、状态码标签、耗时元信息和目标 API 元信息
- **THEN** 成功/失败状态 MUST 使用只读状态表达，而不是按钮样式

#### Scenario: 用户查看日志详情
- **WHEN** 用户打开日志详情
- **THEN** 详情 MUST 使用诊断分组展示基础信息、凭证信息、错误信息和 AI 扩展信息
- **THEN** 结构化字段 MUST 使用统一代码展示组件或元信息组件

### Requirement: Playground MUST 使用清晰的请求与响应展示组件
Unified Access Playground MUST 使用组件化表单、Header/Payload 展示和响应结果展示，使开发者能清楚区分输入、输出、错误和平台失败。

#### Scenario: 用户编辑 Playground 请求
- **WHEN** 用户填写 API Code、方法、API Key、Header 或请求体
- **THEN** 页面 MUST 使用字段组和辅助说明区分必要输入、可选 Header 和请求 Payload
- **THEN** JSON 输入区域 MUST 提供格式化或校验反馈

#### Scenario: 用户查看调用结果
- **WHEN** Playground 返回调用结果
- **THEN** 页面 MUST 使用状态码、Content-Type、耗时、响应 Header 和响应体等组件化区域展示结果
- **THEN** JSON 或文本响应 MUST 使用统一代码展示组件
