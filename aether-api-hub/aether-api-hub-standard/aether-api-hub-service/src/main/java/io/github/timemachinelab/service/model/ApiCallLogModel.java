package io.github.timemachinelab.service.model;

/**
 * API call log summary model.
 */
public class ApiCallLogModel {

    private final String logId;
    private final String targetApiCode;
    private final String targetApiName;
    private final String requestMethod;
    private final String invocationTime;
    private final Long durationMs;
    private final String resultType;
    private final boolean success;
    private final Integer httpStatusCode;

    public ApiCallLogModel(
            String logId,
            String targetApiCode,
            String targetApiName,
            String requestMethod,
            String invocationTime,
            Long durationMs,
            String resultType,
            boolean success,
            Integer httpStatusCode) {
        this.logId = logId;
        this.targetApiCode = targetApiCode;
        this.targetApiName = targetApiName;
        this.requestMethod = requestMethod;
        this.invocationTime = invocationTime;
        this.durationMs = durationMs;
        this.resultType = resultType;
        this.success = success;
        this.httpStatusCode = httpStatusCode;
    }

    public String getLogId() {
        return logId;
    }

    public String getTargetApiCode() {
        return targetApiCode;
    }

    public String getTargetApiName() {
        return targetApiName;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getInvocationTime() {
        return invocationTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public String getResultType() {
        return resultType;
    }

    public boolean isSuccess() {
        return success;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }
}
