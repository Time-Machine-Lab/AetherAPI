package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentToolSpec(name = "extract_import_facts", stage = PlannerStage.EXTRACT_FACTS)
public class ExtractImportFactsPlanningTool implements ImportAgentPlanningTool {

        @Override
        public String stagePromptInstruction() {
                return "这一阶段只提取事实，不要输出最终计划，不要编造缺失值。";
        }

    @Override
    public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
        return ImportAgentPlanningToolSupport.buildFunctionTool(
                objectMapper,
                toolName,
                "从文档摘要、用户最新消息和 currentPlan 中提取高确定性导入事实，不要输出最终计划。关注 assetType、鉴权、异步查询接口、AI profile 与 schema 线索。",
                propertiesNode -> {
                    propertiesNode.set("assetFacts", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "assetName", "assetType", "requestMethod", "upstreamUrl", "categoryCode",
                            "requestExample", "responseExample"));
                    propertiesNode.set("authHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "authScheme", "authConfig"));
                    propertiesNode.set("asyncHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "queryMethod", "queryUrlTemplate", "authMode", "authScheme", "authConfig",
                            "statusPath", "resultPath", "errorPath"));
                    propertiesNode.set("aiProfileHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "provider", "model"));
                    propertiesNode.set("schemaHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "requestJsonSchema", "responseJsonSchema"));
                    propertiesNode.set("unresolvedQuestions", ImportAgentPlanningToolSupport.stringArraySchema(objectMapper));
                });
    }
}
