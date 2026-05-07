package io.github.timemachinelab.domain.platformproxy.model;

/**
 * Domain exception for platform proxy profile rules.
 */
public class PlatformProxyProfileDomainException extends RuntimeException {

    public PlatformProxyProfileDomainException(String message) {
        super(message);
    }

    public PlatformProxyProfileDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
