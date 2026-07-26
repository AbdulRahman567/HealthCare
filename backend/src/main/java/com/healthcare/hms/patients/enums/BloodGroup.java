package com.healthcare.hms.patients.enums;

/**
 * ABO + Rh blood group for transfusion / clinical safety context.
 *
 * <p>Persisted as enum names ({@code A_POSITIVE}, …). API layers may map to
 * display labels such as {@code A+} when responses are introduced.
 */
public enum BloodGroup {

    A_POSITIVE,
    A_NEGATIVE,
    B_POSITIVE,
    B_NEGATIVE,
    AB_POSITIVE,
    AB_NEGATIVE,
    O_POSITIVE,
    O_NEGATIVE,

    /** Not yet typed or unavailable on the chart. */
    UNKNOWN
}
