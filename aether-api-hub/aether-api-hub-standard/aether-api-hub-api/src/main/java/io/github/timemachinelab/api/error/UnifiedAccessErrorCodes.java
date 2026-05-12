package io.github.timemachinelab.api.error;

/**
 * Unified access module error codes.
 */
public final class UnifiedAccessErrorCodes {

    private UnifiedAccessErrorCodes() {
    }

    public static final String TARGET_API_UNAVAILABLE = "TARGET_API_UNAVAILABLE";
    public static final String API_SUBSCRIPTION_REQUIRED = "API_SUBSCRIPTION_REQUIRED";
    public static final String ASYNC_TASK_QUERY_UNAVAILABLE = "ASYNC_TASK_QUERY_UNAVAILABLE";
    public static final String INVALID_TASK_ID = "INVALID_TASK_ID";
    public static final String UPSTREAM_EXECUTION_FAILURE = "UPSTREAM_EXECUTION_FAILURE";
    public static final String UPSTREAM_TIMEOUT = "UPSTREAM_TIMEOUT";
}
