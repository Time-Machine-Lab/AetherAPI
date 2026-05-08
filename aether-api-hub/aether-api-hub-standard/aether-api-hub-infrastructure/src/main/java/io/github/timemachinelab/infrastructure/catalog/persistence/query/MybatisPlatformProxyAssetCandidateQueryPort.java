package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import io.github.timemachinelab.service.model.PlatformProxyAssetCandidateModel;
import io.github.timemachinelab.service.port.out.PlatformProxyAssetCandidateQueryPort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MyBatis-backed platform proxy asset binding candidate query port.
 */
@Repository
public class MybatisPlatformProxyAssetCandidateQueryPort implements PlatformProxyAssetCandidateQueryPort {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final PlatformProxyAssetCandidateQueryMapper mapper;

    public MybatisPlatformProxyAssetCandidateQueryPort(PlatformProxyAssetCandidateQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<PlatformProxyAssetCandidateModel> findPage(
            String keyword,
            String status,
            String boundProfileId,
            int page,
            int size) {
        int offset = Math.max(0, (page - 1) * size);
        return mapper.selectPage(keyword, status, boundProfileId, size, offset)
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public long count(String keyword, String status, String boundProfileId) {
        return mapper.count(keyword, status, boundProfileId);
    }

    private PlatformProxyAssetCandidateModel toModel(PlatformProxyAssetCandidateQueryRecord record) {
        return new PlatformProxyAssetCandidateModel(
                record.getApiCode(),
                record.getAssetName(),
                record.getAssetType(),
                record.getStatus(),
                record.getPublisherDisplayName(),
                record.getProxyProfileId(),
                record.getProxyProfileCode(),
                record.getProxyProfileName(),
                formatInstant(record.getCreatedAt()),
                formatInstant(record.getUpdatedAt())
        );
    }

    private String formatInstant(LocalDateTime value) {
        return value == null ? null : TIME_FORMATTER.withZone(ZoneOffset.UTC).format(value.toInstant(ZoneOffset.UTC));
    }
}
