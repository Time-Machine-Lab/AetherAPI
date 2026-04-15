package io.github.timemachinelab.infrastructure.catalog.persistence.query;

/**
 * Catalog discovery query record.
 */
public class CatalogDiscoveryAssetRecord {

    private String status;
    private String apiCode;
    private String assetName;
    private String assetType;
    private String categoryCode;
    private String categoryName;
    private String requestMethod;
    private String authScheme;
    private String requestTemplate;
    private String requestExample;
    private String responseExample;
    private String aiProvider;
    private String aiModel;
    private Boolean aiStreamingSupported;
    private String aiCapabilityTagsJson;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getAuthScheme() {
        return authScheme;
    }

    public void setAuthScheme(String authScheme) {
        this.authScheme = authScheme;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    public String getRequestExample() {
        return requestExample;
    }

    public void setRequestExample(String requestExample) {
        this.requestExample = requestExample;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public void setResponseExample(String responseExample) {
        this.responseExample = responseExample;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public Boolean getAiStreamingSupported() {
        return aiStreamingSupported;
    }

    public void setAiStreamingSupported(Boolean aiStreamingSupported) {
        this.aiStreamingSupported = aiStreamingSupported;
    }

    public String getAiCapabilityTagsJson() {
        return aiCapabilityTagsJson;
    }

    public void setAiCapabilityTagsJson(String aiCapabilityTagsJson) {
        this.aiCapabilityTagsJson = aiCapabilityTagsJson;
    }
}
