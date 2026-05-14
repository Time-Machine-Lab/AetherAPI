## ADDED Requirements

### Requirement: 控制台 MUST 提供可复用 JSON 与代码展示
控制台 MUST 为 JSON、Header、请求示例、响应示例和 Unified Access 调用结果提供统一可复用的展示模式。

#### Scenario: 用户查看合法 JSON 内容
- **WHEN** 控制台展示示例、Header、Payload 或结果中的合法 JSON 内容
- **THEN** 系统 MUST 使用易读的格式化代码块展示内容
- **THEN** 展示方式 MUST 保留可编辑输入与只读输出之间的语义差异

#### Scenario: 用户查看非法 JSON 或纯文本内容
- **WHEN** 展示内容无法解析为 JSON
- **THEN** 系统 MUST 以纯文本方式展示原始内容
- **THEN** 系统 MUST NOT 仅因为格式化失败就阻断周边工作流

### Requirement: 代码展示 MUST 支持复制
控制台 JSON/代码展示 MUST 在复制示例、Header、Payload 或响应结果有助于开发者工作流的位置提供复制能力。

#### Scenario: 用户复制展示内容
- **WHEN** 用户触发代码展示区域的复制操作
- **THEN** 浏览器剪贴板可用时，系统 MUST 复制当前展示的源内容
- **THEN** 系统 MUST 展示国际化的复制反馈

#### Scenario: 剪贴板访问失败
- **WHEN** 浏览器剪贴板不可用或复制失败
- **THEN** 系统 MUST 保持内容可见
- **THEN** 系统 MUST 展示非破坏性的失败反馈状态

### Requirement: 代码展示 MUST 避免泄露隐藏平台数据
控制台 JSON/代码展示 MUST 只渲染当前前端上下文已经可用且相关接口契约允许展示的数据。

#### Scenario: 调用日志详情缺少原始请求或响应字段
- **WHEN** 当前调用日志详情契约未提供原始请求体、响应体、上游地址或 Header
- **THEN** 代码展示 MUST NOT 伪造这些值
- **THEN** 周边 UI MUST 展示清晰的不可用或契约受限状态，而不是空代码块
