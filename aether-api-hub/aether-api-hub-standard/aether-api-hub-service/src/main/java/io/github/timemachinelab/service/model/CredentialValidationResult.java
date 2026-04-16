package io.github.timemachinelab.service.model;

import io.github.timemachinelab.domain.consumerauth.model.CredentialValidationFailureReason;

/**
 * Internal credential validation result for unified access.
 */
public class CredentialValidationResult {

    private final boolean valid;
    private final ConsumerContextModel consumerContext;
    private final CredentialValidationFailureReason failureReason;
    private final String failureMessage;

    public CredentialValidationResult(
            boolean valid,
            ConsumerContextModel consumerContext,
            CredentialValidationFailureReason failureReason,
            String failureMessage) {
        this.valid = valid;
        this.consumerContext = consumerContext;
        this.failureReason = failureReason;
        this.failureMessage = failureMessage;
    }

    public static CredentialValidationResult valid(ConsumerContextModel consumerContext) {
        return new CredentialValidationResult(true, consumerContext, null, null);
    }

    public static CredentialValidationResult invalid(
            CredentialValidationFailureReason failureReason,
            String failureMessage,
            ConsumerContextModel consumerContext) {
        return new CredentialValidationResult(false, consumerContext, failureReason, failureMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public ConsumerContextModel getConsumerContext() {
        return consumerContext;
    }

    public CredentialValidationFailureReason getFailureReason() {
        return failureReason;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
}
