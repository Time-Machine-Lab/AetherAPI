## 已修改需求

### 需求：Import-agent planner 必须在候选计划生成后执行专门检查阶段
系统必须在 Import Agent planner 生成候选计划后，通过专门检查阶段审查执行关键字段的一致性，并在缺失或冲突时优先降级为 clarificationQuestions。

#### 场景：候选计划缺失关键鉴权字段
- **WHEN** planner 生成的候选计划包含可发布资产，但缺失 `authScheme`、`authConfig` 或两者不一致
- **THEN** 专门检查阶段必须把该问题转化为 targeted clarificationQuestions
- **AND** 最终计划不得因为后处理补救而直接变为 executable

#### 场景：候选计划缺失异步查询关键字段
- **WHEN** planner 生成的候选计划启用了 `asyncTaskConfig.enabled = true`，但缺失 `queryMethod`、`queryUrlTemplate`、`authMode` 或 override 鉴权字段
- **THEN** 专门检查阶段必须识别该缺口并保持计划为非 executable

### 需求：Planner 后处理必须收缩为确定性守卫而非智能补救主路径
系统必须将 planner 后处理阶段限制为结构化解析、current plan 合并、最小兼容归一化和最终 executable gate。系统不得再把高风险字段补救作为主要后处理路径。

#### 场景：关键字段仅存在自由文本线索
- **WHEN** 执行关键字段只在文档摘要、会话自由文本或非声明字段中出现，而未进入结构化 planner 输出
- **THEN** 后处理阶段不得仅凭这些自由文本线索将计划补齐为 executable
- **AND** 系统必须通过 clarificationQuestions 暴露缺失信息

#### 场景：已知低风险别名仍可兼容
- **WHEN** planner 输出使用已知低风险格式别名，例如约定范围内的 schema alias 或 enum alias
- **THEN** 后处理阶段仍可以做兼容性归一化
- **AND** 该兼容性归一化不得替代缺失关键字段的显式结构化提供
