package io.github.timemachinelab.infrastructure.importagent.planner;

/**
 * Import agent planner coordination properties.
 */
public class ImportAgentPlannerProperties {

    private boolean allowProviderFallback;

    public boolean isAllowProviderFallback() {
        return allowProviderFallback;
    }

    public void setAllowProviderFallback(boolean allowProviderFallback) {
        this.allowProviderFallback = allowProviderFallback;
    }
}