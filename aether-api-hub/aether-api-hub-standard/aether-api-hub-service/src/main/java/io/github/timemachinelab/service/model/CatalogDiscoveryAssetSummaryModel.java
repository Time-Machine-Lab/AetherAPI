package io.github.timemachinelab.service.model;

/**
 * Discovery asset summary model.
 */
public class CatalogDiscoveryAssetSummaryModel {

    private final String apiCode;
    private final String assetName;
    private final String assetType;
    private final CatalogDiscoveryCategoryModel category;
    private final CatalogDiscoveryPublisherModel publisher;
    private final String publishedAt;

    public CatalogDiscoveryAssetSummaryModel(
            String apiCode,
            String assetName,
            String assetType,
            CatalogDiscoveryCategoryModel category,
            CatalogDiscoveryPublisherModel publisher,
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

    public String getAssetName() {
        return assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public CatalogDiscoveryCategoryModel getCategory() {
        return category;
    }

    public CatalogDiscoveryPublisherModel getPublisher() {
        return publisher;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}
