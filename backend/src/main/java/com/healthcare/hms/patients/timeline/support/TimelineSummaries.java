package com.healthcare.hms.patients.timeline.support;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helpers for building bounded timeline summary attributes.
 */
public final class TimelineSummaries {

    private static final int MAX_SUMMARY_LENGTH = 160;

    private TimelineSummaries() {
    }

    public static String truncate(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        final String trimmed = value.trim();
        if (trimmed.length() <= MAX_SUMMARY_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_SUMMARY_LENGTH - 1) + "…";
    }

    public static Map<String, String> attrs(final String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("keyValues must be even");
        }
        final Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            final String key = keyValues[i];
            final String value = keyValues[i + 1];
            if (key != null && value != null && !value.isBlank()) {
                map.put(key, value);
            }
        }
        return Map.copyOf(map);
    }
}
