package com.healthcare.hms.clinical.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.clinical.enums.ConsultationStatus;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultationTest {

    @Test
    @DisplayName("start transitions DRAFT → IN_PROGRESS")
    void start_fromDraft() {
        final Consultation consultation = draft();
        consultation.start();
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.IN_PROGRESS);
        assertThat(consultation.getStartedAt()).isNotNull();
        assertThat(consultation.isEditable()).isTrue();
    }

    @Test
    @DisplayName("complete transitions IN_PROGRESS → COMPLETED")
    void complete_fromInProgress() {
        final Consultation consultation = draft();
        consultation.start();
        consultation.complete();
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.COMPLETED);
        assertThat(consultation.getCompletedAt()).isNotNull();
        assertThat(consultation.isEditable()).isFalse();
    }

    @Test
    @DisplayName("cancel transitions editable statuses → CANCELLED")
    void cancel_fromInProgress() {
        final Consultation consultation = draft();
        consultation.start();
        consultation.cancel();
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.CANCELLED);
        assertThat(consultation.isEditable()).isFalse();
    }

    @Test
    @DisplayName("cancel rejects COMPLETED")
    void cancel_fromCompleted_throws() {
        final Consultation consultation = draft();
        consultation.start();
        consultation.complete();
        assertThatThrownBy(consultation::cancel).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("pause and resume round-trip")
    void pauseResume() {
        final Consultation consultation = draft();
        consultation.start();
        consultation.pause();
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.PAUSED);
        consultation.resume();
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.IN_PROGRESS);
    }

    private static Consultation draft() {
        final Consultation consultation = new Consultation();
        consultation.setHospitalId(UUID.randomUUID());
        consultation.setConsultationNumber("C-TEST-1");
        consultation.setPatientId(UUID.randomUUID());
        consultation.setDoctorId(UUID.randomUUID());
        consultation.setDepartmentId(UUID.randomUUID());
        consultation.setConsultationDate(java.time.LocalDate.now());
        consultation.setStatus(ConsultationStatus.DRAFT);
        return consultation;
    }
}
