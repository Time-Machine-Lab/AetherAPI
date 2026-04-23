## ADDED Requirements

### Requirement: 发布流水线 MUST 由 aether-console 专属 tag 触发

`aether-console` 发布流水线 MUST 只在控制台发布 tag 推送时触发，并 MUST 避免普通分支推送、Pull Request 或其他子项目 tag 误触发控制台部署。默认 tag 模式 SHALL 使用 `aether-console-v*`。

#### Scenario: 控制台 tag 触发发布

- **WHEN** 维护者推送 `aether-console-v1.2.3` 这类匹配控制台发布规则的 tag
- **THEN** 系统 MUST 启动 `aether-console` 发布流水线

#### Scenario: 非控制台变更不触发发布

- **WHEN** 维护者推送普通分支、Pull Request 或不匹配控制台发布规则的 tag
- **THEN** 系统 MUST NOT 启动 `aether-console` 发布部署任务

### Requirement: 发布流水线 MUST 执行前端质量门禁

发布流水线 MUST 在构建或推送生产镜像前，基于 `aether-console/package.json` 中的既有脚本完成依赖安装、lint、format 校验、类型检查、单元测试和生产构建。

#### Scenario: 质量门禁全部通过

- **WHEN** `pnpm install --frozen-lockfile`、`pnpm lint`、`pnpm format:check`、`pnpm type-check`、`pnpm test` 和 `pnpm build` 全部成功
- **THEN** 系统 MUST 继续执行 Docker 镜像构建与发布步骤

#### Scenario: 任一质量门禁失败

- **WHEN** lint、format 校验、类型检查、测试或构建中的任一步骤失败
- **THEN** 系统 MUST 停止发布流程，并 MUST NOT 推送或部署新的生产镜像

### Requirement: Docker 镜像 MUST 使用 Nginx 托管构建产物

发布产物 MUST 使用 Docker 多阶段构建，builder 阶段生成 Vite `dist`，runtime 阶段使用 Nginx 托管静态资源并支持 SPA 路由 fallback。

#### Scenario: 镜像构建成功

- **WHEN** 发布流水线构建 `aether-console` Docker 镜像
- **THEN** 镜像 MUST 包含生产构建后的静态资源，并 MUST 使用 Nginx 作为运行时 Web 服务器

#### Scenario: 用户刷新前端路由

- **WHEN** 用户直接访问或刷新 `aether-console` 的前端路由
- **THEN** Nginx MUST 返回前端入口文件以支持 SPA 路由恢复

### Requirement: Nginx MUST 暴露 8888 并反向代理后端 8090

运行中的 `aether-console` 容器 MUST 对外提供前端访问端口 `8888`，并 MUST 将 `/api/` 下的请求反向代理到部署环境中 `8090` 端口上的后端服务。

#### Scenario: 访问前端页面

- **WHEN** 用户访问部署主机的 `8888` 端口
- **THEN** Nginx MUST 返回 `aether-console` 前端静态页面

#### Scenario: 前端调用后端 API

- **WHEN** 浏览器从前端页面请求同源 `/api/v1/**`
- **THEN** Nginx MUST 将请求转发到配置的后端 upstream，且该 upstream MUST 指向后端服务的 `8090` 端口

### Requirement: 发布配置 MUST 支持参数化部署与回滚

发布流水线和部署配置 MUST 通过 secrets、环境变量或 workflow variables 管理镜像仓库、目标服务器、SSH 凭证、容器名称和后端 upstream，并 MUST 提供可追溯的镜像标签与回滚说明。

#### Scenario: 部署参数完整

- **WHEN** 发布所需 secrets 和变量均已配置
- **THEN** 系统 MUST 能拉取本次 tag 对应镜像并重建 `aether-console` 容器

#### Scenario: 需要回滚

- **WHEN** 新版本发布后验证失败
- **THEN** 运维人员 MUST 能根据发布说明使用上一个 tag 镜像恢复 `aether-console` 容器
