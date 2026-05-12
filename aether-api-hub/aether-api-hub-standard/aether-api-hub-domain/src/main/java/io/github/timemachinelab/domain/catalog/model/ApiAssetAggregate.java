package io.github.timemachinelab.domain.catalog.model;

import java.time.Instant;
import java.util.Objects;

/**
 * API asset aggregate root.
 */
public class ApiAssetAggregate {

    private AssetId id;
    private ApiCode code;
    private String ownerUserId;
    private String publisherDisplayName;
    private String name;
    private AssetType type;
    private CategoryRef categoryRef;
    private AssetStatus status;
    private Instant publishedAt;
    private UpstreamEndpointConfig upstreamConfig;
    private String requestTemplate;
    private ExampleSnapshot exampleSnapshot;
    private AsyncTaskConfig asyncTaskConfig;
    private AiCapabilityProfile aiCapabilityProfile;
    private String proxyProfileId;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    protected ApiAssetAggregate() {
    }

    private ApiAssetAggregate(
            AssetId id,
            ApiCode code,
            String ownerUserId,
            String publisherDisplayName,
            String name,
            AssetType type,
            CategoryRef categoryRef,
            AssetStatus status,
            Instant publishedAt,
            UpstreamEndpointConfig upstreamConfig,
            String requestTemplate,
            ExampleSnapshot exampleSnapshot,
            AsyncTaskConfig asyncTaskConfig,
            AiCapabilityProfile aiCapabilityProfile,
            String proxyProfileId,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        this.id = Objects.requireNonNull(id, "Asset id must not be null");
        this.code = Objects.requireNonNull(code, "API code must not be null");
        this.ownerUserId = requireOwnerUserId(ownerUserId);
        this.publisherDisplayName = normalizeOptional(publisherDisplayName);
        this.name = normalizeOptional(name);
        this.type = Objects.requireNonNull(type, "Asset type must not be null");
        this.categoryRef = categoryRef;
        this.status = Objects.requireNonNull(status, "Asset status must not be null");
        this.publishedAt = publishedAt;
        this.upstreamConfig = upstreamConfig;
        this.requestTemplate = normalizeOptional(requestTemplate);
        this.exampleSnapshot = exampleSnapshot;
        this.asyncTaskConfig = asyncTaskConfig;
        this.aiCapabilityProfile = aiCapabilityProfile;
        this.proxyProfileId = normalizeOptional(proxyProfileId);
        this.createdAt = Objects.requireNonNull(createdAt, "Created time must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated time must not be null");
        this.deleted = deleted;
        this.version = version;
        if (this.type != AssetType.AI_API) {
            this.aiCapabilityProfile = null;
        }
        if (this.status != AssetStatus.PUBLISHED) {
            this.publishedAt = null;
        }
    }

    public static ApiAssetAggregate registerDraft(
            AssetId id,
            ApiCode code,
            String ownerUserId,
            String publisherDisplayName,
            AssetType type,
            String name) {
        Instant now = Instant.now();
        return new ApiAssetAggregate(
                id,
                code,
                ownerUserId,
                publisherDisplayName,
                name,
                type,
                null,
                AssetStatus.DRAFT,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now,
                false,
                0L
        );
    }

    public static ApiAssetAggregate reconstitute(
            AssetId id,
            ApiCode code,
            String ownerUserId,
            String publisherDisplayName,
            String name,
            AssetType type,
            CategoryRef categoryRef,
            AssetStatus status,
            Instant publishedAt,
            UpstreamEndpointConfig upstreamConfig,
            String requestTemplate,
            ExampleSnapshot exampleSnapshot,
            AiCapabilityProfile aiCapabilityProfile,
            String proxyProfileId,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return reconstitute(
                id,
                code,
                ownerUserId,
                publisherDisplayName,
                name,
                type,
                categoryRef,
                status,
                publishedAt,
                upstreamConfig,
                requestTemplate,
                exampleSnapshot,
                null,
                aiCapabilityProfile,
                proxyProfileId,
                createdAt,
                updatedAt,
                deleted,
                version
        );
    }

