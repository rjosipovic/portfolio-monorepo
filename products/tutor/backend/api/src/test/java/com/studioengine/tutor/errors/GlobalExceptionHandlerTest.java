package com.studioengine.tutor.errors;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn400ForMissingRequestBody() {
        // given
        var ex = new HttpMessageNotReadableException(
                "Required request body is missing",
                new MockHttpInputMessage(new byte[0]));

        // when
        var response = handler.handleUnreadableBody(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION");
        assertThat(response.getBody().getMessage()).isEqualTo("Malformed or missing request body");
        assertThat(response.getBody().getReason()).isEqualTo("Request body is malformed or missing");
    }

    @Test
    void shouldReturn400ForMissingRequestHeader() throws Exception {
        // given
        var methodParameter = new MethodParameter(
                SampleController.class.getDeclaredMethod("withHeader", String.class), 0);
        var ex = new MissingRequestHeaderException("Stripe-Signature", methodParameter);

        // when
        var response = handler.handleMissingHeader(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION");
        assertThat(response.getBody().getMessage()).isEqualTo("Missing required header");
        assertThat(response.getBody().getReason()).isEqualTo("Header 'Stripe-Signature' is required");
    }

    @Test
    void shouldReturn400ForMissingRequestParameter() {
        // given
        var ex = new MissingServletRequestParameterException("token", "String");

        // when
        var response = handler.handleMissingParam(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION");
        assertThat(response.getBody().getMessage()).isEqualTo("Missing required parameter");
        assertThat(response.getBody().getReason()).isEqualTo("Parameter 'token' is required");
    }

    @Test
    void shouldReturn400ForTypeMismatch() {
        // given
        var ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenReturn((Class) java.util.UUID.class);

        // when
        var response = handler.handleTypeMismatch(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid parameter type");
        assertThat(response.getBody().getReason()).isEqualTo("Parameter 'id' must be of type UUID");
    }

    @Test
    void shouldReturn400ForValidationFailure() {
        // given
        var fieldError = new FieldError("request", "reason", "must not be blank");
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        var ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // when
        var response = handler.handleValidationException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getReason()).isEqualTo("reason must not be blank");
    }

    @Test
    void shouldReturn405ForUnsupportedMethod() {
        // given
        var ex = new HttpRequestMethodNotSupportedException("POST", List.of("PATCH"));

        // when
        var response = handler.handleMethodNotSupported(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(response.getBody().getReason()).isEqualTo("HTTP method 'POST' is not supported for this endpoint");
        // Allow header advertises supported methods
        assertThat(response.getHeaders().getAllow()).containsExactly(HttpMethod.PATCH);
    }

    @Test
    void shouldReturn404ForNoResourceFound() {
        // given
        var ex = new NoResourceFoundException(
                HttpMethod.GET,
                "/api/v1/dashboard/unknown/",   // requestUri (the full request path, trailing slash per your real case)
                "api/v1/dashboard/unknown");

        // when
        var response = handler.handleNoResourceFound(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
        assertThat(response.getBody().getReason()).isEqualTo("No endpoint found for the requested path");
    }

    @Test
    void shouldReturn500ForUnexpectedException() {
        // given
        var ex = new RuntimeException("boom");

        // when
        var response = handler.handleUnexpectedException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL");
        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
        assertThat(response.getBody().getReason()).isNull();
    }

    // Helper to supply a MethodParameter for MissingRequestHeaderException
    @SuppressWarnings("unused")
    private static class SampleController {
        public void withHeader(String header) {}
    }
}
