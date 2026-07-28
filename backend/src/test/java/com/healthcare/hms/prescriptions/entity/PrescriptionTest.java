package com.healthcare.hms.prescriptions.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PrescriptionTest {

    @Test
    @DisplayName("issue transitions DRAFT → ISSUED")
    void issue_fromDraft() {
        final Prescription prescription = draft();
        prescription.issue();
        assertThat(prescription.getStatus()).isEqualTo(PrescriptionStatus.ISSUED);
        assertThat(prescription.getIssuedAt()).isNotNull();
        assertThat(prescription.isLineItemsMutable()).isFalse();
    }

    @Test
    @DisplayName("issue rejects non-DRAFT")
    void issue_fromIssued_throws() {
        final Prescription prescription = draft();
        prescription.issue();
        assertThatThrownBy(prescription::issue).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cancel transitions ISSUED → CANCELLED")
    void cancel_fromIssued() {
        final Prescription prescription = draft();
        prescription.issue();
        prescription.cancel("Patient request");
        assertThat(prescription.getStatus()).isEqualTo(PrescriptionStatus.CANCELLED);
        assertThat(prescription.getCancelledAt()).isNotNull();
        assertThat(prescription.getCancelReason()).isEqualTo("Patient request");
    }

    @Test
    @DisplayName("cancel rejects DISPENSED")
    void cancel_fromDispensed_throws() {
        final Prescription prescription = draft();
        prescription.setStatus(PrescriptionStatus.DISPENSED);
        assertThatThrownBy(() -> prescription.cancel(null)).isInstanceOf(IllegalStateException.class);
    }

    private static Prescription draft() {
        final Prescription prescription = new Prescription();
        prescription.setPrescriptionNumber("RX-TEST-1");
        prescription.setConsultationId(UUID.randomUUID());
        prescription.setHospitalId(UUID.randomUUID());
        prescription.setPatientId(UUID.randomUUID());
        prescription.setDoctorId(UUID.randomUUID());
        prescription.setDepartmentId(UUID.randomUUID());
        prescription.setPrescriptionDate(LocalDate.now());
        prescription.setStatus(PrescriptionStatus.DRAFT);
        return prescription;
    }
}
