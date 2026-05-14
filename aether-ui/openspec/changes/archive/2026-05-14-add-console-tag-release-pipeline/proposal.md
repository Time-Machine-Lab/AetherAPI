## Why

`aether-console` 目前只有本地构建与测试脚本，仓库内可见的 GitHub Actions 仅负责 Discord 通知，缺少面向版本发布的可追溯部署流水线。前端统一规范已要求独立构建、发布前质量门禁和通过版本标记触发生产发布，因此需要为 `aether-console` 补齐 tag 触发的 Docker + Nginx 发布路径。

## What Changes

- 新增 `aether-console` 发布流水线能力，使用 GitHub Actions 在发布 tag 时触发。
- 流水线 MUST 在构建镜像前执行前端最小质量门禁：依赖安装、`lint`、`format:check`、`type-check`、`test`、`build`。
- 发布产物 MUST 使用 Docker 多阶段构建：Node/pnpm 阶段构建 Vite 静态资源，Nginx 阶段托管 `dist`。
- 运行容器 MUST 对外暴露前端端口 `8888`，并由 Nginx 将前端 API 请求反向代理到后端 `8090`。
- 运行时 API 基地址 MUST 保持前端规范要求的环境配置边界，默认让浏览器访问同源 `/api/v1`，由 Nginx 负责转发到后端服务。
- 新增发布配置的必要文档或示例，说明 tag 命名、镜像标签、部署参数、必需 secrets、端口约定和回滚方式。
- 本次变更不修改 `aether-console` 页面、路由、组件、视觉设计，不新增或修改 `../docs/api/*.yaml` 接口契约，也不修改后端业务逻辑。

## Capabilities

### New Capabilities

- `console-tag-release-pipeline`: 定义 `aether-console` 通过 tag 触发的发布流水线、Docker 镜像构建、Nginx 静态部署与后端反向代理端口约定。

### Modified Capabilities

- 无。

## Impact

- 受影响应用：`aether-console`
- 预期新增或调整的工程文件：根目录 `.github/workflows/` 下的发布 workflow，`aether-ui/aether-console` 下的 Dockerfile、Nginx 配置、`.dockerignore`、发布说明或环境示例。
- 依赖的既有规范：`../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 中的 Vue 3 + TypeScript + Vite 技术栈、环境变量规范、最小 CI 流水线和版本标记发布原则。
- 依赖的既有应用配置：`aether-console/package.json` 中的 `pnpm` 脚本、`src/utils/env.ts` 中默认 `VITE_API_BASE_URL=/api/v1`、`vite.config.ts` 中当前本地开发代理配置。
- 运行时部署依赖：后端服务在目标部署网络中必须可被 Nginx 以 `8090` 端口访问；当前后端配置文件可见默认端口为 `8080`，因此后端改端口或服务映射不属于本前端提案范围，需由部署环境或后端发布流程保证。
- 当前 `aether-ui/openspec/project.md` 缺失，本提案以 `aether-ui/openspec/config.yaml`、前端统一规范、`aether-console/DESIGN.md`、现有代码结构和仓库可见 GitHub Actions 现状为依据。
