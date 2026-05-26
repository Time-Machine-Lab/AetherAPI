package io.github.timemachinelab.api.resp;

/**
 * Safe existing asset summary response for import-agent planning.
 */
public class ImportExistingAssetSummaryResp {

    private final String apiCode;
    private final String assetName;
    private final String assetType;
    private final String categoryCode;
    private final String status;
    private final String requestMethod;
    private final String upstreamUrl;
    private final String authScheme;
    private final boolean authConfigured;
    private final boolean asyncTaskConfigured;
    private final boolean aiProfileConfigured;
    private final String updatedAt;

    public ImportExistingAssetSummaryResp(
            String apiCode,
            String assetName,
            String assetType,
            String categoryCode,
            String status,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            boolean authConfigured,
            boolean asyncTaskConfigured,
            boolean aiProfileConfigured,
            String updatedAt) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.categoryCode = categoryCode;
        this.status = status;
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfigured = authConfigured;
        this.asyncTaskConfigured = asyncTaskConfigured;
        this.aiProfileConfigured = aiProfileConfigured;
        this.updatedAt = updatedAt;
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

    public boolean isAuthConfigured() {
        return authConfigured;
    }

    public boolean isAsyncTaskConfigured() {
        return asyncTaskConfigured;
    }

    public boolean isAiProfileConfigured() {
        return aiProfileConfigured;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
