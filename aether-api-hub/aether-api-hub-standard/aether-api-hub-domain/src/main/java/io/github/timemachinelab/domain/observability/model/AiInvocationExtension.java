package io.github.timemachinelab.domain.observability.model;

/**
 * Nullable AI observability extension fields reserved in phase one.
 */
public final class AiInvocationExtension {

    private final String provider;
    private final String model;
    private final Boolean streaming;
    private final String usageSnapshot;
    private final String billingReserved;

    private AiInvocationExtension(
            String provider,
            String model,
            Boolean streaming,
            String usageSnapshot,
            String billingReserved) {
        this.provider = normalize(provider, 64);
        this.model = normalize(model, 128);
        this.streaming = streaming;
        this.usageSnapshot = normalize(usageSnapshot, 512);
        this.billingReserved = normalize(billingReserved, 512);
    }

    public static AiInvocationExtension of(
            String provider,
            String model,
            Boolean streaming,
            String usageSnapshot,
            String billingReserved) {
        return new AiInvocationExtension(provider, model, streaming, usageSnapshot, billingReserved);
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public Boolean getStreaming() {
        return streaming;
    }

    public String getUsageSnapshot() {
        return usageSnapshot;
    }

    public String getBillingReserved() {
        return billingReserved;
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
            throw new IllegalArgumentException("AI extension field must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }
}
