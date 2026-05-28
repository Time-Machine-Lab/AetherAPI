package io.github.timemachinelab.infrastructure.importagent.planner.tools;

/**
 * Legacy grouping for planner tools.
 */
public enum PlannerStage {
    EXTRACT_FACTS("提取导入事实"),
    FILL_SLOTS("补全缺失导入槽位"),
    SUBMIT_PLAN("提交最终导入计划");

    private final String stageLabel;

    PlannerStage(String stageLabel) {
        this.stageLabel = stageLabel;
    }

    public String getStageLabel() {
        return stageLabel;
    }
}
