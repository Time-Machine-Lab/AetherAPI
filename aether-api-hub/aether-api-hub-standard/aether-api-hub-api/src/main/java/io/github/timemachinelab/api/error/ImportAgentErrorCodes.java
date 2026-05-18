package io.github.timemachinelab.api.error;

/**
 * Import agent error codes.
 */
public final class ImportAgentErrorCodes {

    public static final String IMPORT_AGENT_INVALID_REQUEST = "IMPORT_AGENT_INVALID_REQUEST";
    public static final String IMPORT_AGENT_SESSION_NOT_FOUND = "IMPORT_AGENT_SESSION_NOT_FOUND";
    public static final String IMPORT_AGENT_RUN_NOT_FOUND = "IMPORT_AGENT_RUN_NOT_FOUND";
    public static final String IMPORT_AGENT_PLAN_CONFIRMATION_REQUIRED = "IMPORT_AGENT_PLAN_CONFIRMATION_REQUIRED";
    public static final String IMPORT_AGENT_PLAN_VERSION_MISMATCH = "IMPORT_AGENT_PLAN_VERSION_MISMATCH";
    public static final String IMPORT_AGENT_PLAN_NOT_EXECUTABLE = "IMPORT_AGENT_PLAN_NOT_EXECUTABLE";
    public static final String IMPORT_AGENT_EXECUTION_FAILED = "IMPORT_AGENT_EXECUTION_FAILED";

    private ImportAgentErrorCodes() {
    }
}