package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialCode;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialId;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAuthDomainException;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.ExpirationPolicy;
import io.github.timemachinelab.domain.consumerauth.model.KeyFingerprint;
import io.github.timemachinelab.domain.consumerauth.model.LastUsedSnapshot;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.service.model.ApiCredentialModel;
import io.github.timemachinelab.service.model.ApiCredentialPageResult;
import io.github.timemachinelab.service.model.DisableApiCredentialCommand;
import io.github.timemachinelab.service.model.EnableApiCredentialCommand;
import io.github.timemachinelab.service.model.GetApiCredentialDetailQuery;
import io.github.timemachinelab.service.model.IssueApiCredentialCommand;
import io.github.timemachinelab.service.model.IssuedApiCredentialModel;
import io.github.timemachinelab.service.model.LastUsedSnapshotModel;
import io.github.timemachinelab.service.model.ListApiCredentialQuery;
import io.github.timemachinelab.service.model.RevokeApiCredentialCommand;
import io.github.timemachinelab.service.port.in.ApiCredentialUseCase;
import io.github.timemachinelab.service.port.out.ApiCredentialRepositoryPort;
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
import java.util.stream.Collectors;

/**
 * API 凭证应用服务。
 */
public class ApiCredentialApplicationService implements ApiCredentialUseCase {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final String KEY_PREFIX = "ak_live";

    private final ApiCredentialRepositoryPort apiCredentialRepositoryPort;
    private final ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort;
    private final UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort;

    public ApiCredentialApplicationService(
            ApiCredentialRepositoryPort apiCredentialRepositoryPort,
            ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort,
            UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort) {
        this.apiCredentialRepositoryPort = apiCredentialRepositoryPort;
        this.consumerIdentityRepositoryPort = consumerIdentityRepositoryPort;
        this.userConsumerMappingRepositoryPort = userConsumerMappingRepositoryPort;
    }

    @Override
    public IssuedApiCredentialModel issueApiCredential(IssueApiCredentialCommand command) {
        String currentUserId = normalizeCurrentUserId(command.getCurrentUserId());
        ConsumerAggregate consumerAggregate = ensureCurrentUserConsumer(currentUserId);

        String plaintextKey = generatePlaintextKey();
        KeyFingerprint keyFingerprint = buildFingerprint(plaintextKey);
        ApiCredentialAggregate aggregate = ApiCredentialAggregate.issue(
                ApiCredentialId.generate(),
                ApiCredentialCode.of(generateCredentialCode()),
                consumerAggregate.getId(),
                consumerAggregate.getCode(),
                command.getCredentialName(),
                command.getCredentialDescription(),
                keyFingerprint,
                ExpirationPolicy.of(command.getExpireAt())
        );
        apiCredentialRepositoryPort.save(aggregate);
        return toIssuedModel(aggregate, plaintextKey, Instant.now());
    }

    @Override
    public ApiCredentialPageResult listApiCredentials(ListApiCredentialQuery query) {
        String currentUserId = normalizeCurrentUserId(query.getCurrentUserId());
        int page = Math.max(1, query.getPage());
        int size = Math.max(1, Math.min(query.getSize(), 100));
        Instant now = Instant.now();

        UserConsumerMapping mapping = userConsumerMappingRepositoryPort.findActiveByUserId(currentUserId).orElse(null);
        if (mapping == null) {
            return new ApiCredentialPageResult(List.of(), page, size, 0L);
        }

        FilterCriteria filterCriteria = FilterCriteria.from(query.getStatus());
        List<ApiCredentialModel> items = apiCredentialRepositoryPort.findPageByConsumerId(
                        mapping.getConsumerId(), filterCriteria.persistedStatus(), filterCriteria.expiredOnly(), page, size, now)
                .stream()
                .map(aggregate -> toModel(aggregate, now))
                .collect(Collectors.toList());
        long total = apiCredentialRepositoryPort.countByConsumerId(
                mapping.getConsumerId(), filterCriteria.persistedStatus(), filterCriteria.expiredOnly(), now);
        return new ApiCredentialPageResult(items, page, size, total);
    }

    @Override
    public ApiCredentialModel getApiCredentialDetail(GetApiCredentialDetailQuery query) {
        ApiCredentialAggregate aggregate = loadCredential(query.getCurrentUserId(), query.getCredentialId());
        return toModel(aggregate, Instant.now());
    }

    @Override
    public ApiCredentialModel enableApiCredential(EnableApiCredentialCommand command) {
        ApiCredentialAggregate aggregate = loadCredential(command.getCurrentUserId(), command.getCredentialId());
        aggregate.enable();
        apiCredentialRepositoryPort.save(aggregate);
        return toModel(aggregate, Instant.now());
    }

