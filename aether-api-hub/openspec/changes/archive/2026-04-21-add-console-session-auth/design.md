## Context

当前 `aether-console` 的登录页只是在前端本地写入 demo token，从未调用后端登录接口；而 `ApiCredentialController`、`ApiCallLogController` 等控制台接口已经开始依赖 `Principal` 作为当前用户上下文。与此同时，Aether API Hub 现有 `Consumer & Auth` 领域处理的是 API 调用方凭证与统一调用入口鉴权，不是控制台用户登录。

这带来一个明确断层：前端可以“看起来已登录”，但后端没有控制台登录端点、没有控制台 bearer token 签发、没有请求认证过滤链路，因此真实联调时 `current-user` 接口无法建立合法用户上下文。

现有顶层文档已经明确第一期不做完整用户体系、RBAC 或独立管理端，因此本次设计必须满足以下约束：

- 保持 `Hub-first, Gateway-light` 与一期 MVP 范围，不扩展为完整身份平台。
- 保持控制台用户认证与 Consumer/API Key 鉴权边界分离。
- 先更新仓库根目录顶层契约文档 `docs/api/console-auth.yaml`，并要求通过 `tml-docs-spec-generate` 的 API 模板生成。
- 本次不引入新的 `docs/sql/*.sql` 顶层表设计文件，避免把一期范围膨胀为用户中心建设。

## Goals / Non-Goals

**Goals:**

- 为 `aether-console` 提供最小真实后端登录能力，替代本地 demo token。
- 由后端签发可校验的控制台 bearer token，并在受保护接口请求前建立 `Principal`。
- 让现有 `current-user` 类控制台接口在不改业务语义的前提下恢复可用。
- 明确控制台登录接口契约，便于前端联调直接切换到真实登录流程。

**Non-Goals:**

- 不建设完整用户注册、找回密码、角色权限、租户管理或管理端登录体系。
- 不把 Consumer 设计成控制台用户显式可见的业务概念。
- 不修改 Unified Access 面向 API 消费者的 API Key 校验流程。
- 不在本变更内引入控制台用户数据库表、会话表或 refresh token 体系。

## Decisions

### 1. 新增独立的 `console-session-auth` 能力，而不是复用 Consumer/Auth

- 决策：控制台登录单独建模为控制台会话认证能力，对外只暴露控制台登录与当前会话接口。
- 原因：Consumer/Auth 当前语义是“API 调用主体 + API Key”，它服务于统一调用入口；如果强行复用，会混淆控制台用户与 API 消费主体边界。
- 备选方案：直接把控制台登录塞进现有 Consumer/Auth 模块。
- 不采用原因：会让 Consumer 重新暴露到控制台产品流程中，违背此前“Consumer 作为隐式概念长期存在”的约定。

### 2. 一期采用后端受控的控制台登录主体配置，而不是直接引入用户表

- 决策：本期登录校验基于后端受控的控制台登录主体配置，实现最小真实认证闭环，不新增用户相关表结构。
- 原因：当前问题的阻塞点是“没有真实登录链路”，而不是“没有完整用户中心”；配置化主体能以最小成本解决前端联调与当前用户接口可用性问题。
- 备选方案：新增 `console_user`、`console_session` 等表并同步做基础用户管理。
- 不采用原因：会显著扩大一期范围，同时引入额外 SQL 设计、迁移与管理语义，不符合当前 MVP 节奏。

### 3. 使用后端签发的 bearer token 承载控制台会话

- 决策：登录成功后返回由后端签发、可验证、带失效时间的 bearer token，同时返回当前控制台用户基础信息。
- 原因：前端当前已经具备统一注入 `Authorization: Bearer ...` 的能力，bearer token 方案可以最小改动接入现有请求链路。
- 备选方案：继续沿用前端本地 token 命名约定，后端只做字符串白名单判断；或改用服务端 session/cookie。
- 不采用原因：前者仍然不是真实登录；后者会要求额外的跨域、cookie 策略与状态存储设计，本期没有必要。

### 4. 在 Web 入口建立控制台认证过滤链路，给现有控制台接口补齐 `Principal`

- 决策：在进入控制台受保护接口前完成 token 解析、有效性校验和当前用户上下文注入，使 `Principal` 在 controller 中可直接使用。
- 原因：现有 `ApiCredentialController`、`ApiCallLogController` 已经围绕 `Principal` 组织，不应把认证规则回写到 controller、delegate 或 mapper 中。
- 备选方案：在每个 controller 中手工解析 header，或在 delegate 中重复做认证校验。
- 不采用原因：这会破坏分层边界，也会让登录认证逻辑散落在业务代码里。

### 5. 控制台认证接口本期只提供“登录 + 当前会话”

- 决策：新增一个 `ConsoleAuthController`，本期只定义登录与当前会话查询两个接口；前端退出登录继续走本地清理 token。
- 原因：当前联调最需要的是“拿到真实 token”和“刷新后恢复会话”；服务端注销、刷新 token、密码修改都不是一期必需。
- 备选方案：同时引入注销、刷新、修改密码、重置密码等完整会话接口。
- 不采用原因：会超出当前问题范围，且现有前端壳层还没有这些真实流程。

## Risks / Trade-offs

- [配置化控制台主体不支持完整多用户体系] → 通过显式保持 `console-session-auth` 独立能力边界，为后续演进到真实用户模型留下替换空间。
- [当前用户数据隔离能力有限] → 一期先解决真实登录和 `Principal` 建立问题，后续如出现多账号协作需求，再单独拆分用户域变更。
- [控制台 token 与 API Key 并存，容易概念混淆] → 在接口契约、错误语义和设计文档中明确“控制台 bearer token 只用于控制台业务接口，Unified Access 仍使用 API Key”。
- [前端登录页当前没有密码输入] → 本变更只补齐后端能力与接口契约，前端联调时再按接口契约调整表单字段与调用流程。

## Migration Plan

1. 先在仓库根目录新增 `docs/api/console-auth.yaml`，并使用 `tml-docs-spec-generate` 的 API 模板生成顶层接口契约。
2. 按顶层契约实现 `ConsoleAuthController`、控制台 token 签发与认证过滤链路。
3. 将 `current-user` 控制台接口接入统一的控制台认证上下文，不再依赖外部伪造 token。
4. 前端登录流程切换为调用后端登录接口，并使用后端返回的 bearer token 发起后续请求。
5. 若上线后需要快速回退，可临时关闭前端真实登录接入，恢复演示壳层；后端新增能力对 Unified Access 链路无侵入。

## Open Questions

- 一期控制台登录主体配置采用单账号还是少量固定账号列表，后续实现时需要结合部署配置方式最终确认。
- 是否需要在本期补充专用认证错误码，还是先复用现有统一错误返回格式，由实现阶段结合现有错误码体系确定。
