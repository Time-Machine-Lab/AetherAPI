package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.AttachAiCapabilityProfileReq;
import io.github.timemachinelab.api.req.AsyncTaskConfigReq;
import io.github.timemachinelab.api.req.ListApiAssetReq;
import io.github.timemachinelab.api.req.RegisterApiAssetReq;
import io.github.timemachinelab.api.req.ReviseApiAssetReq;
import io.github.timemachinelab.api.resp.AiCapabilityProfileResp;
import io.github.timemachinelab.api.resp.AsyncTaskConfigResp;
import io.github.timemachinelab.api.resp.ApiAssetPageResp;
import io.github.timemachinelab.api.resp.ApiAssetResp;
import io.github.timemachinelab.api.resp.ApiAssetSummaryResp;
import io.github.timemachinelab.domain.catalog.model.AsyncTaskAuthMode;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.ApiAssetPageResult;
import io.github.timemachinelab.service.model.ApiAssetSummaryModel;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.AttachAiCapabilityProfileCommand;
import io.github.timemachinelab.service.model.ListApiAssetQuery;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import org.springframework.stereotype.Component;

/**
 * API asset web delegate.
 */
@Component
public class ApiAssetWebDelegate {

    private final ApiAssetUseCase apiAssetUseCase;

    public ApiAssetWebDelegate(ApiAssetUseCase apiAssetUseCase) {
        this.apiAssetUseCase = apiAssetUseCase;
    }

    public ApiAssetPageResp listAssets(String currentUserId, ListApiAssetReq req) {
        ApiAssetPageResult result = apiAssetUseCase.listAssets(new ListApiAssetQuery(
                currentUserId,
                req.getStatus(),
                req.getCategoryCode(),
                req.getKeyword(),
                req.getPage(),
                req.getSize()
        ));
        return new ApiAssetPageResp(
                result.getItems().stream().map(this::toSummaryResp).toList(),
                result.getPage(),
                result.getSize(),
                result.getTotal()
        );
    }

    public ApiAssetResp registerAsset(String currentUserId, String publisherDisplayName, RegisterApiAssetReq req) {
        ApiAssetModel model = apiAssetUseCase.registerAsset(
                new RegisterApiAssetCommand(
                        currentUserId,
                        publisherDisplayName,
                        req.getApiCode(),
                        req.getAssetType(),
                        req.getAssetName(),
                        req.getRequestJsonSchema(),
                        req.getResponseJsonSchema(),
                        toAsyncTaskConfigModel(req.getAsyncTaskConfig())));
        return toResp(model);
    }

    public ApiAssetResp reviseAsset(
            String currentUserId,
            String publisherDisplayName,
            String apiCode,
            ReviseApiAssetReq req) {
        ApiAssetModel model = apiAssetUseCase.reviseAsset(new ReviseApiAssetCommand(
                currentUserId,
                publisherDisplayName,
                apiCode,
                req.getAssetName(),
                req.isAssetNameSet(),
                req.getAssetType(),
                req.isAssetTypeSet(),
                req.getCategoryCode(),
                req.isCategoryCodeSet(),
                req.getRequestMethod(),
                req.isRequestMethodSet(),
                req.getUpstreamUrl(),
                req.isUpstreamUrlSet(),
                req.getAuthScheme(),
                req.isAuthSchemeSet(),
                req.getAuthConfig(),
                req.isAuthConfigSet(),
                req.getRequestTemplate(),
                req.isRequestTemplateSet(),
                req.getRequestExample(),
                req.isRequestExampleSet(),
                req.getResponseExample(),
                req.isResponseExampleSet(),
                req.getRequestJsonSchema(),
                req.isRequestJsonSchemaSet(),
                req.getResponseJsonSchema(),
                req.isResponseJsonSchemaSet(),
                toAsyncTaskConfigModel(req.getAsyncTaskConfig()),
                req.isAsyncTaskConfigSet()
        ));
        return toResp(model);
    }

    public ApiAssetResp publishAsset(String currentUserId, String publisherDisplayName, String apiCode) {
        return toResp(apiAssetUseCase.publishAsset(currentUserId, publisherDisplayName, apiCode));
    }

