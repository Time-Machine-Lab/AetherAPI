# api-import-agent-upstream-headers Specification

## Purpose
TBD - created by archiving change add-api-asset-upstream-headers. Update Purpose after archive.
## Requirements
### Requirement: Import Agent MUST 将上游请求头建模为结构化计划字段

Import Agent MUST 使用 `assetPlans[].upstreamRequestHeaders` 表示固定上游请求头，不得将其放入 `authConfig`、自由文本备注、请求示例或元数据扩展。

#### Scenario: 抽取文档中的上游请求头

- **WHEN** 来源文档或用户指令识别出 API 调用所需的固定上游请求头
- **THEN** Import Agent 在 `assetPlans[].upstreamRequestHeaders` 中包含这些请求头

#### Scenario: 鉴权配置保持独立

- **WHEN** 文档同时识别出鉴权配置和非鉴权固定请求头
- **THEN** Import Agent 将鉴权信息存入 `authScheme` / `authConfig`，并将非鉴权请求头存入 `upstreamRequestHeaders`

#### Scenario: 缺少请求头值时需要澄清

- **WHEN** 必需的上游请求头名称已知，但值缺失或存在歧义
- **THEN** Import Agent 创建结构化澄清项，目标指向对应的 `assetPlans[].upstreamRequestHeaders` value，并在解决前保持计划不可执行

#### Scenario: 在计划边界拒绝不安全请求头

- **WHEN** planner 输出在 `assetPlans[].upstreamRequestHeaders` 中包含受保护请求头名
- **THEN** Import Agent 拒绝或降级该候选计划，并要求提供安全配置，而不是接受该不安全请求头

#### Scenario: 确认执行会写入 API 资产

- **WHEN** 用户确认包含 `upstreamRequestHeaders` 的 Import Agent 计划并启动执行
- **THEN** 执行器通过现有 API Catalog 资产创建/修订路径传递这些请求头，使保存后的资产包含相同请求头配置

#### Scenario: 公开流式摘要保持安全

- **WHEN** Import Agent 为包含上游请求头的计划发送思考事件、助手回复、执行结果或错误诊断
- **THEN** 它不会暴露敏感形态的请求头值，同时仍允许用户理解哪些请求头名称需要处理

#### Scenario: 代码实现前更新 Import Agent 权威契约

- **WHEN** 项目实现 Import Agent 上游请求头计划能力
- **THEN** 必须先更新 `docs/api/api-import-agent.yaml`，再实现后端 Import Agent 代码

