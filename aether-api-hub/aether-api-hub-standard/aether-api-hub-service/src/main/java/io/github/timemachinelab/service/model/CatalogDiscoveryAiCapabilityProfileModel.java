package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Discovery AI capability profile model.
 */
public class CatalogDiscoveryAiCapabilityProfileModel {

    private final String provider;
    private final String model;
    private final Boolean streamingSupported;
    private final List<String> capabilityTags;

    public CatalogDiscoveryAiCapabilityProfileModel(
            String provider,
            String model,
            Boolean streamingSupported,
            List<String> capabilityTags) {
        this.provider = provider;
        this.model = model;
        this.streamingSupported = streamingSupported;
        this.capabilityTags = capabilityTags == null ? List.of() : List.copyOf(capabilityTags);
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public Boolean getStreamingSupported() {
        return streamingSupported;
    }

    public List<String> getCapabilityTags() {
        return capabilityTags;
    }
}
