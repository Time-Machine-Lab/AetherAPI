package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.AssetProxyBindingModel;
import io.github.timemachinelab.service.model.BindProxyProfileCommand;
import io.github.timemachinelab.service.model.CreatePlatformProxyProfileCommand;
import io.github.timemachinelab.service.model.GetPlatformProxyProfileQuery;
import io.github.timemachinelab.service.model.ListPlatformProxyAssetCandidateQuery;
import io.github.timemachinelab.service.model.ListPlatformProxyProfileQuery;
import io.github.timemachinelab.service.model.PlatformProxyAssetCandidatePageResult;
import io.github.timemachinelab.service.model.PlatformProxyProfileModel;
import io.github.timemachinelab.service.model.PlatformProxyProfilePageResult;
import io.github.timemachinelab.service.model.PlatformProxyProfileStateCommand;
import io.github.timemachinelab.service.model.UnbindProxyProfileCommand;
import io.github.timemachinelab.service.model.UpdatePlatformProxyProfileCommand;

/**
 * Platform proxy profile management use case.
 */
public interface PlatformProxyProfileUseCase {

    PlatformProxyProfilePageResult listProfiles(ListPlatformProxyProfileQuery query);

    PlatformProxyAssetCandidatePageResult listAssetBindingCandidates(ListPlatformProxyAssetCandidateQuery query);

    PlatformProxyProfileModel getProfile(GetPlatformProxyProfileQuery query);

    PlatformProxyProfileModel createProfile(CreatePlatformProxyProfileCommand command);

    PlatformProxyProfileModel updateProfile(UpdatePlatformProxyProfileCommand command);

    PlatformProxyProfileModel enableProfile(PlatformProxyProfileStateCommand command);

    PlatformProxyProfileModel disableProfile(PlatformProxyProfileStateCommand command);

    PlatformProxyProfileModel deleteProfile(PlatformProxyProfileStateCommand command);

    AssetProxyBindingModel bindProxyProfile(BindProxyProfileCommand command);

    AssetProxyBindingModel unbindProxyProfile(UnbindProxyProfileCommand command);
}
