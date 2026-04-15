package io.github.aetherapihub.catalog.service.model;

import java.util.List;

/**
 * 分类分页结果（应用层内部使用）。
 */
public class CategoryPageResult {

    private final List<CategoryModel> items;
    private final int page;
    private final int size;
    private final long total;

    public CategoryPageResult(List<CategoryModel> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<CategoryModel> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
}
