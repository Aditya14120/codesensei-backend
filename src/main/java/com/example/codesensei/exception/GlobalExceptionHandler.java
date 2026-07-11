package com.example.codesensei.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(error -> errors.put(error.getPropertyPath().toString(), error.getMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body", request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, null);
    }

    // Catches auth failures raised inside controller/service methods (e.g. by @PreAuthorize).
    // Failures at the filter-chain level, before the request reaches a controller, are handled
    // by SecurityConfig's authenticationEntryPoint instead, since this advice never sees those.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED,
                "Authentication required, or the token is missing/invalid/expired", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request,
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled API error on {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request, null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message,
            HttpServletRequest request, Map<String, String> validationErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors);

        return ResponseEntity.status(status).body(body);
    }
}
