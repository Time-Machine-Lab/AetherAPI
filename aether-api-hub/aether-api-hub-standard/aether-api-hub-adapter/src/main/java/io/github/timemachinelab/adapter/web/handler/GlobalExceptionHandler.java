package io.github.timemachinelab.adapter.web.handler;

import io.github.timemachinelab.api.error.CatalogErrorCodes;
import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.catalog.model.CategoryDomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器，将领域异常映射为标准响应。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryDomainException.class)
    public ResponseEntity<Map<String, String>> handleCategoryDomainException(CategoryDomainException ex) {
        log.warn("Category domain exception: {}", ex.getMessage());

        String code = mapCategoryExceptionToCode(ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        body.put("message", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (code.equals(CatalogErrorCodes.CATEGORY_NOT_FOUND)) {
            status = HttpStatus.NOT_FOUND;
        } else if (code.equals(CatalogErrorCodes.CATEGORY_CODE_ALREADY_EXISTS)) {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AssetDomainException.class)
    public ResponseEntity<Map<String, String>> handleAssetDomainException(AssetDomainException ex) {
        log.warn("Asset domain exception: {}", ex.getMessage());

        String code = mapAssetExceptionToCode(ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        body.put("message", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (CatalogErrorCodes.ASSET_NOT_FOUND.equals(code)) {
            status = HttpStatus.NOT_FOUND;
        } else if (CatalogErrorCodes.API_CODE_ALREADY_EXISTS.equals(code)) {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());

        Map<String, String> body = new HashMap<>();
        body.put("code", mapIllegalArgumentCode(ex.getMessage()));
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";

        Map<String, String> body = new HashMap<>();
        body.put("code", CatalogErrorCodes.CATEGORY_NAME_INVALID);
        body.put("message", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private String mapCategoryExceptionToCode(String message) {
        if (message.contains("already exists")) {
            return CatalogErrorCodes.CATEGORY_CODE_ALREADY_EXISTS;
        }
        if (message.contains("not found")) {
            return CatalogErrorCodes.CATEGORY_NOT_FOUND;
        }
        if (message.contains("already enabled")) {
            return CatalogErrorCodes.CATEGORY_ALREADY_ENABLED;
        }
        if (message.contains("already disabled")) {
            return CatalogErrorCodes.CATEGORY_ALREADY_DISABLED;
        }
        if (message.contains("deleted")) {
            return CatalogErrorCodes.CATEGORY_DELETED;
        }
        return CatalogErrorCodes.CATEGORY_INVALID;
    }

    private String mapAssetExceptionToCode(String message) {
        if (message.contains("already exists")) {
            return CatalogErrorCodes.API_CODE_ALREADY_EXISTS;
        }
        if (message.contains("already enabled")) {
            return CatalogErrorCodes.ASSET_ALREADY_ENABLED;
        }
        if (message.contains("already disabled")) {
            return CatalogErrorCodes.ASSET_ALREADY_DISABLED;
        }
        if (message.contains("Referenced category is invalid")) {
            return CatalogErrorCodes.ASSET_CATEGORY_INVALID;
        }
        if (message.contains("requires an AI capability profile")) {
            return CatalogErrorCodes.AI_PROFILE_REQUIRED;
        }
        if (message.contains("AI capability profile is only allowed")) {
            return CatalogErrorCodes.AI_PROFILE_UNSUPPORTED;
        }
        if (message.contains("Invalid API code")) {
            return CatalogErrorCodes.API_CODE_INVALID;
        }
        if (message.contains("not found")) {
            return CatalogErrorCodes.ASSET_NOT_FOUND;
        }
        return CatalogErrorCodes.ASSET_ACTIVATION_INCOMPLETE;
    }

    private String mapIllegalArgumentCode(String message) {
        if (message != null && message.contains("ApiCode")) {
            return CatalogErrorCodes.API_CODE_INVALID;
        }
        return CatalogErrorCodes.CATEGORY_CODE_INVALID;
    }
}
