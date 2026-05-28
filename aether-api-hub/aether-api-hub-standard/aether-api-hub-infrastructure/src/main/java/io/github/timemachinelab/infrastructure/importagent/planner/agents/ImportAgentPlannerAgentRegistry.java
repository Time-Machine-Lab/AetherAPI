package io.github.timemachinelab.infrastructure.importagent.planner.agents;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry for declaration-only import-agent planner agents.
 */
@Component
public class ImportAgentPlannerAgentRegistry {

    private final List<ImportAgentPlannerAgentSpec> agents;
    private final Map<String, ImportAgentPlannerAgentSpec> agentsByName;

    public ImportAgentPlannerAgentRegistry() {
        this(defaultAgents());
    }

    public ImportAgentPlannerAgentRegistry(List<ImportAgentPlannerAgentSpec> agents) {
        if (agents == null || agents.isEmpty()) {
                        throw new IllegalStateException("至少需要声明一个导入规划 Agent");
        }
        this.agents = agents.stream()
                .sorted(Comparator.comparingInt(ImportAgentPlannerAgentSpec::order)
                        .thenComparing(ImportAgentPlannerAgentSpec::name))
                .toList();
        this.agentsByName = this.agents.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ImportAgentPlannerAgentSpec::name,
                        Function.identity(),
                        (left, right) -> {
                                                        throw new IllegalStateException("导入规划 Agent 名称重复：" + left.name());
                        }));
    }

    public List<ImportAgentPlannerAgentSpec> getAgents() {
        return agents;
    }

    public ImportAgentPlannerAgentSpec getAgent(String name) {
        ImportAgentPlannerAgentSpec spec = agentsByName.get(name);
        if (spec == null) {
                        throw new IllegalArgumentException("未知的导入规划 Agent：" + name);
        }
        return spec;
    }

    public static List<ImportAgentPlannerAgentSpec> defaultAgents() {
        return List.of(
                spec("goal_capture", ImportAgentPlannerAgentRole.GOAL_CAPTURE, 10,
                        """
                        捕获用户的 API 导入目标、显式约束、文档来源和最新一轮对话。
                        返回 JSON 对象，包含 goal、constraints、knownInputs 和 openQuestions。
                        不要创建或修正导入计划。
                        """,
                        List.of(), ImportAgentPlannerOutputMode.STRUCTURED_NOTES,
                        "goal", "目标已识别", "规划器正在识别导入目标和约束。"),
                spec("observation", ImportAgentPlannerAgentRole.OBSERVATION, 20,
                        """
                        观察提供的文档摘要、当前计划、近期对话和可用分类。
                        只有在工具调用可用时才使用已声明的工具。
                        返回观察到的事实和不确定性，不要填补缺失的执行值。
                        """,
                        List.of("extract_import_facts"), ImportAgentPlannerOutputMode.STRUCTURED_NOTES,
                        "observation", "观察已完成", "规划器正在收集平台事实和文档事实。"),
                spec("state_estimation", ImportAgentPlannerAgentRole.STATE_ESTIMATION, 30,
                        """
                        估计当前导入状态：候选资产、认证依据、示例、Schema、异步任务依据、AI 配置依据和置信度。
                        异步任务只使用 asyncTaskConfig.queryResponseJsonSchema 描述任务查询响应体；不要生成 statusPath、resultPath 或 errorPath。
                        只返回 JSON。不要推断缺少上下文支撑的值。
                        """,
                        List.of("fill_import_slots"), ImportAgentPlannerOutputMode.STRUCTURED_NOTES,
                        "state", "状态已估计", "规划器正在估计已知字段和不确定项。"),
                spec("gap_comparator", ImportAgentPlannerAgentRole.GAP_COMPARISON, 40,
                        """
                        将估计状态与最低可执行导入计划契约进行比较。
                        返回 missingFields、risks 和 clarificationNeeds。不要修改计划。
                        """,
                        List.of(), ImportAgentPlannerOutputMode.STRUCTURED_NOTES,
                        "gaps", "差距已比较", "规划器正在检查仍然缺失的内容。"),
                spec("plan_controller", ImportAgentPlannerAgentRole.PLAN_CONTROL, 50,
                        """
                        决定流程应该提出澄清问题，还是继续生成计划。
                        返回 JSON 控制决策，包含理由和下一步所需输出。
                        不要执行写入，也不要编造缺失的执行值。
                        """,
                        List.of(), ImportAgentPlannerOutputMode.STRUCTURED_NOTES,
                        "control", "控制决策已生成", "规划器正在选择下一步规划动作。"),
                spec("plan_synthesis", ImportAgentPlannerAgentRole.PLAN_SYNTHESIS, 60,
                        """
                        基于上下文和前序阶段输出生成完整的导入计划草稿 JSON。
                        如果识别到异步任务查询响应示例或字段文档，请生成 asyncTaskConfig.queryResponseJsonSchema；证据不足时保持为空并通过 clarificationQuestions 询问任务查询响应示例或字段说明，不要要求用户提供 JSONPath。
                        如果用户要求修改 existingAssetCandidatesJson 或 targetExistingAssetsJson 中已有 API 的少数字段，并表达其他不变，请生成 UPDATE_EXISTING 补丁计划，只填 apiCode、action、changedFields 和 changedFields 对应的新值。
                        如果必需执行字段缺失，请通过 clarificationQuestions 让计划保持不可执行。
                        不要依赖平台侧修复。
                        """,
                        List.of(), ImportAgentPlannerOutputMode.STRUCTURED_DRAFT,
                        "synthesis", "计划草稿已生成", "规划器正在起草导入计划。"),
                spec("plan_review", ImportAgentPlannerAgentRole.PLAN_REVIEW, 70,
                        """
                        审查草稿的一致性、无依据假设、缺失执行字段、异步任务形态、认证完整性、分类歧义和用户意图一致性。
                        返回审查发现；如有必要，同时返回修订后的计划草稿。
                        """,
                        List.of(), ImportAgentPlannerOutputMode.STRUCTURED_DRAFT,
                        "review", "计划已审查", "规划器正在审查草稿一致性。"),
                spec("final_plan", ImportAgentPlannerAgentRole.FINAL_PLAN, 80,
                        """
                        生成最终导入计划载荷。可用时优先调用 submit_import_plan 工具。
                        最终 JSON 必须包含 summary、clarificationQuestions、categoryPlans 和 assetPlans。
                        每个 assetPlans[] 必须包含 action，取值 CREATE、UPDATE_EXISTING 或 UPSERT。用户意图不明确时不要默认 UPSERT，应保持计划不可执行并提出澄清。
                        UPDATE_EXISTING 必须使用 changedFields 表达本次明确修改的字段；未列入 changedFields 的字段视为保持已有资产原值。
                        当用户指定 apicode 且表达“其他不变”或只要求修改某个字段时，生成最小 UPDATE_EXISTING 补丁；不要询问未列入 changedFields 的 AI 配置、请求方法、上游 URL、认证方案或认证密钥。
                        authConfig 必须是纯字符串：HEADER_TOKEN 使用 Authorization: Bearer token，QUERY_TOKEN 使用 access_token=token；不要输出 JSON 对象或 JSON 字符串。
                        asyncTaskConfig 只允许用 queryResponseJsonSchema 描述任务查询响应体；不要输出 statusPath、resultPath 或 errorPath。
                        缺失的执行字段必须继续保持缺失，并表示为澄清需求。
                        """,
                        List.of("submit_import_plan"), ImportAgentPlannerOutputMode.FINAL_PLAN,
                        "final", "最终计划已提交", "规划器正在提交最终计划载荷。")
        );
    }

    private static ImportAgentPlannerAgentSpec spec(
            String name,
            ImportAgentPlannerAgentRole role,
            int order,
            String prompt,
            List<String> allowedTools,
            ImportAgentPlannerOutputMode outputMode,
            String streamStage,
            String streamTitle,
            String streamSummary) {
        return new ImportAgentPlannerAgentSpec(
                name,
                role,
                order,
                prompt,
                allowedTools,
                outputMode,
                streamStage,
                streamTitle,
                streamSummary);
    }
}
