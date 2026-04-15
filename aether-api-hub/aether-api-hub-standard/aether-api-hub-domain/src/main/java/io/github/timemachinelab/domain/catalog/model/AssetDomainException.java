package io.github.timemachinelab.domain.catalog.model;

/**
 * API 资产领域异常。
 */
public class AssetDomainException extends RuntimeException {

    public AssetDomainException(String message) {
        super(message);
    }

    public AssetDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

