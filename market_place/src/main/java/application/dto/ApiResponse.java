package application.dto;

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
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, T errorDetails) {
        return new ApiResponse<>(false, message, errorDetails, LocalDateTime.now());
    }
}
