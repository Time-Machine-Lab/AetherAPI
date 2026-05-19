package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentToolSpec(name = "submit_import_plan", stage = PlannerStage.SUBMIT_PLAN)
public class SubmitImportPlanPlanningTool implements ImportAgentPlanningTool {

    @Override
    public String stagePromptInstruction() {
        return "这一阶段必须返回完整最终计划；如果仍有高优先级缺失字段，再输出 clarificationQuestions。";
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
                "提交当前 API 导入请求的完整导入计划，或提交对话式澄清问题。异步任务查询接口必须写入提交资产的 asyncTaskConfig，不要作为独立 assetPlan。",
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