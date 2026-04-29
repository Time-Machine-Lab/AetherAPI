## ADDED Requirements

### Requirement: 控制台 MUST 定义展示型组件视觉规范
`aether-console` MUST 在项目级设计文档中定义展示型组件视觉规范，覆盖元信息、标签、字段标题、列表行、代码展示和状态反馈等高频模式。

#### Scenario: 开发者实现新的展示型组件
- **WHEN** 开发者新增或修改控制台展示型组件
- **THEN** 实现 MUST 能回溯到 `aether-console/DESIGN.md` 中的组件视觉规则
- **THEN** 实现 MUST 使用既有 Tailwind、设计 Token、shadcn-vue 基础组件和 lucide 图标体系

#### Scenario: 页面需要表达只读状态
- **WHEN** 页面展示状态、类型、方法、分类、调用结果等只读信息
- **THEN** 页面 MUST 使用只读 Badge/Tag/Meta 组件表达
- **THEN** 页面 MUST NOT 使用 Button 样式伪装只读信息

### Requirement: 元信息组件 MUST 支持图标化可扫读表达
控制台 MUST 提供或采用统一元信息组件，用于展示发布者、时间、分类、请求方法、认证方式、状态码、耗时等短信息。

#### Scenario: 页面展示 API 元信息
- **WHEN** 页面展示发布者、发布时间、分类、请求方法或认证方式
- **THEN** 系统 MUST 使用图标 + 文本或图标 + 标签的结构化表达
- **THEN** 图标 MUST 来源于已有图标库或已封装图标组件

#### Scenario: 元信息字段不可用
- **WHEN** 某个元信息字段缺失
- **THEN** 组件 MUST 支持省略该项或显示中性不可用状态
- **THEN** 组件 MUST NOT 推断或伪造缺失字段

### Requirement: 字段组与 Label MUST 具备清晰层次
控制台表单 MUST 通过字段组标题、辅助说明、视觉强调和必填/可选状态提升可读性。

#### Scenario: 用户查看复杂资产配置表单
- **WHEN** 表单包含多个配置区域
- **THEN** 页面 MUST 使用字段组标题或分隔结构表达不同配置区域
- **THEN** 关键字段 MUST 在 Label 附近提供辅助说明或帮助提示

#### Scenario: 字段存在必填或可选语义
- **WHEN** 表单字段存在必填、可选、推荐填写或高级配置语义
- **THEN** 系统 MUST 使用一致的文本或视觉标记表达该语义
- **THEN** 系统 MUST 不依赖占位符作为唯一说明来源

### Requirement: JSON 与代码展示 MUST 组件化
控制台 MUST 使用统一组件展示 JSON、Header、Payload、请求示例、响应示例和调用结果等代码类内容。

#### Scenario: 用户查看 JSON 示例
- **WHEN** 页面展示合法 JSON 示例或响应
- **THEN** 系统 MUST 以格式化代码块展示内容
- **THEN** 系统 MUST 提供复制能力

#### Scenario: 用户查看非法 JSON 或普通文本
- **WHEN** 内容无法解析为 JSON
- **THEN** 系统 MUST 以纯文本形式展示原始内容
- **THEN** 系统 MUST 提供非阻塞的格式化失败提示

### Requirement: 状态反馈 MUST 使用专用组件表达
控制台 MUST 为加载中、空态、错误态、不可用态和成功反馈提供统一组件或组件模式。

#### Scenario: 列表没有数据
- **WHEN** 市场、资产、日志或 API Key 列表为空
- **THEN** 页面 MUST 使用统一空态组件或空态模式
- **THEN** 空态 MUST 包含清晰标题、说明和可选行动入口

#### Scenario: 能力暂不可用
- **WHEN** 页面展示尚未实现或当前契约不支持的能力
- **THEN** 页面 MUST 使用不可用态表达
- **THEN** 页面 MUST NOT 将不可用能力呈现为可点击的真实操作
