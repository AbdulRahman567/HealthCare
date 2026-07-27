package com.healthcare.hms.common.email;

/**
 * Shared helpers so email senders never write raw recipients into application logs.
 */
final class EmailLogRedaction {

    private EmailLogRedaction() {
    }

    /**
     * Masks an email address for logs (keeps domain for ops triage).
     * Example: {@code j***@hospital.example}.
     */
    static String maskRecipient(final String to) {
        if (to == null || to.isBlank()) {
            return "[blank]";
        }
        final String trimmed = to.trim();
        final int at = trimmed.indexOf('@');
        if (at <= 0 || at == trimmed.length() - 1) {
            return "[redacted]";
        }
        final char first = trimmed.charAt(0);
        return first + "***@" + trimmed.substring(at + 1);
    }
}
