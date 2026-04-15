package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * 绑定 AI 能力档案命令。
 */
public class AttachAiCapabilityProfileCommand {

    private final String apiCode;
    private final String provider;
    private final String model;
    private final boolean streamingSupported;
    private final List<String> capabilityTags;

    public AttachAiCapabilityProfileCommand(
            String apiCode, String provider, String model, boolean streamingSupported, List<String> capabilityTags) {
        this.apiCode = apiCode;
        this.provider = provider;
        this.model = model;
        this.streamingSupported = streamingSupported;
        this.capabilityTags = capabilityTags;
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

