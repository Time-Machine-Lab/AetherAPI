## ADDED Requirements

### Requirement: 控制台 MUST 隐藏暂时下线分区的标准入口
`aether-console` MUST 在当前发布范围内隐藏 `category-manage`、`usage`、`orders`、`billing` 与 `docs` 这五个暂时下线分区的标准入口，不再把它们作为用户默认可达的控制台功能展示。

#### Scenario: 用户查看控制台侧边导航
- **WHEN** 已登录用户进入受保护的控制台壳层
- **THEN** 侧边导航 MUST 不展示 `category-manage`、`usage`、`orders`、`billing` 与 `docs`
- **THEN** 侧边导航 MUST 继续保留 `catalog-browse`、`catalog-manage`、`credentials`、`api-call-logs` 与 `unified-access-playground`

#### Scenario: 用户查看控制台顶部工具区
- **WHEN** 已登录用户查看控制台顶部工具区
- **THEN** 顶部工具区 MUST 不展示 `usage` 与 `docs` 的快捷入口

### Requirement: 控制台 MUST 不再通过辅助壳层界面宣传暂时隐藏分区
凡是由控制台壳层导航模型衍生的辅助界面，例如登录页帮助预览、工作台预览或同类入口面板，MUST 与当前可见功能集合保持一致，不得继续宣传暂时隐藏的分区。

#### Scenario: 用户查看登录页或其他壳层预览入口
- **WHEN** 控制台渲染登录页帮助内容、工作区预览卡片或其他壳层衍生入口
- **THEN** 界面 MUST 不再展示分类管理或其他已暂时隐藏分区的入口、标题或引导

### Requirement: 控制台 MUST 为暂时隐藏分区提供可预期的回退行为
当用户通过旧书签、刷新后的 hash 或其他非标准入口访问 `#category-manage`、`#usage`、`#orders`、`#billing` 或 `#docs` 时，控制台 MUST 回退到仍然可见的默认管理目的地，而不是保持在隐藏分区状态或展示死角界面。

#### Scenario: 用户访问暂时隐藏分区的旧 hash
- **WHEN** 已登录用户进入 `console-workspace` 且 hash 指向 `#category-manage`、`#usage`、`#orders`、`#billing` 或 `#docs`
- **THEN** 控制台 MUST 将界面解析到当前仍可见的默认管理分区
- **THEN** 控制台 MUST 不把隐藏分区标记为当前激活导航项

#### Scenario: 用户进入默认资产管理工作流
- **WHEN** 已登录用户以无 hash 或有效可见入口进入 `console-workspace`
- **THEN** 控制台 MUST 继续展示当前保留的资产管理与其他未隐藏功能
- **THEN** 控制台 MUST 不渲染仅属于暂时隐藏分区的交互界面