    @Override
    public ApiCredentialModel disableApiCredential(DisableApiCredentialCommand command) {
        ApiCredentialAggregate aggregate = loadCredential(command.getCurrentUserId(), command.getCredentialId());
        aggregate.disable();
        apiCredentialRepositoryPort.save(aggregate);
        return toModel(aggregate, Instant.now());
    }

    @Override
    public ApiCredentialModel revokeApiCredential(RevokeApiCredentialCommand command) {
        ApiCredentialAggregate aggregate = loadCredential(command.getCurrentUserId(), command.getCredentialId());
        aggregate.revoke();
        apiCredentialRepositoryPort.save(aggregate);
        return toModel(aggregate, Instant.now());
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

    private ApiCredentialAggregate loadCredential(String currentUserIdRaw, String credentialIdRaw) {
        String currentUserId = normalizeCurrentUserId(currentUserIdRaw);
        ApiCredentialId credentialId = ApiCredentialId.of(credentialIdRaw);
        UserConsumerMapping mapping = userConsumerMappingRepositoryPort.findActiveByUserId(currentUserId)
                .orElseThrow(() -> new ConsumerAuthDomainException("API credential not found for current user"));
        return apiCredentialRepositoryPort.findByIdAndConsumerId(credentialId, mapping.getConsumerId())
                .orElseThrow(() -> new ConsumerAuthDomainException("API credential not found for current user"));
    }

    private ApiCredentialModel toModel(ApiCredentialAggregate aggregate, Instant now) {
        return new ApiCredentialModel(
                aggregate.getId().getValue(),
                aggregate.getCode().getValue(),
                aggregate.getName(),
                aggregate.getDescription(),
                aggregate.getKeyFingerprint().getMaskedKey(),
                aggregate.getKeyFingerprint().getPrefix(),
                displayStatus(aggregate, now),
                formatInstant(aggregate.getExpirationPolicy().getExpireAt()),
                formatInstant(aggregate.getRevokedAt()),
                formatInstant(aggregate.getCreatedAt()),
                formatInstant(aggregate.getUpdatedAt()),
                toSnapshotModel(aggregate.getLastUsedSnapshot())
        );
    }

    private IssuedApiCredentialModel toIssuedModel(ApiCredentialAggregate aggregate, String plaintextKey, Instant now) {
        ApiCredentialModel model = toModel(aggregate, now);
        return new IssuedApiCredentialModel(
                model.getCredentialId(),
                model.getCredentialCode(),
                model.getCredentialName(),
                model.getCredentialDescription(),
                model.getMaskedKey(),
                model.getKeyPrefix(),
                model.getStatus(),
                model.getExpireAt(),
                model.getRevokedAt(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                model.getLastUsedSnapshot(),
                plaintextKey
        );
    }

    private LastUsedSnapshotModel toSnapshotModel(LastUsedSnapshot snapshot) {
        if (snapshot == null || snapshot.getLastUsedAt() == null) {
            return new LastUsedSnapshotModel(null, null, null);
        }
        return new LastUsedSnapshotModel(
                formatInstant(snapshot.getLastUsedAt()),
                snapshot.getLastUsedChannel(),
                snapshot.getLastUsedResult()
        );
    }

    private String displayStatus(ApiCredentialAggregate aggregate, Instant now) {
        if (aggregate.getStatus() == ApiCredentialStatus.ENABLED && aggregate.isExpired(now)) {
            return "EXPIRED";
        }
        return aggregate.getStatus().name();
    }

    private String normalizeCurrentUserId(String currentUserId) {
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new IllegalArgumentException("Current user id must not be blank");
        }
        return currentUserId.trim();
    }

    private String generatePlaintextKey() {
        return KEY_PREFIX + "_" + UUID.randomUUID().toString().replace("-", "") + shortHash(UUID.randomUUID().toString());
    }

    private KeyFingerprint buildFingerprint(String plaintextKey) {
        String suffix = plaintextKey.substring(Math.max(plaintextKey.length() - 4, 0));
        return KeyFingerprint.of(KEY_PREFIX, KEY_PREFIX + "_****" + suffix, sha256Hex(plaintextKey));
    }

    private String generateCredentialCode() {
        return "cred_" + UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
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
            throw new ConsumerAuthDomainException("SHA-256 algorithm is unavailable", ex);
        }
    }

    private String formatInstant(Instant instant) {
        return instant == null ? null : TIME_FORMATTER.withZone(ZoneOffset.UTC).format(instant);
    }

    private record FilterCriteria(ApiCredentialStatus persistedStatus, boolean expiredOnly) {
        static FilterCriteria from(String rawStatus) {
            if (rawStatus == null || rawStatus.isBlank()) {
                return new FilterCriteria(null, false);
            }
            String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
            if ("EXPIRED".equals(normalized)) {
                return new FilterCriteria(ApiCredentialStatus.ENABLED, true);
            }
            return new FilterCriteria(ApiCredentialStatus.valueOf(normalized), false);
        }
    }
}
