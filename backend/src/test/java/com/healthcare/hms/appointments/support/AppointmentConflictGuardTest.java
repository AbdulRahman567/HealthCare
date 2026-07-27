package com.healthcare.hms.appointments.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentConflictGuardTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;

    private AppointmentConflictGuard guard;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID patientId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID doctorId = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        guard = new AppointmentConflictGuard(appointmentRepository, doctorRepository, patientRepository);
    }

    @Test
    void assertNoConflictsUnderLock_locksPatientThenDoctor_byUuidOrder() {
        when(patientRepository.findByIdAndTenantIdForUpdate(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findByIdAndTenantIdForUpdate(doctorId, tenantId))
                .thenReturn(Optional.of(new Doctor()));
        when(appointmentRepository.findDoctorSlotConflicts(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.findPatientSlotConflicts(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        guard.assertNoConflictsUnderLock(
                tenantId,
                patientId,
                doctorId,
                LocalDate.of(2026, 8, 1),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                null
        );

        // patientId < doctorId ⇒ patient locked first
        verify(patientRepository).findByIdAndTenantIdForUpdate(patientId, tenantId);
        verify(doctorRepository).findByIdAndTenantIdForUpdate(doctorId, tenantId);
    }

    @Test
    void assertNoConflictsUnderLock_throwsOnDoctorOverlap() {
        when(patientRepository.findByIdAndTenantIdForUpdate(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findByIdAndTenantIdForUpdate(doctorId, tenantId))
                .thenReturn(Optional.of(new Doctor()));
        when(appointmentRepository.findDoctorSlotConflicts(
                eq(tenantId), eq(doctorId), any(), any(), any(), any(), any()
        )).thenReturn(List.of(new com.healthcare.hms.appointments.entity.Appointment()));

        assertThatThrownBy(() -> guard.assertNoConflictsUnderLock(
                tenantId,
                patientId,
                doctorId,
                LocalDate.of(2026, 8, 1),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                null
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Doctor already has an appointment");

        verify(appointmentRepository, never()).findPatientSlotConflicts(any(), any(), any(), any(), any(), any(), any());
    }
}
