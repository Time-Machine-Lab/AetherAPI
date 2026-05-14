## 1. 契约与边界确认

- [x] 1.1 阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md` 和 `../docs/api/platform-proxy-profile.yaml`，确认资产候选搜索接口、分层和视觉边界。
- [x] 1.2 确认本前端变更消费已有 `GET /platform/proxy-profiles/asset-binding-candidates`，不需要新增或修改 `../docs/api/`、前端统一规范或 `aether-console/DESIGN.md`。

## 2. API 层

- [x] 2.1 扩展 `src/api/platform-proxy-profile/platform-proxy-profile.dto.ts`，新增候选搜索查询 DTO、候选响应 DTO 和分页响应 DTO。
- [x] 2.2 扩展 `src/api/platform-proxy-profile/platform-proxy-profile.types.ts`，新增候选领域类型和查询类型。
- [x] 2.3 在 `platform-proxy-profile.api.ts` 新增 `listPlatformProxyAssetCandidates`，严格映射路径、查询参数和白名单响应字段。
- [x] 2.4 更新 API adapter 测试，覆盖端点路径、查询参数、分页映射和不映射敏感代理/上游配置字段。
- [x] 2.5 更新 mock handlers，提供候选搜索、分页、关键字过滤、状态过滤和绑定摘要种子数据。

## 3. Composable

- [x] 3.1 扩展 `usePlatformProxyProfiles` 依赖注入和状态，新增候选关键字、候选列表、页码、总数、加载、错误和选中候选。
- [x] 3.2 实现候选搜索、分页加载和选择候选填充 `bindingApiCode` 的函数。
- [x] 3.3 确保候选搜索失败不清空 `bindingApiCode`、`bindingProfileId` 或最后一次 `bindingResult`。
- [x] 3.4 更新 composable 测试，覆盖候选搜索成功、候选选择、分页、失败兜底和管理员门禁。

## 4. UI 与 i18n

- [x] 4.1 更新 `PlatformProxyWorkspace.vue` 资产绑定面板，保留 `apiCode` 输入框并新增候选搜索输入、搜索按钮、候选列表和分页动作。
- [x] 4.2 候选列表展示资产名称、`apiCode`、状态、发布者和当前代理绑定摘要，不展示代理主机、端口、账号或密码。
- [x] 4.3 更新 `zh-CN` 与 `en-US` 文案，覆盖候选搜索、空状态、错误、分页和选择动作。

## 5. 验证

- [x] 5.1 运行相关 Vitest：API adapter、composable、平台代理工作区相关测试。
- [x] 5.2 运行 `pnpm type-check`。
- [x] 5.3 运行 `pnpm lint`。
- [x] 5.4 运行 `openspec status --change add-platform-proxy-asset-candidate-select` 和 `openspec instructions apply --change add-platform-proxy-asset-candidate-select --json`，确认任务完成。
