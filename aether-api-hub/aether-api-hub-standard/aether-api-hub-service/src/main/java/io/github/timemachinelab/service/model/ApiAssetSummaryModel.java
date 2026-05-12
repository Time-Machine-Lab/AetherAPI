package io.github.timemachinelab.service.model;

/**
 * Asset management summary model.
 */
public class ApiAssetSummaryModel {

    private final String apiCode;
    private final String assetName;
    private final String assetType;
    private final String categoryCode;
    private final String categoryName;
    private final String status;
    private final String publisherDisplayName;
    private final String publishedAt;
    private final String updatedAt;
    private final boolean asyncTaskQueryEnabled;

    public ApiAssetSummaryModel(
            String apiCode,
            String assetName,
            String assetType,
            String categoryCode,
            String categoryName,
            String status,
            String publisherDisplayName,
            String publishedAt,
            String updatedAt) {
        this(apiCode, assetName, assetType, categoryCode, categoryName, status, publisherDisplayName, publishedAt, updatedAt, false);
    }

    public ApiAssetSummaryModel(
            String apiCode,
            String assetName,
            String assetType,
            String categoryCode,
            String categoryName,
            String status,
            String publisherDisplayName,
            String publishedAt,
            String updatedAt,
            boolean asyncTaskQueryEnabled) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.status = status;
        this.publisherDisplayName = publisherDisplayName;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
        this.asyncTaskQueryEnabled = asyncTaskQueryEnabled;
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

    public String getCategoryName() {
        return categoryName;
    }

    public String getStatus() {
        return status;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public boolean isAsyncTaskQueryEnabled() {
        return asyncTaskQueryEnabled;
    }
}