    public ApiAssetResp unpublishAsset(String currentUserId, String apiCode) {
        return toResp(apiAssetUseCase.unpublishAsset(currentUserId, apiCode));
    }

    public ApiAssetResp deleteAsset(String currentUserId, String apiCode) {
        return toResp(apiAssetUseCase.deleteAsset(currentUserId, apiCode));
    }

    public ApiAssetResp attachAiCapabilityProfile(
            String currentUserId,
            String publisherDisplayName,
            String apiCode,
            AttachAiCapabilityProfileReq req) {
        ApiAssetModel model = apiAssetUseCase.attachAiCapabilityProfile(
                new AttachAiCapabilityProfileCommand(
                        currentUserId,
                        publisherDisplayName,
                        apiCode,
                        req.getProvider(),
                        req.getModel(),
                        req.isStreamingSupported(),
                        req.getCapabilityTags()
                )
        );
        return toResp(model);
    }

    public ApiAssetResp getAssetByCode(String currentUserId, String apiCode) {
        return toResp(apiAssetUseCase.getAssetByCode(currentUserId, apiCode));
    }

    private ApiAssetSummaryResp toSummaryResp(ApiAssetSummaryModel model) {
        return new ApiAssetSummaryResp(
                model.getApiCode(),
                model.getAssetName(),
                AssetType.valueOf(model.getAssetType()),
                model.getCategoryCode(),
                model.getCategoryName(),
                AssetStatus.valueOf(model.getStatus()),
                model.getPublisherDisplayName(),
                model.getPublishedAt(),
                model.getUpdatedAt(),
                model.isAsyncTaskQueryEnabled()
        );
    }

    private ApiAssetResp toResp(ApiAssetModel model) {
        return new ApiAssetResp(
                model.getId(),
                model.getApiCode(),
                model.getAssetName(),
                AssetType.valueOf(model.getAssetType()),
                model.getCategoryCode(),
                AssetStatus.valueOf(model.getStatus()),
                model.getPublisherDisplayName(),
                model.getPublishedAt(),
                model.getRequestMethod() == null ? null : RequestMethod.valueOf(model.getRequestMethod()),
                model.getUpstreamUrl(),
                model.getAuthScheme() == null ? null : AuthScheme.valueOf(model.getAuthScheme()),
                model.getAuthConfig(),
                model.getRequestTemplate(),
                model.getRequestExample(),
                model.getResponseExample(),
                model.getRequestJsonSchema(),
                model.getResponseJsonSchema(),
                toAsyncTaskConfigResp(model.getAsyncTaskConfig()),
                model.getAiProvider() == null
                        ? null
                        : new AiCapabilityProfileResp(
                                model.getAiProvider(),
                                model.getAiModel(),
                                model.getAiStreamingSupported(),
                                model.getAiCapabilityTags()
                        ),
                model.isDeleted(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }

    private AsyncTaskConfigModel toAsyncTaskConfigModel(AsyncTaskConfigReq req) {
        if (req == null) {
            return null;
        }
        return new AsyncTaskConfigModel(
                req.getEnabled(),
                req.getQueryMethod() == null ? null : req.getQueryMethod().name(),
                req.getQueryUrlTemplate(),
                req.getAuthMode() == null ? null : req.getAuthMode().name(),
                req.getAuthScheme() == null ? null : req.getAuthScheme().name(),
                req.getAuthConfig(),
                req.getStatusPath(),
                req.getResultPath(),
                req.getErrorPath()
        );
    }

    private AsyncTaskConfigResp toAsyncTaskConfigResp(AsyncTaskConfigModel model) {
        if (model == null) {
            return null;
        }
        return new AsyncTaskConfigResp(
                model.getEnabled(),
                model.getQueryMethod() == null ? null : RequestMethod.valueOf(model.getQueryMethod()),
                model.getQueryUrlTemplate(),
                model.getAuthMode() == null ? null : AsyncTaskAuthMode.valueOf(model.getAuthMode()),
                model.getAuthScheme() == null ? null : AuthScheme.valueOf(model.getAuthScheme()),
                model.getAuthConfig(),
                model.getStatusPath(),
                model.getResultPath(),
                model.getErrorPath()
        );
    }
}
