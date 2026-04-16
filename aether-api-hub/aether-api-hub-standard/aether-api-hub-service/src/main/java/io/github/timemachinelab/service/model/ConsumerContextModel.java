package io.github.timemachinelab.service.model;

/**
 * Structured consumer context for unified access.
 */
public class ConsumerContextModel {

    private final String consumerId;
    private final String consumerCode;
    private final String consumerName;
    private final String consumerType;
    private final String credentialId;
    private final String credentialCode;
    private final String credentialStatus;
    private final String keyPrefix;
    private final String maskedKey;

    public ConsumerContextModel(
            String consumerId,
            String consumerCode,
            String consumerName,
            String consumerType,
            String credentialId,
            String credentialCode,
            String credentialStatus,
            String keyPrefix,
            String maskedKey) {
        this.consumerId = consumerId;
        this.consumerCode = consumerCode;
        this.consumerName = consumerName;
        this.consumerType = consumerType;
        this.credentialId = credentialId;
        this.credentialCode = credentialCode;
        this.credentialStatus = credentialStatus;
        this.keyPrefix = keyPrefix;
        this.maskedKey = maskedKey;
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

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getMaskedKey() {
        return maskedKey;
    }
}
