package com.healthcare.hms.clinical.enums;

/**
 * Structured clinical note categories.
 *
 * <p>SOAP sections ({@link #SUBJECTIVE}, {@link #OBJECTIVE}, {@link #ASSESSMENT}, {@link #PLAN})
 * plus progress, procedure, and discharge documentation.
 */
public enum ClinicalNoteType {

    /** Subjective — patient-reported symptoms and history. */
    SUBJECTIVE,

    /** Objective — examination findings. */
    OBJECTIVE,

    /** Assessment — clinical impression. */
    ASSESSMENT,

    /** Plan — treatment and investigation plan. */
    PLAN,

    /** Interval / progress note during ongoing care. */
    PROGRESS,

    /** Procedure / intervention documentation. */
    PROCEDURE,

    /** Discharge summary note. */
    DISCHARGE,

    /** Patient advice and counselling. */
    ADVICE,

    /** General unstructured note when other categories do not apply. */
    GENERAL
}
