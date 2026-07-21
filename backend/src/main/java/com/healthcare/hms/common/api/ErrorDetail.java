package com.healthcare.hms.common.api;

/**
 * Field-level validation or constraint error detail.
 */
public record ErrorDetail(
        String field,
        String message
) {
}
