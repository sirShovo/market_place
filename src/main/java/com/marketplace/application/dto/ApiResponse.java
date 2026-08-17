package com.marketplace.application.dto;

import java.time.LocalDateTime;

/**
 * Generic wrapper for all API responses.
 * Standardizes the format of both successful and error responses.
 *
 * @param <T> The type of the payload data.
 * @param success Indicates whether the request was successful.
 * @param message A human-readable message providing context about the result.
 * @param data The payload data (null in case of error).
 * @param timestamp The exact time the response was generated.
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp
) {
    /**
     * Creates a successful API response.
     *
     * @param message A success message.
     * @param data The payload data to be returned.
     * @param <T> The type of the payload data.
     * @return A populated ApiResponse indicating success.
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /**
     * Creates an error API response without detailed payload.
     *
     * @param message An error message.
     * @param <T> The type of the payload data.
     * @return A populated ApiResponse indicating an error.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }

    /**
     * Creates an error API response with detailed error information.
     *
     * @param message An error message.
     * @param errorDetails Detailed data regarding the error.
     * @param <T> The type of the error details.
     * @return A populated ApiResponse indicating an error with specific details.
     */
    public static <T> ApiResponse<T> error(String message, T errorDetails) {
        return new ApiResponse<>(false, message, errorDetails, LocalDateTime.now());
    }
}