    public static ApiAssetAggregate reconstitute(
            AssetId id,
            ApiCode code,
            String ownerUserId,
            String publisherDisplayName,
            String name,
            AssetType type,
            CategoryRef categoryRef,
            AssetStatus status,
            Instant publishedAt,
            UpstreamEndpointConfig upstreamConfig,
            String requestTemplate,
            ExampleSnapshot exampleSnapshot,
            AsyncTaskConfig asyncTaskConfig,
            AiCapabilityProfile aiCapabilityProfile,
            String proxyProfileId,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return new ApiAssetAggregate(
                id,
                code,
                ownerUserId,
                publisherDisplayName,
                name,
                type,
                categoryRef,
                status,
                publishedAt,
                upstreamConfig,
                requestTemplate,
                exampleSnapshot,
                asyncTaskConfig,
                aiCapabilityProfile,
                proxyProfileId,
                createdAt,
                updatedAt,
                deleted,
                version
        );
    }

    public void assertOwnedBy(String currentUserId) {
        String normalizedCurrentUserId = requireOwnerUserId(currentUserId);
        if (!ownerUserId.equals(normalizedCurrentUserId)) {
            throw new AssetDomainException("Asset not found: " + code.getValue());
        }
    }

    public void revise(
            String assetName,
            AssetType assetType,
            CategoryRef newCategoryRef,
            UpstreamEndpointConfig newUpstreamConfig,
            String newRequestTemplate,
            ExampleSnapshot newExampleSnapshot,
            String newPublisherDisplayName) {
        revise(
                assetName,
                assetType,
                newCategoryRef,
                newUpstreamConfig,
                newRequestTemplate,
                newExampleSnapshot,
                this.asyncTaskConfig,
                newPublisherDisplayName
        );
    }

    public void revise(
            String assetName,
            AssetType assetType,
            CategoryRef newCategoryRef,
            UpstreamEndpointConfig newUpstreamConfig,
            String newRequestTemplate,
            ExampleSnapshot newExampleSnapshot,
            AsyncTaskConfig newAsyncTaskConfig,
            String newPublisherDisplayName) {
        ensureNotDeleted();

        AssetType previousType = this.type;
        CategoryRef previousCategoryRef = this.categoryRef;
        UpstreamEndpointConfig previousUpstreamConfig = this.upstreamConfig;
        AsyncTaskConfig previousAsyncTaskConfig = this.asyncTaskConfig;

        this.publisherDisplayName = normalizeOptional(newPublisherDisplayName);
        this.name = normalizeOptional(assetName);
        this.type = Objects.requireNonNull(assetType, "Asset type must not be null");
        this.categoryRef = newCategoryRef;
        this.upstreamConfig = newUpstreamConfig;
        this.requestTemplate = normalizeOptional(newRequestTemplate);
        this.exampleSnapshot = newExampleSnapshot;
        this.asyncTaskConfig = newAsyncTaskConfig;

        if (this.type != AssetType.AI_API) {
            this.aiCapabilityProfile = null;
        }

        if (this.status == AssetStatus.PUBLISHED
                && requiresRepublish(previousType, previousCategoryRef, previousUpstreamConfig, previousAsyncTaskConfig)) {
            this.status = AssetStatus.UNPUBLISHED;
            this.publishedAt = null;
        }

        touch();
    }

    public void attachAiCapabilityProfile(AiCapabilityProfile profile, String newPublisherDisplayName) {
        ensureNotDeleted();
        if (this.type != AssetType.AI_API) {
            throw new AssetDomainException("AI capability profile is only allowed for AI_API assets");
        }
        this.publisherDisplayName = normalizeOptional(newPublisherDisplayName);
        this.aiCapabilityProfile = Objects.requireNonNull(profile, "AI capability profile must not be null");
        touch();
    }

    public void publish(CategoryValidityChecker categoryValidityChecker, String newPublisherDisplayName) {
        ensureNotDeleted();
        if (this.status == AssetStatus.PUBLISHED) {
            throw new AssetDomainException("Asset is already published");
        }
        ensurePublishReady(categoryValidityChecker);
        this.publisherDisplayName = normalizeOptional(newPublisherDisplayName);
        this.status = AssetStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        touch();
    }

