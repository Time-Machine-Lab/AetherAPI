package io.github.timemachinelab.service.model;

/**
 * Current-user asset workspace list query.
 */
public class ListApiAssetQuery {

    private final String currentUserId;
    private final String status;
    private final String categoryCode;
    private final String keyword;
    private final int page;
    private final int size;

    public ListApiAssetQuery(String currentUserId, String status, String categoryCode, String keyword, int page, int size) {
        this.currentUserId = currentUserId;
        this.status = status;
        this.categoryCode = categoryCode;
        this.keyword = keyword;
        this.page = page;
        this.size = size;
    }

    public String getCurrentUserId() {
        return currentUserId;
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
