package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.catalog.model.AiCapabilityProfile;
import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.catalog.model.AssetId;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.CategoryRef;
import io.github.timemachinelab.domain.catalog.model.CategoryValidityChecker;
import io.github.timemachinelab.domain.catalog.model.ExampleSnapshot;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.domain.catalog.model.UpstreamEndpointConfig;
import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.AttachAiCapabilityProfileCommand;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * API 资产管理应用服务。
 */
public class ApiAssetApplicationService implements ApiAssetUseCase {

    private final ApiAssetRepositoryPort apiAssetRepositoryPort;
    private final CategoryValidityChecker categoryValidityChecker;

    public ApiAssetApplicationService(
            ApiAssetRepositoryPort apiAssetRepositoryPort, CategoryValidityChecker categoryValidityChecker) {
        this.apiAssetRepositoryPort = apiAssetRepositoryPort;
        this.categoryValidityChecker = categoryValidityChecker;
    }

    @Override
    public ApiAssetModel registerAsset(RegisterApiAssetCommand command) {
        ApiCode apiCode = ApiCode.of(command.getApiCode());
        if (apiAssetRepositoryPort.existsByCode(apiCode)) {
            throw new AssetDomainException("API code already exists: " + apiCode.getValue());
        }
        AssetType assetType = command.getAssetType();
        if (assetType == null) {
            throw new IllegalArgumentException("Asset type must not be null");
        }

        ApiAssetAggregate aggregate = ApiAssetAggregate.registerDraft(
                AssetId.generate(),
                apiCode,
                assetType,
                command.getAssetName()
        );
        apiAssetRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public ApiAssetModel reviseAsset(ReviseApiAssetCommand command) {
        ApiAssetAggregate aggregate = loadAggregate(ApiCode.of(command.getApiCode()));

        String assetName = command.isAssetNameSet() ? command.getAssetName() : aggregate.getName();
        AssetType assetType = command.isAssetTypeSet() && command.getAssetType() != null
                ? command.getAssetType()
                : aggregate.getType();
        CategoryRef categoryRef = command.isCategoryCodeSet()
                ? CategoryRef.of(command.getCategoryCode())
                : aggregate.getCategoryRef();
        UpstreamEndpointConfig upstreamConfig = mergeUpstreamConfig(aggregate, command);
        String requestTemplate = command.isRequestTemplateSet()
                ? command.getRequestTemplate()
                : aggregate.getRequestTemplate();
        ExampleSnapshot exampleSnapshot = mergeExamples(aggregate, command);

        aggregate.revise(assetName, assetType, categoryRef, upstreamConfig, requestTemplate, exampleSnapshot);
        apiAssetRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public ApiAssetModel enableAsset(String apiCode) {
        ApiAssetAggregate aggregate = loadAggregate(ApiCode.of(apiCode));
        aggregate.enable(categoryValidityChecker);
        apiAssetRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public ApiAssetModel disableAsset(String apiCode) {
        ApiAssetAggregate aggregate = loadAggregate(ApiCode.of(apiCode));
        aggregate.disable();
        apiAssetRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public ApiAssetModel attachAiCapabilityProfile(AttachAiCapabilityProfileCommand command) {
        ApiAssetAggregate aggregate = loadAggregate(ApiCode.of(command.getApiCode()));
        AiCapabilityProfile profile = AiCapabilityProfile.of(
                command.getProvider(),
                command.getModel(),
                command.isStreamingSupported(),
                command.getCapabilityTags()
        );
        aggregate.attachAiCapabilityProfile(profile);
        apiAssetRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    @Override
    public ApiAssetModel getAssetByCode(String apiCode) {
        return toModel(loadAggregate(ApiCode.of(apiCode)));
    }

    private ApiAssetAggregate loadAggregate(ApiCode code) {
        return apiAssetRepositoryPort.findByCode(code)
                .orElseThrow(() -> new AssetDomainException("Asset not found: " + code.getValue()));
    }

    private UpstreamEndpointConfig mergeUpstreamConfig(ApiAssetAggregate aggregate, ReviseApiAssetCommand command) {
        boolean shouldMerge = command.isRequestMethodSet()
                || command.isUpstreamUrlSet()
                || command.isAuthSchemeSet()
                || command.isAuthConfigSet();
        if (!shouldMerge) {
            return aggregate.getUpstreamConfig();
        }

        RequestMethod requestMethod = command.isRequestMethodSet()
                ? command.getRequestMethod()
                : aggregate.getUpstreamConfig() == null ? null : aggregate.getUpstreamConfig().getRequestMethod();
        String upstreamUrl = command.isUpstreamUrlSet()
                ? command.getUpstreamUrl()
                : aggregate.getUpstreamConfig() == null ? null : aggregate.getUpstreamConfig().getUpstreamUrl();
        AuthScheme authScheme = command.isAuthSchemeSet()
                ? command.getAuthScheme()
                : aggregate.getUpstreamConfig() == null ? null : aggregate.getUpstreamConfig().getAuthScheme();
        String authConfig = command.isAuthConfigSet()
                ? command.getAuthConfig()
                : aggregate.getUpstreamConfig() == null ? null : aggregate.getUpstreamConfig().getAuthConfig();
        return UpstreamEndpointConfig.of(requestMethod, upstreamUrl, authScheme, authConfig);
    }

    private ExampleSnapshot mergeExamples(ApiAssetAggregate aggregate, ReviseApiAssetCommand command) {
        boolean shouldMerge = command.isRequestExampleSet() || command.isResponseExampleSet();
        if (!shouldMerge) {
            return aggregate.getExampleSnapshot();
        }

        String requestExample = command.isRequestExampleSet()
                ? command.getRequestExample()
                : aggregate.getExampleSnapshot() == null ? null : aggregate.getExampleSnapshot().getRequestExample();
        String responseExample = command.isResponseExampleSet()
                ? command.getResponseExample()
                : aggregate.getExampleSnapshot() == null ? null : aggregate.getExampleSnapshot().getResponseExample();
        return ExampleSnapshot.of(requestExample, responseExample);
    }

    private ApiAssetModel toModel(ApiAssetAggregate aggregate) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return new ApiAssetModel(
                aggregate.getId().getValue(),
                aggregate.getCode().getValue(),
                aggregate.getName(),
                aggregate.getType().name(),
                aggregate.getCategoryRef() == null ? null : aggregate.getCategoryRef().getCode(),
                aggregate.getStatus().name(),
                aggregate.getUpstreamConfig() == null || aggregate.getUpstreamConfig().getRequestMethod() == null
                        ? null
                        : aggregate.getUpstreamConfig().getRequestMethod().name(),
                aggregate.getUpstreamConfig() == null ? null : aggregate.getUpstreamConfig().getUpstreamUrl(),
                aggregate.getUpstreamConfig() == null || aggregate.getUpstreamConfig().getAuthScheme() == null
                        ? null
                        : aggregate.getUpstreamConfig().getAuthScheme().name(),
                aggregate.getUpstreamConfig() == null ? null : aggregate.getUpstreamConfig().getAuthConfig(),
                aggregate.getRequestTemplate(),
                aggregate.getExampleSnapshot() == null ? null : aggregate.getExampleSnapshot().getRequestExample(),
                aggregate.getExampleSnapshot() == null ? null : aggregate.getExampleSnapshot().getResponseExample(),
                aggregate.getAiCapabilityProfile() == null ? null : aggregate.getAiCapabilityProfile().getProvider(),
                aggregate.getAiCapabilityProfile() == null ? null : aggregate.getAiCapabilityProfile().getModel(),
                aggregate.getAiCapabilityProfile() == null ? null : aggregate.getAiCapabilityProfile().isStreamingSupported(),
                aggregate.getAiCapabilityProfile() == null ? null : List.copyOf(aggregate.getAiCapabilityProfile().getCapabilityTags()),
                formatter.format(aggregate.getCreatedAt()),
                formatter.format(aggregate.getUpdatedAt())
        );
    }
}
