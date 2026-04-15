package io.github.timemachinelab.domain.catalog.model;

import java.time.Instant;
import java.util.Objects;

/**
 * API 资产聚合根。
 */
public class ApiAssetAggregate {

    private AssetId id;
    private ApiCode code;
    private String name;
    private AssetType type;
    private CategoryRef categoryRef;
    private AssetStatus status;
    private UpstreamEndpointConfig upstreamConfig;
    private String requestTemplate;
    private ExampleSnapshot exampleSnapshot;
    private AiCapabilityProfile aiCapabilityProfile;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    protected ApiAssetAggregate() {
    }

    private ApiAssetAggregate(
            AssetId id,
            ApiCode code,
            String name,
            AssetType type,
            CategoryRef categoryRef,
            AssetStatus status,
            UpstreamEndpointConfig upstreamConfig,
            String requestTemplate,
            ExampleSnapshot exampleSnapshot,
            AiCapabilityProfile aiCapabilityProfile,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        this.id = id;
        this.code = code;
        this.name = normalizeOptional(name);
        this.type = Objects.requireNonNull(type, "Asset type must not be null");
        this.categoryRef = categoryRef;
        this.status = Objects.requireNonNull(status, "Asset status must not be null");
        this.upstreamConfig = upstreamConfig;
        this.requestTemplate = normalizeOptional(requestTemplate);
        this.exampleSnapshot = exampleSnapshot;
        this.aiCapabilityProfile = aiCapabilityProfile;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.version = version;
        if (this.type != AssetType.AI_API) {
            this.aiCapabilityProfile = null;
        }
    }

    public static ApiAssetAggregate registerDraft(AssetId id, ApiCode code, AssetType type, String name) {
        Instant now = Instant.now();
        return new ApiAssetAggregate(
                id,
                code,
                name,
                type,
                null,
                AssetStatus.DRAFT,
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
            String name,
            AssetType type,
            CategoryRef categoryRef,
            AssetStatus status,
            UpstreamEndpointConfig upstreamConfig,
            String requestTemplate,
            ExampleSnapshot exampleSnapshot,
            AiCapabilityProfile aiCapabilityProfile,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return new ApiAssetAggregate(
                id,
                code,
                name,
                type,
                categoryRef,
                status,
                upstreamConfig,
                requestTemplate,
                exampleSnapshot,
                aiCapabilityProfile,
                createdAt,
                updatedAt,
                deleted,
                version
        );
    }

    public void revise(
            String assetName,
            AssetType assetType,
            CategoryRef newCategoryRef,
            UpstreamEndpointConfig newUpstreamConfig,
            String newRequestTemplate,
            ExampleSnapshot newExampleSnapshot) {
        ensureNotDeleted();

        AssetType previousType = this.type;
        CategoryRef previousCategoryRef = this.categoryRef;
        UpstreamEndpointConfig previousUpstreamConfig = this.upstreamConfig;

        this.name = normalizeOptional(assetName);
        this.type = Objects.requireNonNull(assetType, "Asset type must not be null");
        this.categoryRef = newCategoryRef;
        this.upstreamConfig = newUpstreamConfig;
        this.requestTemplate = normalizeOptional(newRequestTemplate);
        this.exampleSnapshot = newExampleSnapshot;

        if (this.type != AssetType.AI_API) {
            this.aiCapabilityProfile = null;
        }

        if (this.status == AssetStatus.ENABLED && requiresRevalidation(previousType, previousCategoryRef, previousUpstreamConfig)) {
            this.status = AssetStatus.DRAFT;
        }

        touch();
    }

    public void replaceExamples(String requestExample, String responseExample) {
        this.exampleSnapshot = ExampleSnapshot.of(requestExample, responseExample);
        touch();
    }

    public void attachAiCapabilityProfile(AiCapabilityProfile profile) {
        ensureNotDeleted();
        if (this.type != AssetType.AI_API) {
            throw new AssetDomainException("AI capability profile is only allowed for AI_API assets");
        }
        this.aiCapabilityProfile = Objects.requireNonNull(profile, "AI capability profile must not be null");
        touch();
    }

    public void enable(CategoryValidityChecker categoryValidityChecker) {
        ensureNotDeleted();
        if (this.status == AssetStatus.ENABLED) {
            throw new AssetDomainException("Asset is already enabled");
        }
        ensureActivationReady(categoryValidityChecker);
        this.status = AssetStatus.ENABLED;
        touch();
    }

    public void disable() {
        ensureNotDeleted();
        if (this.status == AssetStatus.DISABLED) {
            throw new AssetDomainException("Asset is already disabled");
        }
        this.status = AssetStatus.DISABLED;
        touch();
    }

    public boolean isActivationReady(CategoryValidityChecker categoryValidityChecker) {
        if (name == null || categoryRef == null || upstreamConfig == null || !upstreamConfig.isComplete()) {
            return false;
        }
        if (!categoryValidityChecker.isValid(categoryRef)) {
            return false;
        }
        return type != AssetType.AI_API || aiCapabilityProfile != null;
    }

    private void ensureActivationReady(CategoryValidityChecker categoryValidityChecker) {
        if (name == null) {
            throw new AssetDomainException("Asset name must be provided before enabling");
        }
        if (categoryRef == null) {
            throw new AssetDomainException("Category code must be provided before enabling");
        }
        if (upstreamConfig == null || !upstreamConfig.isComplete()) {
            throw new AssetDomainException("Upstream endpoint configuration is incomplete");
        }
        if (!categoryValidityChecker.isValid(categoryRef)) {
            throw new AssetDomainException("Referenced category is invalid");
        }
        if (type == AssetType.AI_API && aiCapabilityProfile == null) {
            throw new AssetDomainException("AI_API asset requires an AI capability profile before enabling");
        }
    }

    private boolean requiresRevalidation(
            AssetType previousType, CategoryRef previousCategoryRef, UpstreamEndpointConfig previousUpstreamConfig) {
        boolean criticalConfigChanged = !Objects.equals(previousCategoryRef, this.categoryRef)
                || (previousUpstreamConfig == null ? this.upstreamConfig != null : previousUpstreamConfig.hasCriticalDifference(this.upstreamConfig));
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

    public AssetId getId() {
        return id;
    }

    public ApiCode getCode() {
        return code;
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

    public UpstreamEndpointConfig getUpstreamConfig() {
        return upstreamConfig;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public ExampleSnapshot getExampleSnapshot() {
        return exampleSnapshot;
    }

    public AiCapabilityProfile getAiCapabilityProfile() {
        return aiCapabilityProfile;
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
