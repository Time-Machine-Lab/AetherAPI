package io.github.timemachinelab.domain.consumerauth.service;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.CredentialValidationFailureReason;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain validation for credential lifecycle and consumer availability.
 */
public class CredentialValidationDomainService {

    public CredentialValidationFailureReason validate(
            ApiCredentialAggregate credential, ConsumerAggregate consumer, Instant now) {
        Objects.requireNonNull(credential, "ApiCredentialAggregate must not be null");
        Objects.requireNonNull(consumer, "ConsumerAggregate must not be null");
        Objects.requireNonNull(now, "Validation time must not be null");

        CredentialValidationFailureReason credentialFailure = credential.validateForAccess(now);
        if (credentialFailure != null) {
            return credentialFailure;
        }
        if (!consumer.isAvailable()) {
            return CredentialValidationFailureReason.CONSUMER_UNAVAILABLE;
        }
        return null;
    }
}
