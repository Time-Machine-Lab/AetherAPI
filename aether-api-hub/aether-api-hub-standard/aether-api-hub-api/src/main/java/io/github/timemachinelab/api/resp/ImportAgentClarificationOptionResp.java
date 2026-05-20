package io.github.timemachinelab.api.resp;

/**
 * Import agent structured clarification option response.
 */
public class ImportAgentClarificationOptionResp {

    private final String value;
    private final String label;

    public ImportAgentClarificationOptionResp(String value, String label) {
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
