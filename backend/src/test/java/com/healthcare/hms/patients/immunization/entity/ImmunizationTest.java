package com.healthcare.hms.patients.immunization.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Immunization due-date rules")
class ImmunizationTest {

    @Test
    @DisplayName("isDueOnOrBefore is true when nextDueDate is today and status ADMINISTERED")
    void due_today_administered() {
        final Immunization immunization = base();
        immunization.setNextDueDate(LocalDate.of(2026, 7, 26));
        immunization.setStatus(ImmunizationStatus.ADMINISTERED);

        assertThat(immunization.isDueOnOrBefore(LocalDate.of(2026, 7, 26))).isTrue();
    }

    @Test
    @DisplayName("isDueOnOrBefore is false when nextDueDate is in the future")
    void due_future_notYet() {
        final Immunization immunization = base();
        immunization.setNextDueDate(LocalDate.of(2026, 8, 1));
        immunization.setStatus(ImmunizationStatus.ADMINISTERED);

        assertThat(immunization.isDueOnOrBefore(LocalDate.of(2026, 7, 26))).isFalse();
    }

    @Test
    @DisplayName("isDueOnOrBefore is false when status is not ADMINISTERED")
    void due_refused_ignored() {
        final Immunization immunization = base();
        immunization.setNextDueDate(LocalDate.of(2026, 7, 1));
        immunization.setStatus(ImmunizationStatus.REFUSED);

        assertThat(immunization.isDueOnOrBefore(LocalDate.of(2026, 7, 26))).isFalse();
    }

    @Test
    @DisplayName("isUpcoming is true for nextDueDate within window")
    void upcoming_withinWindow() {
        final Immunization immunization = base();
        immunization.setNextDueDate(LocalDate.of(2026, 8, 15));
        immunization.setStatus(ImmunizationStatus.ADMINISTERED);

        assertThat(immunization.isUpcoming(
                LocalDate.of(2026, 7, 26),
                LocalDate.of(2026, 8, 31)
        )).isTrue();
    }

    private static Immunization base() {
        final Immunization immunization = new Immunization();
        immunization.setVaccineName("Hepatitis B");
        immunization.setDoseNumber(1);
        immunization.setAdministrationDate(LocalDate.of(2026, 1, 15));
        immunization.setHealthcareProvider("Dr. Smith");
        return immunization;
    }
}
