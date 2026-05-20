package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlanningToolSupportTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("submit plan schema should encode execution-critical conditional constraints")
    void shouldEncodeExecutionCriticalConditionalConstraints() {
        JsonNode schema = new SubmitImportPlanPlanningTool()
                .buildDefinition(OBJECT_MAPPER, "submit_import_plan")
                .path("function")
                .path("parameters")
                .path("properties")
                .path("assetPlans")
                .path("items");

        assertTrue(schema.has("additionalProperties"));
        assertEquals(false, schema.path("additionalProperties").asBoolean());
        assertEquals(3, schema.path("properties").path("authScheme").path("enum").size());
        assertEquals(2, schema.path("properties").path("aiProfile").path("required").size());
        assertEquals("provider", schema.path("properties").path("aiProfile").path("required").get(0).asText());
        assertEquals("model", schema.path("properties").path("aiProfile").path("required").get(1).asText());

        JsonNode assetConditions = schema.path("allOf");
        assertTrue(assetConditions.isArray());
        assertTrue(hasConditionalRequirement(assetConditions, "authScheme", "authConfig"));
        assertTrue(hasConditionalRequirement(assetConditions, "publishAfterImport", "requestMethod"));
        assertTrue(hasConditionalRequirement(assetConditions, "publishAfterImport", "aiProfile"));

        JsonNode asyncSchema = schema.path("properties").path("asyncTaskConfig");
        assertEquals(".*\\{taskId\\}.*", asyncSchema.path("properties").path("queryUrlTemplate").path("pattern").asText());
        JsonNode asyncConditions = asyncSchema.path("allOf");
        assertTrue(asyncConditions.isArray());
        assertTrue(hasConditionalRequirement(asyncConditions, "enabled", "authMode"));
        assertTrue(hasConditionalRequirement(asyncConditions, "authMode", "authScheme"));
        assertTrue(hasConditionalRequirement(asyncConditions, "authMode", "authConfig"));
    }

    private static boolean hasConditionalRequirement(JsonNode conditions, String ifField, String requiredField) {
        for (JsonNode condition : conditions) {
            JsonNode ifRequired = condition.path("if").path("required");
            JsonNode thenRequired = condition.path("then").path("required");
            if (containsText(ifRequired, ifField) && containsText(thenRequired, requiredField)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsText(JsonNode array, String expected) {
        for (JsonNode value : array) {
            if (expected.equals(value.asText())) {
                return true;
            }
        }
        return false;
    }
}