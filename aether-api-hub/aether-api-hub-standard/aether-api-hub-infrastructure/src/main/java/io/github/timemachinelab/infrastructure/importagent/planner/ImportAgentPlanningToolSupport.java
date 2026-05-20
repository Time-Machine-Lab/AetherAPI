package io.github.timemachinelab.infrastructure.importagent.planner;

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

    static ObjectNode constBooleanSchema(ObjectMapper objectMapper, boolean value) {
        return objectMapper.createObjectNode().put("const", value);
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

    static void appendConditionalRequired(
            ObjectMapper objectMapper,
            ObjectNode schema,
            Consumer<ObjectNode> ifPropertiesCustomizer,
            String[] ifRequiredProperties,
            Consumer<ObjectNode> thenPropertiesCustomizer,
            String... thenRequiredProperties) {
        ArrayNode allOf = schema.has("allOf") && schema.get("allOf").isArray()
                ? (ArrayNode) schema.get("allOf")
                : schema.putArray("allOf");
        ObjectNode condition = allOf.addObject();

        ObjectNode ifNode = condition.putObject("if");
        ifNode.put("type", "object");
        ObjectNode ifProperties = ifNode.putObject("properties");
        ifPropertiesCustomizer.accept(ifProperties);
        if (ifRequiredProperties != null && ifRequiredProperties.length > 0) {
            ArrayNode required = ifNode.putArray("required");
            for (String field : ifRequiredProperties) {
                required.add(field);
            }
        }

        ObjectNode thenNode = condition.putObject("then");
        thenNode.put("type", "object");
        ObjectNode thenProperties = thenNode.putObject("properties");
        thenPropertiesCustomizer.accept(thenProperties);
        if (thenRequiredProperties != null && thenRequiredProperties.length > 0) {
            ArrayNode required = thenNode.putArray("required");
            for (String field : thenRequiredProperties) {
                required.add(field);
            }
        }
    }

    static ObjectNode buildAssetPlanSchema(ObjectMapper objectMapper, boolean strictRequired) {
        ObjectNode schema = strictObjectSchema(objectMapper, assetProperties -> {
            assetProperties.set("apiCode", stringSchema(objectMapper));
            assetProperties.set("assetName", stringSchema(objectMapper));
            assetProperties.set("assetType", enumStringSchema(objectMapper, null,
                    "STANDARD_API", "AI_API"));
            assetProperties.set("categoryCode", stringSchema(objectMapper));
            assetProperties.set("requestMethod", enumStringSchema(objectMapper, null,
                    "GET", "POST", "PUT", "PATCH", "DELETE"));
            assetProperties.set("upstreamUrl", stringSchema(objectMapper));
            assetProperties.set("authScheme", enumStringSchema(objectMapper,
                    "上游鉴权方案，只能是 NONE、HEADER_TOKEN 或 QUERY_TOKEN。",
                    "NONE", "HEADER_TOKEN", "QUERY_TOKEN"));
            assetProperties.set("authConfig", stringSchema(objectMapper)
                    .put("description", "上游安全配置。HEADER_TOKEN 示例：Authorization: Bearer <token>；QUERY_TOKEN 示例：access_token=<token>。如果 authScheme 不是 NONE，必须写在这里。"));
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
            appendConditionalRequired(
                objectMapper,
                schema,
                ifProperties -> ifProperties.set("authScheme", enumStringSchema(objectMapper, null,
                    "HEADER_TOKEN", "QUERY_TOKEN")),
                new String[]{"authScheme"},
                thenProperties -> {
                },
                "authConfig");
            appendConditionalRequired(
                objectMapper,
                schema,
                ifProperties -> ifProperties.set("publishAfterImport", constBooleanSchema(objectMapper, true)),
                new String[]{"publishAfterImport"},
                thenProperties -> {
                },
                "categoryCode", "requestMethod", "upstreamUrl", "authScheme");
            appendConditionalRequired(
                objectMapper,
                schema,
                ifProperties -> {
                    ifProperties.set("publishAfterImport", constBooleanSchema(objectMapper, true));
                    ifProperties.set("assetType", enumStringSchema(objectMapper, null, "AI_API"));
                },
                new String[]{"publishAfterImport", "assetType"},
                thenProperties -> thenProperties.set("aiProfile", buildAiProfileSchema(objectMapper)),
                "aiProfile");
            return schema;
    }

    static ObjectNode buildAsyncTaskSchema(ObjectMapper objectMapper) {
        ObjectNode asyncTaskConfig = strictObjectSchema(objectMapper, asyncTaskProperties -> {
            asyncTaskProperties.set("enabled", booleanSchema(objectMapper));
            asyncTaskProperties.set("queryMethod", enumStringSchema(objectMapper, null, "GET", "POST"));
                asyncTaskProperties.set("queryUrlTemplate", stringSchema(
                    objectMapper,
                    "任务查询 URL 模板，必须包含 {taskId} 占位符。",
                    ".*\\{taskId\\}.*"));
            asyncTaskProperties.set("authMode", enumStringSchema(objectMapper, null,
                    "SAME_AS_SUBMIT", "OVERRIDE"));
            asyncTaskProperties.set("authScheme", enumStringSchema(objectMapper,
                    "任务查询接口的上游鉴权方案。authMode 为 SAME_AS_SUBMIT 时可为空或与提交接口一致；OVERRIDE 时必须明确。",
                    "NONE", "HEADER_TOKEN", "QUERY_TOKEN"));
            asyncTaskProperties.set("authConfig", stringSchema(objectMapper)
                    .put("description", "任务查询接口的上游安全配置，仅在 authMode 为 OVERRIDE 时必须明确。"));
            asyncTaskProperties.set("statusPath", stringSchema(objectMapper));
            asyncTaskProperties.set("resultPath", stringSchema(objectMapper));
            asyncTaskProperties.set("errorPath", stringSchema(objectMapper));
        });
        asyncTaskConfig.put("description", "异步任务查询配置。上游提交任务后按 taskId 查询结果的接口应放在这里，不应单独创建资产。");
        appendConditionalRequired(
                objectMapper,
                asyncTaskConfig,
                ifProperties -> ifProperties.set("enabled", constBooleanSchema(objectMapper, true)),
                new String[]{"enabled"},
                thenProperties -> {
                },
                "queryMethod", "queryUrlTemplate", "authMode");
        appendConditionalRequired(
                objectMapper,
                asyncTaskConfig,
                ifProperties -> ifProperties.set("authMode", enumStringSchema(objectMapper, null, "OVERRIDE")),
                new String[]{"authMode"},
                thenProperties -> {
                },
                "authScheme", "authConfig");
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
            categoryProperties.set("action", enumStringSchema(objectMapper, null,
                    "USE_EXISTING", "CREATE_IF_MISSING"));
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