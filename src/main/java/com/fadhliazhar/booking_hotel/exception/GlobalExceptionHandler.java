package com.fadhliazhar.booking_hotel.exception;

import com.fadhliazhar.booking_hotel.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the booking hotel application
 * Provides centralized error handling and consistent error responses
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
    }

    /**
     * Handle business validation exceptions
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessValidation(
            BusinessValidationException ex, HttpServletRequest request) {
        
        log.warn("Business validation error: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.warn("Validation error on path: {}", request.getRequestURI());
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        List<String> globalErrors = new ArrayList<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.computeIfAbsent(error.getField(), k -> new ArrayList<>())
                      .add(error.getDefaultMessage());
        });
        
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            globalErrors.add(error.getDefaultMessage());
        });
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("fieldErrors", fieldErrors);
        errorDetails.put("globalErrors", globalErrors);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errorDetails));
    }

    /**
     * Handle bind exceptions (form data validation)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<?>> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        log.warn("Bind exception on path: {}", request.getRequestURI());
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        
        for (FieldError error : ex.getFieldErrors()) {
            fieldErrors.computeIfAbsent(error.getField(), k -> new ArrayList<>())
                      .add(error.getDefaultMessage());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", fieldErrors));
    }

    /**
     * Handle constraint violation exceptions (Bean Validation)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        log.warn("Constraint violation on path: {}", request.getRequestURI());
        
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Constraint validation failed", errors));
    }

    /**
     * Handle data integrity violations (database constraints)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        log.error("Data integrity violation on path: {} - Error: {}", 
                 request.getRequestURI(), ex.getMessage());
        
        String message = "Data integrity violation";
        String rootCause = ex.getMostSpecificCause().getMessage();
        
        // Provide more user-friendly messages for common constraint violations
        if (rootCause != null) {
            if (rootCause.contains("Duplicate entry")) {
                message = "A record with this value already exists";
            } else if (rootCause.contains("foreign key constraint")) {
                message = "Referenced resource does not exist";
            } else if (rootCause.contains("cannot be null")) {
                message = "Required field cannot be empty";
            }
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(HttpStatus.CONFLICT.value(), message, null));
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        log.warn("Authentication failed on path: {} - Error: {}", 
                request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Authentication failed", null));
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        log.warn("Access denied on path: {} - Error: {}", 
                request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied", null));
    }

    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        log.warn("Method argument type mismatch on path: {} - Parameter: {} - Error: {}", 
                request.getRequestURI(), ex.getName(), ex.getMessage());
        
        String message = String.format("Invalid value for parameter '%s'. Expected type: %s", 
                                     ex.getName(), ex.getRequiredType().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message, null));
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        log.warn("Missing request parameter on path: {} - Parameter: {}", 
                request.getRequestURI(), ex.getParameterName());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message, null));
    }

    /**
     * Handle HTTP message not readable (malformed JSON)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        log.warn("HTTP message not readable on path: {} - Error: {}", 
                request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Malformed JSON request", null));
    }

    /**
     * Handle unsupported HTTP methods
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        log.warn("HTTP method not supported on path: {} - Method: {}", 
                request.getRequestURI(), ex.getMethod());
        
        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), message, null));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error on path: {} - Error: {}", 
                 request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                                  "An unexpected error occurred", null));
    }
}
