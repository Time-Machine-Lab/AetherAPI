package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlanningToolRegistryTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("registry should build submit-plan schema from registered tool definition")
    void shouldBuildSubmitPlanSchemaFromRegisteredTool() {
        ImportAgentPlanningToolRegistry registry = ImportAgentPlanningToolRegistry.defaultRegistry();

        ArrayNode tools = registry.buildTools(PlannerStage.SUBMIT_PLAN, OBJECT_MAPPER);

        assertEquals(1, tools.size());
        assertEquals("submit_import_plan", tools.get(0).path("function").path("name").asText());
        assertTrue(tools.get(0)
                .path("function")
                .path("parameters")
                .path("required")
                .toString()
                .contains("assetPlans"));
    }

    @Test
    @DisplayName("registry should reject duplicate tool names during initialization")
    void shouldRejectDuplicateToolNames() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new ImportAgentPlanningToolRegistry(List.of(
                        new DuplicateExtractToolOne(),
                        new DuplicateExtractToolTwo(),
                        new FillImportSlotsPlanningTool(),
                        new SubmitImportPlanPlanningTool()
                )));

        assertTrue(exception.getMessage().contains("Duplicate import-agent planning tool name"));
    }

    @Test
    @DisplayName("registry should reject tools without registration annotation")
    void shouldRejectToolsWithoutRegistrationAnnotation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new ImportAgentPlanningToolRegistry(List.of(
                        new NoAnnotationTool(),
                        new FillImportSlotsPlanningTool(),
                        new SubmitImportPlanPlanningTool(),
                        new ExtractImportFactsPlanningTool()
                )));

        assertTrue(exception.getMessage().contains("missing @ImportAgentToolSpec"));
    }

    @Test
    @DisplayName("registry should order tools deterministically within the same stage")
    void shouldOrderToolsDeterministicallyWithinStage() {
        ImportAgentPlanningToolRegistry registry = new ImportAgentPlanningToolRegistry(List.of(
                new OrderedExtractToolLater(),
                new OrderedExtractToolEarlier(),
                new FillImportSlotsPlanningTool(),
                new SubmitImportPlanPlanningTool()
        ));

        List<ImportAgentPlanningToolDescriptor> tools = registry.getTools(PlannerStage.EXTRACT_FACTS);

        assertEquals(List.of("ordered_extract_earlier", "ordered_extract_later"),
                tools.stream().map(ImportAgentPlanningToolDescriptor::name).toList());
        assertEquals("ordered_extract_earlier", registry.primaryToolName(PlannerStage.EXTRACT_FACTS));
    }

    @ImportAgentToolSpec(name = "duplicate_extract", stage = PlannerStage.EXTRACT_FACTS, order = 0)
    private static final class DuplicateExtractToolOne implements ImportAgentPlanningTool {
        @Override
        public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
            return tool(objectMapper, toolName);
        }
    }

    @ImportAgentToolSpec(name = "duplicate_extract", stage = PlannerStage.EXTRACT_FACTS, order = 1)
    private static final class DuplicateExtractToolTwo implements ImportAgentPlanningTool {
        @Override
        public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
            return tool(objectMapper, toolName);
        }
    }

    @ImportAgentToolSpec(name = "ordered_extract_later", stage = PlannerStage.EXTRACT_FACTS, order = 10)
    private static final class OrderedExtractToolLater implements ImportAgentPlanningTool {
        @Override
        public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
            return tool(objectMapper, toolName);
        }
    }

    @ImportAgentToolSpec(name = "ordered_extract_earlier", stage = PlannerStage.EXTRACT_FACTS, order = 1)
    private static final class OrderedExtractToolEarlier implements ImportAgentPlanningTool {
        @Override
        public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
            return tool(objectMapper, toolName);
        }
    }

    private static final class NoAnnotationTool implements ImportAgentPlanningTool {
        @Override
        public ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName) {
            return tool(objectMapper, toolName);
        }
    }

    private static ObjectNode tool(ObjectMapper objectMapper, String toolName) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("type", "function");
        tool.putObject("function").put("name", toolName);
        return tool;
    }
}