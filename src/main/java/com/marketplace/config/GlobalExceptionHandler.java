package com.marketplace.config;

import com.marketplace.application.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler configuration.
 *
 * This component acts as a centralized error handling mechanism across the entire API.
 * It catches exceptions thrown by controllers or lower layers and maps them to a
 * standardized HTTP response using the {@link ApiResponse} wrapper.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors thrown when a method argument annotated with @Valid fails validation.
     * Extracts field-specific error messages and bundles them into the error response.
     *
     * @param ex The MethodArgumentNotValidException thrown by Spring Validation.
     * @return A standardized ResponseEntity with a 400 BAD REQUEST status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }

    /**
     * Fallback exception handler for any unexpected server errors.
     * Prevents stack traces from leaking to the client in production.
     *
     * @param ex The generic Exception caught.
     * @return A standardized ResponseEntity with a 500 INTERNAL SERVER ERROR status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {
        // In a production environment, errors should be logged and sensitive messages masked.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
    }
}
