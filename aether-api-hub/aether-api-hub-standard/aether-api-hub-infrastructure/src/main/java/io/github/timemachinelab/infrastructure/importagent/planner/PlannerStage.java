package io.github.timemachinelab.infrastructure.importagent.planner;

/**
 * Stages of import-agent planning tool orchestration.
 */
public enum PlannerStage {
    EXTRACT_FACTS("抽取导入事实"),
    FILL_SLOTS("补齐缺失槽位"),
    SUBMIT_PLAN("提交最终计划");

    private final String stageLabel;

    PlannerStage(String stageLabel) {
        this.stageLabel = stageLabel;
    }

    public String getStageLabel() {
        return stageLabel;
    }
}