package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.delegate.CategoryWebDelegate;
import io.github.timemachinelab.api.req.CreateCategoryReq;
import io.github.timemachinelab.api.req.RenameCategoryReq;
import io.github.timemachinelab.api.resp.CategoryPageResp;
import io.github.timemachinelab.api.resp.CategoryResp;
import io.github.timemachinelab.api.resp.CategoryValidityResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 分类管理 HTTP 接入点。
 */
@RestController
@RequestMapping("/api/v1/categories")
@AutoResp
public class CategoryController {

    private final CategoryWebDelegate delegate;

    public CategoryController(CategoryWebDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * 创建新分类。
     */
    @PostMapping
    public CategoryResp createCategory(@Valid @RequestBody CreateCategoryReq req) {
        return delegate.createCategory(req);
    }

    /**
     * 分页查询分类列表。
     */
    @GetMapping
    public CategoryPageResp listCategories(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return delegate.listCategories(status, page, size);
    }

    /**
     * 根据分类编码查询分类详情。
     */
    @GetMapping("/{categoryCode}")
    public CategoryResp getCategoryByCode(@PathVariable String categoryCode) {
        return delegate.getCategoryByCode(categoryCode);
    }

    /**
     * 重命名分类。
     */
    @PutMapping("/{categoryCode}")
    public CategoryResp renameCategory(
            @PathVariable String categoryCode,
            @Valid @RequestBody RenameCategoryReq req) {
        return delegate.renameCategory(categoryCode, req);
    }

    /**
     * 启用分类。
     */
    @PatchMapping("/{categoryCode}/enable")
    public CategoryResp enableCategory(@PathVariable String categoryCode) {
        return delegate.enableCategory(categoryCode);
    }

    /**
     * 停用分类。
     */
    @PatchMapping("/{categoryCode}/disable")
    public CategoryResp disableCategory(@PathVariable String categoryCode) {
        return delegate.disableCategory(categoryCode);
    }

    /**
     * 校验分类有效性（供下游资产模块调用）。
     */
    @GetMapping("/validate")
    public CategoryValidityResp validateCategory(@RequestParam String categoryCode) {
        return delegate.validateCategory(categoryCode);
    }
}
