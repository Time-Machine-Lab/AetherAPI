package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.CredentialValidationFailureReason;
import io.github.timemachinelab.domain.consumerauth.service.CredentialValidationDomainService;
import io.github.timemachinelab.service.model.ConsumerContextModel;
import io.github.timemachinelab.service.model.CredentialValidationResult;
import io.github.timemachinelab.service.model.ValidateApiCredentialCommand;
import io.github.timemachinelab.service.port.in.CredentialValidationUseCase;
import io.github.timemachinelab.service.port.out.ApiCredentialRepositoryPort;
import io.github.timemachinelab.service.port.out.ConsumerIdentityRepositoryPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Credential validation use case for unified access.
 */
public class CredentialValidationApplicationService implements CredentialValidationUseCase {

    private static final String DEFAULT_ACCESS_CHANNEL = "UNIFIED_ACCESS";
    private static final String SUCCESS_RESULT = "SUCCESS";

    private final ApiCredentialRepositoryPort apiCredentialRepositoryPort;
    private final ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort;
    private final CredentialValidationDomainService credentialValidationDomainService;

    public CredentialValidationApplicationService(
            ApiCredentialRepositoryPort apiCredentialRepositoryPort,
            ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort,
            CredentialValidationDomainService credentialValidationDomainService) {
        this.apiCredentialRepositoryPort = apiCredentialRepositoryPort;
        this.consumerIdentityRepositoryPort = consumerIdentityRepositoryPort;
        this.credentialValidationDomainService = credentialValidationDomainService;
    }

    @Override
    public CredentialValidationResult validateApiCredential(ValidateApiCredentialCommand command) {
        String plaintextKey = normalizePlaintextKey(command.getPlaintextKey());
        String accessChannel = normalizeAccessChannel(command.getAccessChannel());
        ApiCredentialAggregate credential = apiCredentialRepositoryPort.findByFingerprintHash(sha256Hex(plaintextKey))
                .orElse(null);
        if (credential == null) {
            return CredentialValidationResult.invalid(
                    CredentialValidationFailureReason.CREDENTIAL_NOT_FOUND,
                    failureMessage(CredentialValidationFailureReason.CREDENTIAL_NOT_FOUND),
                    null
            );
        }

        ConsumerAggregate consumer = consumerIdentityRepositoryPort.findById(credential.getConsumerId()).orElse(null);
        ConsumerContextModel context = consumer == null ? null : toConsumerContext(credential, consumer);
        if (consumer == null) {
            credential.markUsed(accessChannel, CredentialValidationFailureReason.CONSUMER_UNAVAILABLE.name());
            apiCredentialRepositoryPort.save(credential);
            return CredentialValidationResult.invalid(
                    CredentialValidationFailureReason.CONSUMER_UNAVAILABLE,
                    failureMessage(CredentialValidationFailureReason.CONSUMER_UNAVAILABLE),
                    context
            );
        }

        CredentialValidationFailureReason failureReason = credentialValidationDomainService.validate(
                credential, consumer, Instant.now());
        if (failureReason == null) {
            credential.markUsed(accessChannel, SUCCESS_RESULT);
            apiCredentialRepositoryPort.save(credential);
            return CredentialValidationResult.valid(toConsumerContext(credential, consumer));
        }

        credential.markUsed(accessChannel, failureReason.name());
        apiCredentialRepositoryPort.save(credential);
        return CredentialValidationResult.invalid(
                failureReason,
                failureMessage(failureReason),
                toConsumerContext(credential, consumer)
        );
    }

    private ConsumerContextModel toConsumerContext(ApiCredentialAggregate credential, ConsumerAggregate consumer) {
        return new ConsumerContextModel(
                consumer.getId().getValue(),
                consumer.getCode().getValue(),
                consumer.getName(),
                consumer.getType().name(),
                credential.getId().getValue(),
                credential.getCode().getValue(),
                credential.getStatus().name(),
                credential.getKeyFingerprint().getPrefix(),
                credential.getKeyFingerprint().getMaskedKey()
        );
    }

    private String normalizePlaintextKey(String plaintextKey) {
        if (plaintextKey == null || plaintextKey.isBlank()) {
            throw new IllegalArgumentException("Credential plaintext key must not be blank");
        }
        return plaintextKey.trim();
    }

    private String normalizeAccessChannel(String accessChannel) {
        if (accessChannel == null || accessChannel.isBlank()) {
            return DEFAULT_ACCESS_CHANNEL;
        }
        return accessChannel.trim().toUpperCase(Locale.ROOT);
    }

    private String failureMessage(CredentialValidationFailureReason failureReason) {
        return switch (failureReason) {
            case CREDENTIAL_NOT_FOUND -> "API credential was not found";
            case CREDENTIAL_DISABLED -> "API credential is disabled";
            case CREDENTIAL_REVOKED -> "API credential is revoked";
            case CREDENTIAL_EXPIRED -> "API credential is expired";
            case CONSUMER_UNAVAILABLE -> "Consumer is unavailable";
        };
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}
