## 1. 前置确认与发布参数

- [x] 1.1 重新阅读 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`aether-console/DESIGN.md`、本 change 的 `proposal.md`、`design.md` 和 `specs/console-tag-release-pipeline/spec.md`，确认本次实现不涉及接口契约、页面视觉或组件规范变更。
- [x] 1.2 确认发布 tag 规则采用 `aether-console-v*`，并在 workflow 与发布说明中保持一致。
- [x] 1.3 确认部署环境后端服务已通过 `8090` 端口暴露，或明确由部署变量 `AETHER_BACKEND_UPSTREAM` 指向可访问的 `8090` upstream。
- [x] 1.4 约定并记录必需 GitHub secrets / variables，包括目标主机、SSH 用户、SSH 私钥、SSH 端口、镜像仓库、容器名称、后端 upstream 和可选部署目录。

## 2. Docker 与 Nginx 发布产物

- [x] 2.1 在 `aether-console` 下新增 `.dockerignore`，排除 `node_modules`、`dist`、测试缓存、本地环境文件和无关编辑器产物。
- [x] 2.2 在 `aether-console` 下新增多阶段 `Dockerfile`，builder 阶段使用 pnpm 安装依赖并构建 Vite `dist`，runtime 阶段使用 Nginx 托管静态资源。
- [x] 2.3 新增 Nginx 配置模板，监听容器内 `8888`，支持 SPA fallback，并将 `/api/` 请求反向代理到 `AETHER_BACKEND_UPSTREAM`。
- [x] 2.4 增加容器启动时的 Nginx 配置渲染机制，确保后端 upstream 可通过环境变量覆盖，默认目标端口为 `8090`。
- [ ] 2.5 本地构建镜像并运行容器，验证 `http://localhost:8888` 可返回前端页面，Nginx 配置语法校验通过。
  - Blocked: 当前工作区缺少 `docker` 命令，无法执行本地镜像构建、容器运行或 `nginx -t`；已完成 Dockerfile/Nginx 静态配置检查，并在 `aether-console/RELEASE.md` 记录等价 dry-run 命令。

## 3. GitHub Actions 发布流水线

- [x] 3.1 在根目录 `.github/workflows/` 新增 `aether-console-release.yml`，仅监听 `aether-console-v*` tag 推送。
- [x] 3.2 配置 workflow checkout、Node/pnpm 环境和 pnpm 缓存，工作目录限定为 `aether-ui/aether-console`。
- [x] 3.3 在镜像构建前依次执行 `pnpm install --frozen-lockfile`、`pnpm lint`、`pnpm format:check`、`pnpm type-check`、`pnpm test`、`pnpm build`。
- [x] 3.4 配置 Docker Buildx、Docker Hub 登录、镜像构建与推送，镜像标签至少包含触发 tag 和 `latest-console`，登录凭证使用 Organization secrets 中的 `DOCKER_USERNAME` 与 `DOCKER_PASSWORD`。
- [x] 3.5 配置 SSH 部署步骤，在目标服务器登录 Docker Hub、拉取 tag 镜像、停止旧容器并以 `8888:8888` 和后端 upstream 环境变量启动新容器。
- [x] 3.6 确保 workflow 权限最小化，显式声明 `contents: read`，不再申请 package 写入权限。

## 4. 发布说明与运维验证

- [x] 4.1 在 `aether-console` 发布说明中记录 tag 发布命令、必需 secrets / variables、镜像命名规则、端口约定和后端 `8090` 前置条件。
- [x] 4.2 在发布说明中记录首次部署检查步骤，包括 Docker 权限、Docker Hub 拉取权限、`8888` 端口占用、后端 upstream 连通性和 Nginx 反代验证。
- [x] 4.3 在发布说明中记录回滚步骤，说明如何使用上一个 `aether-console-v*` tag 镜像恢复容器。
- [x] 4.4 补充一次 dry-run 或本地等价验证记录，证明 Docker 构建、Nginx 配置渲染、端口暴露和 SPA fallback 满足 spec。

## 5. 最终验证

- [ ] 5.1 在 `aether-ui/aether-console` 运行 `pnpm lint`、`pnpm format:check`、`pnpm type-check`、`pnpm test`、`pnpm build`。
  - Blocked: `pnpm lint`、`pnpm type-check`、`pnpm test`、`pnpm build` 已通过；`pnpm format:check` 当前失败，范围包含 98 个既有文件，超出本次发布流水线变更的最小实现范围。
- [x] 5.2 在仓库根目录验证 workflow YAML 语法和引用路径，确保工作目录、Dockerfile 路径和构建上下文正确。
- [x] 5.3 运行 `openspec validate add-console-tag-release-pipeline --strict`，确保变更规格仍然有效。
- [x] 5.4 如条件允许，推送测试 tag 触发流水线；如不能触发远端发布，则记录未执行原因和可替代的本地验证证据。
