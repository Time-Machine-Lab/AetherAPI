package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API call log error response.
 */
public class ApiCallLogErrorResp {

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("errorType")
    private String errorType;

    @JsonProperty("errorSummary")
    private String errorSummary;

    public ApiCallLogErrorResp() {
    }

    public ApiCallLogErrorResp(String errorCode, String errorType, String errorSummary) {
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.errorSummary = errorSummary;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public void setErrorSummary(String errorSummary) {
        this.errorSummary = errorSummary;
    }
}
