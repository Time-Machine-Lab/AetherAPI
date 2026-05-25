## ADDED Requirements

### Requirement: Unified Access MUST 转发配置的上游请求头

Unified Access MUST 在目标 API 资产解析成功后、出站请求执行前，将资产所有者配置的 `upstreamRequestHeaders` 发送给上游 API，用于普通资产调用。

#### Scenario: 转发配置请求头

- **WHEN** Unified Access 解析到带有上游请求头配置的目标 API 资产
- **THEN** 出站上游请求包含这些配置请求头

#### Scenario: 未配置请求头时保持现有行为

- **WHEN** Unified Access 解析到未配置上游请求头的目标 API 资产
- **THEN** 出站请求行为与本变更前保持一致

#### Scenario: 配置请求头不得绕过受保护名称过滤

- **WHEN** 配置的上游请求头使用 `Authorization`、`Content-Type`、`Host`、`Content-Length`、`X-Aether-*` 或 hop-by-hop header 等受保护名称
- **THEN** Unified Access 不会将该配置作为通用上游请求头转发

#### Scenario: 鉴权保持独立

- **WHEN** 目标 API 资产同时具有上游请求头和 `HEADER_TOKEN` 鉴权配置
- **THEN** Unified Access 按现有 `authScheme` / `authConfig` 行为应用鉴权头，并独立应用配置的非鉴权请求头

#### Scenario: 诊断信息脱敏请求头值

- **WHEN** Unified Access 返回上游执行失败或写入转发诊断
- **THEN** 响应和日志不会暴露看起来像 token、API key、bearer value 或 secret 的配置请求头值

### Requirement: 异步任务查询 MUST 避免意外继承提交请求头

除非资产配置显式建模异步任务查询请求头行为，否则 Unified Access MUST NOT 将 submit-request 的 `upstreamRequestHeaders` 盲目应用到异步任务查询转发。

#### Scenario: 默认不发送 submit-only 请求头到任务查询

- **WHEN** 资产为提交请求声明 `upstreamRequestHeaders`，调用方查询异步任务
- **THEN** Unified Access 默认不会自动将这些提交请求头发送到上游任务查询端点

#### Scenario: 保持现有任务查询鉴权行为

- **WHEN** 异步任务查询使用 `SAME_AS_SUBMIT` 或 `OVERRIDE` 鉴权模式
- **THEN** Unified Access 继续按文档化 async task auth 配置应用任务查询鉴权
