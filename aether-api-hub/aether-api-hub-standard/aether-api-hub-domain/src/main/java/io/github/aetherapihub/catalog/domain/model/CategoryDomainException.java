package io.github.aetherapihub.catalog.domain.model;

/**
 * 分类业务异常。
 */
public class CategoryDomainException extends RuntimeException {

    private final String errorCode;

    public CategoryDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
