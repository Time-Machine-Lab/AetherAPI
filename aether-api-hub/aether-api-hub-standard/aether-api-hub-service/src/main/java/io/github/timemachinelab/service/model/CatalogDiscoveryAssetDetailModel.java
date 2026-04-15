package io.github.timemachinelab.service.model;

/**
 * Discovery asset detail model.
 */
public class CatalogDiscoveryAssetDetailModel {

    private final String apiCode;
    private final String assetName;
    private final String assetType;
    private final CatalogDiscoveryCategoryModel category;
    private final String requestMethod;
    private final String authScheme;
    private final String requestTemplate;
    private final CatalogDiscoveryExampleSnapshotModel exampleSnapshot;
    private final CatalogDiscoveryAiCapabilityProfileModel aiCapabilityProfile;

    public CatalogDiscoveryAssetDetailModel(
            String apiCode,
            String assetName,
            String assetType,
            CatalogDiscoveryCategoryModel category,
            String requestMethod,
            String authScheme,
            String requestTemplate,
            CatalogDiscoveryExampleSnapshotModel exampleSnapshot,
            CatalogDiscoveryAiCapabilityProfileModel aiCapabilityProfile) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.category = category;
        this.requestMethod = requestMethod;
        this.authScheme = authScheme;
        this.requestTemplate = requestTemplate;
        this.exampleSnapshot = exampleSnapshot;
        this.aiCapabilityProfile = aiCapabilityProfile;
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

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getAuthScheme() {
        return authScheme;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public CatalogDiscoveryExampleSnapshotModel getExampleSnapshot() {
        return exampleSnapshot;
    }

    public CatalogDiscoveryAiCapabilityProfileModel getAiCapabilityProfile() {
        return aiCapabilityProfile;
    }
}
