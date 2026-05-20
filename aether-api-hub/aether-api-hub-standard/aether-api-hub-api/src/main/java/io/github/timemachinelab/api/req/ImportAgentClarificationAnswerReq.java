package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.Size;

/**
 * Import agent structured clarification answer request.
 */
public class ImportAgentClarificationAnswerReq {

    @Size(max = 256)
    private String clarificationId;

    @Size(max = 512)
    private String targetPath;

    @Size(max = 128)
    private String fieldKey;

    @Size(max = 64000)
    private String value;

    public String getClarificationId() {
        return clarificationId;
    }

    public void setClarificationId(String clarificationId) {
        this.clarificationId = clarificationId;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
