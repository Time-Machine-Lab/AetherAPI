package io.github.timemachinelab.domain.consolesessionauth.model;

/**
 * Console session auth domain exception.
 */
public class ConsoleSessionAuthDomainException extends RuntimeException {

    public ConsoleSessionAuthDomainException(String message) {
        super(message);
    }

    public ConsoleSessionAuthDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
