package io.github.timemachinelab.infrastructure.catalog.persistence.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.timemachinelab.domain.catalog.model.AiCapabilityProfile;
import io.github.timemachinelab.domain.catalog.model.AsyncTaskAuthMode;
import io.github.timemachinelab.domain.catalog.model.AsyncTaskConfig;
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
import io.github.timemachinelab.domain.catalog.model.UpstreamRequestHeader;
import io.github.timemachinelab.infrastructure.catalog.persistence.entity.ApiAssetDo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * API asset converter.
 */
public final class ApiAssetConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
                        source.getAuthConfig(),
                        toUpstreamRequestHeaders(source.getUpstreamRequestHeaders())
                ),
                source.getRequestTemplate(),
                ExampleSnapshot.of(source.getRequestExample(), source.getResponseExample()),
                source.getRequestJsonSchema(),
                source.getResponseJsonSchema(),
                toAsyncTaskConfig(source.getAsyncTaskConfig()),
                source.getCapabilityExtensions(),
                source.getPolicyExtensions(),
                source.getMetadataExtensions(),
                toAiCapabilityProfile(source),
                source.getProxyProfileId(),
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
        target.setUpstreamRequestHeaders(source.getUpstreamConfig() == null
                ? null
                : serializeUpstreamRequestHeaders(source.getUpstreamConfig().getUpstreamRequestHeaders()));
        target.setRequestTemplate(source.getRequestTemplate());
        target.setRequestExample(source.getExampleSnapshot() == null ? null : source.getExampleSnapshot().getRequestExample());
        target.setResponseExample(source.getExampleSnapshot() == null ? null : source.getExampleSnapshot().getResponseExample());
        target.setRequestJsonSchema(source.getRequestJsonSchema());
        target.setResponseJsonSchema(source.getResponseJsonSchema());
        target.setAsyncTaskConfig(serializeAsyncTaskConfig(source.getAsyncTaskConfig()));
        target.setCapabilityExtensions(source.getCapabilityExtensions());
        target.setPolicyExtensions(source.getPolicyExtensions());
        target.setMetadataExtensions(source.getMetadataExtensions());
        target.setAiProvider(source.getAiCapabilityProfile() == null ? null : source.getAiCapabilityProfile().getProvider());
        target.setAiModel(source.getAiCapabilityProfile() == null ? null : source.getAiCapabilityProfile().getModel());
        target.setAiStreamingSupported(source.getAiCapabilityProfile() == null ? null : source.getAiCapabilityProfile().isStreamingSupported());
        target.setAiCapabilityTagsJson(source.getAiCapabilityProfile() == null ? null : serializeTags(source.getAiCapabilityProfile().getCapabilityTags()));
        target.setProxyProfileId(source.getProxyProfileId());
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

    private static AsyncTaskConfig toAsyncTaskConfig(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        JsonNode root = readJsonNode(json, "async task config");
        try {
            return AsyncTaskConfig.of(
                    readBooleanField(root, "enabled"),
                    toRequestMethod(readStringField(root, "queryMethod")),
                    readStringField(root, "queryUrlTemplate"),
                    toAsyncTaskAuthMode(readStringField(root, "authMode")),
                    toAuthScheme(readStringField(root, "authScheme")),
                    readStringField(root, "authConfig"),
                    readStringField(root, "queryResponseJsonSchema")
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Stored async task config is invalid", ex);
        }
    }

    private static String serializeAsyncTaskConfig(AsyncTaskConfig config) {
        if (config == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder("{");
        appendJsonBoolean(builder, "enabled", config.isEnabled());
        appendJsonString(builder, "queryMethod", config.getQueryMethod() == null ? null : config.getQueryMethod().name());
        appendJsonString(builder, "queryUrlTemplate", config.getQueryUrlTemplate());
        appendJsonString(builder, "authMode", config.getAuthMode() == null ? null : config.getAuthMode().name());
        appendJsonString(builder, "authScheme", config.getAuthScheme() == null ? null : config.getAuthScheme().name());
        appendJsonString(builder, "authConfig", config.getAuthConfig());
        appendJsonString(builder, "queryResponseJsonSchema", config.getQueryResponseJsonSchema());
        builder.append('}');
        return builder.toString();
    }

    private static List<UpstreamRequestHeader> toUpstreamRequestHeaders(String json) {
        List<UpstreamRequestHeader> result = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return result;
        }
        JsonNode root = readJsonNode(json, "upstream request headers");
        if (root == null || root.isNull()) {
            return result;
        }
        if (root.isObject()) {
            root = OBJECT_MAPPER.createArrayNode().add(root);
        }
        if (!root.isArray()) {
            return result;
        }
        for (JsonNode headerNode : root) {
            String name = readStringField(headerNode, "name");
            String value = readStringField(headerNode, "value");
            if (name != null || value != null) {
                result.add(UpstreamRequestHeader.of(name, value));
            }
        }
        return result;
    }

    private static String serializeUpstreamRequestHeaders(List<UpstreamRequestHeader> headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("[");
        for (UpstreamRequestHeader header : headers) {
            if (header == null) {
                continue;
            }
            if (builder.length() > 1) {
                builder.append(',');
            }
            StringBuilder objectBuilder = new StringBuilder("{");
            appendJsonString(objectBuilder, "name", header.getName());
            appendJsonString(objectBuilder, "value", header.getValue());
            objectBuilder.append('}');
            builder.append(objectBuilder);
        }
        builder.append(']');
        return builder.length() == 2 ? null : builder.toString();
    }

    private static void appendJsonBoolean(StringBuilder builder, String fieldName, boolean value) {
        appendCommaIfNeeded(builder);
        builder.append('"').append(fieldName).append("\":").append(value);
    }

    private static void appendJsonString(StringBuilder builder, String fieldName, String value) {
        if (value == null) {
            return;
        }
        appendCommaIfNeeded(builder);
        builder.append('"').append(fieldName).append("\":\"").append(escapeJson(value)).append('"');
    }

    private static void appendCommaIfNeeded(StringBuilder builder) {
        if (builder.length() > 1) {
            builder.append(',');
        }
    }

    private static JsonNode readJsonNode(String json, String fieldLabel) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException ex) {
            throw new IllegalStateException("Stored " + fieldLabel + " is invalid JSON", ex);
        }
    }

    private static Boolean readBooleanField(JsonNode root, String fieldName) {
        if (root == null || fieldName == null) {
            return null;
        }
        JsonNode fieldNode = root.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        if (fieldNode.isBoolean()) {
            return fieldNode.booleanValue();
        }
        if (fieldNode.isTextual()) {
            String normalized = fieldNode.asText().trim();
            if (normalized.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            }
            if (normalized.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    private static String readStringField(JsonNode root, String fieldName) {
        if (root == null || fieldName == null) {
            return null;
        }
        JsonNode fieldNode = root.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        String rawValue = fieldNode.isTextual() ? fieldNode.asText() : fieldNode.toString();
        return rawValue == null || rawValue.isBlank() ? null : rawValue;
    }

    private static RequestMethod toRequestMethod(String value) {
        return value == null ? null : RequestMethod.valueOf(value);
    }

    private static AsyncTaskAuthMode toAsyncTaskAuthMode(String value) {
        return value == null ? null : AsyncTaskAuthMode.valueOf(value);
    }

    private static AuthScheme toAuthScheme(String value) {
        return value == null ? null : AuthScheme.valueOf(value);
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
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
        JsonNode root = readJsonNode(json, "AI capability tags");
        if (root == null || !root.isArray()) {
            return result;
        }
        for (JsonNode item : root) {
            if (item == null || item.isNull()) {
                continue;
            }
            String value = item.isTextual() ? item.asText() : item.toString();
            if (value != null && !value.isBlank()) {
                result.add(value);
            }
        }
        return result;
    }

}
