package io.github.timemachinelab.infrastructure.importagent.planner.tools;

/**
 * Legacy grouping for planner tools.
 */
public enum PlannerStage {
    EXTRACT_FACTS("Extract import facts"),
    FILL_SLOTS("Fill missing import slots"),
    SUBMIT_PLAN("Submit final import plan");

    private final String stageLabel;

    PlannerStage(String stageLabel) {
        this.stageLabel = stageLabel;
    }

    public String getStageLabel() {
        return stageLabel;
    }
}
