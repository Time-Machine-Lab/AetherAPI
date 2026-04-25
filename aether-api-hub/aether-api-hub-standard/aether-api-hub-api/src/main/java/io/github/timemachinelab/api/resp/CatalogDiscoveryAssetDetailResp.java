package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;

/**
 * Discovery asset detail response.
 */
public class CatalogDiscoveryAssetDetailResp {

    @JsonProperty("apiCode")
    private String apiCode;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("category")
    private CatalogDiscoveryCategoryResp category;

    @JsonProperty("publisher")
    private CatalogDiscoveryPublisherResp publisher;

    @JsonProperty("publishedAt")
    private String publishedAt;

    @JsonProperty("requestMethod")
    private RequestMethod requestMethod;

    @JsonProperty("authScheme")
    private AuthScheme authScheme;

    @JsonProperty("requestTemplate")
    private String requestTemplate;

    @JsonProperty("exampleSnapshot")
    private CatalogDiscoveryExampleSnapshotResp exampleSnapshot;

    @JsonProperty("aiCapabilityProfile")
    private CatalogDiscoveryAiCapabilityProfileResp aiCapabilityProfile;

    public CatalogDiscoveryAssetDetailResp() {
    }

    public CatalogDiscoveryAssetDetailResp(
            String apiCode,
            String assetName,
            AssetType assetType,
            CatalogDiscoveryCategoryResp category,
            CatalogDiscoveryPublisherResp publisher,
            String publishedAt,
            RequestMethod requestMethod,
            AuthScheme authScheme,
            String requestTemplate,
            CatalogDiscoveryExampleSnapshotResp exampleSnapshot,
            CatalogDiscoveryAiCapabilityProfileResp aiCapabilityProfile) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.category = category;
        this.publisher = publisher;
        this.publishedAt = publishedAt;
        this.requestMethod = requestMethod;
        this.authScheme = authScheme;
        this.requestTemplate = requestTemplate;
        this.exampleSnapshot = exampleSnapshot;
        this.aiCapabilityProfile = aiCapabilityProfile;
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

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public CatalogDiscoveryCategoryResp getCategory() {
        return category;
    }

    public void setCategory(CatalogDiscoveryCategoryResp category) {
        this.category = category;
    }

    public CatalogDiscoveryPublisherResp getPublisher() {
        return publisher;
    }

    public void setPublisher(CatalogDiscoveryPublisherResp publisher) {
        this.publisher = publisher;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    public void setAuthScheme(AuthScheme authScheme) {
        this.authScheme = authScheme;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    public CatalogDiscoveryExampleSnapshotResp getExampleSnapshot() {
        return exampleSnapshot;
    }

    public void setExampleSnapshot(CatalogDiscoveryExampleSnapshotResp exampleSnapshot) {
        this.exampleSnapshot = exampleSnapshot;
    }

    public CatalogDiscoveryAiCapabilityProfileResp getAiCapabilityProfile() {
        return aiCapabilityProfile;
    }

    public void setAiCapabilityProfile(CatalogDiscoveryAiCapabilityProfileResp aiCapabilityProfile) {
        this.aiCapabilityProfile = aiCapabilityProfile;
    }
}
