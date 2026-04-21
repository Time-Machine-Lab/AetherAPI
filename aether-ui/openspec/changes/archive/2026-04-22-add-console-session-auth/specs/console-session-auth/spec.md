## ADDED Requirements

### Requirement: 控制台 MUST 使用真实后端会话接口建立登录态
`aether-console` MUST 以 `docs/api/console-auth.yaml` 为唯一权威契约来源，通过控制台登录接口建立登录态，而不是继续在前端本地生成 demo token 或伪造当前用户资料。

#### Scenario: 用户使用有效凭证登录控制台
- **WHEN** 用户在控制台登录页提交有效的 `loginName` 与 `password`
- **THEN** 前端 MUST 调用 `docs/api/console-auth.yaml` 定义的控制台登录接口
- **THEN** 前端 MUST 保存后端返回的 `accessToken`、失效时间信息以及 `currentUser`
- **THEN** 前端 MUST 进入原本计划访问的受保护控制台页面或默认受保护首页

#### Scenario: 用户登录失败
- **WHEN** 控制台登录接口返回参数错误或认证失败
- **THEN** 前端 MUST 保持在登录页
- **THEN** 前端 MUST 提供清晰的国际化错误反馈
- **THEN** 前端 MUST 不创建任何本地伪造登录态

### Requirement: 控制台 MUST 在刷新与直达受保护路由时恢复当前会话
`aether-console` MUST 在应用刷新、浏览器重开或用户直接访问 `meta.requiresAuth` 路由时，基于已保存的控制台 bearer token 调用当前会话接口完成会话恢复判定，并以后端返回结果作为认证状态的唯一事实来源。

#### Scenario: 已保存 token 的用户刷新页面
- **WHEN** 本地已保存控制台 bearer token，且用户刷新页面或重新打开控制台
- **THEN** 前端 MUST 调用当前会话查询接口恢复登录态
- **THEN** 若接口返回有效会话，前端 MUST 用后端返回的 `currentUser` 更新本地认证状态
- **THEN** 受保护页面 MUST 在恢复判定完成后再继续渲染

#### Scenario: 已保存 token 已失效
- **WHEN** 本地存在 bearer token，但当前会话查询接口返回未登录或 token 无效
- **THEN** 前端 MUST 清理本地认证状态
- **THEN** 前端 MUST 将用户引导回登录页
- **THEN** 前端 MUST 不继续把该 token 视为有效控制台会话

### Requirement: 控制台 MUST 将受保护路由与管理接口绑定到统一会话失效回退
所有依赖控制台登录态的受保护页面与管理接口请求 MUST 使用同一套控制台会话状态，并在未登录或会话失效时统一回退到登录入口，而不是让页面继续在失效状态下执行受保护逻辑。

#### Scenario: 未登录用户访问受保护控制台页面
- **WHEN** 用户访问 `meta.requiresAuth` 的控制台页面且本地没有有效控制台会话
- **THEN** 前端 MUST 跳转到登录页
- **THEN** 前端 MUST 保留最小必要的跳转上下文，以便登录成功后恢复原目标页面

#### Scenario: 已登录用户在控制台管理接口请求中遇到会话失效
- **WHEN** API Key 管理、API 调用日志或其他受保护控制台接口返回控制台会话未授权错误
- **THEN** 前端 MUST 清理当前控制台登录态
- **THEN** 前端 MUST 提供明确的会话失效反馈并引导用户重新登录
- **THEN** 前端 MUST 不继续持有失效用户资料或在页面中假定用户仍然已登录

### Requirement: 控制台 MUST 清晰区分 bearer 登录态与 Unified Access API Key
控制台前端 MUST 明确表达“控制台 bearer token 仅用于控制台业务接口认证，Unified Access 仍然依赖 `X-Aether-Api-Key`”这一边界，避免把两种认证语义混为一谈。

#### Scenario: 用户查看调用工作台或相关鉴权说明
- **WHEN** 前端展示调用工作台、帮助说明或登录相关引导
- **THEN** 页面 MUST 明确说明 Unified Access 使用 `X-Aether-Api-Key`，而不是使用控制台 bearer token 代替
- **THEN** 前端 MUST 不把控制台登录成功表述为“已经具备统一接入调用权限”

#### Scenario: 控制台登录态变化不改写统一接入鉴权规则
- **WHEN** 用户在调用工作台中进行统一接入调用
- **THEN** 前端 MUST 继续按照现有 Unified Access 能力要求收集和发送 `X-Aether-Api-Key`
- **THEN** 前端 MUST 不因为控制台 bearer token 存在就省略或替换 API Key 输入
