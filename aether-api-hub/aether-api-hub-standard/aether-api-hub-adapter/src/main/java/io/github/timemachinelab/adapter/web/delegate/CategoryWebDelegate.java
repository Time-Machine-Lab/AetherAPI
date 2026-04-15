package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.CreateCategoryReq;
import io.github.timemachinelab.api.req.RenameCategoryReq;
import io.github.timemachinelab.api.resp.CategoryPageResp;
import io.github.timemachinelab.api.resp.CategoryResp;
import io.github.timemachinelab.api.resp.CategoryValidityResp;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;
import io.github.timemachinelab.service.model.CategoryModel;
import io.github.timemachinelab.service.model.CategoryPageResult;
import io.github.timemachinelab.service.model.CategoryValidityResult;
import io.github.timemachinelab.service.model.CreateCategoryCommand;
import io.github.timemachinelab.service.model.RenameCategoryCommand;
import io.github.timemachinelab.service.port.in.CategoryUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类管理 Web Delegate，负责 DTO 转换与用例编排。
 */
@Component
public class CategoryWebDelegate {

    private final CategoryUseCase categoryUseCase;

    public CategoryWebDelegate(CategoryUseCase categoryUseCase) {
        this.categoryUseCase = categoryUseCase;
    }

    public CategoryResp createCategory(CreateCategoryReq req) {
        CreateCategoryCommand command = new CreateCategoryCommand(
                req.getCategoryCode(),
                req.getCategoryName()
        );
        CategoryModel model = categoryUseCase.createCategory(command);
        return toResp(model);
    }

    public CategoryResp renameCategory(String categoryCode, RenameCategoryReq req) {
        RenameCategoryCommand command = new RenameCategoryCommand(
                categoryCode,
                req.getCategoryName()
        );
        CategoryModel model = categoryUseCase.renameCategory(command);
        return toResp(model);
    }

    public CategoryResp enableCategory(String categoryCode) {
        CategoryModel model = categoryUseCase.enableCategory(categoryCode);
        return toResp(model);
    }

    public CategoryResp disableCategory(String categoryCode) {
        CategoryModel model = categoryUseCase.disableCategory(categoryCode);
        return toResp(model);
    }

    public CategoryResp getCategoryByCode(String categoryCode) {
        CategoryModel model = categoryUseCase.getCategoryByCode(categoryCode);
        return toResp(model);
    }

    public CategoryPageResp listCategories(String status, int page, int size) {
        CategoryStatus categoryStatus = null;
        if (status != null && !status.isBlank()) {
            categoryStatus = CategoryStatus.valueOf(status.toUpperCase());
        }

        CategoryPageResult result = categoryUseCase.listCategories(categoryStatus, page, size);

        List<CategoryResp> items = result.getItems().stream()
                .map(this::toResp)
                .collect(Collectors.toList());

        return new CategoryPageResp(items, result.getPage(), result.getSize(), result.getTotal());
    }

    public CategoryValidityResp validateCategory(String categoryCode) {
        CategoryValidityResult result = categoryUseCase.validateCategory(categoryCode);
        return toValidityResp(result);
    }

    private CategoryResp toResp(CategoryModel model) {
        return new CategoryResp(
                model.getId(),
                model.getCode(),
                model.getName(),
                io.github.timemachinelab.domain.catalog.model.CategoryStatus.valueOf(model.getStatus()),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }

    private CategoryValidityResp toValidityResp(CategoryValidityResult result) {
        return new CategoryValidityResp(
                result.getCategoryCode(),
                result.isValid(),
                result.getReason()
        );
    }
}
