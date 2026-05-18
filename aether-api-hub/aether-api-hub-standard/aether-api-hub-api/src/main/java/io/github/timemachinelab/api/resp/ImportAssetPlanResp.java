package io.github.timemachinelab.api.resp;

/**
 * Import asset plan response.
 */
public class ImportAssetPlanResp {

    private final String apiCode;
    private final String assetName;
    private final String assetType;
    private final String categoryCode;
    private final String requestMethod;
    private final String upstreamUrl;
    private final String authScheme;
    private final String authConfig;
    private final String requestTemplate;
    private final String requestExample;
    private final String responseExample;
    private final String requestJsonSchema;
    private final String responseJsonSchema;
    private final boolean publishAfterImport;
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
            ImportAiProfileResp aiProfile) {
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
        this.aiProfile = aiProfile;
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

    public String getRequestJsonSchema() {
        return requestJsonSchema;
    }

    public String getResponseJsonSchema() {
        return responseJsonSchema;
    }

    public boolean isPublishAfterImport() {
        return publishAfterImport;
    }

    public ImportAiProfileResp getAiProfile() {
        return aiProfile;
    }
}