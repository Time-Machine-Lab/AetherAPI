package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API call log AI extension response.
 */
public class ApiCallLogAiExtensionResp {

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("model")
    private String model;

    @JsonProperty("streaming")
    private Boolean streaming;

    @JsonProperty("usageSnapshot")
    private String usageSnapshot;

    public ApiCallLogAiExtensionResp() {
    }

    public ApiCallLogAiExtensionResp(String provider, String model, Boolean streaming, String usageSnapshot) {
        this.provider = provider;
        this.model = model;
        this.streaming = streaming;
        this.usageSnapshot = usageSnapshot;
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

    public Boolean getStreaming() {
        return streaming;
    }

    public void setStreaming(Boolean streaming) {
        this.streaming = streaming;
    }

    public String getUsageSnapshot() {
        return usageSnapshot;
    }

    public void setUsageSnapshot(String usageSnapshot) {
        this.usageSnapshot = usageSnapshot;
    }
}
