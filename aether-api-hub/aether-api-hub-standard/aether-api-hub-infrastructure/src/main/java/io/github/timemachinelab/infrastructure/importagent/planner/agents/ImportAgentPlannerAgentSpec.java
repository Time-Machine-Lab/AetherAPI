package io.github.timemachinelab.infrastructure.importagent.planner.agents;

import java.util.List;
import java.util.Objects;

/**
 * Declaration-only agent specification.
 */
public record ImportAgentPlannerAgentSpec(
        String name,
        ImportAgentPlannerAgentRole role,
        int order,
        String promptTemplate,
        List<String> allowedTools,
        ImportAgentPlannerOutputMode outputMode,
        String streamStage,
        String streamTitle,
        String streamSummary) {

    public ImportAgentPlannerAgentSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Agent 名称不能为空");
        }
        Objects.requireNonNull(role, "Agent 角色不能为空");
        if (promptTemplate == null || promptTemplate.isBlank()) {
            throw new IllegalArgumentException("Agent 提示词模板不能为空");
        }
        allowedTools = allowedTools == null ? List.of() : List.copyOf(allowedTools);
        outputMode = outputMode == null ? ImportAgentPlannerOutputMode.STRUCTURED_NOTES : outputMode;
        streamStage = streamStage == null || streamStage.isBlank() ? name : streamStage;
        streamTitle = streamTitle == null || streamTitle.isBlank() ? name : streamTitle;
        streamSummary = streamSummary == null ? "" : streamSummary;
    }
}
