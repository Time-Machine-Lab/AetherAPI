package io.github.timemachinelab.domain.catalog.model;

/**
 * 分类领域公共异常基类。
 */
public class CategoryDomainException extends RuntimeException {

    public CategoryDomainException(String message) {
        super(message);
    }

    public CategoryDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
