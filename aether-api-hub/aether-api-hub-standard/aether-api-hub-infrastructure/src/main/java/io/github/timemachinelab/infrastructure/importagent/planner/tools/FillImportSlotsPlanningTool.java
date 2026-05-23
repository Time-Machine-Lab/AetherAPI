package io.github.timemachinelab.infrastructure.importagent.planner.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentToolSpec(name = "fill_import_slots", stage = PlannerStage.FILL_SLOTS)
public class FillImportSlotsPlanningTool implements ImportAgentPlanningTool {

    @Override
    public String stagePromptInstruction() {
        return "只返回有依据的槽位补丁。未知执行值必须继续保持缺失。";
    }

    @Override
    public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
        return ImportAgentPlanningToolSupport.buildFunctionTool(
                objectMapper,
                toolName,
                "基于观察事实和前序阶段输出返回有依据的字段补丁。",
                propertiesNode -> {
                    propertiesNode.set("categoryPlans", ImportAgentPlanningToolSupport.arraySchema(objectMapper,
                            ImportAgentPlanningToolSupport.buildCategoryPlanSchema(objectMapper, false)));
                    propertiesNode.set("assetPlans", ImportAgentPlanningToolSupport.arraySchema(objectMapper,
                            ImportAgentPlanningToolSupport.buildAssetPlanSchema(objectMapper, false)));
                    propertiesNode.set("remainingMissingSlots", ImportAgentPlanningToolSupport.stringArraySchema(objectMapper));
                });
    }
}
