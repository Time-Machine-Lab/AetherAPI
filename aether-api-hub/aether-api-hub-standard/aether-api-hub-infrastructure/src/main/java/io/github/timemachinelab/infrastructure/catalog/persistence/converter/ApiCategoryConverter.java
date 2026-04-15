package io.github.timemachinelab.infrastructure.catalog.persistence.converter;

import io.github.timemachinelab.domain.catalog.model.ApiCategoryAggregate;
import io.github.timemachinelab.domain.catalog.model.CategoryCode;
import io.github.timemachinelab.domain.catalog.model.CategoryId;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;
import io.github.timemachinelab.infrastructure.catalog.persistence.entity.ApiCategoryDo;

import java.time.ZoneOffset;

/**
 * API 分类 DO ↔ Aggregate 转换器。
 */
public final class ApiCategoryConverter {

    private ApiCategoryConverter() {
    }

    /**
     * 将 DO 转换为 Aggregate。
     */
    public static ApiCategoryAggregate toAggregate(ApiCategoryDo source) {
        if (source == null) {
            return null;
        }
        return ApiCategoryAggregate.reconstitute(
                CategoryId.of(source.getId()),
                CategoryCode.of(source.getCategoryCode()),
                source.getCategoryName(),
                CategoryStatus.valueOf(source.getStatus()),
                source.getCreatedAt().toInstant(ZoneOffset.UTC),
                source.getUpdatedAt().toInstant(ZoneOffset.UTC),
                Boolean.TRUE.equals(source.getIsDeleted()),
                source.getVersion() != null ? source.getVersion() : 0L
        );
    }

    /**
     * 将 Aggregate 转换为 DO（用于保存）。
     */
    public static ApiCategoryDo toDo(ApiCategoryAggregate source) {
        if (source == null) {
            return null;
        }
        ApiCategoryDo target = new ApiCategoryDo();
        target.setId(source.getId().getValue());
        target.setCategoryCode(source.getCode().getValue());
        target.setCategoryName(source.getName());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(java.time.LocalDateTime.ofInstant(source.getCreatedAt(), ZoneOffset.UTC));
        target.setUpdatedAt(java.time.LocalDateTime.ofInstant(source.getUpdatedAt(), ZoneOffset.UTC));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
        return target;
    }

    /**
     * 更新现有 DO 字段（基于 Aggregate 变化）。
     */
    public static void updateDo(ApiCategoryDo target, ApiCategoryAggregate source) {
        if (target == null || source == null) {
            return;
        }
        target.setCategoryName(source.getName());
        target.setStatus(source.getStatus().name());
        target.setUpdatedAt(java.time.LocalDateTime.ofInstant(source.getUpdatedAt(), ZoneOffset.UTC));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
    }
}
