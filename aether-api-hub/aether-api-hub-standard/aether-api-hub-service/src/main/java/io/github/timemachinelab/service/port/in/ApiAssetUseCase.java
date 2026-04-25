package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.ApiAssetPageResult;
import io.github.timemachinelab.service.model.AttachAiCapabilityProfileCommand;
import io.github.timemachinelab.service.model.ListApiAssetQuery;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;

/**
 * API asset use case.
 */
public interface ApiAssetUseCase {

    ApiAssetPageResult listAssets(ListApiAssetQuery query);

    ApiAssetModel registerAsset(RegisterApiAssetCommand command);

    ApiAssetModel reviseAsset(ReviseApiAssetCommand command);

    ApiAssetModel publishAsset(String currentUserId, String publisherDisplayName, String apiCode);

    ApiAssetModel unpublishAsset(String currentUserId, String apiCode);

    ApiAssetModel attachAiCapabilityProfile(AttachAiCapabilityProfileCommand command);

    ApiAssetModel getAssetByCode(String currentUserId, String apiCode);

    ApiAssetModel deleteAsset(String currentUserId, String apiCode);
}
