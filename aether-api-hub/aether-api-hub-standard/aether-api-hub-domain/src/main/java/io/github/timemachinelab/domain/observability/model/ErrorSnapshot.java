package io.github.timemachinelab.domain.observability.model;

/**
 * Error snapshot captured for a failed platform call log.
 */
public final class ErrorSnapshot {

    private final String errorCode;
    private final String errorType;
    private final String errorSummary;

    private ErrorSnapshot(String errorCode, String errorType, String errorSummary) {
        this.errorCode = normalize(errorCode, 64);
        this.errorType = normalize(errorType, 64);
        this.errorSummary = normalize(errorSummary, 512);
    }

    public static ErrorSnapshot of(String errorCode, String errorType, String errorSummary) {
        return new ErrorSnapshot(errorCode, errorType, errorSummary);
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

    private static String normalize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException("Error snapshot field must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }
}
