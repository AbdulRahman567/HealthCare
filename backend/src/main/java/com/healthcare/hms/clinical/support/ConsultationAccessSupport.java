package com.healthcare.hms.clinical.support;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
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
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Healthcare preconditions for consultation encounters.
 */
@Component
public class ConsultationAccessSupport {

    private static final Set<AppointmentStatus> CONSULTABLE_APPOINTMENT_STATUSES = Set.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CONFIRMED
    );

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;

    public ConsultationAccessSupport(
            final PatientRepository patientRepository,
            final DoctorRepository doctorRepository,
            final DepartmentRepository departmentRepository,
            final HospitalRepository hospitalRepository,
            final AppointmentRepository appointmentRepository,
            final ConsultationRepository consultationRepository
    ) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
    }

    public Patient requireActivePatient(final UUID tenantId, final UUID patientId) {
        final Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (patient.getStatus() != PatientStatus.ACTIVE) {
            throw new BusinessException(
                    "PATIENT_NOT_CONSULTABLE",
                    "Only ACTIVE patients can have consultations (status=" + patient.getStatus() + ")"
            );
        }
        return patient;
    }

    public Doctor requireConsultingDoctor(final UUID tenantId, final UUID doctorId) {
        final Doctor doctor = doctorRepository.findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (doctor.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new BusinessException(
                    "DOCTOR_NOT_CONSULTING",
                    "Doctor is not ACTIVE for consultations (status=" + doctor.getEmploymentStatus() + ")"
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
            throw new BusinessException("DEPARTMENT_NOT_ACTIVE", "Department is not ACTIVE for consultations");
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

    public Appointment requireConsultableAppointment(
            final UUID tenantId,
            final UUID appointmentId,
            final UUID patientId,
            final UUID doctorId
    ) {
        final Appointment appointment = appointmentRepository.findByIdAndTenantId(appointmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        if (!appointment.getPatientId().equals(patientId)) {
            throw new BusinessException(
                    "APPOINTMENT_PATIENT_MISMATCH",
                    "Appointment does not belong to the specified patient"
            );
        }
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new BusinessException(
                    "APPOINTMENT_DOCTOR_MISMATCH",
                    "Appointment does not belong to the specified doctor"
            );
        }
        if (!CONSULTABLE_APPOINTMENT_STATUSES.contains(appointment.getStatus())) {
            throw new BusinessException(
                    "APPOINTMENT_NOT_CONSULTABLE",
                    "Appointment status does not allow consultation (status=" + appointment.getStatus() + ")"
            );
        }
        consultationRepository.findByTenantIdAndAppointmentId(tenantId, appointmentId).ifPresent(existing -> {
            throw new ConflictException(
                    "CONSULTATION_ALREADY_EXISTS",
                    "A consultation already exists for this appointment"
            );
        });
        return appointment;
    }
}
