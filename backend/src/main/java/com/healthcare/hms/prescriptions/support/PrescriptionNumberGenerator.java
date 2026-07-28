package com.healthcare.hms.prescriptions.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Generates hospital-facing prescription numbers unique per tenant among live rows.
 */
@Component
public class PrescriptionNumberGenerator {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    public String next() {
        final String day = LocalDate.now().format(DAY);
        final String token = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 10)
                .toUpperCase(Locale.ROOT);
        return "RX-" + day + "-" + token;
    }
}
