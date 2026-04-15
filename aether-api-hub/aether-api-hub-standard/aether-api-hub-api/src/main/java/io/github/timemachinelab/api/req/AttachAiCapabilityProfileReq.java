package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 绑定 AI 能力档案请求。
 */
public class AttachAiCapabilityProfileReq {

    @NotBlank(message = "Provider must not be blank")
    @Size(min = 1, max = 128, message = "Provider must be 1-128 characters")
    @JsonProperty("provider")
    private String provider;

    @NotBlank(message = "Model must not be blank")
    @Size(min = 1, max = 128, message = "Model must be 1-128 characters")
    @JsonProperty("model")
    private String model;

    @JsonProperty("streamingSupported")
    private boolean streamingSupported;

    @NotEmpty(message = "Capability tags must not be empty")
    @JsonProperty("capabilityTags")
    private List<String> capabilityTags;

    public AttachAiCapabilityProfileReq() {
    }

    public AttachAiCapabilityProfileReq(
            String provider, String model, boolean streamingSupported, List<String> capabilityTags) {
        this.provider = provider;
        this.model = model;
        this.streamingSupported = streamingSupported;
        this.capabilityTags = capabilityTags;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isStreamingSupported() {
        return streamingSupported;
    }

    public void setStreamingSupported(boolean streamingSupported) {
        this.streamingSupported = streamingSupported;
    }

    public List<String> getCapabilityTags() {
        return capabilityTags;
    }

    public void setCapabilityTags(List<String> capabilityTags) {
        this.capabilityTags = capabilityTags;
    }
}

