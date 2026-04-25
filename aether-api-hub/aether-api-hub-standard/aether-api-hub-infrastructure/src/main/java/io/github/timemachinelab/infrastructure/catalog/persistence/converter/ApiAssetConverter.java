package io.github.timemachinelab.infrastructure.catalog.persistence.converter;

import io.github.timemachinelab.domain.catalog.model.AiCapabilityProfile;
import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetId;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.CategoryRef;
import io.github.timemachinelab.domain.catalog.model.ExampleSnapshot;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.domain.catalog.model.UpstreamEndpointConfig;
import io.github.timemachinelab.infrastructure.catalog.persistence.entity.ApiAssetDo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API asset converter.
 */
public final class ApiAssetConverter {

    private static final Pattern JSON_STRING_PATTERN = Pattern.compile("\"((?:\\\\.|[^\"])*)\"");

    private ApiAssetConverter() {
    }

    public static ApiAssetAggregate toAggregate(ApiAssetDo source) {
        if (source == null) {
            return null;
        }
        return ApiAssetAggregate.reconstitute(
                AssetId.of(source.getId()),
                ApiCode.of(source.getApiCode()),
                source.getOwnerUserId(),
                source.getPublisherDisplayName(),
                source.getAssetName(),
                AssetType.valueOf(source.getAssetType()),
                CategoryRef.of(source.getCategoryCode()),
                AssetStatus.valueOf(source.getStatus()),
                toInstant(source.getPublishedAt()),
                UpstreamEndpointConfig.of(
                        source.getRequestMethod() == null ? null : RequestMethod.valueOf(source.getRequestMethod()),
                        source.getUpstreamUrl(),
                        source.getAuthScheme() == null ? null : AuthScheme.valueOf(source.getAuthScheme()),
                        source.getAuthConfig()
                ),
                source.getRequestTemplate(),
                ExampleSnapshot.of(source.getRequestExample(), source.getResponseExample()),
                toAiCapabilityProfile(source),
                toInstant(source.getCreatedAt()),
                toInstant(source.getUpdatedAt()),
                Boolean.TRUE.equals(source.getIsDeleted()),
                source.getVersion() == null ? 0L : source.getVersion()
        );
    }

    public static ApiAssetDo toDo(ApiAssetAggregate source) {
        if (source == null) {
            return null;
        }
        ApiAssetDo target = new ApiAssetDo();
        updateAllFields(target, source);
        return target;
    }

    public static void updateDo(ApiAssetDo target, ApiAssetAggregate source) {
        if (target == null || source == null) {
            return;
        }
        updateAllFields(target, source);
    }

    private static void updateAllFields(ApiAssetDo target, ApiAssetAggregate source) {
        target.setId(source.getId().getValue());
        target.setApiCode(source.getCode().getValue());
        target.setOwnerUserId(source.getOwnerUserId());
        target.setPublisherDisplayName(source.getPublisherDisplayName());
        target.setAssetName(source.getName());
        target.setAssetType(source.getType().name());
        target.setCategoryCode(source.getCategoryRef() == null ? null : source.getCategoryRef().getCode());
        target.setStatus(source.getStatus().name());
        target.setPublishedAt(toLocalDateTime(source.getPublishedAt()));
        target.setRequestMethod(source.getUpstreamConfig() == null || source.getUpstreamConfig().getRequestMethod() == null
                ? null
                : source.getUpstreamConfig().getRequestMethod().name());
        target.setUpstreamUrl(source.getUpstreamConfig() == null ? null : source.getUpstreamConfig().getUpstreamUrl());
        target.setAuthScheme(source.getUpstreamConfig() == null || source.getUpstreamConfig().getAuthScheme() == null
                ? null
                : source.getUpstreamConfig().getAuthScheme().name());
        target.setAuthConfig(source.getUpstreamConfig() == null ? null : source.getUpstreamConfig().getAuthConfig());
        target.setRequestTemplate(source.getRequestTemplate());
        target.setRequestExample(source.getExampleSnapshot() == null ? null : source.getExampleSnapshot().getRequestExample());
        target.setResponseExample(source.getExampleSnapshot() == null ? null : source.getExampleSnapshot().getResponseExample());
        target.setAiProvider(source.getAiCapabilityProfile() == null ? null : source.getAiCapabilityProfile().getProvider());
        target.setAiModel(source.getAiCapabilityProfile() == null ? null : source.getAiCapabilityProfile().getModel());
        target.setAiStreamingSupported(source.getAiCapabilityProfile() == null ? null : source.getAiCapabilityProfile().isStreamingSupported());
        target.setAiCapabilityTagsJson(source.getAiCapabilityProfile() == null ? null : serializeTags(source.getAiCapabilityProfile().getCapabilityTags()));
        target.setCreatedAt(toLocalDateTime(source.getCreatedAt()));
        target.setUpdatedAt(toLocalDateTime(source.getUpdatedAt()));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
    }

    private static AiCapabilityProfile toAiCapabilityProfile(ApiAssetDo source) {
        if (source.getAiProvider() == null || source.getAiModel() == null || source.getAiStreamingSupported() == null) {
            return null;
        }
        List<String> tags = parseTags(source.getAiCapabilityTagsJson());
        if (tags.isEmpty()) {
            return null;
        }
        return AiCapabilityProfile.of(
                source.getAiProvider(),
                source.getAiModel(),
                source.getAiStreamingSupported(),
                tags
        );
    }

    private static java.time.Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    private static LocalDateTime toLocalDateTime(java.time.Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private static String serializeTags(List<String> tags) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('"')
                    .append(tags.get(i).replace("\\", "\\\\").replace("\"", "\\\""))
                    .append('"');
        }
        builder.append(']');
        return builder.toString();
    }

    private static List<String> parseTags(String json) {
        List<String> result = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return result;
        }
        Matcher matcher = JSON_STRING_PATTERN.matcher(json);
        while (matcher.find()) {
            result.add(matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\"));
        }
        return result;
    }
}
