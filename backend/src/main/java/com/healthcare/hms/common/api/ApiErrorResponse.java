package com.healthcare.hms.common.api;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard API error payload aligned with project API contract.
 */
public record ApiErrorResponse(
        boolean success,
        String message,
        String errorCode,
        List<ErrorDetail> errors,
        OffsetDateTime timestamp,
        String path
) {

    public static ApiErrorResponse of(
            final String message,
            final String errorCode,
            final List<ErrorDetail> errors,
            final String path
    ) {
        return new ApiErrorResponse(
                false,
                message,
                errorCode,
                errors == null ? List.of() : List.copyOf(errors),
                OffsetDateTime.now(),
                path
        );
    }

    public static ApiErrorResponse of(final String message, final String errorCode, final String path) {
        return of(message, errorCode, List.of(), path);
    }
}
