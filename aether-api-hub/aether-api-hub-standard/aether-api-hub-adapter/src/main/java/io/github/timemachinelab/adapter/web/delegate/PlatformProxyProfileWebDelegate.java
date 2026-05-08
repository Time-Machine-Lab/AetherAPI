package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.BindProxyProfileReq;
import io.github.timemachinelab.api.req.CreatePlatformProxyProfileReq;
import io.github.timemachinelab.api.req.ListPlatformProxyAssetCandidateReq;
import io.github.timemachinelab.api.req.UpdatePlatformProxyProfileReq;
import io.github.timemachinelab.api.resp.AssetProxyBindingResp;
import io.github.timemachinelab.api.resp.PlatformProxyAssetCandidatePageResp;
import io.github.timemachinelab.api.resp.PlatformProxyAssetCandidateResp;
import io.github.timemachinelab.api.resp.PlatformProxyProfilePageResp;
import io.github.timemachinelab.api.resp.PlatformProxyProfileResp;
import io.github.timemachinelab.service.model.AssetProxyBindingModel;
import io.github.timemachinelab.service.model.BindProxyProfileCommand;
import io.github.timemachinelab.service.model.CreatePlatformProxyProfileCommand;
import io.github.timemachinelab.service.model.GetPlatformProxyProfileQuery;
import io.github.timemachinelab.service.model.ListPlatformProxyAssetCandidateQuery;
import io.github.timemachinelab.service.model.ListPlatformProxyProfileQuery;
import io.github.timemachinelab.service.model.PlatformProxyAssetCandidateModel;
import io.github.timemachinelab.service.model.PlatformProxyAssetCandidatePageResult;
import io.github.timemachinelab.service.model.PlatformProxyProfileModel;
import io.github.timemachinelab.service.model.PlatformProxyProfilePageResult;
import io.github.timemachinelab.service.model.PlatformProxyProfileStateCommand;
import io.github.timemachinelab.service.model.UnbindProxyProfileCommand;
import io.github.timemachinelab.service.model.UpdatePlatformProxyProfileCommand;
import io.github.timemachinelab.service.port.in.PlatformProxyProfileUseCase;
import org.springframework.stereotype.Component;

/**
 * Platform proxy profile web delegate.
 */
@Component
public class PlatformProxyProfileWebDelegate {

    private final PlatformProxyProfileUseCase useCase;

    public PlatformProxyProfileWebDelegate(PlatformProxyProfileUseCase useCase) {
        this.useCase = useCase;
    }

    public PlatformProxyProfilePageResp listProfiles(String role, Boolean enabled, String keyword, int page, int size) {
        PlatformProxyProfilePageResult result = useCase.listProfiles(
                new ListPlatformProxyProfileQuery(role, enabled, keyword, page, size));
        return new PlatformProxyProfilePageResp(
                result.getItems().stream().map(this::toResp).toList(),
                result.getPage(),
                result.getSize(),
                result.getTotal()
        );
    }

    public PlatformProxyAssetCandidatePageResp listAssetBindingCandidates(
            String role,
            ListPlatformProxyAssetCandidateReq req) {
        PlatformProxyAssetCandidatePageResult result = useCase.listAssetBindingCandidates(
                new ListPlatformProxyAssetCandidateQuery(
                        role,
                        req.getKeyword(),
                        req.getStatus(),
                        req.getBoundProfileId(),
                        req.getPage(),
                        req.getSize()
                ));
        return new PlatformProxyAssetCandidatePageResp(
                result.getItems().stream().map(this::toResp).toList(),
                result.getPage(),
                result.getSize(),
                result.getTotal()
        );
    }

    public PlatformProxyProfileResp getProfile(String role, String profileId) {
        return toResp(useCase.getProfile(new GetPlatformProxyProfileQuery(role, profileId)));
    }

    public PlatformProxyProfileResp createProfile(String role, CreatePlatformProxyProfileReq req) {
        return toResp(useCase.createProfile(new CreatePlatformProxyProfileCommand(
                role,
                req.getProfileCode(),
                req.getProfileName(),
                req.getProxyType(),
                req.getProxyHost(),
                req.getProxyPort() == null ? 0 : req.getProxyPort(),
                req.getUsername(),
                req.getPassword(),
                req.getEnabled()
        )));
    }

    public PlatformProxyProfileResp updateProfile(String role, String profileId, UpdatePlatformProxyProfileReq req) {
        return toResp(useCase.updateProfile(new UpdatePlatformProxyProfileCommand(
                role,
                profileId,
                req.getProfileCode(),
                req.getProfileName(),
                req.getProxyType(),
                req.getProxyHost(),
                req.getProxyPort() == null ? 0 : req.getProxyPort(),
                req.getUsername(),
                req.getPassword(),
                req.getEnabled()
        )));
    }

    public PlatformProxyProfileResp enableProfile(String role, String profileId) {
        return toResp(useCase.enableProfile(new PlatformProxyProfileStateCommand(role, profileId)));
    }

    public PlatformProxyProfileResp disableProfile(String role, String profileId) {
        return toResp(useCase.disableProfile(new PlatformProxyProfileStateCommand(role, profileId)));
    }

    public PlatformProxyProfileResp deleteProfile(String role, String profileId) {
        return toResp(useCase.deleteProfile(new PlatformProxyProfileStateCommand(role, profileId)));
    }

    public AssetProxyBindingResp bindProxyProfile(String role, String apiCode, BindProxyProfileReq req) {
        return toResp(useCase.bindProxyProfile(new BindProxyProfileCommand(role, apiCode, req.getProfileId())));
    }

    public AssetProxyBindingResp unbindProxyProfile(String role, String apiCode) {
        return toResp(useCase.unbindProxyProfile(new UnbindProxyProfileCommand(role, apiCode)));
    }

    private PlatformProxyProfileResp toResp(PlatformProxyProfileModel model) {
        return new PlatformProxyProfileResp(
                model.getId(),
                model.getProfileCode(),
                model.getProfileName(),
                model.getProxyType(),
                model.getProxyHost(),
                model.getProxyPort(),
                model.getUsername(),
                model.isCredentialConfigured(),
                model.isEnabled(),
                model.isDeleted(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }

    private AssetProxyBindingResp toResp(AssetProxyBindingModel model) {
        return new AssetProxyBindingResp(
                model.getApiCode(),
                model.getProxyProfileId(),
                model.getProxyProfileCode(),
                model.getProxyProfileName()
        );
    }

    private PlatformProxyAssetCandidateResp toResp(PlatformProxyAssetCandidateModel model) {
        return new PlatformProxyAssetCandidateResp(
                model.getApiCode(),
                model.getAssetName(),
                model.getAssetType(),
                model.getStatus(),
                model.getPublisherDisplayName(),
                model.getProxyProfileId(),
                model.getProxyProfileCode(),
                model.getProxyProfileName(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}
