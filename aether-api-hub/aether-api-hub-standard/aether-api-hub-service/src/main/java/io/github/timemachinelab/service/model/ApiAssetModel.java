package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * API 资产应用层模型。
 */
public class ApiAssetModel {

    private final String id;
    private final String apiCode;
    private final String assetName;
    private final String assetType;
    private final String categoryCode;
    private final String status;
    private final String requestMethod;
    private final String upstreamUrl;
    private final String authScheme;
    private final String authConfig;
    private final String requestTemplate;
    private final String requestExample;
    private final String responseExample;
    private final String aiProvider;
    private final String aiModel;
    private final Boolean aiStreamingSupported;
    private final List<String> aiCapabilityTags;
    private final String createdAt;
    private final String updatedAt;

    public ApiAssetModel(
            String id,
            String apiCode,
            String assetName,
            String assetType,
            String categoryCode,
            String status,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            String authConfig,
            String requestTemplate,
            String requestExample,
            String responseExample,
            String aiProvider,
            String aiModel,
            Boolean aiStreamingSupported,
            List<String> aiCapabilityTags,
            String createdAt,
            String updatedAt) {
        this.id = id;
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.categoryCode = categoryCode;
        this.status = status;
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
        this.requestTemplate = requestTemplate;
        this.requestExample = requestExample;
        this.responseExample = responseExample;
        this.aiProvider = aiProvider;
        this.aiModel = aiModel;
        this.aiStreamingSupported = aiStreamingSupported;
        this.aiCapabilityTags = aiCapabilityTags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getStatus() {
        return status;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public String getAuthScheme() {
        return authScheme;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public String getRequestExample() {
        return requestExample;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public String getAiModel() {
        return aiModel;
    }

    public Boolean getAiStreamingSupported() {
        return aiStreamingSupported;
    }

    public List<String> getAiCapabilityTags() {
        return aiCapabilityTags;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}

