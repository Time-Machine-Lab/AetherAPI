package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentPlannerSubagentSpec(name = "clarification_strategy", role = ImportAgentPlannerSubagentRole.CLARIFICATION_STRATEGY, order = 50)
public class ClarificationStrategyPlannerSubagent implements ImportAgentPlannerSubagent {

    @Override
    public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        JsonNode existingQuestions = candidatePlan.path("clarificationQuestions");
        if (!existingQuestions.isArray()) {
            return;
        }
        for (JsonNode questionNode : (ArrayNode) existingQuestions) {
            String question = questionNode.asText(null);
            if (question != null && !question.isBlank()) {
                context.addClarificationQuestion(question);
            }
        }
    }
}