## Context

当前 Import Agent planner 已经采用 staged tool-calling，`extract_import_facts`、`fill_import_slots`、`submit_import_plan` 三个工具都由 `OpenAiCompatibleImportAgentPlannerProvider` 在类内手工定义并按阶段选择。这个实现已经能满足当前功能，但也把几个不同层级的职责叠在了一起：

- provider 负责阶段编排和 HTTP 调用；
- provider 同时还负责 tool 元数据、tool schema 和阶段工具列表；
- `ImportAgentPlannerJsonSupport` 负责 deterministic reconcile、normalize 和 validate。

在上一轮 change 已经补强 tool-calling 约束后，tool schema 的演进会越来越频繁。如果继续把每个 tool 的名称、阶段和 JSON schema 都内联在 provider 中，后续新增工具、调整阶段顺序、做重复 name 校验或补单工具测试时，都会继续推高 provider 的维护成本。

由于当前运行环境已经是 Spring Boot，最自然的注册方式不是额外做一轮类路径扫描，而是让每个 planning tool 成为独立 Spring Bean，再由一个 registry 在启动时收集并校验这些 bean。这样可以让 provider 回到“编排者”角色，同时保留当前 staged tool-calling 行为、feature gate 和对外契约。

## Goals / Non-Goals

**Goals:**

- 让每个 Import Agent planning tool 以独立类承载自己的稳定元数据和 schema 定义。
- 用 Spring Bean 收集 + 注解元数据 + registry 聚合替代 provider 内联的 tool 定义方式。
- 让 provider 只负责编排、request 组装、LLM 调用和响应解析，不再手工维护全部 tool schema。
- 在启动时对 tool name、stage 和顺序做一致性校验，避免运行时才发现注册错误。
- 保持现有 staged tool-calling、deterministic reconcile、public API、SQL 表结构和 feature gate 不变。

**Non-Goals:**

- 不把 planner tool 扩展为本地执行型通用 agent framework。
- 不把 deterministic reconcile、slot filling 或 plan validate 逻辑迁移到 tool 类中。
- 不引入脱离 Spring 容器的反射扫描或额外插件机制。
- 不修改 `docs/api/` 与 `docs/sql/` 顶层权威文档。

## Decisions

### Decision 1: 每个 planning tool 使用独立类 + 注解元数据声明

新增一个轻量 SPI，例如 `ImportAgentPlanningTool`，由每个 tool 类实现，用于输出该 tool 的 function schema。新增一个注解，例如 `ImportAgentToolSpec`，承载稳定元数据：

- `name`
- `stage`
- `order`

tool 类负责描述自己的 schema；注解负责声明稳定注册信息。这样可以避免把复杂 JSON schema 塞进注解参数里，同时也避免 provider 继续维护大量工具常量和 schema 构造逻辑。

备选方案：只保留接口，不加注解，改由每个实现类自己暴露 `name()`、`stage()`、`order()` 方法。

不采用原因：纯接口方法虽然也能工作，但元数据和 schema 代码仍然混在同一个实现中，缺少显式声明点；启动时统一读取和校验也不如注解直观。

### Decision 2: 用 Spring Bean 收集 tool，而不是自定义类路径扫描

registry 通过 Spring 注入 `List<ImportAgentPlanningTool>` 收集所有 tool bean，再读取其注解元数据建立按阶段分组的只读索引。这个索引需要在启动时完成以下校验：

- tool bean 必须带有注册注解；
- tool `name` 不能重复；
- tool `stage` 不能为空；
- 同阶段工具必须按 `order` 排序；
- registry 输出的工具顺序必须稳定。

备选方案：使用 `ClassPathScanningCandidateComponentProvider` 或其他反射扫描方式，从包路径中查找带注解的类。

不采用原因：当前系统已经使用 Spring Bean 管理 provider，本地再做一次扫描只会增加复杂度和测试负担，而且对当前仓库没有额外收益。

### Decision 3: provider 只做 orchestration，不再拥有 tool schema 细节

`OpenAiCompatibleImportAgentPlannerProvider` 应继续负责：

- 按 `PlannerStage` 驱动多阶段规划；
- 组装 messages、tool choice 和 request body；
- 调用 OpenAI-compatible 接口；
- 解析 tool call 返回并与 `ImportAgentPlannerJsonSupport` 协同构建最终计划。

但 provider 不再保留 `buildExtractFactsTool`、`buildFillSlotsTool`、`buildSubmitPlanTool` 这类手工 schema 组装方法，而是通过 registry 按阶段取回 tool 定义。

备选方案：保留 provider 内部的 schema 构造，只把 name 和 stage 挪到配置表里。

不采用原因：这只能减少少量常量定义，不能真正降低 provider 的职责复杂度，也无法把单个 tool schema 独立测试出来。

### Decision 4: deterministic reconcile 和 validate 继续留在 JsonSupport

`ImportAgentPlannerJsonSupport` 仍然是当前 planner 输出规范化和兜底校验的中心：

- merge current plan
- reconcile missing slots
- normalize async task query assets
- validate plan

tool 类只负责“我向模型暴露什么 schema”，而不是“我如何修正最终 plan”。这样可以避免把业务语义错误地下沉到 tool registry 或单个 tool 类中，保持现有 planner 边界不变。

备选方案：让 tool 类除了 schema 外，再提供 execute/reconcile/fixup 等本地方法。

不采用原因：当前这些 tool 是发给 LLM 的 function contract，不是本地执行函数。把它们设计成本地执行框架会混淆架构，并扩大本次重构范围。

### Decision 5: 对注解元数据与 schema name 做一致性保护

注解中的 `name` 与 tool 最终输出的 function name 存在双真相风险。为避免漂移，registry 或统一 schema builder 应负责把注解中的 `name` 写入最终 function 定义，或者在启动时校验 schema name 与注解值一致。这样能降低“注解注册成功但实际 schema name 不匹配”的运行时隐患。

备选方案：允许 tool 类在 schema 里自行写 name，registry 只负责收集 bean。

不采用原因：一旦同一 tool 的注解 name 和 schema name 分叉，provider 在构造 request 与解析返回时就可能出现难以发现的错配。

## Risks / Trade-offs

- [重构后类数量上升] → 用 registry 集中聚合并保持每个 tool 类只承载 schema，避免拆类后又形成新的复杂交叉依赖。
- [tool 顺序变化影响模型行为] → 在 registry 中显式按 `stage + order` 排序，并为顺序建立回归测试。
- [注解元数据与 schema 定义漂移] → 统一由 registry 注入 function name，或在启动时做强一致性校验。
- [provider 测试需要重排] → 拆分为 tool schema 单测、registry 单测和 provider 编排单测，降低单一测试文件承担的责任。
- [过度设计为通用 agent framework] → 限定本 change 只服务 Import Agent planner tool 定义，不引入 execute 接口、自治循环或跨模块插件能力。