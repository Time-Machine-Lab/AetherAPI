package io.github.timemachinelab.service.model;

/**
 * Failure categories that happen before upstream forwarding begins.
 */
public enum PlatformPreForwardFailureType {
    INVALID_API_CODE,
    INVALID_CREDENTIAL,
    TARGET_NOT_FOUND,
    TARGET_UNAVAILABLE
}
