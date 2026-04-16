package io.github.timemachinelab.domain.consumerauth.model;

/**
 * Consumer & Auth 领域异常。
 */
public class ConsumerAuthDomainException extends RuntimeException {

    public ConsumerAuthDomainException(String message) {
        super(message);
    }

    public ConsumerAuthDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
