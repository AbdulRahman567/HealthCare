package com.healthcare.hms.common.api;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        OffsetDateTime timestamp
) {

    public static <T> ApiResponse<T> success(final String message, final T data) {
        return new ApiResponse<>(true, message, data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> failure(final String message) {
        return new ApiResponse<>(false, message, null, OffsetDateTime.now());
    }
}
