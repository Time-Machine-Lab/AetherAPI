package io.github.aetherapihub.catalog.adapter.converter;

import io.github.aetherapihub.catalog.api.dto.CategoryResp;
import io.github.aetherapihub.catalog.api.dto.CategoryValidityResp;
import io.github.aetherapihub.catalog.service.model.CategoryModel;
import io.github.aetherapihub.catalog.service.model.CategoryValidityResult;

/**
 * 分类领域模型与 API DTO 之间的转换器（adapter 层使用）。
 */
public final class CategoryConverter {

    private CategoryConverter() {}

    public static CategoryResp toResp(CategoryModel model) {
        return CategoryResp.builder()
                .id(model.getId().getValue())
                .categoryCode(model.getCode().getValue())
                .categoryName(model.getName())
                .status(model.getStatus().name())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    public static CategoryValidityResp toValidityResp(CategoryValidityResult result) {
        return CategoryValidityResp.builder()
                .categoryCode(result.getCategoryCode())
                .valid(result.isValid())
                .reason(result.getReason())
                .build();
    }
}
