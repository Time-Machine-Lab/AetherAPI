package io.github.timemachinelab.service.model;

/**
 * Structured platform-side failure result for unified access pre-forward rejection.
 */
public class UnifiedAccessPlatformFailureModel {

    private final String code;
    private final String message;
    private final PlatformPreForwardFailureType failureType;
    private final String apiCode;
    private final int httpStatus;
    private final ConsumerContextModel consumerContext;

    public UnifiedAccessPlatformFailureModel(
            String code,
            String message,
            PlatformPreForwardFailureType failureType,
            String apiCode,
            int httpStatus) {
        this(code, message, failureType, apiCode, httpStatus, null);
    }

    public UnifiedAccessPlatformFailureModel(
            String code,
            String message,
            PlatformPreForwardFailureType failureType,
            String apiCode,
            int httpStatus,
            ConsumerContextModel consumerContext) {
        this.code = code;
        this.message = message;
        this.failureType = failureType;
        this.apiCode = apiCode;
        this.httpStatus = httpStatus;
        this.consumerContext = consumerContext;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public PlatformPreForwardFailureType getFailureType() {
        return failureType;
    }

    public String getApiCode() {
        return apiCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public ConsumerContextModel getConsumerContext() {
        return consumerContext;
    }
}
