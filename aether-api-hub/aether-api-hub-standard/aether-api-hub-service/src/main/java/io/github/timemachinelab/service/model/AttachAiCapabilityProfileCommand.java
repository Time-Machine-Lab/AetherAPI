package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Attach AI capability profile command.
 */
public class AttachAiCapabilityProfileCommand {

    private final String ownerUserId;
    private final String publisherDisplayName;
    private final String apiCode;
    private final String provider;
    private final String model;
    private final boolean streamingSupported;
    private final List<String> capabilityTags;

    public AttachAiCapabilityProfileCommand(
            String ownerUserId,
            String publisherDisplayName,
            String apiCode,
            String provider,
            String model,
            boolean streamingSupported,
            List<String> capabilityTags) {
        this.ownerUserId = ownerUserId;
        this.publisherDisplayName = publisherDisplayName;
        this.apiCode = apiCode;
        this.provider = provider;
        this.model = model;
        this.streamingSupported = streamingSupported;
        this.capabilityTags = capabilityTags;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public String getApiCode() {
        return apiCode;
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
