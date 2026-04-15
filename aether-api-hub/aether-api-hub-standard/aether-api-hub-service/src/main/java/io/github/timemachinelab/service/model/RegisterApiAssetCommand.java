package io.github.timemachinelab.service.model;

import io.github.timemachinelab.domain.catalog.model.AssetType;

/**
 * 注册 API 资产命令。
 */
public class RegisterApiAssetCommand {

    private final String apiCode;
    private final AssetType assetType;
    private final String assetName;

    public RegisterApiAssetCommand(String apiCode, AssetType assetType, String assetName) {
        this.apiCode = apiCode;
        this.assetType = assetType;
        this.assetName = assetName;
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

