package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAuthDomainException;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionAggregate;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionDomainException;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionId;
import io.github.timemachinelab.service.model.ApiSubscriptionModel;
import io.github.timemachinelab.service.model.ApiSubscriptionPageResult;
import io.github.timemachinelab.service.model.ApiSubscriptionStatusModel;
import io.github.timemachinelab.service.model.CancelApiSubscriptionCommand;
import io.github.timemachinelab.service.model.GetApiSubscriptionStatusQuery;
import io.github.timemachinelab.service.model.ListApiSubscriptionQuery;
import io.github.timemachinelab.service.model.SubscribeApiCommand;
import io.github.timemachinelab.service.port.in.ApiSubscriptionUseCase;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiSubscriptionRepositoryPort;
import io.github.timemachinelab.service.port.out.ConsumerIdentityRepositoryPort;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * API subscription application service.
 */
public class ApiSubscriptionApplicationService implements ApiSubscriptionUseCase {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ApiSubscriptionRepositoryPort apiSubscriptionRepositoryPort;
    private final ApiAssetRepositoryPort apiAssetRepositoryPort;
    private final ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort;
    private final UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort;

    public ApiSubscriptionApplicationService(
            ApiSubscriptionRepositoryPort apiSubscriptionRepositoryPort,
            ApiAssetRepositoryPort apiAssetRepositoryPort,
            ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort,
            UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort) {
        this.apiSubscriptionRepositoryPort = apiSubscriptionRepositoryPort;
        this.apiAssetRepositoryPort = apiAssetRepositoryPort;
        this.consumerIdentityRepositoryPort = consumerIdentityRepositoryPort;
        this.userConsumerMappingRepositoryPort = userConsumerMappingRepositoryPort;
    }

    @Override
    public ApiSubscriptionModel subscribe(SubscribeApiCommand command) {
        String currentUserId = normalizeCurrentUserId(command.getCurrentUserId());
        ApiCode apiCode = ApiCode.of(command.getApiCode());
        ApiAssetAggregate targetAsset = loadPublishedAsset(apiCode);
        if (targetAsset.getOwnerUserId().equals(currentUserId)) {
            return ApiSubscriptionModel.ownerAccess(
                    targetAsset.getCode().getValue(),
                    targetAsset.getName(),
                    targetAsset.getOwnerUserId()
            );
        }

        ConsumerAggregate consumer = ensureCurrentUserConsumer(currentUserId);
        return apiSubscriptionRepositoryPort
                .findActiveByConsumerIdAndApiCode(consumer.getId(), apiCode)
                .map(this::toModel)
                .orElseGet(() -> createSubscription(currentUserId, consumer, targetAsset));
    }

    @Override
    public ApiSubscriptionPageResult listSubscriptions(ListApiSubscriptionQuery query) {
        String currentUserId = normalizeCurrentUserId(query.getCurrentUserId());
        int page = Math.max(1, query.getPage());
        int size = Math.max(1, Math.min(query.getSize(), 100));
        UserConsumerMapping mapping = userConsumerMappingRepositoryPort.findActiveByUserId(currentUserId).orElse(null);
        if (mapping == null) {
            return new ApiSubscriptionPageResult(List.of(), page, size, 0L);
        }
        return new ApiSubscriptionPageResult(
                apiSubscriptionRepositoryPort.findPageByConsumerId(mapping.getConsumerId(), page, size)
                        .stream()
                        .map(this::toModel)
                        .toList(),
                page,
                size,
                apiSubscriptionRepositoryPort.countByConsumerId(mapping.getConsumerId())
        );
    }

    @Override
    public ApiSubscriptionStatusModel getSubscriptionStatus(GetApiSubscriptionStatusQuery query) {
        String currentUserId = normalizeCurrentUserId(query.getCurrentUserId());
        ApiCode apiCode = ApiCode.of(query.getApiCode());
        ApiAssetAggregate targetAsset = apiAssetRepositoryPort.findByCode(apiCode).orElse(null);
        if (targetAsset != null && targetAsset.getOwnerUserId().equals(currentUserId)) {
            return new ApiSubscriptionStatusModel(apiCode.getValue(), "OWNER", null, null, false, true);
        }

        UserConsumerMapping mapping = userConsumerMappingRepositoryPort.findActiveByUserId(currentUserId).orElse(null);
        if (mapping == null) {
            return new ApiSubscriptionStatusModel(apiCode.getValue(), "NOT_SUBSCRIBED", null, null, false, false);
        }
        return apiSubscriptionRepositoryPort.findActiveByConsumerIdAndApiCode(mapping.getConsumerId(), apiCode)
                .map(subscription -> new ApiSubscriptionStatusModel(
                        apiCode.getValue(),
                        "SUBSCRIBED",
                        subscription.getId().getValue(),
                        subscription.getStatus().name(),
                        true,
                        false
                ))
                .orElseGet(() -> new ApiSubscriptionStatusModel(
                        apiCode.getValue(),
                        "NOT_SUBSCRIBED",
                        null,
                        null,
                        false,
                        false
                ));
    }

