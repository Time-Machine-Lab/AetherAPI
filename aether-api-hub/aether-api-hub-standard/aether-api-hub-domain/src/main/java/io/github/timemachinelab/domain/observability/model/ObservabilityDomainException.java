package io.github.timemachinelab.domain.observability.model;

/**
 * Observability domain exception.
 */
public class ObservabilityDomainException extends RuntimeException {

    public ObservabilityDomainException(String message) {
        super(message);
    }

    public ObservabilityDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
