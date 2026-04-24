package io.github.timemachinelab.infrastructure.catalog.persistence.query;

import io.github.timemachinelab.service.model.ApiAssetSummaryModel;
import io.github.timemachinelab.service.port.out.ApiAssetQueryPort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MyBatis-backed asset management query port.
 */
@Repository
public class MybatisApiAssetQueryPort implements ApiAssetQueryPort {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ApiAssetManagementQueryMapper mapper;

    public MybatisApiAssetQueryPort(ApiAssetManagementQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ApiAssetSummaryModel> findPage(String status, String categoryCode, String keyword, int page, int size) {
        return mapper.selectPage(status, categoryCode, keyword, size, Math.max(0, (page - 1) * size))
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public long count(String status, String categoryCode, String keyword) {
        return mapper.count(status, categoryCode, keyword);
    }

    private ApiAssetSummaryModel toModel(ApiAssetManagementQueryRecord record) {
        return new ApiAssetSummaryModel(
                record.getApiCode(),
                record.getAssetName(),
                record.getAssetType(),
                record.getCategoryCode(),
                record.getCategoryName(),
                record.getStatus(),
                formatInstant(record.getUpdatedAt())
        );
    }

    private String formatInstant(LocalDateTime value) {
        return value == null ? null : TIME_FORMATTER.withZone(ZoneOffset.UTC).format(value.toInstant(ZoneOffset.UTC));
    }
}
