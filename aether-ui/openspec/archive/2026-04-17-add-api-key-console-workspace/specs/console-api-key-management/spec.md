## ADDED Requirements

### Requirement: 控制台 SHALL 提供当前用户 API Key 工作区

`aether-console` 的受保护工作区 SHALL 通过现有 `credentials` 导航入口暴露一个面向当前登录用户的凭证管理分区，并且 SHALL 消费 `docs/api/api-credential.yaml` 中定义的“当前用户 API Key”契约。

#### Scenario: 进入凭证工作区

- **WHEN** 已认证用户从控制台侧边栏打开 `credentials` 分区
- **THEN** 控制台将用户路由到受保护工作区中的真实凭证区域，而不是占位面板
- **THEN** 页面通过专门的 API 层模块加载数据，而不是在页面组件中直接发起裸请求

### Requirement: 控制台 SHALL 支持掩码列表和详情浏览

控制台 SHALL 允许当前用户浏览掩码 API Keys、查看选中 key 的详情，并展示契约支持的生命周期字段，包括状态、过期时间、吊销时间、创建时间、更新时间，以及存在时的 `lastUsedSnapshot`。

#### Scenario: 浏览当前用户 API Key 列表

- **WHEN** 凭证工作区请求当前用户的 API Keys
- **THEN** 控制台根据列表响应渲染分页条目
- **THEN** 每个条目只展示 `maskedKey` 和其他非敏感字段
- **THEN** 控制台在列表或详情刷新流程中 MUST NOT 暴露 `plaintextKey`

#### Scenario: 查看尚无使用快照的凭证

- **WHEN** 某个凭证响应中的 `lastUsedSnapshot` 为 null 或空值
- **THEN** 控制台展示明确的“暂无使用记录”空状态
- **THEN** 控制台 MUST NOT 伪造本地使用历史或推断出的鉴权结果

### Requirement: 控制台 SHALL 支持契约定义的当前用户生命周期操作

控制台 SHALL 允许当前用户通过 `docs/api/api-credential.yaml` 定义的操作创建、启用、停用和吊销 API Keys，并且 SHALL 在每次成功变更后刷新展示中的凭证状态。

#### Scenario: 创建 API Key

- **WHEN** 用户在凭证工作区提交合法的创建表单
- **THEN** 控制台调用权威契约中定义的创建接口
- **THEN** 创建成功态在创建后展示流程中准确显示返回的 `plaintextKey`
- **THEN** 随后的列表或详情重新加载只显示掩码 key 数据

#### Scenario: 变更凭证状态

- **WHEN** 用户对某个凭证触发启用、停用或吊销，且后端接受该操作
- **THEN** 控制台使用最新 API 响应更新可见状态
- **THEN** 当前状态下不可执行的生命周期操作不会被伪装成可用操作展示给用户

### Requirement: 控制台 SHALL 在凭证管理中保持隐藏 Consumer 模型

凭证工作区 SHALL 以“我的 API Keys”来表达产品能力，并且 MUST NOT 在用户工作流中暴露显式 `Consumer` 标识、创建动作或可编辑 `Consumer` 字段。

#### Scenario: 渲染凭证归属上下文

- **WHEN** 工作区渲染凭证数据和辅助说明文案
- **THEN** 面向用户的界面以“当前用户的 API Keys”为主要表达
- **THEN** 控制台不会把显式的 `Consumer` 管理表单、表格或主体标识渲染成可操作对象
