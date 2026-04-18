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

    public TargetApiSnapshotModel(
            String assetId,
            String apiCode,
            String assetName,
            String assetType,
            String requestMethod,
            String upstreamUrl,
            String authScheme,
            String authConfig,
            boolean streamingSupported) {
        this.assetId = assetId;
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
        this.streamingSupported = streamingSupported;
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
}
