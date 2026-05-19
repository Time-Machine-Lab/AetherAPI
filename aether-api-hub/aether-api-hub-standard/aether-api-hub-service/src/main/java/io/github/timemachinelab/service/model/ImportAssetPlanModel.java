package io.github.timemachinelab.service.model;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;

/**
 * Import asset plan model.
 */
public class ImportAssetPlanModel {

    private final String apiCode;
    private final String assetName;
    private final AssetType assetType;
    private final String categoryCode;
    private final RequestMethod requestMethod;
    private final String upstreamUrl;
    private final AuthScheme authScheme;
    private final String authConfig;
    private final String requestTemplate;
    private final String requestExample;
    private final String responseExample;
    private final String requestJsonSchema;
    private final String responseJsonSchema;
    private final boolean publishAfterImport;
    private final AsyncTaskConfigModel asyncTaskConfig;
    private final ImportAiProfileModel aiProfile;

    public ImportAssetPlanModel(
            String apiCode,
            String assetName,
            AssetType assetType,
            String categoryCode,
            RequestMethod requestMethod,
            String upstreamUrl,
            AuthScheme authScheme,
            String authConfig,
            String requestTemplate,
            String requestExample,
            String responseExample,
            String requestJsonSchema,
            String responseJsonSchema,
            boolean publishAfterImport,
            AsyncTaskConfigModel asyncTaskConfig,
            ImportAiProfileModel aiProfile) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.categoryCode = categoryCode;
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
        this.requestTemplate = requestTemplate;
        this.requestExample = requestExample;
        this.responseExample = responseExample;
        this.requestJsonSchema = requestJsonSchema;
        this.responseJsonSchema = responseJsonSchema;
        this.publishAfterImport = publishAfterImport;
        this.asyncTaskConfig = asyncTaskConfig;
        this.aiProfile = aiProfile;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public AuthScheme getAuthScheme() {
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

    public String getRequestJsonSchema() {
        return requestJsonSchema;
    }

    public String getResponseJsonSchema() {
        return responseJsonSchema;
    }

    public boolean isPublishAfterImport() {
        return publishAfterImport;
    }

    public AsyncTaskConfigModel getAsyncTaskConfig() {
        return asyncTaskConfig;
    }

    public ImportAiProfileModel getAiProfile() {
        return aiProfile;
    }
}
