package io.github.timemachinelab.service.model;

/**
 * Import agent session status.
 */
public enum ImportAgentSessionStatus {
    WAITING_FOR_PLAN,
    WAITING_FOR_CONFIRMATION,
    WAITING_FOR_CLARIFICATION,
    CONFIRMED,
    EXECUTING,
    COMPLETED,
    FAILED
}