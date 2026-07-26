package com.healthcare.hms.patients.immunization.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.patients.immunization.dto.request.UpsertImmunizationRequest;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.enums.VaccineRoute;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UpsertImmunizationRequest validation")
class UpsertImmunizationRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("valid request passes")
    void valid_passes() {
        final Set<ConstraintViolation<UpsertImmunizationRequest>> violations = validator.validate(validRequest(
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 7, 15)
        ));
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("nextDueDate before administrationDate fails")
    void nextDueBeforeAdmin_fails() {
        final Set<ConstraintViolation<UpsertImmunizationRequest>> violations = validator.validate(validRequest(
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 1, 15)
        ));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("nextDueDate"));
    }

    @Test
    @DisplayName("dose number below 1 fails")
    void doseTooLow_fails() {
        final UpsertImmunizationRequest request = new UpsertImmunizationRequest(
                "Hep B",
                null,
                0,
                null,
                null,
                LocalDate.of(2026, 1, 15),
                null,
                "Dr. Smith",
                VaccineRoute.INTRAMUSCULAR,
                ImmunizationStatus.ADMINISTERED,
                null
        );
        assertThat(validator.validate(request)).anyMatch(v -> v.getPropertyPath().toString().equals("doseNumber"));
    }

    private static UpsertImmunizationRequest validRequest(final LocalDate admin, final LocalDate nextDue) {
        return new UpsertImmunizationRequest(
                "Hepatitis B",
                "CVX-08",
                1,
                "GSK",
                "LOT-1",
                admin,
                nextDue,
                "Dr. Smith",
                VaccineRoute.INTRAMUSCULAR,
                ImmunizationStatus.ADMINISTERED,
                "First dose"
        );
    }
}
