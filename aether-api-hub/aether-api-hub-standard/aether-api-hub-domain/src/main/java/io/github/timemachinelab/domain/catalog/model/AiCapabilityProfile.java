package io.github.timemachinelab.domain.catalog.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI 能力档案值对象。
 */
public final class AiCapabilityProfile {

    private final String provider;
    private final String model;
    private final boolean streamingSupported;
    private final List<String> capabilityTags;

    private AiCapabilityProfile(String provider, String model, boolean streamingSupported, List<String> capabilityTags) {
        this.provider = provider;
        this.model = model;
        this.streamingSupported = streamingSupported;
        this.capabilityTags = capabilityTags;
    }

    public static AiCapabilityProfile of(String provider, String model, boolean streamingSupported, List<String> capabilityTags) {
        String normalizedProvider = normalize(provider, "provider");
        String normalizedModel = normalize(model, "model");
        if (capabilityTags == null || capabilityTags.isEmpty()) {
            throw new IllegalArgumentException("AI capability tags must not be empty");
        }
        List<String> normalizedTags = capabilityTags.stream()
                .map(tag -> normalize(tag, "capabilityTag"))
                .collect(Collectors.toList());
        return new AiCapabilityProfile(normalizedProvider, normalizedModel, streamingSupported, List.copyOf(normalizedTags));
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public boolean isStreamingSupported() {
        return streamingSupported;
    }

    public List<String> getCapabilityTags() {
        return capabilityTags;
    }

    private static String normalize(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiCapabilityProfile that = (AiCapabilityProfile) o;
        return streamingSupported == that.streamingSupported
                && Objects.equals(provider, that.provider)
                && Objects.equals(model, that.model)
                && Objects.equals(capabilityTags, that.capabilityTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, model, streamingSupported, capabilityTags);
    }
}

