package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentToolSpec(name = "fill_import_slots", stage = PlannerStage.FILL_SLOTS)
public class FillImportSlotsPlanningTool implements ImportAgentPlanningTool {

    @Override
    public String stagePromptInstruction() {
        return "这一阶段只补齐缺失槽位并返回 patch；不能删除 currentPlanJson 已有字段，也不要生成新的澄清问题。";
    }

    @Override
    public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
        return ImportAgentPlanningToolSupport.buildFunctionTool(
                objectMapper,
                toolName,
                "基于 extracted facts、currentPlan 和最近 turns 仅补齐缺失槽位。不要删除已有字段，不要输出完整最终计划。",
                propertiesNode -> {
                    propertiesNode.set("categoryPlans", ImportAgentPlanningToolSupport.arraySchema(objectMapper,
                            ImportAgentPlanningToolSupport.buildCategoryPlanSchema(objectMapper, false)));
                    propertiesNode.set("assetPlans", ImportAgentPlanningToolSupport.arraySchema(objectMapper,
                            ImportAgentPlanningToolSupport.buildAssetPlanSchema(objectMapper, false)));
                    propertiesNode.set("remainingMissingSlots", ImportAgentPlanningToolSupport.stringArraySchema(objectMapper));
                });
    }
}