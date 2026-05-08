package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import java.time.LocalDateTime;

/**
 * Platform proxy asset binding candidate query record.
 */
public class PlatformProxyAssetCandidateQueryRecord {

    private String apiCode;
    private String assetName;
    private String assetType;
    private String status;
    private String publisherDisplayName;
    private String proxyProfileId;
    private String proxyProfileCode;
    private String proxyProfileName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getApiCode() { return apiCode; }
    public void setApiCode(String apiCode) { this.apiCode = apiCode; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPublisherDisplayName() { return publisherDisplayName; }
    public void setPublisherDisplayName(String publisherDisplayName) { this.publisherDisplayName = publisherDisplayName; }
    public String getProxyProfileId() { return proxyProfileId; }
    public void setProxyProfileId(String proxyProfileId) { this.proxyProfileId = proxyProfileId; }
    public String getProxyProfileCode() { return proxyProfileCode; }
    public void setProxyProfileCode(String proxyProfileCode) { this.proxyProfileCode = proxyProfileCode; }
    public String getProxyProfileName() { return proxyProfileName; }
    public void setProxyProfileName(String proxyProfileName) { this.proxyProfileName = proxyProfileName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
