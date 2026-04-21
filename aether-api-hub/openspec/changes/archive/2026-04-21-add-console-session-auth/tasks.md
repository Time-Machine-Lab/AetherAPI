## 1. 顶层契约文档

- [x] 1.1 阅读并对齐 `docs/spec/Aether API HUB 后端代码开发规范文档.md`、现有架构文档与控制台登录问题边界，确认本变更仅覆盖控制台会话认证。
- [x] 1.2 使用 `tml-docs-spec-generate` 的 API 模板生成仓库根目录 `docs/api/console-auth.yaml`，并确保其与 `ConsoleAuthController.java` 保持一一映射。

## 2. 控制台认证核心能力

- [x] 2.1 实现控制台登录应用服务链路，基于后端受控的控制台登录主体配置完成凭证校验与 token 签发。
- [x] 2.2 实现控制台 bearer token 的校验与当前用户上下文解析能力，为受保护控制台接口提供统一认证入口。

## 3. Web 接口与受保护路由接入

- [x] 3.1 实现 `ConsoleAuthController` 及其 `Req / Resp` 契约，对外提供登录与当前会话查询接口。
- [x] 3.2 将控制台认证链路接入受保护控制台接口，使 `ApiCredentialController`、`ApiCallLogController` 等 `current-user` 接口在进入业务逻辑前建立 `Principal`。
- [x] 3.3 统一未登录或 token 无效时的控制台接口失败语义，避免继续落入空 `Principal` 的业务异常路径。

## 4. 验证与联调保障

- [x] 4.1 为控制台登录成功、登录失败、当前会话查询成功、当前会话查询失败补充必要测试。
- [x] 4.2 为现有 `current-user` API Key 管理与调用日志查询接口补充认证联动验证，确认它们可使用真实控制台 token 正常工作。
- [x] 4.3 验证 Unified Access 的 API Key 鉴权链路不受控制台 token 引入影响，并整理前端从 demo token 切换到真实登录接口的联调说明。
