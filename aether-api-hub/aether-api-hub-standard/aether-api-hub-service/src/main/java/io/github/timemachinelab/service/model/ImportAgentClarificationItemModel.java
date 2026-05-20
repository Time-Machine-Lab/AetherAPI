package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Structured clarification item model.
 */
public class ImportAgentClarificationItemModel {

    private final String id;
    private final String targetPath;
    private final String fieldKey;
    private final String label;
    private final String description;
    private final String inputType;
    private final boolean required;
    private final List<ImportAgentClarificationOptionModel> options;
    private final String currentValue;
    private final String defaultValue;
    private final String defaultLabel;
    private final String defaultSource;
    private final String defaultConfidence;

    public ImportAgentClarificationItemModel(
            String id,
            String targetPath,
            String fieldKey,
            String label,
            String description,
            String inputType,
            boolean required,
            List<ImportAgentClarificationOptionModel> options,
            String currentValue) {
        this(
                id,
                targetPath,
                fieldKey,
                label,
                description,
                inputType,
                required,
                options,
                currentValue,
                null,
                null,
                null,
                null);
    }

    public ImportAgentClarificationItemModel(
            String id,
            String targetPath,
            String fieldKey,
            String label,
            String description,
            String inputType,
            boolean required,
            List<ImportAgentClarificationOptionModel> options,
            String currentValue,
            String defaultValue,
            String defaultLabel,
            String defaultSource,
            String defaultConfidence) {
        this.id = id;
        this.targetPath = targetPath;
        this.fieldKey = fieldKey;
        this.label = label;
        this.description = description;
        this.inputType = inputType;
        this.required = required;
        this.options = options == null ? List.of() : List.copyOf(options);
        this.currentValue = currentValue;
        this.defaultValue = defaultValue;
        this.defaultLabel = defaultLabel;
        this.defaultSource = defaultSource;
        this.defaultConfidence = defaultConfidence;
    }

    public String getId() {
        return id;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getInputType() {
        return inputType;
    }

    public boolean isRequired() {
        return required;
    }

    public List<ImportAgentClarificationOptionModel> getOptions() {
        return options;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }

    public String getDefaultSource() {
        return defaultSource;
    }

    public String getDefaultConfidence() {
        return defaultConfidence;
    }
}
