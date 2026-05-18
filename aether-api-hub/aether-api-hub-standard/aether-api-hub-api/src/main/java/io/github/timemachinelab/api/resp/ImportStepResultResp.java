package io.github.timemachinelab.api.resp;

/**
 * Import step result response.
 */
public class ImportStepResultResp {

    private final String stepType;
    private final String targetRef;
    private final String status;
    private final String message;

    public ImportStepResultResp(String stepType, String targetRef, String status, String message) {
        this.stepType = stepType;
        this.targetRef = targetRef;
        this.status = status;
        this.message = message;
    }

    public String getStepType() {
        return stepType;
    }

    public String getTargetRef() {
        return targetRef;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}