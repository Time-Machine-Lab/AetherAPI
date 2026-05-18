## MODIFIED Requirements

### Requirement: 控制台 MUST 隐藏暂时下线分区的标准入口
`aether-console` MUST 在当前发布范围内隐藏 `category-manage`、`usage`、`orders`、`billing` 与 `docs` 这五个暂时下线分区的标准入口，不再把它们作为用户默认可达的控制台功能展示。

#### Scenario: 用户查看控制台侧边导航
- **WHEN** 已登录用户进入受保护的控制台壳层
- **THEN** 侧边导航 MUST 不展示 `category-manage`、`usage`、`orders`、`billing` 与 `docs`
- **THEN** 侧边导航 MUST 继续保留 `catalog-browse`、`catalog-manage`、`import-agent`、`credentials`、`api-call-logs` 与 `unified-access-playground`

#### Scenario: 用户查看控制台顶部工具区
- **WHEN** 已登录用户查看控制台顶部工具区
- **THEN** 顶部工具区 MUST 不展示 `usage` 与 `docs` 的快捷入口