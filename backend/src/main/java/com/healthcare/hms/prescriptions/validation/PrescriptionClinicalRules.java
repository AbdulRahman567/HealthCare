package com.healthcare.hms.prescriptions.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Domain rules for prescriptions and medicine lines.
 */
public final class PrescriptionClinicalRules {

    private PrescriptionClinicalRules() {
    }

    public static String normalizeMedicineKey(final String medicineName) {
        if (medicineName == null) {
            return null;
        }
        return medicineName.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public static boolean hasDuplicateMedicines(final List<String> medicineNames) {
        if (medicineNames == null || medicineNames.isEmpty()) {
            return false;
        }
        final Set<String> seen = new HashSet<>();
        for (final String name : medicineNames) {
            final String key = normalizeMedicineKey(name);
            if (key == null || key.isBlank()) {
                continue;
            }
            if (!seen.add(key)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFoodTimingValid(final Boolean beforeFood, final Boolean afterFood) {
        return !(Boolean.TRUE.equals(beforeFood) && Boolean.TRUE.equals(afterFood));
    }
}
