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
    private final AsyncTaskConfigModel asyncTaskConfig;

    public RegisterApiAssetCommand(
            String ownerUserId,
            String publisherDisplayName,
            String apiCode,
            AssetType assetType,
            String assetName) {
        this(ownerUserId, publisherDisplayName, apiCode, assetType, assetName, null);
    }

    public RegisterApiAssetCommand(
            String ownerUserId,
            String publisherDisplayName,
            String apiCode,
            AssetType assetType,
            String assetName,
            AsyncTaskConfigModel asyncTaskConfig) {
        this.ownerUserId = ownerUserId;
        this.publisherDisplayName = publisherDisplayName;
        this.apiCode = apiCode;
        this.assetType = assetType;
        this.assetName = assetName;
        this.asyncTaskConfig = asyncTaskConfig;
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

    public AsyncTaskConfigModel getAsyncTaskConfig() {
        return asyncTaskConfig;
    }
}
