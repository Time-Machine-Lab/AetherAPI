package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Confirm import plan request.
 */
public class ConfirmImportAgentPlanReq {

    @NotNull
    @Min(1)
    private Integer planVersion;

    public Integer getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(Integer planVersion) {
        this.planVersion = planVersion;
    }
}