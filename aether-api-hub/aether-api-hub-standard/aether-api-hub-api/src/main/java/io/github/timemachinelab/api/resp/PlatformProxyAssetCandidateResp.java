package io.github.timemachinelab.api.resp;

/**
 * Platform proxy asset binding candidate response.
 */
public class PlatformProxyAssetCandidateResp {

    private final String apiCode;
    private final String assetName;
    private final String assetType;
    private final String status;
    private final String publisherDisplayName;
    private final String proxyProfileId;
    private final String proxyProfileCode;
    private final String proxyProfileName;
    private final String createdAt;
    private final String updatedAt;

    public PlatformProxyAssetCandidateResp(
            String apiCode,
            String assetName,
            String assetType,
            String status,
            String publisherDisplayName,
            String proxyProfileId,
            String proxyProfileCode,
            String proxyProfileName,
            String createdAt,
            String updatedAt) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.status = status;
        this.publisherDisplayName = publisherDisplayName;
        this.proxyProfileId = proxyProfileId;
        this.proxyProfileCode = proxyProfileCode;
        this.proxyProfileName = proxyProfileName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getApiCode() { return apiCode; }
    public String getAssetName() { return assetName; }
    public String getAssetType() { return assetType; }
    public String getStatus() { return status; }
    public String getPublisherDisplayName() { return publisherDisplayName; }
    public String getProxyProfileId() { return proxyProfileId; }
    public String getProxyProfileCode() { return proxyProfileCode; }
    public String getProxyProfileName() { return proxyProfileName; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
