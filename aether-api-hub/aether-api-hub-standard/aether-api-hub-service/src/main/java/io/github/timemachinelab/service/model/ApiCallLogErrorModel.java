package io.github.timemachinelab.service.model;

/**
 * API call log error model.
 */
public class ApiCallLogErrorModel {

    private final String errorCode;
    private final String errorType;
    private final String errorSummary;

    public ApiCallLogErrorModel(String errorCode, String errorType, String errorSummary) {
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.errorSummary = errorSummary;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorSummary() {
        return errorSummary;
    }
}
