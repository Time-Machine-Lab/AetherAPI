package io.github.timemachinelab.service.model;

/**
 * Import step result model.
 */
public class ImportStepResultModel {

    private final ImportAgentStepType stepType;
    private final String targetRef;
    private final ImportStepResultStatus status;
    private final String message;

    public ImportStepResultModel(
            ImportAgentStepType stepType,
            String targetRef,
            ImportStepResultStatus status,
            String message) {
        this.stepType = stepType;
        this.targetRef = targetRef;
        this.status = status;
        this.message = message;
    }

    public ImportAgentStepType getStepType() {
        return stepType;
    }

    public String getTargetRef() {
        return targetRef;
    }

    public ImportStepResultStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}