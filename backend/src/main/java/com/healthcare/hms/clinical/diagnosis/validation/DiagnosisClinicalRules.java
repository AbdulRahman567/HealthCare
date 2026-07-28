package com.healthcare.hms.clinical.diagnosis.validation;

import com.healthcare.hms.clinical.enums.DiagnosisType;

/**
 * Domain rules for consultation diagnoses.
 */
public final class DiagnosisClinicalRules {

    /** ICD-10-CM pattern: one letter, two digits, optional dot extension. */
    private static final String ICD10_PATTERN = "^[A-TV-Z][0-9]{2}(\\.[0-9A-TV-Z]{1,4})?$";

    private DiagnosisClinicalRules() {
    }

    public static boolean isValidIcd10Code(final String icdCode) {
        if (icdCode == null || icdCode.isBlank()) {
            return true;
        }
        return icdCode.trim().toUpperCase().matches(ICD10_PATTERN);
    }

    public static String normalizeIcd10Code(final String icdCode) {
        if (icdCode == null || icdCode.isBlank()) {
            return null;
        }
        return icdCode.trim().toUpperCase();
    }

    public static boolean isPrimaryType(final DiagnosisType type) {
        return type == DiagnosisType.PRIMARY;
    }
}
