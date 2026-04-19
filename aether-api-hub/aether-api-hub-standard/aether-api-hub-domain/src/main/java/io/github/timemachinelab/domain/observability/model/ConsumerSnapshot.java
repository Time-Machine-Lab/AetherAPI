package io.github.timemachinelab.domain.observability.model;

/**
 * Caller snapshot captured in a platform call log.
 */
public final class ConsumerSnapshot {

    private final String consumerId;
    private final String consumerCode;
    private final String consumerName;
    private final String consumerType;
    private final String credentialId;
    private final String credentialCode;
    private final String credentialStatus;

    private ConsumerSnapshot(
            String consumerId,
            String consumerCode,
            String consumerName,
            String consumerType,
            String credentialId,
            String credentialCode,
            String credentialStatus) {
        this.consumerId = normalize(consumerId, 36);
        this.consumerCode = normalize(consumerCode, 64);
        this.consumerName = normalize(consumerName, 128);
        this.consumerType = normalize(consumerType, 32);
        this.credentialId = normalize(credentialId, 36);
        this.credentialCode = normalize(credentialCode, 64);
        this.credentialStatus = normalize(credentialStatus, 20);
    }

    public static ConsumerSnapshot of(
            String consumerId,
            String consumerCode,
            String consumerName,
            String consumerType,
            String credentialId,
            String credentialCode,
            String credentialStatus) {
        return new ConsumerSnapshot(
                consumerId, consumerCode, consumerName, consumerType, credentialId, credentialCode, credentialStatus);
    }

    public String getConsumerId() {
        return consumerId;
    }

    public String getConsumerCode() {
        return consumerCode;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public String getCredentialCode() {
        return credentialCode;
    }

    public String getCredentialStatus() {
        return credentialStatus;
    }

    private static String normalize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException("Consumer snapshot field must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }
}
