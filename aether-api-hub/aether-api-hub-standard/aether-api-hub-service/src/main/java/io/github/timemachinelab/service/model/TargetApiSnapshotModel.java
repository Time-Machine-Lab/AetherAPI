package io.github.timemachinelab.service.model;

/**
 * Resolved target API snapshot for a unified access invocation.
 */
public class TargetApiSnapshotModel {

    private final String assetId;
    private final String apiCode;
    private final String assetName;
    private final String assetType;
    private final String requestMethod;
    private final String upstreamUrl;
    private final String authScheme;
    private final String authConfig;
    private final boolean streamingSupported;
    private final String aiProvider;
    private final String aiModel;
    private final String proxyProfileId;
    private final ProxyProfileSnapshotModel proxyProfile;
    private final AsyncTaskConfigModel asyncTaskConfig;

    public TargetApiSnapshotModel(
            String assetId,
            String apiCode,
            String assetName,
            String assetType,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            String authConfig,
            boolean streamingSupported,
            String aiProvider,
            String aiModel) {
        this(
                assetId,
                apiCode,
                assetName,
                assetType,
                requestMethod,
                upstreamUrl,
                authScheme,
                authConfig,
                streamingSupported,
                aiProvider,
                aiModel,
                null,
                null,
                null
        );
    }

    public TargetApiSnapshotModel(
            String assetId,
            String apiCode,
            String assetName,
            String assetType,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            String authConfig,
            boolean streamingSupported,
            String aiProvider,
            String aiModel,
            String proxyProfileId,
            ProxyProfileSnapshotModel proxyProfile) {
        this(
                assetId,
                apiCode,
                assetName,
                assetType,
                requestMethod,
                upstreamUrl,
                authScheme,
                authConfig,
                streamingSupported,
                aiProvider,
                aiModel,
                proxyProfileId,
                proxyProfile,
                null
        );
    }

    public TargetApiSnapshotModel(
            String assetId,
            String apiCode,
            String assetName,
            String assetType,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            String authConfig,
            boolean streamingSupported,
            String aiProvider,
            String aiModel,
            String proxyProfileId,
            ProxyProfileSnapshotModel proxyProfile,
            AsyncTaskConfigModel asyncTaskConfig) {
        this.assetId = assetId;
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
        this.streamingSupported = streamingSupported;
        this.aiProvider = aiProvider;
        this.aiModel = aiModel;
        this.proxyProfileId = proxyProfileId;
        this.proxyProfile = proxyProfile;
        this.asyncTaskConfig = asyncTaskConfig;
    }

    public String getAssetId() {
        return assetId;
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

    public boolean isStreamingSupported() {
        return streamingSupported;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public String getAiModel() {
        return aiModel;
    }

    public String getProxyProfileId() {
        return proxyProfileId;
    }

    public ProxyProfileSnapshotModel getProxyProfile() {
        return proxyProfile;
    }

    public AsyncTaskConfigModel getAsyncTaskConfig() {
        return asyncTaskConfig;
    }
}
