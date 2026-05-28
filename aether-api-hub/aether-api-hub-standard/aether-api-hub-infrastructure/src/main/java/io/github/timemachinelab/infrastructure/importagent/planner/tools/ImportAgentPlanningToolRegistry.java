package io.github.timemachinelab.infrastructure.importagent.planner.tools;

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
    private final Map<String, ImportAgentPlanningToolDescriptor> toolsByName;

    public ImportAgentPlanningToolRegistry(List<ImportAgentPlanningTool> tools) {
        RegistryIndex index = buildIndex(tools);
        this.toolsByStage = index.toolsByStage();
        this.toolsByName = index.toolsByName();
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

    public ArrayNode buildTools(List<String> toolNames, ObjectMapper objectMapper) {
        ArrayNode tools = objectMapper.createArrayNode();
        for (ImportAgentPlanningToolDescriptor descriptor : getTools(toolNames)) {
            tools.add(descriptor.tool().buildDefinition(objectMapper, descriptor.name()));
        }
        return tools;
    }

    public List<ImportAgentPlanningToolDescriptor> getTools(PlannerStage stage) {
        Objects.requireNonNull(stage, "规划阶段不能为空");
        List<ImportAgentPlanningToolDescriptor> descriptors = toolsByStage.get(stage);
        if (descriptors == null || descriptors.isEmpty()) {
            throw new IllegalStateException("规划阶段未注册任何工具：" + stage);
        }
        return descriptors;
    }

    public String primaryToolName(PlannerStage stage) {
        return primaryTool(stage).name();
    }

    public ImportAgentPlanningToolDescriptor primaryTool(PlannerStage stage) {
        return getTools(stage).get(0);
    }

    public List<ImportAgentPlanningToolDescriptor> getTools(List<String> toolNames) {
        if (toolNames == null || toolNames.isEmpty()) {
            return List.of();
        }
        List<ImportAgentPlanningToolDescriptor> descriptors = new ArrayList<>();
        for (String toolName : toolNames) {
            ImportAgentPlanningToolDescriptor descriptor = toolsByName.get(toolName);
            if (descriptor == null) {
                throw new IllegalStateException("未知的导入规划工具：" + toolName);
            }
            descriptors.add(descriptor);
        }
        return List.copyOf(descriptors);
    }

    public ImportAgentPlanningToolDescriptor getTool(String toolName) {
        ImportAgentPlanningToolDescriptor descriptor = toolsByName.get(toolName);
        if (descriptor == null) {
            throw new IllegalStateException("未知的导入规划工具：" + toolName);
        }
        return descriptor;
    }

    private RegistryIndex buildIndex(List<ImportAgentPlanningTool> tools) {
        if (tools == null || tools.isEmpty()) {
            throw new IllegalStateException("至少需要注册一个规划工具");
        }
        Map<PlannerStage, List<ImportAgentPlanningToolDescriptor>> indexed = new EnumMap<>(PlannerStage.class);
        Map<String, ImportAgentPlanningToolDescriptor> names = new HashMap<>();
        for (ImportAgentPlanningTool tool : tools) {
            ImportAgentPlanningToolDescriptor descriptor = buildDescriptor(tool);
            ImportAgentPlanningToolDescriptor previous = names.putIfAbsent(descriptor.name(), descriptor);
            if (previous != null) {
                throw new IllegalStateException("导入规划工具名称重复：" + descriptor.name());
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
                throw new IllegalStateException("规划阶段缺少对应工具：" + stage);
            }
        }
        return new RegistryIndex(Map.copyOf(indexed), Map.copyOf(names));
    }

    private ImportAgentPlanningToolDescriptor buildDescriptor(ImportAgentPlanningTool tool) {
        Objects.requireNonNull(tool, "规划工具不能为空");
        ImportAgentToolSpec spec = AnnotationUtils.findAnnotation(tool.getClass(), ImportAgentToolSpec.class);
        if (spec == null) {
            throw new IllegalStateException("规划工具缺少 @ImportAgentToolSpec 标注：" + tool.getClass().getName());
        }
        if (!hasText(spec.name())) {
            throw new IllegalStateException("规划工具名称不能为空：" + tool.getClass().getName());
        }
        if (spec.stage() == null) {
            throw new IllegalStateException("规划工具阶段不能为空：" + tool.getClass().getName());
        }
        return new ImportAgentPlanningToolDescriptor(spec.name().trim(), spec.stage(), spec.order(), tool);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record RegistryIndex(
            Map<PlannerStage, List<ImportAgentPlanningToolDescriptor>> toolsByStage,
            Map<String, ImportAgentPlanningToolDescriptor> toolsByName) {
    }
}
