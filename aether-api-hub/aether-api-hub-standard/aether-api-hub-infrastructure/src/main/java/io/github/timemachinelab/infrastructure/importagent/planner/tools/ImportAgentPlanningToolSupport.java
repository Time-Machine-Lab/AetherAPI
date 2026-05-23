package io.github.timemachinelab.infrastructure.importagent.planner.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.function.Consumer;

final class ImportAgentPlanningToolSupport {

    private ImportAgentPlanningToolSupport() {
    }

    static ObjectNode buildFunctionTool(
            ObjectMapper objectMapper,
            String toolName,
            String description,
            Consumer<ObjectNode> propertiesCustomizer,
            String... requiredProperties) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("type", "function");
        ObjectNode functionNode = tool.putObject("function");
        functionNode.put("name", toolName);
        functionNode.put("description", description);
        functionNode.set("parameters", strictObjectSchema(objectMapper, propertiesCustomizer, requiredProperties));
        return tool;
    }

    static ObjectNode strictObjectSchema(
            ObjectMapper objectMapper,
            Consumer<ObjectNode> propertiesCustomizer,
            String... requiredProperties) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        ObjectNode properties = schema.putObject("properties");
        propertiesCustomizer.accept(properties);
        if (requiredProperties != null && requiredProperties.length > 0) {
            ArrayNode required = schema.putArray("required");
            for (String requiredProperty : requiredProperties) {
                required.add(requiredProperty);
            }
        }
        return schema;
    }

    static ObjectNode stringSchema(ObjectMapper objectMapper) {
        return objectMapper.createObjectNode().put("type", "string");
    }

    static ObjectNode booleanSchema(ObjectMapper objectMapper) {
        return objectMapper.createObjectNode().put("type", "boolean");
    }

    static ObjectNode stringArraySchema(ObjectMapper objectMapper) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "array");
        schema.set("items", stringSchema(objectMapper));
        return schema;
    }

    static ObjectNode arraySchema(ObjectMapper objectMapper, ObjectNode itemSchema) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "array");
        schema.set("items", itemSchema);
        return schema;
    }

    static ObjectNode enumStringSchema(ObjectMapper objectMapper, String description, String... values) {
        ObjectNode schema = stringSchema(objectMapper);
        if (description != null && !description.isBlank()) {
            schema.put("description", description);
        }
        ArrayNode enumValues = schema.putArray("enum");
        for (String value : values) {
            enumValues.add(value);
        }
        return schema;
    }

    static ObjectNode stringSchema(ObjectMapper objectMapper, String description, String pattern) {
        ObjectNode schema = stringSchema(objectMapper);
        if (description != null && !description.isBlank()) {
            schema.put("description", description);
        }
        if (pattern != null && !pattern.isBlank()) {
            schema.put("pattern", pattern);
        }
        return schema;
    }

    static ObjectNode buildAssetPlanSchema(ObjectMapper objectMapper, boolean strictRequired) {
        return strictObjectSchema(objectMapper, assetProperties -> {
            assetProperties.set("apiCode", stringSchema(objectMapper));
            assetProperties.set("assetName", stringSchema(objectMapper));
            assetProperties.set("assetType", enumStringSchema(objectMapper, null, "STANDARD_API", "AI_API"));
            assetProperties.set("categoryCode", stringSchema(objectMapper));
            assetProperties.set("requestMethod", enumStringSchema(objectMapper, null, "GET", "POST", "PUT", "PATCH", "DELETE"));
            assetProperties.set("upstreamUrl", stringSchema(objectMapper));
            assetProperties.set("authScheme", enumStringSchema(objectMapper, "上游认证方案。", "NONE", "HEADER_TOKEN", "QUERY_TOKEN"));
            assetProperties.set("authConfig", stringSchema(objectMapper, "当 authScheme 不是 NONE 时填写上游认证配置。", null));
            assetProperties.set("requestTemplate", stringSchema(objectMapper));
            assetProperties.set("requestExample", stringSchema(objectMapper));
            assetProperties.set("responseExample", stringSchema(objectMapper));
            assetProperties.set("requestJsonSchema", stringSchema(objectMapper));
            assetProperties.set("responseJsonSchema", stringSchema(objectMapper));
            assetProperties.set("publishAfterImport", booleanSchema(objectMapper));
            assetProperties.set("asyncTaskConfig", buildAsyncTaskSchema(objectMapper));
            assetProperties.set("aiProfile", buildAiProfileSchema(objectMapper));
        }, strictRequired
                ? new String[]{"apiCode", "assetName", "assetType"}
                : new String[]{"apiCode"});
    }

    static ObjectNode buildAsyncTaskSchema(ObjectMapper objectMapper) {
        ObjectNode asyncTaskConfig = strictObjectSchema(objectMapper, asyncTaskProperties -> {
            asyncTaskProperties.set("enabled", booleanSchema(objectMapper));
            asyncTaskProperties.set("queryMethod", enumStringSchema(objectMapper, null, "GET", "POST"));
            asyncTaskProperties.set("queryUrlTemplate", stringSchema(
                    objectMapper,
                    "异步查询 URL 模板。请使用 {taskId} 作为任务 ID 占位符。",
                    ".*\\{taskId\\}.*"));
            asyncTaskProperties.set("authMode", enumStringSchema(objectMapper, null, "SAME_AS_SUBMIT", "OVERRIDE"));
            asyncTaskProperties.set("authScheme", enumStringSchema(objectMapper, "异步查询认证方案。", "NONE", "HEADER_TOKEN", "QUERY_TOKEN"));
            asyncTaskProperties.set("authConfig", stringSchema(objectMapper, "当 authMode 为 OVERRIDE 时填写异步查询认证配置。", null));
            asyncTaskProperties.set("statusPath", stringSchema(objectMapper));
            asyncTaskProperties.set("resultPath", stringSchema(objectMapper));
            asyncTaskProperties.set("errorPath", stringSchema(objectMapper));
        });
        asyncTaskConfig.put("description", "提交后查询类 API 的异步任务查询配置。");
        return asyncTaskConfig;
    }

    static ObjectNode buildAiProfileSchema(ObjectMapper objectMapper) {
        return strictObjectSchema(objectMapper, aiProfileProperties -> {
            aiProfileProperties.set("provider", stringSchema(objectMapper));
            aiProfileProperties.set("model", stringSchema(objectMapper));
            aiProfileProperties.set("streamingSupported", booleanSchema(objectMapper));
            aiProfileProperties.set("capabilityTags", stringArraySchema(objectMapper));
        }, "provider", "model");
    }

    static ObjectNode buildCategoryPlanSchema(ObjectMapper objectMapper, boolean requireAction) {
        return strictObjectSchema(objectMapper, categoryProperties -> {
            categoryProperties.set("categoryCode", stringSchema(objectMapper));
            categoryProperties.set("categoryName", stringSchema(objectMapper));
            categoryProperties.set("action", enumStringSchema(objectMapper, null, "USE_EXISTING", "CREATE_IF_MISSING"));
        }, requireAction ? new String[]{"categoryCode", "action"} : new String[]{"categoryCode"});
    }

    static ObjectNode buildNamedObjectArraySchema(ObjectMapper objectMapper, String... fields) {
        return arraySchema(objectMapper, strictObjectSchema(objectMapper, propertiesNode -> {
            for (String field : fields) {
                propertiesNode.set(field, stringSchema(objectMapper));
            }
        }));
    }
}
