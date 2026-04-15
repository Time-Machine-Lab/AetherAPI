package io.github.aetherapihub.catalog.adapter.controller;

import io.github.aetherapihub.catalog.api.CatalogErrorCodes;
import io.github.aetherapihub.catalog.domain.model.CategoryDomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器，将领域异常映射为标准 HTTP 响应。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryDomainException.class)
    public ResponseEntity<Map<String, String>> handleCategoryDomainException(CategoryDomainException ex) {
        log.warn("业务异常: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        HttpStatus status = mapToHttpStatus(ex.getErrorCode());
        Map<String, String> body = new HashMap<>();
        body.put("code", ex.getErrorCode());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("code", "VALIDATION_ERROR");
        body.put("message", ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("code", "INVALID_ARGUMENT");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private HttpStatus mapToHttpStatus(String errorCode) {
        return switch (errorCode) {
            case CatalogErrorCodes.CATEGORY_CODE_DUPLICATE -> HttpStatus.CONFLICT;
            case CatalogErrorCodes.CATEGORY_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
