package io.github.timemachinelab.infrastructure.importagent.planner.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlanningToolSupportTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("submit plan schema should expose final plan fields")
    void shouldExposeFinalPlanFields() {
        JsonNode schema = new SubmitImportPlanPlanningTool()
                .buildDefinition(OBJECT_MAPPER, "submit_import_plan")
                .path("function")
                .path("parameters");

        assertEquals(false, schema.path("additionalProperties").asBoolean());
        assertTrue(schema.path("required").toString().contains("assetPlans"));
        JsonNode assetSchema = schema.path("properties").path("assetPlans").path("items");
        assertEquals(3, assetSchema.path("properties").path("authScheme").path("enum").size());
        assertEquals(2, assetSchema.path("properties").path("aiProfile").path("required").size());
        assertEquals(".*\\{taskId\\}.*", assetSchema.path("properties").path("asyncTaskConfig")
                .path("properties").path("queryUrlTemplate").path("pattern").asText());
    }
}
