package io.github.aetherapihub.catalog.adapter.controller;

import io.github.aetherapihub.catalog.adapter.converter.CategoryConverter;
import io.github.aetherapihub.catalog.api.dto.*;
import io.github.aetherapihub.catalog.api.CatalogErrorCodes;
import io.github.aetherapihub.catalog.domain.model.CategoryDomainException;
import io.github.aetherapihub.catalog.service.CategoryApplicationService;
import io.github.aetherapihub.catalog.service.model.CategoryPageResult;
import io.github.aetherapihub.catalog.service.model.CategoryValidityResult;
import io.github.aetherapihub.catalog.service.model.CreateCategoryCommand;
import io.github.aetherapihub.catalog.service.model.RenameCategoryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理 HTTP 接入层。
 * <p>
 * 负责请求接收、参数校验、DTO 与应用模型转换、异常映射。
 * 不承载任何业务规则。
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryApplicationService categoryService;

    @GetMapping
    public ResponseEntity<CategoryPageResp> listCategories(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        CategoryPageResult result = categoryService.listCategories(status, page, size);
        List<CategoryResp> items = result.getItems().stream()
                .map(CategoryConverter::toResp).toList();
        CategoryPageResp resp = CategoryPageResp.builder()
                .items(items)
                .page(result.getPage())
                .size(result.getSize())
                .total(result.getTotal())
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<CategoryResp> createCategory(@RequestBody CreateCategoryReq req) {
        CreateCategoryCommand command = new CreateCategoryCommand(
                req.getCategoryCode(), req.getCategoryName());
        CategoryResp resp = CategoryConverter.toResp(categoryService.createCategory(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{categoryCode}")
    public ResponseEntity<CategoryResp> getCategory(@PathVariable String categoryCode) {
        CategoryResp resp = categoryService.getByCode(categoryCode)
                .map(CategoryConverter::toResp)
                .orElseThrow(() -> new CategoryDomainException(
                        CatalogErrorCodes.CATEGORY_NOT_FOUND,
                        "分类不存在: " + categoryCode));
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{categoryCode}")
    public ResponseEntity<CategoryResp> renameCategory(
            @PathVariable String categoryCode,
            @RequestBody RenameCategoryReq req) {
        RenameCategoryCommand command = new RenameCategoryCommand(categoryCode, req.getCategoryName());
        CategoryResp resp = CategoryConverter.toResp(categoryService.renameCategory(command));
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{categoryCode}/enable")
    public ResponseEntity<CategoryResp> enableCategory(@PathVariable String categoryCode) {
        CategoryResp resp = CategoryConverter.toResp(categoryService.enableCategory(categoryCode));
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{categoryCode}/disable")
    public ResponseEntity<CategoryResp> disableCategory(@PathVariable String categoryCode) {
        CategoryResp resp = CategoryConverter.toResp(categoryService.disableCategory(categoryCode));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/validate")
    public ResponseEntity<CategoryValidityResp> validateCategory(
            @RequestParam String categoryCode) {
        CategoryValidityResult result = categoryService.checkValidity(categoryCode);
        return ResponseEntity.ok(CategoryConverter.toValidityResp(result));
    }
}
