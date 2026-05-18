package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Import AI profile model.
 */
public class ImportAiProfileModel {

    private final String provider;
    private final String model;
    private final boolean streamingSupported;
    private final List<String> capabilityTags;

    public ImportAiProfileModel(String provider, String model, boolean streamingSupported, List<String> capabilityTags) {
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

    public boolean isStreamingSupported() {
        return streamingSupported;
    }

    public List<String> getCapabilityTags() {
        return capabilityTags;
    }
}