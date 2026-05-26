package io.github.timemachinelab.infrastructure.importagent.planner.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ImportAgentToolSpec(name = "extract_import_facts", stage = PlannerStage.EXTRACT_FACTS)
public class ExtractImportFactsPlanningTool implements ImportAgentPlanningTool {

    @Override
    public String stagePromptInstruction() {
        return "只提取已观察到的事实。不要提交最终计划，也不要编造缺失值。";
    }

    @Override
    public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
        return ImportAgentPlanningToolSupport.buildFunctionTool(
                objectMapper,
                toolName,
                "从文档、用户对话和当前计划中提取高置信度导入事实。",
                propertiesNode -> {
                    propertiesNode.set("assetFacts", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "assetName", "assetType", "requestMethod", "upstreamUrl", "categoryCode",
                            "requestExample", "responseExample"));
                    propertiesNode.set("authHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "authScheme", "authConfig"));
                    propertiesNode.set("upstreamHeaderHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "name", "value"));
                    propertiesNode.set("asyncHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "queryMethod", "queryUrlTemplate", "authMode", "authScheme", "authConfig",
                            "queryResponseJsonSchema"));
                    propertiesNode.set("aiProfileHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "provider", "model"));
                    propertiesNode.set("schemaHints", ImportAgentPlanningToolSupport.buildNamedObjectArraySchema(objectMapper,
                            "apiCode", "requestJsonSchema", "responseJsonSchema"));
                    propertiesNode.set("unresolvedQuestions", ImportAgentPlanningToolSupport.stringArraySchema(objectMapper));
                });
    }
}
