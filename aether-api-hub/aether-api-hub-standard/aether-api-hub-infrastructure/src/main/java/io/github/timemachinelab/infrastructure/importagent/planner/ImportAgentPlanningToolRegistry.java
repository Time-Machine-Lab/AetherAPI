package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Registry of planner tool schemas grouped by planning stage.
 */
@Component
public class ImportAgentPlanningToolRegistry {

    private final Map<PlannerStage, List<ImportAgentPlanningToolDescriptor>> toolsByStage;

    public ImportAgentPlanningToolRegistry(List<ImportAgentPlanningTool> tools) {
        this.toolsByStage = buildIndex(tools);
    }

    public static ImportAgentPlanningToolRegistry defaultRegistry() {
        return new ImportAgentPlanningToolRegistry(List.of(
                new ExtractImportFactsPlanningTool(),
                new FillImportSlotsPlanningTool(),
                new SubmitImportPlanPlanningTool()
        ));
    }

    public ArrayNode buildTools(PlannerStage stage, ObjectMapper objectMapper) {
        ArrayNode tools = objectMapper.createArrayNode();
        for (ImportAgentPlanningToolDescriptor descriptor : getTools(stage)) {
            tools.add(descriptor.tool().buildDefinition(objectMapper, descriptor.name()));
        }
        return tools;
    }

    public List<ImportAgentPlanningToolDescriptor> getTools(PlannerStage stage) {
        Objects.requireNonNull(stage, "Planner stage must not be null");
        List<ImportAgentPlanningToolDescriptor> descriptors = toolsByStage.get(stage);
        if (descriptors == null || descriptors.isEmpty()) {
            throw new IllegalStateException("No planning tools registered for stage " + stage);
        }
        return descriptors;
    }

    public String primaryToolName(PlannerStage stage) {
        return primaryTool(stage).name();
    }

    public ImportAgentPlanningToolDescriptor primaryTool(PlannerStage stage) {
        return getTools(stage).get(0);
    }

    private Map<PlannerStage, List<ImportAgentPlanningToolDescriptor>> buildIndex(List<ImportAgentPlanningTool> tools) {
        if (tools == null || tools.isEmpty()) {
            throw new IllegalStateException("At least one planning tool must be registered");
        }
        Map<PlannerStage, List<ImportAgentPlanningToolDescriptor>> indexed = new EnumMap<>(PlannerStage.class);
        Map<String, ImportAgentPlanningTool> names = new HashMap<>();
        for (ImportAgentPlanningTool tool : tools) {
            ImportAgentPlanningToolDescriptor descriptor = buildDescriptor(tool);
            ImportAgentPlanningTool previous = names.putIfAbsent(descriptor.name(), descriptor.tool());
            if (previous != null) {
                throw new IllegalStateException("Duplicate import-agent planning tool name: " + descriptor.name());
            }
            indexed.computeIfAbsent(descriptor.stage(), ignored -> new ArrayList<>()).add(descriptor);
        }
        for (Map.Entry<PlannerStage, List<ImportAgentPlanningToolDescriptor>> entry : indexed.entrySet()) {
            entry.getValue().sort(Comparator
                    .comparingInt(ImportAgentPlanningToolDescriptor::order)
                    .thenComparing(ImportAgentPlanningToolDescriptor::name));
            entry.setValue(List.copyOf(entry.getValue()));
        }
        for (PlannerStage stage : Arrays.asList(PlannerStage.values())) {
            if (!indexed.containsKey(stage) || indexed.get(stage).isEmpty()) {
                throw new IllegalStateException("Missing import-agent planning tool for stage " + stage);
            }
        }
        return Map.copyOf(indexed);
    }

    private ImportAgentPlanningToolDescriptor buildDescriptor(ImportAgentPlanningTool tool) {
        Objects.requireNonNull(tool, "Planning tool must not be null");
        ImportAgentToolSpec spec = AnnotationUtils.findAnnotation(tool.getClass(), ImportAgentToolSpec.class);
        if (spec == null) {
            throw new IllegalStateException("Planning tool is missing @ImportAgentToolSpec: " + tool.getClass().getName());
        }
        if (!hasText(spec.name())) {
            throw new IllegalStateException("Planning tool name must not be blank: " + tool.getClass().getName());
        }
        if (spec.stage() == null) {
            throw new IllegalStateException("Planning tool stage must not be null: " + tool.getClass().getName());
        }
        return new ImportAgentPlanningToolDescriptor(spec.name().trim(), spec.stage(), spec.order(), tool);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}