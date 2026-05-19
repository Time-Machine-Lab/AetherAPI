package io.github.timemachinelab.infrastructure.importagent.planner;

/**
 * Import agent LLM planner properties.
 */
public class ImportAgentLlmPlannerProperties {

    private boolean enabled;
    private String baseUrl;
    private String endpointPath;
    private String apiKey;
    private String model;
    private Double temperature;
    private Integer maxCompletionTokens;
    private Integer requestTimeoutSeconds;
    private String systemPrompt;
    private boolean toolCallingEnabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEndpointPath() {
        return endpointPath;
    }

    public void setEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxCompletionTokens() {
        return maxCompletionTokens;
    }

    public void setMaxCompletionTokens(Integer maxCompletionTokens) {
        this.maxCompletionTokens = maxCompletionTokens;
    }

    public Integer getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(Integer requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public boolean isToolCallingEnabled() {
        return toolCallingEnabled;
    }

    public void setToolCallingEnabled(boolean toolCallingEnabled) {
        this.toolCallingEnabled = toolCallingEnabled;
    }
}