package com.healthcare.hms.patients.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.patients.enums.BloodGroup;
import com.healthcare.hms.patients.enums.Gender;
import com.healthcare.hms.patients.enums.PatientStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PatientTest {

    @Test
    @DisplayName("defaults match registration contract")
    void defaults_areActiveUnknownDemographics() {
        final Patient patient = new Patient();

        assertThat(patient.getStatus()).isEqualTo(PatientStatus.ACTIVE);
        assertThat(patient.getGender()).isEqualTo(Gender.UNKNOWN);
        assertThat(patient.getBloodGroup()).isEqualTo(BloodGroup.UNKNOWN);
        assertThat(patient.getEmergencyContact()).isNotNull();
        assertThat(patient.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("setEmergencyContact null resets to empty embeddable")
    void setEmergencyContact_nullSafe() {
        final Patient patient = newPatient();
        patient.getEmergencyContact().setName("Ali Khan");
        patient.setEmergencyContact(null);

        assertThat(patient.getEmergencyContact()).isNotNull();
        assertThat(patient.getEmergencyContact().getName()).isNull();
    }

    @Test
    @DisplayName("deactivate requires ACTIVE status")
    void deactivate_fromActive() {
        final Patient patient = newPatient();
        patient.deactivate();
        assertThat(patient.getStatus()).isEqualTo(PatientStatus.INACTIVE);
    }

    @Test
    @DisplayName("deactivate rejects non-ACTIVE")
    void deactivate_fromInactive_throws() {
        final Patient patient = newPatient();
        patient.setStatus(PatientStatus.INACTIVE);
        assertThatThrownBy(patient::deactivate)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("reactivate requires INACTIVE status")
    void reactivate_fromInactive() {
        final Patient patient = newPatient();
        patient.setStatus(PatientStatus.INACTIVE);
        patient.reactivate();
        assertThat(patient.getStatus()).isEqualTo(PatientStatus.ACTIVE);
    }

    private static Patient newPatient() {
        final Patient patient = new Patient();
        patient.setMrn("MRN-0001");
        patient.setFirstName("Sara");
        patient.setLastName("Ahmed");
        patient.setDateOfBirth(LocalDate.of(1990, 5, 12));
        patient.setGender(Gender.FEMALE);
        patient.setBloodGroup(BloodGroup.O_POSITIVE);
        return patient;
    }
}
