package io.github.timemachinelab.api.resp;

import java.util.List;

/**
 * Import asset plan response.
 */
public class ImportAssetPlanResp {

    private final String action;
    private final String apiCode;
    private final ImportExistingAssetSummaryResp matchedExistingAsset;
    private final String assetName;
    private final String assetType;
    private final String categoryCode;
    private final String requestMethod;
    private final String upstreamUrl;
    private final String authScheme;
    private final String authConfig;
    private final List<ImportUpstreamRequestHeaderResp> upstreamRequestHeaders;
    private final String requestTemplate;
    private final String requestExample;
    private final String responseExample;
    private final String requestJsonSchema;
    private final String responseJsonSchema;
    private final boolean publishAfterImport;
    private final List<String> changedFields;
    private final AsyncTaskConfigResp asyncTaskConfig;
    private final ImportAiProfileResp aiProfile;

    public ImportAssetPlanResp(
            String apiCode,
            String assetName,
            String assetType,
            String categoryCode,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            String authConfig,
            String requestTemplate,
            String requestExample,
            String responseExample,
            String requestJsonSchema,
            String responseJsonSchema,
            boolean publishAfterImport,
            AsyncTaskConfigResp asyncTaskConfig,
            ImportAiProfileResp aiProfile) {
        this(
                apiCode,
                assetName,
                assetType,
                categoryCode,
                requestMethod,
                upstreamUrl,
                authScheme,
                authConfig,
                null,
                requestTemplate,
                requestExample,
                responseExample,
                requestJsonSchema,
                responseJsonSchema,
                publishAfterImport,
                asyncTaskConfig,
                aiProfile
        );
    }

    public ImportAssetPlanResp(
            String apiCode,
            String assetName,
            String assetType,
            String categoryCode,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            String authConfig,
            List<ImportUpstreamRequestHeaderResp> upstreamRequestHeaders,
            String requestTemplate,
            String requestExample,
            String responseExample,
            String requestJsonSchema,
            String responseJsonSchema,
            boolean publishAfterImport,
            AsyncTaskConfigResp asyncTaskConfig,
            ImportAiProfileResp aiProfile) {
        this(
                null,
                apiCode,
                null,
                assetName,
                assetType,
                categoryCode,
                requestMethod,
                upstreamUrl,
                authScheme,
                authConfig,
                upstreamRequestHeaders,
                requestTemplate,
                requestExample,
                responseExample,
                requestJsonSchema,
                responseJsonSchema,
                publishAfterImport,
                null,
                asyncTaskConfig,
                aiProfile
        );
    }

    public ImportAssetPlanResp(
            String action,
            String apiCode,
            ImportExistingAssetSummaryResp matchedExistingAsset,
            String assetName,
            String assetType,
            String categoryCode,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            String authConfig,
            List<ImportUpstreamRequestHeaderResp> upstreamRequestHeaders,
            String requestTemplate,
            String requestExample,
            String responseExample,
            String requestJsonSchema,
            String responseJsonSchema,
            boolean publishAfterImport,
            List<String> changedFields,
            AsyncTaskConfigResp asyncTaskConfig,
            ImportAiProfileResp aiProfile) {
        this.action = action;
        this.apiCode = apiCode;
        this.matchedExistingAsset = matchedExistingAsset;
        this.assetName = assetName;
        this.assetType = assetType;
        this.categoryCode = categoryCode;
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
        this.upstreamRequestHeaders = upstreamRequestHeaders;
        this.requestTemplate = requestTemplate;
        this.requestExample = requestExample;
        this.responseExample = responseExample;
        this.requestJsonSchema = requestJsonSchema;
        this.responseJsonSchema = responseJsonSchema;
        this.publishAfterImport = publishAfterImport;
        this.changedFields = changedFields == null ? null : List.copyOf(changedFields);
        this.asyncTaskConfig = asyncTaskConfig;
        this.aiProfile = aiProfile;
    }

    public String getAction() {
        return action;
    }

    public String getApiCode() {
        return apiCode;
    }

    public ImportExistingAssetSummaryResp getMatchedExistingAsset() {
        return matchedExistingAsset;
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

    public List<ImportUpstreamRequestHeaderResp> getUpstreamRequestHeaders() {
        return upstreamRequestHeaders;
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

    public List<String> getChangedFields() {
        return changedFields;
    }

    public AsyncTaskConfigResp getAsyncTaskConfig() {
        return asyncTaskConfig;
    }

    public ImportAiProfileResp getAiProfile() {
        return aiProfile;
    }
}
