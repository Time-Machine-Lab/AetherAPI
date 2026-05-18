package io.github.timemachinelab.api.resp;

import java.util.List;

/**
 * Import AI profile response.
 */
public class ImportAiProfileResp {

    private final String provider;
    private final String model;
    private final boolean streamingSupported;
    private final List<String> capabilityTags;

    public ImportAiProfileResp(String provider, String model, boolean streamingSupported, List<String> capabilityTags) {
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