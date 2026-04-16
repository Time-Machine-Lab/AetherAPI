package io.github.timemachinelab.domain.consumerauth.model;

/**
 * Unified access credential validation failure reasons.
 */
public enum CredentialValidationFailureReason {
    CREDENTIAL_NOT_FOUND,
    CREDENTIAL_DISABLED,
    CREDENTIAL_REVOKED,
    CREDENTIAL_EXPIRED,
    CONSUMER_UNAVAILABLE
}
