package io.github.timemachinelab.api.error;

/**
 * API subscription module error codes.
 */
public final class ApiSubscriptionErrorCodes {

    private ApiSubscriptionErrorCodes() {
    }

    public static final String API_SUBSCRIPTION_NOT_FOUND = "API_SUBSCRIPTION_NOT_FOUND";
    public static final String API_SUBSCRIPTION_INVALID = "API_SUBSCRIPTION_INVALID";
    public static final String API_SUBSCRIPTION_ALREADY_CANCELLED = "API_SUBSCRIPTION_ALREADY_CANCELLED";
    public static final String API_SUBSCRIPTION_OWNER_ACCESS = "API_SUBSCRIPTION_OWNER_ACCESS";
    public static final String API_SUBSCRIPTION_REQUIRED = "API_SUBSCRIPTION_REQUIRED";
}
