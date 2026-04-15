package io.github.aetherapihub.catalog.infrastructure.persistence.converter;

import io.github.aetherapihub.catalog.domain.model.ApiCategoryAggregate;
import io.github.aetherapihub.catalog.domain.model.CategoryCode;
import io.github.aetherapihub.catalog.domain.model.CategoryId;
import io.github.aetherapihub.catalog.domain.model.CategoryStatus;
import io.github.aetherapihub.catalog.infrastructure.persistence.entity.ApiCategoryDo;

/**
 * DO 与聚合根之间的转换器。
 */
public final class ApiCategoryConverter {

    private ApiCategoryConverter() {}

    public static ApiCategoryAggregate toAggregate(ApiCategoryDo from) {
        return new ApiCategoryAggregate(
                new CategoryId(from.getId()),
                new CategoryCode(from.getCategoryCode()),
                from.getCategoryName(),
                from.getStatusEnum(),
                from.getCreatedAt(),
                from.getUpdatedAt(),
                Boolean.TRUE.equals(from.getIsDeleted()),
                from.getVersion() == null ? 0L : from.getVersion()
        );
    }

    public static ApiCategoryDo toDo(ApiCategoryAggregate from) {
        ApiCategoryDo to = new ApiCategoryDo();
        to.setId(from.getId().getValue());
        to.setCategoryCode(from.getCode().getValue());
        to.setCategoryName(from.getName());
        to.setStatus(from.getStatus().name());
        to.setCreatedAt(from.getCreatedAt());
        to.setUpdatedAt(from.getUpdatedAt());
        to.setIsDeleted(from.isDeleted());
        to.setVersion(from.getVersion());
        return to;
    }
}
