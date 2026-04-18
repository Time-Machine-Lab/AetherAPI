package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Platform-side pre-forward failure response for unified access.
 */
public class UnifiedAccessPlatformFailureResp {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("failureType")
    private String failureType;

    @JsonProperty("traceId")
    private String traceId;

    @JsonProperty("apiCode")
    private String apiCode;

    public UnifiedAccessPlatformFailureResp() {
    }

    public UnifiedAccessPlatformFailureResp(
            String code,
            String message,
            String failureType,
            String traceId,
            String apiCode) {
        this.code = code;
        this.message = message;
        this.failureType = failureType;
        this.traceId = traceId;
        this.apiCode = apiCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFailureType() {
        return failureType;
    }

    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }
}
