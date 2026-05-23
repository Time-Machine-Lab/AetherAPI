package io.github.timemachinelab.service.model;

/**
 * Lightweight category candidate exposed to the import planner.
 */
public class ImportAgentCategoryCandidateModel {

    private final String categoryCode;
    private final String categoryName;
    private final String status;

    public ImportAgentCategoryCandidateModel(String categoryCode, String categoryName, String status) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.status = status;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getStatus() {
        return status;
    }
}
