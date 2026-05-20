package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Import agent plan model.
 */
public class ImportAgentPlanModel {

    private final Integer version;
    private final boolean executable;
    private final String summary;
    private final List<String> clarificationQuestions;
    private final List<ImportAgentClarificationItemModel> clarificationItems;
    private final List<ImportCategoryPlanModel> categoryPlans;
    private final List<ImportAssetPlanModel> assetPlans;

    public ImportAgentPlanModel(
            Integer version,
            boolean executable,
            String summary,
            List<String> clarificationQuestions,
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans) {
        this(version, executable, summary, clarificationQuestions, List.of(), categoryPlans, assetPlans);
    }

    public ImportAgentPlanModel(
            Integer version,
            boolean executable,
            String summary,
            List<String> clarificationQuestions,
            List<ImportAgentClarificationItemModel> clarificationItems,
            List<ImportCategoryPlanModel> categoryPlans,
            List<ImportAssetPlanModel> assetPlans) {
        this.version = version;
        this.executable = executable;
        this.summary = summary;
        this.clarificationQuestions = clarificationQuestions == null ? List.of() : List.copyOf(clarificationQuestions);
        this.clarificationItems = clarificationItems == null ? List.of() : List.copyOf(clarificationItems);
        this.categoryPlans = categoryPlans == null ? List.of() : List.copyOf(categoryPlans);
        this.assetPlans = assetPlans == null ? List.of() : List.copyOf(assetPlans);
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isExecutable() {
        return executable;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getClarificationQuestions() {
        return clarificationQuestions;
    }

    public List<ImportAgentClarificationItemModel> getClarificationItems() {
        return clarificationItems;
    }

    public List<ImportCategoryPlanModel> getCategoryPlans() {
        return categoryPlans;
    }

    public List<ImportAssetPlanModel> getAssetPlans() {
        return assetPlans;
    }
}
