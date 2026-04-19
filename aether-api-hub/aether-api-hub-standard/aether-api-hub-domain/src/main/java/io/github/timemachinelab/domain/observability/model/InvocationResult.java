package io.github.timemachinelab.domain.observability.model;

import java.util.Locale;

/**
 * Result snapshot captured for a platform call log.
 */
public final class InvocationResult {

    private final String resultType;
    private final boolean success;
    private final Integer httpStatusCode;

    private InvocationResult(String resultType, boolean success, Integer httpStatusCode) {
        this.resultType = normalizeResultType(resultType);
        this.success = success;
        this.httpStatusCode = httpStatusCode;
    }

    public static InvocationResult success(String resultType, Integer httpStatusCode) {
        return new InvocationResult(resultType, true, httpStatusCode);
    }

    public static InvocationResult failure(String resultType, Integer httpStatusCode) {
        return new InvocationResult(resultType, false, httpStatusCode);
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

    private static String normalizeResultType(String resultType) {
        if (resultType == null || resultType.isBlank()) {
            throw new IllegalArgumentException("Invocation result type must not be blank");
        }
        String normalized = resultType.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 32) {
            throw new IllegalArgumentException("Invocation result type must not exceed 32 characters");
        }
        return normalized;
    }
}
