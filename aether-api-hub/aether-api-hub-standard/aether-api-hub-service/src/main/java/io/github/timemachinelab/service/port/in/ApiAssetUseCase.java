package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.ApiAssetPageResult;
import io.github.timemachinelab.service.model.AttachAiCapabilityProfileCommand;
import io.github.timemachinelab.service.model.ListApiAssetQuery;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;

/**
 * API 资产管理用例入口。
 */
public interface ApiAssetUseCase {

    ApiAssetPageResult listAssets(ListApiAssetQuery query);

    ApiAssetModel registerAsset(RegisterApiAssetCommand command);

    ApiAssetModel reviseAsset(ReviseApiAssetCommand command);

    ApiAssetModel enableAsset(String apiCode);

    ApiAssetModel disableAsset(String apiCode);

    ApiAssetModel attachAiCapabilityProfile(AttachAiCapabilityProfileCommand command);

    ApiAssetModel getAssetByCode(String apiCode);
}

