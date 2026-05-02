package io.github.timemachinelab.domain.subscription.model;

/**
 * API subscription domain exception.
 */
public class ApiSubscriptionDomainException extends RuntimeException {

    public ApiSubscriptionDomainException(String message) {
        super(message);
    }

    public ApiSubscriptionDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
