package io.github.timemachinelab.api.error;

/**
 * Consumer & Auth 模块错误码。
 */
public final class ConsumerAuthErrorCodes {

    private ConsumerAuthErrorCodes() {
    }

    public static final String CURRENT_USER_REQUIRED = "CURRENT_USER_REQUIRED";
    public static final String API_CREDENTIAL_INVALID = "API_CREDENTIAL_INVALID";
    public static final String API_CREDENTIAL_NOT_FOUND = "API_CREDENTIAL_NOT_FOUND";
    public static final String API_CREDENTIAL_ALREADY_ENABLED = "API_CREDENTIAL_ALREADY_ENABLED";
    public static final String API_CREDENTIAL_ALREADY_DISABLED = "API_CREDENTIAL_ALREADY_DISABLED";
    public static final String API_CREDENTIAL_ALREADY_REVOKED = "API_CREDENTIAL_ALREADY_REVOKED";
    public static final String API_CREDENTIAL_EXPIRED = "API_CREDENTIAL_EXPIRED";
    public static final String API_CREDENTIAL_REVOKED = "API_CREDENTIAL_REVOKED";
    public static final String CONSUMER_UNAVAILABLE = "CONSUMER_UNAVAILABLE";
}
