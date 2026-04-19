package io.github.timemachinelab.service.model;

/**
 * API call log AI extension model.
 */
public class ApiCallLogAiExtensionModel {

    private final String provider;
    private final String model;
    private final Boolean streaming;
    private final String usageSnapshot;

    public ApiCallLogAiExtensionModel(String provider, String model, Boolean streaming, String usageSnapshot) {
        this.provider = provider;
        this.model = model;
        this.streaming = streaming;
        this.usageSnapshot = usageSnapshot;
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
}
