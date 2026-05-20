## 已修改需求

### 需求：控制台 import-agent 工作流必须将 thinking 事件与最终 reply 分开展示
当 import-agent stream 返回结构化 `thinking` 事件时，控制台工作流必须将这些事件作为独立的过程摘要展示，而不是把它们拼接进最终助手回复文本。

#### 场景：流式会话返回 thinking 事件
- **WHEN** 当前用户创建 import-agent 会话或追加澄清轮次时，后端 stream 返回一个或多个 `thinking` 事件
- **THEN** 前端必须将这些事件渲染到独立的 thinking 时间线或思考卡片列表中
- **AND** 前端必须继续把 `message` 事件作为最终助手回复增量显示，而不是把两者混为同一段文本

#### 场景：thinking 与 reply 同时存在
- **WHEN** 同一条流式会话既返回 `thinking` 事件，也返回最终 `message` delta
- **THEN** 前端必须保持 thinking 区和 reply 区在视觉上可区分
- **AND** 最终 `session` 快照到达后，必须继续以服务端返回的会话事实更新计划与轮次视图

### 需求：控制台 import-agent 工作流必须在缺少 thinking 事件时保持向后兼容
thinking 事件是对现有 import-agent stream 的增强能力。当后端未返回 `thinking` 事件时，前端必须继续保持现有 status / reply / session 工作流可用。

#### 场景：后端仍返回旧事件集合
- **WHEN** import-agent stream 只返回 `status`、`message`、`session`、`error` 和 `done` 事件
- **THEN** 前端必须继续显示现有流式状态提示和 reply 增量
- **AND** 前端不得因为缺少 thinking 事件而中断工作流或显示错误空壳

#### 场景：thinking 流会在下一次会话开始前被重置
- **WHEN** 用户开启一条新的 create-session 或 append-turn 流
- **THEN** 前端必须清理上一条流残留的 thinking 时间线
- **AND** 新流的 thinking 与 reply 状态必须从空状态重新开始累计
