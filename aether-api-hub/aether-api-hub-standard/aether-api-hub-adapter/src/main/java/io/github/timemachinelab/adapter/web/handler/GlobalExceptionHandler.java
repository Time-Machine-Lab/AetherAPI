package io.github.timemachinelab.adapter.web.handler;

import io.github.timemachinelab.api.error.CatalogErrorCodes;
import io.github.timemachinelab.api.error.ConsoleSessionAuthErrorCodes;
import io.github.timemachinelab.api.error.ConsumerAuthErrorCodes;
import io.github.timemachinelab.api.error.ObservabilityErrorCodes;
import io.github.timemachinelab.api.resp.UnifiedAccessPlatformFailureResp;
import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.catalog.model.CategoryDomainException;
import io.github.timemachinelab.domain.consolesessionauth.model.ConsoleSessionAuthDomainException;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAuthDomainException;
import io.github.timemachinelab.domain.observability.model.ObservabilityDomainException;
import io.github.timemachinelab.service.model.UnifiedAccessPlatformFailureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器，将领域异常映射为标准响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UnifiedAccessPlatformFailureException.class)
    public ResponseEntity<UnifiedAccessPlatformFailureResp> handleUnifiedAccessPlatformFailure(
            UnifiedAccessPlatformFailureException ex) {
        log.warn("Unified access pre-forward failure: {}", ex.getMessage());

        return ResponseEntity.status(ex.getFailure().getHttpStatus()).body(
                new UnifiedAccessPlatformFailureResp(
                        ex.getFailure().getCode(),
                        ex.getFailure().getMessage(),
                        ex.getFailure().getFailureType().name(),
                        null,
                        ex.getFailure().getApiCode()
                )
        );
    }

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

    @ExceptionHandler(ConsumerAuthDomainException.class)
    public ResponseEntity<Map<String, String>> handleConsumerAuthDomainException(ConsumerAuthDomainException ex) {
        log.warn("Consumer auth domain exception: {}", ex.getMessage());

        String code = mapConsumerAuthExceptionToCode(ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        body.put("message", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ConsumerAuthErrorCodes.API_CREDENTIAL_NOT_FOUND.equals(code)) {
            status = HttpStatus.NOT_FOUND;
        } else if (ConsumerAuthErrorCodes.API_CREDENTIAL_ALREADY_ENABLED.equals(code)
                || ConsumerAuthErrorCodes.API_CREDENTIAL_ALREADY_DISABLED.equals(code)
                || ConsumerAuthErrorCodes.API_CREDENTIAL_ALREADY_REVOKED.equals(code)
                || ConsumerAuthErrorCodes.API_CREDENTIAL_EXPIRED.equals(code)
                || ConsumerAuthErrorCodes.API_CREDENTIAL_REVOKED.equals(code)) {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ConsoleSessionAuthDomainException.class)
    public ResponseEntity<Map<String, String>> handleConsoleSessionAuthDomainException(ConsoleSessionAuthDomainException ex) {
        log.warn("Console session auth exception: {}", ex.getMessage());

        Map<String, String> body = new HashMap<>();
        body.put("code", mapConsoleSessionAuthExceptionToCode(ex.getMessage()));
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ObservabilityDomainException.class)
    public ResponseEntity<Map<String, String>> handleObservabilityDomainException(ObservabilityDomainException ex) {
        log.warn("Observability domain exception: {}", ex.getMessage());

        String code = mapObservabilityExceptionToCode(ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        body.put("message", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ObservabilityErrorCodes.API_CALL_LOG_NOT_FOUND.equals(code)) {
            status = HttpStatus.NOT_FOUND;
        }
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument: {}", ex.getMessage());

        if (isFrameworkBindingIllegalArgumentException(ex)) {
            return buildFrameworkBindingErrorResponse(request.getRequestURI());
        }
        if (isAssetListQueryIllegalArgument(ex.getMessage(), request.getRequestURI())) {
            return buildAssetListQueryErrorResponse();
        }

        Map<String, String> body = new HashMap<>();
        body.put("code", mapIllegalArgumentCode(ex.getMessage()));
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class,
            MethodArgumentTypeMismatchException.class,
            BindException.class,
            ServletRequestBindingException.class
    })
    public ResponseEntity<Map<String, String>> handleFrameworkBindingException(
            Exception ex, HttpServletRequest request) {
        log.warn("Framework binding failure on {}: {}", request.getRequestURI(), ex.getMessage());
        return buildFrameworkBindingErrorResponse(request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";

        Map<String, String> body = new HashMap<>();
        String objectName = ex.getBindingResult().getObjectName();
        if (objectName != null && objectName.toLowerCase().contains("consolesignin")) {
            body.put("code", ConsoleSessionAuthErrorCodes.CONSOLE_SIGN_IN_REQUEST_INVALID);
        } else if (objectName != null && objectName.toLowerCase().contains("credential")) {
            body.put("code", ConsumerAuthErrorCodes.API_CREDENTIAL_INVALID);
        } else if (objectName != null && objectName.toLowerCase().contains("apicalllog")) {
            body.put("code", ObservabilityErrorCodes.API_CALL_LOG_INVALID_QUERY);
        } else if (objectName != null && objectName.toLowerCase().contains("listapiasset")) {
            body.put("code", CatalogErrorCodes.ASSET_INVALID_QUERY);
            message = "Invalid asset list query parameters";
        } else {
            body.put("code", CatalogErrorCodes.CATEGORY_NAME_INVALID);
        }
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
        if (message.contains("already published")) {
            return CatalogErrorCodes.ASSET_ALREADY_PUBLISHED;
        }
        if (message.contains("already unpublished") || message.contains("is not published")) {
            return CatalogErrorCodes.ASSET_ALREADY_UNPUBLISHED;
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
        return CatalogErrorCodes.ASSET_PUBLISH_INCOMPLETE;
    }

    private String mapConsumerAuthExceptionToCode(String message) {
        if (message.contains("not found")) {
            return ConsumerAuthErrorCodes.API_CREDENTIAL_NOT_FOUND;
        }
        if (message.contains("already enabled")) {
            return ConsumerAuthErrorCodes.API_CREDENTIAL_ALREADY_ENABLED;
        }
        if (message.contains("already disabled")) {
            return ConsumerAuthErrorCodes.API_CREDENTIAL_ALREADY_DISABLED;
        }
        if (message.contains("already revoked")) {
            return ConsumerAuthErrorCodes.API_CREDENTIAL_ALREADY_REVOKED;
        }
        if (message.contains("Expired API credential")) {
            return ConsumerAuthErrorCodes.API_CREDENTIAL_EXPIRED;
        }
        if (message.contains("Revoked API credential")) {
            return ConsumerAuthErrorCodes.API_CREDENTIAL_REVOKED;
        }
        if (message.contains("consumer is unavailable")) {
            return ConsumerAuthErrorCodes.CONSUMER_UNAVAILABLE;
        }
        return ConsumerAuthErrorCodes.API_CREDENTIAL_INVALID;
    }

    private String mapConsoleSessionAuthExceptionToCode(String message) {
        if (message != null && message.contains("sign-in credentials")) {
            return ConsoleSessionAuthErrorCodes.CONSOLE_SIGN_IN_CREDENTIALS_INVALID;
        }
        return ConsoleSessionAuthErrorCodes.CONSOLE_SESSION_UNAUTHORIZED;
    }

    private String mapObservabilityExceptionToCode(String message) {
        if (message.contains("not found")) {
            return ObservabilityErrorCodes.API_CALL_LOG_NOT_FOUND;
        }
        return ObservabilityErrorCodes.API_CALL_LOG_INVALID_QUERY;
    }

    private String mapIllegalArgumentCode(String message) {
        if (message != null && message.contains("ApiCode")) {
            return CatalogErrorCodes.API_CODE_INVALID;
        }
        if (message != null && (message.contains("ApiCallLog")
                || message.contains("Invocation")
                || message.contains("API call log"))) {
            return ObservabilityErrorCodes.API_CALL_LOG_INVALID_QUERY;
        }
        if (message != null && (message.contains("console sign-in")
                || message.contains("Console sign-in"))) {
            return ConsoleSessionAuthErrorCodes.CONSOLE_SIGN_IN_REQUEST_INVALID;
        }
        if (message != null && (message.contains("Console session")
                || message.contains("console session"))) {
            return ConsoleSessionAuthErrorCodes.CONSOLE_SESSION_UNAUTHORIZED;
        }
        if (message != null && message.contains("Current user")) {
            return ConsumerAuthErrorCodes.CURRENT_USER_REQUIRED;
        }
        if (message != null && (message.contains("Credential") || message.contains("ExpireAt"))) {
            return ConsumerAuthErrorCodes.API_CREDENTIAL_INVALID;
        }
        return CatalogErrorCodes.CATEGORY_CODE_INVALID;
    }

    private boolean isFrameworkBindingIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("-parameters")
                || message.contains("parameter name information")
                || message.contains("Name for argument of type")
                || message.contains("PathVariable")
                || message.contains("RequestParam");
    }

    private ResponseEntity<Map<String, String>> buildFrameworkBindingErrorResponse(String requestUri) {
        Map<String, String> body = new HashMap<>();
        body.put("code", resolveFrameworkBindingCode(requestUri));
        body.put("message", resolveFrameworkBindingMessage(requestUri));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private String resolveFrameworkBindingCode(String requestUri) {
        if (requestUri != null) {
            if (isAssetListQueryRequest(requestUri)) {
                return CatalogErrorCodes.ASSET_INVALID_QUERY;
            }
            if (requestUri.startsWith("/api/v1/console/auth")) {
                return ConsoleSessionAuthErrorCodes.CONSOLE_SIGN_IN_REQUEST_INVALID;
            }
            if (requestUri.startsWith("/api/v1/current-user/api-keys")) {
                return ConsumerAuthErrorCodes.API_CREDENTIAL_INVALID;
            }
            if (requestUri.startsWith("/api/v1/current-user/api-call-logs")) {
                return ObservabilityErrorCodes.API_CALL_LOG_INVALID_QUERY;
            }
            if (requestUri.startsWith("/api/v1/current-user/assets/")
                    || requestUri.startsWith("/api/v1/discovery/assets")) {
                return CatalogErrorCodes.API_CODE_INVALID;
            }
        }
        return CatalogErrorCodes.CATEGORY_CODE_INVALID;
    }

    private String resolveFrameworkBindingMessage(String requestUri) {
        if (requestUri != null) {
            if (isAssetListQueryRequest(requestUri)) {
                return "Invalid asset list query parameters";
            }
            if (requestUri.startsWith("/api/v1/console/auth")) {
                return "Invalid console sign-in request parameters";
            }
            if (requestUri.startsWith("/api/v1/current-user/api-keys")) {
                return "Invalid API credential request parameters";
            }
            if (requestUri.startsWith("/api/v1/current-user/api-call-logs")) {
                return "Invalid API call log request parameters";
            }
            if (requestUri.startsWith("/api/v1/current-user/assets/")
                    || requestUri.startsWith("/api/v1/discovery/assets")) {
                return "Invalid asset request parameters";
            }
        }
        return "Invalid category request parameters";
    }

    private boolean isAssetListQueryRequest(String requestUri) {
        return "/api/v1/current-user/assets".equals(requestUri)
                || "/api/v1/current-user/assets/".equals(requestUri);
    }

    private boolean isAssetListQueryIllegalArgument(String message, String requestUri) {
        if (!isAssetListQueryRequest(requestUri) || message == null) {
            return false;
        }
        return message.contains("Asset status filter")
                || message.contains("CategoryCode")
                || message.contains("category code");
    }

    private ResponseEntity<Map<String, String>> buildAssetListQueryErrorResponse() {
        Map<String, String> body = new HashMap<>();
        body.put("code", CatalogErrorCodes.ASSET_INVALID_QUERY);
        body.put("message", "Invalid asset list query parameters");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
