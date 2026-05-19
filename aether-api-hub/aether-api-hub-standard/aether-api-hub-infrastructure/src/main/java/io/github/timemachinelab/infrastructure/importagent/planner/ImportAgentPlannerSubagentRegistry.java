package io.github.timemachinelab.infrastructure.importagent.planner;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for internal planner subagents.
 */
@Component
public class ImportAgentPlannerSubagentRegistry {

    private final List<ImportAgentPlannerSubagentDescriptor> descriptors;

    public ImportAgentPlannerSubagentRegistry(List<ImportAgentPlannerSubagent> subagents) {
        this.descriptors = buildDescriptors(subagents);
    }

    public static ImportAgentPlannerSubagentRegistry defaultRegistry() {
        return new ImportAgentPlannerSubagentRegistry(List.of(
                new DocumentFactsPlannerSubagent(),
                new AuthRecognitionPlannerSubagent(),
                new AsyncPatternPlannerSubagent(),
                new PlanReviewPlannerSubagent(),
                new ClarificationStrategyPlannerSubagent()
        ));
    }

    public List<ImportAgentPlannerSubagentDescriptor> getSubagents() {
        return descriptors;
    }

    private List<ImportAgentPlannerSubagentDescriptor> buildDescriptors(List<ImportAgentPlannerSubagent> subagents) {
        if (subagents == null || subagents.isEmpty()) {
            throw new IllegalStateException("At least one planner subagent must be registered");
        }
        List<ImportAgentPlannerSubagentDescriptor> resolved = new ArrayList<>();
        Map<String, ImportAgentPlannerSubagent> names = new HashMap<>();
        for (ImportAgentPlannerSubagent subagent : subagents) {
            ImportAgentPlannerSubagentDescriptor descriptor = buildDescriptor(subagent);
            ImportAgentPlannerSubagent previous = names.putIfAbsent(descriptor.name(), descriptor.subagent());
            if (previous != null) {
                throw new IllegalStateException("Duplicate import-agent planner subagent name: " + descriptor.name());
            }
            resolved.add(descriptor);
        }
        resolved.sort(Comparator
                .comparingInt(ImportAgentPlannerSubagentDescriptor::order)
                .thenComparing(ImportAgentPlannerSubagentDescriptor::name));
        return List.copyOf(resolved);
    }

    private ImportAgentPlannerSubagentDescriptor buildDescriptor(ImportAgentPlannerSubagent subagent) {
        Objects.requireNonNull(subagent, "Planner subagent must not be null");
        ImportAgentPlannerSubagentSpec spec = AnnotationUtils.findAnnotation(subagent.getClass(), ImportAgentPlannerSubagentSpec.class);
        if (spec == null) {
            throw new IllegalStateException("Planner subagent is missing @ImportAgentPlannerSubagentSpec: " + subagent.getClass().getName());
        }
        if (spec.name() == null || spec.name().isBlank()) {
            throw new IllegalStateException("Planner subagent name must not be blank: " + subagent.getClass().getName());
        }
        if (spec.role() == null) {
            throw new IllegalStateException("Planner subagent role must not be null: " + subagent.getClass().getName());
        }
        return new ImportAgentPlannerSubagentDescriptor(spec.name().trim(), spec.role(), spec.order(), subagent);
    }
}