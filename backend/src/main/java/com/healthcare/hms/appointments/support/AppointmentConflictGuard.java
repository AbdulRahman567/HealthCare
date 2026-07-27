package com.healthcare.hms.appointments.support;

import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.repository.PatientRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Prevents double-booking: overlapping SCHEDULED/CONFIRMED slots for the same
 * doctor or the same patient.
 *
 * <p>Serializes concurrent book/reschedule attempts for a doctor+patient pair via
 * pessimistic locks (ordered by UUID to avoid deadlocks).
 */
@Component
public class AppointmentConflictGuard {

    public static final Set<AppointmentStatus> ACTIVE_SLOT_STATUSES =
            EnumSet.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentConflictGuard(
            final AppointmentRepository appointmentRepository,
            final DoctorRepository doctorRepository,
            final PatientRepository patientRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Locks doctor and patient rows (stable UUID order), then asserts no overlapping active slots.
     */
    public void assertNoConflictsUnderLock(
            final UUID tenantId,
            final UUID patientId,
            final UUID doctorId,
            final LocalDate date,
            final LocalTime startTime,
            final LocalTime endTime,
            final UUID excludeAppointmentId
    ) {
        lockBookingActors(tenantId, patientId, doctorId);
        assertNoDoctorConflict(tenantId, doctorId, date, startTime, endTime, excludeAppointmentId);
        assertNoPatientConflict(tenantId, patientId, date, startTime, endTime, excludeAppointmentId);
    }

    public void assertNoDoctorConflict(
            final UUID tenantId,
            final UUID doctorId,
            final LocalDate date,
            final LocalTime startTime,
            final LocalTime endTime,
            final UUID excludeAppointmentId
    ) {
        if (!appointmentRepository.findDoctorSlotConflicts(
                tenantId, doctorId, date, startTime, endTime, ACTIVE_SLOT_STATUSES, excludeAppointmentId
        ).isEmpty()) {
            throw new ConflictException(
                    "DOCTOR_DOUBLE_BOOKING",
                    "Doctor already has an appointment overlapping this time slot"
            );
        }
    }

    public void assertNoPatientConflict(
            final UUID tenantId,
            final UUID patientId,
            final LocalDate date,
            final LocalTime startTime,
            final LocalTime endTime,
            final UUID excludeAppointmentId
    ) {
        if (!appointmentRepository.findPatientSlotConflicts(
                tenantId, patientId, date, startTime, endTime, ACTIVE_SLOT_STATUSES, excludeAppointmentId
        ).isEmpty()) {
            throw new ConflictException(
                    "PATIENT_APPOINTMENT_CONFLICT",
                    "Patient already has an appointment overlapping this time slot"
            );
        }
    }

    private void lockBookingActors(final UUID tenantId, final UUID patientId, final UUID doctorId) {
        // Stable lock order prevents A→B vs B→A deadlocks under concurrency.
        if (patientId.compareTo(doctorId) < 0) {
            lockPatient(tenantId, patientId);
            lockDoctor(tenantId, doctorId);
        } else {
            lockDoctor(tenantId, doctorId);
            lockPatient(tenantId, patientId);
        }
    }

    private void lockDoctor(final UUID tenantId, final UUID doctorId) {
        doctorRepository.findByIdAndTenantIdForUpdate(doctorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private void lockPatient(final UUID tenantId, final UUID patientId) {
        patientRepository.findByIdAndTenantIdForUpdate(patientId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }
}
