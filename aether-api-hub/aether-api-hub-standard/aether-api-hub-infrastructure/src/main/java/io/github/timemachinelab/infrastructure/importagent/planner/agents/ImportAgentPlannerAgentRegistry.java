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
            throw new IllegalStateException("At least one import-agent planner agent must be declared");
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
                            throw new IllegalStateException("Duplicate import-agent planner agent name: " + left.name());
                        }));
    }

    public List<ImportAgentPlannerAgentSpec> getAgents() {
        return agents;
    }

    public ImportAgentPlannerAgentSpec getAgent(String name) {
        ImportAgentPlannerAgentSpec spec = agentsByName.get(name);
        if (spec == null) {
            throw new IllegalArgumentException("Unknown import-agent planner agent: " + name);
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
                        authConfig 必须是纯字符串：HEADER_TOKEN 使用 Authorization: Bearer token，QUERY_TOKEN 使用 access_token=token；不要输出 JSON 对象或 JSON 字符串。
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
