package io.github.timemachinelab.service.model;

/**
 * Management asset list query.
 */
public class ListApiAssetQuery {

    private final String status;
    private final String categoryCode;
    private final String keyword;
    private final int page;
    private final int size;

    public ListApiAssetQuery(String status, String categoryCode, String keyword, int page, int size) {
        this.status = status;
        this.categoryCode = categoryCode;
        this.keyword = keyword;
        this.page = page;
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
