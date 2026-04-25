package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import io.github.timemachinelab.service.model.CatalogDiscoveryAiCapabilityProfileModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryAssetDetailModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryAssetSummaryModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryCategoryModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryExampleSnapshotModel;
import io.github.timemachinelab.service.model.CatalogDiscoveryPublisherModel;
import io.github.timemachinelab.service.port.out.CatalogDiscoveryQueryPort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis-backed catalog discovery query port.
 */
@Repository
public class MybatisCatalogDiscoveryQueryPort implements CatalogDiscoveryQueryPort {

    private static final Pattern JSON_STRING_PATTERN = Pattern.compile("\"((?:\\\\.|[^\"])*)\"");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final CatalogDiscoveryMapper mapper;

    public MybatisCatalogDiscoveryQueryPort(CatalogDiscoveryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<CatalogDiscoveryAssetSummaryModel> listDiscoverableAssets() {
        return mapper.selectAssetSummaries().stream()
                .map(this::toSummaryModel)
                .toList();
    }

    @Override
    public Optional<CatalogDiscoveryAssetDetailModel> findDiscoverableAssetDetail(String apiCode) {
        CatalogDiscoveryAssetRecord record = mapper.selectAssetDetail(apiCode);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(toDetailModel(record));
    }

    private CatalogDiscoveryAssetSummaryModel toSummaryModel(CatalogDiscoveryAssetRecord record) {
        return new CatalogDiscoveryAssetSummaryModel(
                record.getApiCode(),
                record.getAssetName(),
                record.getAssetType(),
                toCategoryModel(record),
                toPublisherModel(record),
                formatInstant(record.getPublishedAt())
        );
    }

    private CatalogDiscoveryAssetDetailModel toDetailModel(CatalogDiscoveryAssetRecord record) {
        return new CatalogDiscoveryAssetDetailModel(
                record.getApiCode(),
                record.getAssetName(),
                record.getAssetType(),
                toCategoryModel(record),
                toPublisherModel(record),
                formatInstant(record.getPublishedAt()),
                record.getRequestMethod(),
                record.getAuthScheme(),
                record.getRequestTemplate(),
                toExampleSnapshotModel(record),
                toAiProfileModel(record)
        );
    }

    private CatalogDiscoveryCategoryModel toCategoryModel(CatalogDiscoveryAssetRecord record) {
        if (record.getCategoryCode() == null && record.getCategoryName() == null) {
            return null;
        }
        return new CatalogDiscoveryCategoryModel(record.getCategoryCode(), record.getCategoryName());
    }

    private CatalogDiscoveryPublisherModel toPublisherModel(CatalogDiscoveryAssetRecord record) {
        if (isBlank(record.getPublisherDisplayName())) {
            return null;
        }
        return new CatalogDiscoveryPublisherModel(record.getPublisherDisplayName());
    }

    private CatalogDiscoveryExampleSnapshotModel toExampleSnapshotModel(CatalogDiscoveryAssetRecord record) {
        if (isBlank(record.getRequestExample()) && isBlank(record.getResponseExample())) {
            return null;
        }
        return new CatalogDiscoveryExampleSnapshotModel(record.getRequestExample(), record.getResponseExample());
    }

    private CatalogDiscoveryAiCapabilityProfileModel toAiProfileModel(CatalogDiscoveryAssetRecord record) {
        if (!"AI_API".equals(record.getAssetType())) {
            return null;
        }
        if (isBlank(record.getAiProvider()) || isBlank(record.getAiModel()) || record.getAiStreamingSupported() == null) {
            return null;
        }
        return new CatalogDiscoveryAiCapabilityProfileModel(
                record.getAiProvider(),
                record.getAiModel(),
                record.getAiStreamingSupported(),
                parseTags(record.getAiCapabilityTagsJson())
        );
    }

    private List<String> parseTags(String json) {
        List<String> result = new ArrayList<>();
        if (isBlank(json)) {
            return result;
        }
        Matcher matcher = JSON_STRING_PATTERN.matcher(json);
        while (matcher.find()) {
            result.add(matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\"));
        }
        return result;
    }

    private String formatInstant(LocalDateTime value) {
        return value == null ? null : TIME_FORMATTER.withZone(ZoneOffset.UTC).format(value.toInstant(ZoneOffset.UTC));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
