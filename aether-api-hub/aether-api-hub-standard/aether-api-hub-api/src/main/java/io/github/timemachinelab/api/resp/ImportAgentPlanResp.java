package io.github.timemachinelab.api.resp;

import java.util.List;

/**
 * Import agent plan response.
 */
public class ImportAgentPlanResp {

    private final Integer version;
    private final boolean executable;
    private final String summary;
    private final List<String> clarificationQuestions;
    private final List<ImportAgentClarificationItemResp> clarificationItems;
    private final List<ImportCategoryPlanResp> categoryPlans;
    private final List<ImportAssetPlanResp> assetPlans;

    public ImportAgentPlanResp(
            Integer version,
            boolean executable,
            String summary,
            List<String> clarificationQuestions,
            List<ImportCategoryPlanResp> categoryPlans,
            List<ImportAssetPlanResp> assetPlans) {
        this(version, executable, summary, clarificationQuestions, List.of(), categoryPlans, assetPlans);
    }

    public ImportAgentPlanResp(
            Integer version,
            boolean executable,
            String summary,
            List<String> clarificationQuestions,
            List<ImportAgentClarificationItemResp> clarificationItems,
            List<ImportCategoryPlanResp> categoryPlans,
            List<ImportAssetPlanResp> assetPlans) {
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

    public List<ImportAgentClarificationItemResp> getClarificationItems() {
        return clarificationItems;
    }

    public List<ImportCategoryPlanResp> getCategoryPlans() {
        return categoryPlans;
    }

    public List<ImportAssetPlanResp> getAssetPlans() {
        return assetPlans;
    }
}
