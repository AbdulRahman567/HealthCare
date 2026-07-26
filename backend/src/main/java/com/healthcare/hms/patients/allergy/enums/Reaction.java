package com.healthcare.hms.patients.allergy.enums;

/**
 * Controlled vocabulary for observed / reported allergic reactions.
 *
 * <p>Structured (not free-text) so banner alerts and future drug-allergy checks
 * can reason about reaction class (e.g. anaphylaxis vs mild rash).
 */
public enum Reaction {

    ANAPHYLAXIS,
    ANGIOEDEMA,
    URTICARIA,
    RASH,
    ITCHING,
    BRONCHOSPASM,
    WHEEZING,
    DYSPNEA,
    NAUSEA,
    VOMITING,
    DIARRHEA,
    HYPOTENSION,
    SWELLING,
    OTHER
}
