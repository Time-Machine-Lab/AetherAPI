package io.github.timemachinelab.infrastructure.importagent.planner.stream;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerAgentSpec;
import io.github.timemachinelab.service.model.ImportAgentStreamEmitter;

/**
 * Safe stream summaries for planner stages.
 */
public final class ImportAgentPlannerStreamSummaries {

    private ImportAgentPlannerStreamSummaries() {
    }

    public static void emitStart(ImportAgentStreamEmitter stream, ImportAgentPlannerAgentSpec agent) {
        if (stream != null) {
            stream.thinking(agent.streamStage(), agent.streamTitle(), agent.streamSummary());
        }
    }

    public static void emitComplete(ImportAgentStreamEmitter stream, ImportAgentPlannerAgentSpec agent, JsonNode output) {
        if (stream != null) {
            stream.thinking(agent.streamStage(), agent.streamTitle(), "阶段已完成：" + summarize(output));
        }
    }

    public static String summarize(JsonNode node) {
        if (node == null || node.isNull()) {
            return "空";
        }
        if (node.isArray()) {
            return "数组(size=" + node.size() + ")";
        }
        if (!node.isObject()) {
            return node.getNodeType().name().toLowerCase();
        }
        return "对象(assetPlans=" + sizeOfArray(node, "assetPlans")
                + ", categoryPlans=" + sizeOfArray(node, "categoryPlans")
                + ", clarificationQuestions=" + sizeOfArray(node, "clarificationQuestions")
                + ", missingFields=" + sizeOfArray(node, "missingFields")
                + ", risks=" + sizeOfArray(node, "risks")
                + ")";
    }

    private static int sizeOfArray(JsonNode node, String fieldName) {
        JsonNode child = node.path(fieldName);
        return child.isArray() ? child.size() : 0;
    }
}
