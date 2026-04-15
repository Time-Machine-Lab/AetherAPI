package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AI 能力档案响应。
 */
public class AiCapabilityProfileResp {

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("model")
    private String model;

    @JsonProperty("streamingSupported")
    private Boolean streamingSupported;

    @JsonProperty("capabilityTags")
    private List<String> capabilityTags;

    public AiCapabilityProfileResp() {
    }

    public AiCapabilityProfileResp(String provider, String model, Boolean streamingSupported, List<String> capabilityTags) {
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

    public Boolean getStreamingSupported() {
        return streamingSupported;
    }

    public void setStreamingSupported(Boolean streamingSupported) {
        this.streamingSupported = streamingSupported;
    }

    public List<String> getCapabilityTags() {
        return capabilityTags;
    }

    public void setCapabilityTags(List<String> capabilityTags) {
        this.capabilityTags = capabilityTags;
    }
}

