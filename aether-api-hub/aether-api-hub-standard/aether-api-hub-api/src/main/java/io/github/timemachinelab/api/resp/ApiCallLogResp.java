package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API call log summary response.
 */
public class ApiCallLogResp {

    @JsonProperty("logId")
    private String logId;

    @JsonProperty("targetApiCode")
    private String targetApiCode;

    @JsonProperty("targetApiName")
    private String targetApiName;

    @JsonProperty("requestMethod")
    private String requestMethod;

    @JsonProperty("invocationTime")
    private String invocationTime;

    @JsonProperty("durationMs")
    private Long durationMs;

    @JsonProperty("resultType")
    private String resultType;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("httpStatusCode")
    private Integer httpStatusCode;

    public ApiCallLogResp() {
    }

    public ApiCallLogResp(
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

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getTargetApiCode() {
        return targetApiCode;
    }

    public void setTargetApiCode(String targetApiCode) {
        this.targetApiCode = targetApiCode;
    }

    public String getTargetApiName() {
        return targetApiName;
    }

    public void setTargetApiName(String targetApiName) {
        this.targetApiName = targetApiName;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getInvocationTime() {
        return invocationTime;
    }

    public void setInvocationTime(String invocationTime) {
        this.invocationTime = invocationTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
