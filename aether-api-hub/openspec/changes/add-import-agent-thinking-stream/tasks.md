## 1. 权威对齐

- [ ] 1.1 结合 `docs/spec/` 开发规范、`docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`、`docs/api/api-import-agent.yaml` 和 `docs/sql/api_import_agent_*.sql` 重新核对，确认本次 change 不需要新增 controller endpoint，也不需要新增权威表结构。
- [ ] 1.2 更新 `docs/api/api-import-agent.yaml`，记录新的 SSE `thinking` 事件、其载荷字段，以及禁止暴露 raw CoT 或携带 secret 的载荷边界。

## 2. 流式事件模型

- [ ] 2.1 将当前仅支持 reply 的 `Consumer<String>` 流式回调替换为结构化 import-agent stream emitter，使其能够发出 `status`、`thinking`、`message`、`session`、`error`、`done` 事件。
- [ ] 2.2 重构 `ApiImportAgentWebDelegate`，让 stream endpoint 按结构化事件写出 SSE，同时保留最终 `session` 快照和现有 `message` delta 行为。
- [ ] 2.3 保持 create-session 与 append-turn 两条 stream endpoint 对忽略未知 SSE 事件的客户端向后兼容。

## 3. Planner 与 Reply 接入

- [ ] 3.1 更新 `OpenAiCompatibleImportAgentPlannerProvider` 及相关 planning 流程，使 extract / fill / submit 阶段和 fallback planning 路径能够发出脱敏后的 `thinking` 摘要。
- [ ] 3.2 更新 planner 内部 subagent orchestration，使 subagent 开始 / 完成、merge / review 决策以及 clarification downgrade 节点能够发出安全的 `thinking` 事件，且不泄露敏感字段值。
- [ ] 3.3 保持 `OpenAiCompatibleImportAgentReplyPort` 继续作为用户可见 reply delta 的生产者，同时确保 thinking 事件不会混入最终 assistant message 文本。

## 4. 安全与验证

- [ ] 4.1 为 SSE 事件序列化补充回归测试，覆盖 `thinking` 事件、最终 `session` 输出，以及对现有事件消费者的加法式兼容。
- [ ] 4.2 增加测试，证明 thinking 事件不会暴露 raw CoT、`authConfig` secret、prompt 正文或未脱敏的上游 payload 片段。
- [ ] 4.3 使用 Java 17 运行相关 adapter、service、infrastructure Maven 测试，并记录任何超出本次 change 范围的环境问题。
