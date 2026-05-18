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
    private final String requestJsonSchema;
    private final String responseJsonSchema;
    private final AsyncTaskConfigModel asyncTaskConfig;
    private final String capabilityExtensions;
    private final String policyExtensions;
    private final String metadataExtensions;

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
        this(ownerUserId, publisherDisplayName, apiCode, assetType, assetName, null, null, asyncTaskConfig);
    }

    public RegisterApiAssetCommand(
            String ownerUserId,
            String publisherDisplayName,
            String apiCode,
            AssetType assetType,
            String assetName,
            String requestJsonSchema,
            String responseJsonSchema,
            AsyncTaskConfigModel asyncTaskConfig) {
        this(ownerUserId, publisherDisplayName, apiCode, assetType, assetName,
                requestJsonSchema, responseJsonSchema, asyncTaskConfig,
                null, null, null);
    }

    public RegisterApiAssetCommand(
            String ownerUserId,
            String publisherDisplayName,
            String apiCode,
            AssetType assetType,
            String assetName,
            String requestJsonSchema,
            String responseJsonSchema,
            AsyncTaskConfigModel asyncTaskConfig,
            String capabilityExtensions,
            String policyExtensions,
            String metadataExtensions) {
        this.ownerUserId = ownerUserId;
        this.publisherDisplayName = publisherDisplayName;
        this.apiCode = apiCode;
        this.assetType = assetType;
        this.assetName = assetName;
        this.requestJsonSchema = requestJsonSchema;
        this.responseJsonSchema = responseJsonSchema;
        this.asyncTaskConfig = asyncTaskConfig;
        this.capabilityExtensions = capabilityExtensions;
        this.policyExtensions = policyExtensions;
        this.metadataExtensions = metadataExtensions;
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

    public String getRequestJsonSchema() {
        return requestJsonSchema;
    }

    public String getResponseJsonSchema() {
        return responseJsonSchema;
    }

    public AsyncTaskConfigModel getAsyncTaskConfig() {
        return asyncTaskConfig;
    }

    public String getCapabilityExtensions() {
        return capabilityExtensions;
    }

    public String getPolicyExtensions() {
        return policyExtensions;
    }

    public String getMetadataExtensions() {
        return metadataExtensions;
    }
}
