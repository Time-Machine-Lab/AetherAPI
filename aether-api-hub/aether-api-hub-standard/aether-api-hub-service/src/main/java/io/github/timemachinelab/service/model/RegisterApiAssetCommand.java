package io.github.timemachinelab.service.model;

import io.github.timemachinelab.domain.catalog.model.AssetType;

/**
 * Register API asset command.
 */
public class RegisterApiAssetCommand {

    private final String ownerUserId;
    private final String publisherDisplayName;
    private final String apiCode;
    private final AssetType assetType;
    private final String assetName;

    public RegisterApiAssetCommand(
            String ownerUserId,
            String publisherDisplayName,
            String apiCode,
            AssetType assetType,
            String assetName) {
        this.ownerUserId = ownerUserId;
        this.publisherDisplayName = publisherDisplayName;
        this.apiCode = apiCode;
        this.assetType = assetType;
        this.assetName = assetName;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public String getApiCode() {
        return apiCode;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public String getAssetName() {
        return assetName;
    }
}
