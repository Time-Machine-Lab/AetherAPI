package io.github.timemachinelab.service.model;

/**
 * 分类分页结果模型。
 */
public class CategoryPageResult {

    private final java.util.List<CategoryModel> items;
    private final int page;
    private final int size;
    private final long total;

    public CategoryPageResult(java.util.List<CategoryModel> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public java.util.List<CategoryModel> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotal() {
        return total;
    }
}
