package io.github.timemachinelab.infrastructure.importagent.planner.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentToolSpec(name = "submit_import_plan", stage = PlannerStage.SUBMIT_PLAN)
public class SubmitImportPlanPlanningTool implements ImportAgentPlanningTool {

    @Override
    public String stagePromptInstruction() {
        return "提交完整最终计划。如果执行字段缺失，请写入 clarificationQuestions。";
    }

    @Override
    public boolean requiresStrictContentFallback() {
        return true;
    }

    @Override
    public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
        return ImportAgentPlanningToolSupport.buildFunctionTool(
                objectMapper,
                toolName,
                "提交最终 API 导入计划，用于持久化并等待后续用户确认。",
                propertiesNode -> {
                    propertiesNode.set("summary", ImportAgentPlanningToolSupport.stringSchema(objectMapper));
                    propertiesNode.set("clarificationQuestions", ImportAgentPlanningToolSupport.stringArraySchema(objectMapper));
                    propertiesNode.set("categoryPlans", ImportAgentPlanningToolSupport.arraySchema(objectMapper,
                            ImportAgentPlanningToolSupport.buildCategoryPlanSchema(objectMapper, true)));
                    propertiesNode.set("assetPlans", ImportAgentPlanningToolSupport.arraySchema(objectMapper,
                            ImportAgentPlanningToolSupport.buildAssetPlanSchema(objectMapper, true)));
                },
                "assetPlans");
    }
}
