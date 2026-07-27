package com.healthcare.hms.appointments.support;

import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.repository.HospitalRepository;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.PatientStatus;
import com.healthcare.hms.patients.repository.PatientRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Healthcare booking preconditions: active patient, schedulable doctor, open department,
 * and no past slots (evaluated in hospital timezone).
 */
@Component
public class AppointmentBookingAccessSupport {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;

    public AppointmentBookingAccessSupport(
            final PatientRepository patientRepository,
            final DoctorRepository doctorRepository,
            final DepartmentRepository departmentRepository,
            final HospitalRepository hospitalRepository
    ) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
    }

    public Patient requireActivePatient(final UUID tenantId, final UUID patientId) {
        final Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (patient.getStatus() != PatientStatus.ACTIVE) {
            throw new BusinessException(
                    "PATIENT_NOT_BOOKABLE",
                    "Only ACTIVE patients can be booked for appointments (status=" + patient.getStatus() + ")"
            );
        }
        return patient;
    }

    public Doctor requireSchedulableDoctor(final UUID tenantId, final UUID doctorId) {
        final Doctor doctor = doctorRepository.findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (doctor.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new BusinessException(
                    "DOCTOR_NOT_SCHEDULABLE",
                    "Doctor is not ACTIVE for scheduling (status=" + doctor.getEmploymentStatus() + ")"
            );
        }
        return doctor;
    }

    public Department requireActiveDepartment(
            final UUID tenantId,
            final UUID departmentId,
            final Doctor doctor
    ) {
        final Department department = departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        if (department.getStatus() != DepartmentStatus.ACTIVE) {
            throw new BusinessException(
                    "DEPARTMENT_NOT_ACTIVE",
                    "Department is not ACTIVE for scheduling"
            );
        }
        if (!department.getHospitalId().equals(doctor.getHospitalId())) {
            throw new BusinessException(
                    "DEPARTMENT_HOSPITAL_MISMATCH",
                    "Department does not belong to the doctor's hospital"
            );
        }
        return department;
    }

    public Hospital requireHospital(final UUID tenantId, final UUID hospitalId) {
        return hospitalRepository.findByIdAndTenantId(hospitalId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
    }

    public void assertNotInPast(
            final Hospital hospital,
            final LocalDate appointmentDate,
            final LocalTime startTime
    ) {
        final ZoneId zone = resolveZone(hospital.getTimezone());
        final LocalDateTime slotStart = LocalDateTime.of(appointmentDate, startTime);
        final LocalDateTime now = LocalDateTime.now(zone);
        if (slotStart.isBefore(now)) {
            throw new BusinessException(
                    "PAST_APPOINTMENT",
                    "Cannot book or move an appointment into the past"
            );
        }
    }

    private static ZoneId resolveZone(final String timezone) {
        try {
            return ZoneId.of(timezone == null || timezone.isBlank() ? Hospital.DEFAULT_TIMEZONE : timezone);
        } catch (final Exception ex) {
            return ZoneId.of(Hospital.DEFAULT_TIMEZONE);
        }
    }
}
