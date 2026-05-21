package io.github.timemachinelab.service.model;

/**
 * Import agent stream event names.
 */
public enum ImportAgentStreamEventType {
    STATUS("status"),
    THINKING("thinking"),
    MESSAGE("message"),
    SESSION("session"),
    ERROR("error"),
    DONE("done");

    private final String eventName;

    ImportAgentStreamEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
