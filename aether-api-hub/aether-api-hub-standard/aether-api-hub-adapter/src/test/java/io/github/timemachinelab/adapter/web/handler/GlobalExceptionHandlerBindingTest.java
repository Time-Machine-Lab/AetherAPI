package io.github.timemachinelab.adapter.web.handler;

import io.github.timemachinelab.api.error.CatalogErrorCodes;
import io.github.timemachinelab.api.error.ConsumerAuthErrorCodes;
import io.github.timemachinelab.api.error.ObservabilityErrorCodes;
import io.github.timemachinelab.api.req.ListApiAssetReq;
import io.github.timemachinelab.api.req.ListApiCallLogReq;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerBindingTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingCategoryParameterMapsToCategoryFamily() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/categories");
        ResponseEntity<Map<String, String>> response = handler.handleFrameworkBindingException(
                new MissingServletRequestParameterException("page", "int"), request);

        Map<String, String> body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(CatalogErrorCodes.CATEGORY_CODE_INVALID, body.get("code"));
        assertEquals("Invalid category request parameters", body.get("message"));
    }

    @Test
    void missingCredentialParameterMapsToCredentialFamily() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/current-user/api-keys");

        Map<String, String> body = handler.handleFrameworkBindingException(
                new MissingServletRequestParameterException("page", "int"), request).getBody();

        assertEquals(ConsumerAuthErrorCodes.API_CREDENTIAL_INVALID, body.get("code"));
        assertEquals("Invalid API credential request parameters", body.get("message"));
    }

    @Test
    void typeMismatchMapsToCallLogFamily() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/current-user/api-call-logs");
        Method method = GlobalExceptionHandlerBindingTest.class.getDeclaredMethod("samplePageMethod", int.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "page", parameter, new NumberFormatException("For input string: abc"));

        Map<String, String> body = handler.handleFrameworkBindingException(ex, request).getBody();

        assertEquals(ObservabilityErrorCodes.API_CALL_LOG_INVALID_QUERY, body.get("code"));
        assertEquals("Invalid API call log request parameters", body.get("message"));
    }

    @Test
    void reflectionHintIsSanitizedForAssetRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/assets/deepseek-v3");
        IllegalArgumentException ex = new IllegalArgumentException(
                "Name for argument of type [java.lang.String] not specified, and parameter name information "
                        + "not available via reflection. Ensure that the compiler uses the '-parameters' flag.");

        Map<String, String> body = handler.handleIllegalArgumentException(ex, request).getBody();

        assertEquals(CatalogErrorCodes.API_CODE_INVALID, body.get("code"));
        assertEquals("Invalid asset request parameters", body.get("message"));
        assertFalse(body.get("message").contains("-parameters"));
        assertFalse(body.get("message").contains("reflection"));
    }

    @Test
    void bindExceptionAlsoUsesFamilySpecificCallLogCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/current-user/api-call-logs");
        BindException ex = new BindException(new ListApiCallLogReq(), "listApiCallLogReq");
        ex.rejectValue("page", "typeMismatch", "Failed to convert page");

        Map<String, String> body = handler.handleFrameworkBindingException(ex, request).getBody();

        assertEquals(ObservabilityErrorCodes.API_CALL_LOG_INVALID_QUERY, body.get("code"));
        assertEquals("Invalid API call log request parameters", body.get("message"));
    }

    @Test
    void bindExceptionUsesAssetListQueryCodeForManagementList() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/assets");
        BindException ex = new BindException(new ListApiAssetReq(), "listApiAssetReq");
        ex.rejectValue("page", "Min", "Page must be greater than or equal to 1");

        Map<String, String> body = handler.handleFrameworkBindingException(ex, request).getBody();

        assertEquals(CatalogErrorCodes.ASSET_INVALID_QUERY, body.get("code"));
        assertEquals("Invalid asset list query parameters", body.get("message"));
    }

    @SuppressWarnings("unused")
    private void samplePageMethod(int page) {
    }
}
