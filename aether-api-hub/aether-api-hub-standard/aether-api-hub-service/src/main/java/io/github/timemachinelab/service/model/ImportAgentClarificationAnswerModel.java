package io.github.timemachinelab.service.model;

/**
 * Structured clarification answer model.
 */
public class ImportAgentClarificationAnswerModel {

    private final String clarificationId;
    private final String targetPath;
    private final String fieldKey;
    private final String value;

    public ImportAgentClarificationAnswerModel(
            String clarificationId,
            String targetPath,
            String fieldKey,
            String value) {
        this.clarificationId = clarificationId;
        this.targetPath = targetPath;
        this.fieldKey = fieldKey;
        this.value = value;
    }

    public String getClarificationId() {
        return clarificationId;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getValue() {
        return value;
    }
}
