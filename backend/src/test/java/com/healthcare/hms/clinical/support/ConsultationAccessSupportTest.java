package com.healthcare.hms.clinical.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.hospitals.repository.HospitalRepository;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.repository.PatientRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultationAccessSupportTest {

    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private ConsultationRepository consultationRepository;

    private ConsultationAccessSupport support;

    @BeforeEach
    void setUp() {
        support = new ConsultationAccessSupport(
                patientRepository,
                doctorRepository,
                departmentRepository,
                hospitalRepository,
                appointmentRepository,
                consultationRepository
        );
    }

    @Test
    @DisplayName("reject create consultation for COMPLETED appointment")
    void requireConsultableAppointment_rejectsCompleted() {
        final UUID tenantId = UUID.randomUUID();
        final UUID appointmentId = UUID.randomUUID();
        final UUID patientId = UUID.randomUUID();
        final UUID doctorId = UUID.randomUUID();

        final Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findByIdAndTenantId(appointmentId, tenantId))
                .thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> support.requireConsultableAppointment(
                        tenantId, appointmentId, patientId, doctorId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not allow consultation");
    }
}
