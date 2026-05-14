## Context

`aether-console` 是 Vite/Vue 3/TypeScript 前端应用，当前 `package.json` 已提供 `lint`、`format:check`、`type-check`、`test`、`build` 等工程脚本，`src/utils/env.ts` 默认将 API 基地址设为 `/api/v1`。仓库根目录已有 `.github/workflows/discord-github-notify.yml`，说明当前仓库已经使用 GitHub Actions，但尚未提供前端发布 workflow。

前端统一规范要求每个前端仓库独立构建，生产发布建议通过版本标记或人工确认步骤执行，并要求环境相关地址通过 Vite 环境变量与统一配置注入。`aether-console` 的页面视觉和交互不在本次变更范围内，`DESIGN.md` 仅作为确认无页面设计变更的权威边界。

## Goals / Non-Goals

**Goals:**

- 为 `aether-console` 增加 tag 触发的 GitHub Actions 发布流水线。
- 在发布前执行现有前端质量门禁，避免未通过 lint、format、类型检查、测试或构建的产物进入镜像。
- 使用 Docker 多阶段构建生成可部署镜像，并以 Nginx 托管 Vite 静态资源。
- 让容器对外提供 `8888` 前端端口，并通过 Nginx 将 `/api/` 请求反向代理到后端 `8090`。
- 提供可配置、可追溯、可回滚的镜像标签和部署参数。

**Non-Goals:**

- 不修改后端代码、后端默认端口配置或后端发布流程。
- 不新增或修改 `../docs/api/*.yaml` 接口契约。
- 不修改 `aether-console` 页面、路由、组件、样式、i18n 或业务 API 调用代码。
- 不引入新的前端运行时框架、E2E 平台或复杂部署编排系统。

## Decisions

### Decision 1: 使用 GitHub Actions + scoped tag 触发

流水线使用 GitHub Actions，因为仓库已经存在 `.github/workflows` 且无其他 CI 系统痕迹。触发 tag 建议采用 `aether-console-v*`，例如 `aether-console-v1.0.0`，以避免 monorepo 中后端、管理台或营销站的 tag 误触发控制台发布。

备选方案是监听所有 tag，但这会让任意版本标记触发 `aether-console` 发布，不适合当前多子项目仓库。

### Decision 2: 质量门禁先于镜像构建

workflow 在 Docker build 前执行 `pnpm install --frozen-lockfile`、`pnpm lint`、`pnpm format:check`、`pnpm type-check`、`pnpm test`、`pnpm build`。这与前端统一规范的最小流水线一致，并额外纳入现有测试脚本，确保镜像只包装已验证产物。

备选方案是在 Dockerfile 内部完成所有校验，但会降低 CI 日志可读性，也不利于区分质量失败和镜像构建失败。

### Decision 3: Docker 多阶段构建，Nginx 作为运行时

Dockerfile 使用 Node/pnpm builder 阶段构建 `dist`，runtime 阶段使用 Nginx 镜像托管静态文件。Nginx 负责 SPA fallback，并把 `/api/` 转发到可配置后端 upstream。

备选方案是使用 `vite preview` 或 Node 静态服务运行生产环境，但这不如 Nginx 简洁稳定，也不满足用户明确提出的 Nginx 部署要求。

### Decision 4: API 走同源路径，反代 upstream 可配置

前端构建默认保持 `VITE_API_BASE_URL=/api/v1`，浏览器请求同源 `/api/v1`，Nginx 将 `/api/` 转发到 `AETHER_BACKEND_UPSTREAM`，默认值建议为 `http://host.docker.internal:8090` 或部署环境注入的服务名 upstream，例如 `http://aether-api-hub:8090`。

这样可以避免在前端构建产物中写死后端主机地址，符合前端规范中“禁止硬编码环境相关地址”的要求。后端当前可见默认端口为 `8080`，因此后端服务暴露 `8090` 必须由部署环境或后端发布流程保证。

### Decision 5: 镜像默认发布到 GHCR，部署通过 SSH 参数化

镜像默认推送到 GitHub Container Registry，标签至少包含原始 git tag 和 `latest-console`。部署阶段通过 secrets 注入目标主机、SSH 凭证、容器名称、后端 upstream 等参数，在服务器上拉取镜像并重建容器。

备选方案是只构建镜像不部署，或直接在服务器上构建。前者不能完成“发布流水线”闭环，后者对服务器环境依赖更重、可追溯性更差。

## Risks / Trade-offs

- [Risk] tag 命名规则与团队现有发布习惯不一致 -> Mitigation: 在发布文档中明确默认 `aether-console-v*`，如团队已有标准可在 apply 阶段只调整 workflow trigger。
- [Risk] 后端实际仍监听 `8080` 而部署要求反代 `8090` -> Mitigation: 前端 Nginx 只配置 upstream 到 `8090`，同时在文档和任务中把“后端服务必须暴露 `8090`”列为部署前置检查。
- [Risk] SSH 直连部署依赖服务器 Docker 权限和 secrets 配置 -> Mitigation: tasks 中要求列出必需 secrets、首次部署检查和失败回滚步骤。
- [Risk] Nginx 配置里的运行时环境变量替换不生效会导致 upstream 固定 -> Mitigation: 使用 entrypoint 模板或显式生成配置，并加入容器本地配置验证任务。
- [Risk] GHCR 包权限未配置导致服务器拉取失败 -> Mitigation: 文档说明镜像包可见性、`docker login ghcr.io` 和部署 token 要求。

## Migration Plan

1. 在 `aether-console` 新增 Dockerfile、Nginx 配置模板、`.dockerignore` 和发布说明。
2. 在根目录 `.github/workflows/` 新增 tag 触发的发布 workflow。
3. 配置 GitHub repository/package 权限和部署 secrets。
4. 首次使用测试 tag 触发 workflow，确认质量门禁、镜像推送、服务器拉取、容器端口 `8888` 和 `/api/v1` 反代到后端 `8090`。
5. 如发布失败，保留上一版本容器或用上一 tag 镜像重新 `docker run` 回滚。

## Open Questions

- 目标部署服务器、SSH 用户、镜像包可见性和具体 secret 名称需要在 apply 阶段按团队环境确认或按默认名称落地。
- 后端服务是否已经在目标环境监听或映射到 `8090` 需要由后端部署侧确认；本变更不修改后端端口配置。