    public void unpublish() {
        ensureNotDeleted();
        if (this.status != AssetStatus.PUBLISHED) {
            throw new AssetDomainException("Asset is already unpublished");
        }
        this.status = AssetStatus.UNPUBLISHED;
        this.publishedAt = null;
        touch();
    }

    public void softDelete() {
        ensureNotDeleted();
        this.deleted = true;
        this.publishedAt = null;
        touch();
    }

    public void bindProxyProfile(String newProxyProfileId) {
        ensureNotDeleted();
        this.proxyProfileId = requireProxyProfileId(newProxyProfileId);
        touch();
    }

    public void unbindProxyProfile() {
        ensureNotDeleted();
        this.proxyProfileId = null;
        touch();
    }

    public boolean isPublishReady(CategoryValidityChecker categoryValidityChecker) {
        if (name == null || categoryRef == null || upstreamConfig == null || !upstreamConfig.isComplete()) {
            return false;
        }
        if (!categoryValidityChecker.isValid(categoryRef)) {
            return false;
        }
        return type != AssetType.AI_API || aiCapabilityProfile != null;
    }

    private void ensurePublishReady(CategoryValidityChecker categoryValidityChecker) {
        if (name == null) {
            throw new AssetDomainException("Asset name must be provided before publishing");
        }
        if (categoryRef == null) {
            throw new AssetDomainException("Category code must be provided before publishing");
        }
        if (upstreamConfig == null || !upstreamConfig.isComplete()) {
            throw new AssetDomainException("Upstream endpoint configuration is incomplete");
        }
        if (!categoryValidityChecker.isValid(categoryRef)) {
            throw new AssetDomainException("Referenced category is invalid");
        }
        if (type == AssetType.AI_API && aiCapabilityProfile == null) {
            throw new AssetDomainException("AI_API asset requires an AI capability profile before publishing");
        }
    }

    private boolean requiresRepublish(
            AssetType previousType,
            CategoryRef previousCategoryRef,
            UpstreamEndpointConfig previousUpstreamConfig,
            AsyncTaskConfig previousAsyncTaskConfig) {
        boolean criticalConfigChanged = !Objects.equals(previousCategoryRef, this.categoryRef)
                || (previousUpstreamConfig == null
                ? this.upstreamConfig != null
                : previousUpstreamConfig.hasCriticalDifference(this.upstreamConfig))
                || (previousAsyncTaskConfig == null
                ? this.asyncTaskConfig != null
                : previousAsyncTaskConfig.hasCriticalDifference(this.asyncTaskConfig));
        boolean aiRequirementChanged = previousType != this.type && this.type == AssetType.AI_API && this.aiCapabilityProfile == null;
        return criticalConfigChanged || aiRequirementChanged;
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new AssetDomainException("Asset has been deleted");
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
        this.version++;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String requireOwnerUserId(String ownerUserId) {
        String normalized = normalizeOptional(ownerUserId);
        if (normalized == null) {
            throw new IllegalArgumentException("Owner user id must not be blank");
        }
        return normalized;
    }

    private static String requireProxyProfileId(String proxyProfileId) {
        String normalized = normalizeOptional(proxyProfileId);
        if (normalized == null) {
            throw new IllegalArgumentException("Proxy profile id must not be blank");
        }
        return normalized;
    }

    public AssetId getId() {
        return id;
    }

    public ApiCode getCode() {
        return code;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public String getName() {
        return name;
    }

    public AssetType getType() {
        return type;
    }

    public CategoryRef getCategoryRef() {
        return categoryRef;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public UpstreamEndpointConfig getUpstreamConfig() {
        return upstreamConfig;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public ExampleSnapshot getExampleSnapshot() {
        return exampleSnapshot;
    }

    public AsyncTaskConfig getAsyncTaskConfig() {
        return asyncTaskConfig;
    }

    public AiCapabilityProfile getAiCapabilityProfile() {
        return aiCapabilityProfile;
    }

    public String getProxyProfileId() {
        return proxyProfileId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiAssetAggregate that = (ApiAssetAggregate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