    @Override
    public ApiSubscriptionModel cancelSubscription(CancelApiSubscriptionCommand command) {
        String currentUserId = normalizeCurrentUserId(command.getCurrentUserId());
        UserConsumerMapping mapping = userConsumerMappingRepositoryPort.findActiveByUserId(currentUserId)
                .orElseThrow(() -> new ApiSubscriptionDomainException("API subscription not found for current user"));
        ApiSubscriptionAggregate subscription = apiSubscriptionRepositoryPort.findById(ApiSubscriptionId.of(command.getSubscriptionId()))
                .orElseThrow(() -> new ApiSubscriptionDomainException("API subscription not found for current user"));
        if (!subscription.getSubscriberConsumerId().equals(mapping.getConsumerId())) {
            throw new ApiSubscriptionDomainException("API subscription not found for current user");
        }
        subscription.cancel();
        apiSubscriptionRepositoryPort.save(subscription);
        return toModel(subscription);
    }

    private ApiSubscriptionModel createSubscription(
            String currentUserId,
            ConsumerAggregate consumer,
            ApiAssetAggregate targetAsset) {
        ApiSubscriptionAggregate aggregate = ApiSubscriptionAggregate.createActive(
                ApiSubscriptionId.generate(),
                generateSubscriptionCode(),
                currentUserId,
                consumer.getId(),
                consumer.getCode(),
                targetAsset.getCode(),
                targetAsset.getOwnerUserId(),
                targetAsset.getName()
        );
        apiSubscriptionRepositoryPort.save(aggregate);
        return toModel(aggregate);
    }

    private ApiAssetAggregate loadPublishedAsset(ApiCode apiCode) {
        ApiAssetAggregate targetAsset = apiAssetRepositoryPort.findByCode(apiCode)
                .orElseThrow(() -> new ApiSubscriptionDomainException("Subscribable API asset not found: " + apiCode.getValue()));
        if (targetAsset.getStatus() != AssetStatus.PUBLISHED || targetAsset.isDeleted()) {
            throw new ApiSubscriptionDomainException("Subscribable API asset not found: " + apiCode.getValue());
        }
        return targetAsset;
    }

    private ConsumerAggregate ensureCurrentUserConsumer(String currentUserId) {
        return userConsumerMappingRepositoryPort.findActiveByUserId(currentUserId)
                .map(mapping -> consumerIdentityRepositoryPort.findById(mapping.getConsumerId())
                        .orElseThrow(() -> new ConsumerAuthDomainException("Consumer not found for current user")))
                .map(consumer -> {
                    if (!consumer.isAvailable()) {
                        throw new ConsumerAuthDomainException("Current user's consumer is unavailable");
                    }
                    return consumer;
                })
                .orElseGet(() -> createCurrentUserConsumer(currentUserId));
    }

    private ConsumerAggregate createCurrentUserConsumer(String currentUserId) {
        ConsumerCode consumerCode = ConsumerCode.of(generateConsumerCode(currentUserId));
        ConsumerAggregate consumerAggregate = ConsumerAggregate.createInternal(
                ConsumerId.generate(),
                consumerCode,
                "user-" + shortHash(currentUserId)
        );
        consumerIdentityRepositoryPort.save(consumerAggregate);
        userConsumerMappingRepositoryPort.save(
                UserConsumerMapping.createActive(currentUserId, consumerAggregate.getId(), consumerAggregate.getCode()));
        return consumerAggregate;
    }

    private ApiSubscriptionModel toModel(ApiSubscriptionAggregate aggregate) {
        return new ApiSubscriptionModel(
                aggregate.getId().getValue(),
                aggregate.getApiCode().getValue(),
                aggregate.getAssetName(),
                aggregate.getAssetOwnerUserId(),
                aggregate.getStatus().name(),
                aggregate.isActive(),
                false,
                formatInstant(aggregate.getCreatedAt()),
                formatInstant(aggregate.getUpdatedAt()),
                formatInstant(aggregate.getCancelledAt())
        );
    }

    private String normalizeCurrentUserId(String currentUserId) {
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new IllegalArgumentException("Current user id must not be blank");
        }
        return currentUserId.trim();
    }

    private String generateSubscriptionCode() {
        return "sub_" + UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
    }

    private String generateConsumerCode(String currentUserId) {
        return "consumer_" + shortHash(currentUserId) + shortHash(currentUserId + "_mapping");
    }

    private String shortHash(String input) {
        return sha256Hex(input).substring(0, 12);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new ApiSubscriptionDomainException("SHA-256 algorithm is unavailable", ex);
        }
    }

    private String formatInstant(Instant instant) {
        return instant == null ? null : TIME_FORMATTER.withZone(ZoneOffset.UTC).format(instant);
    }
}
