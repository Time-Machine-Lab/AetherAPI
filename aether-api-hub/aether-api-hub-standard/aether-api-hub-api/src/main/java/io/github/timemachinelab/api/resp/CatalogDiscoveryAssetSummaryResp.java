package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.domain.catalog.model.AssetType;

/**
 * Discovery asset summary response.
 */
public class CatalogDiscoveryAssetSummaryResp {

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

    public CatalogDiscoveryAssetSummaryResp() {
    }

    public CatalogDiscoveryAssetSummaryResp(
            String apiCode,
            String assetName,
            AssetType assetType,
            CatalogDiscoveryCategoryResp category,
            CatalogDiscoveryPublisherResp publisher,
            String publishedAt) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.category = category;
        this.publisher = publisher;
        this.publishedAt = publishedAt;
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
}
