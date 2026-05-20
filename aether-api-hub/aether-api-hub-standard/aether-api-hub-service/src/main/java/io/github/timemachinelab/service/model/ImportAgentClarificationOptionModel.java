package io.github.timemachinelab.service.model;

/**
 * Structured clarification option model.
 */
public class ImportAgentClarificationOptionModel {

    private final String value;
    private final String label;

    public ImportAgentClarificationOptionModel(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
