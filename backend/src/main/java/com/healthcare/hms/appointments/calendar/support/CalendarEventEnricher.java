package com.healthcare.hms.appointments.calendar.support;

import com.healthcare.hms.appointments.calendar.dto.response.CalendarEventResponse;
import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Enriches appointment rows for calendar views using a fixed number of batch queries
 * (appointments page + patients + doctors + users + departments) — never N+1.
 */
@Component
public class CalendarEventEnricher {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public CalendarEventEnricher(
            final PatientRepository patientRepository,
            final DoctorRepository doctorRepository,
            final UserRepository userRepository,
            final DepartmentRepository departmentRepository
    ) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<CalendarEventResponse> enrich(final UUID tenantId, final List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            return List.of();
        }

        final Set<UUID> patientIds = appointments.stream().map(Appointment::getPatientId).collect(Collectors.toSet());
        final Set<UUID> doctorIds = appointments.stream().map(Appointment::getDoctorId).collect(Collectors.toSet());
        final Set<UUID> departmentIds = appointments.stream()
                .map(Appointment::getDepartmentId)
                .collect(Collectors.toSet());

        final Map<UUID, Patient> patients = patientRepository.findByTenantIdAndIdIn(tenantId, patientIds).stream()
                .collect(Collectors.toMap(Patient::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final List<Doctor> doctorRows = doctorRepository.findByTenantIdAndIdIn(tenantId, doctorIds);
        final Map<UUID, Doctor> doctors = doctorRows.stream()
                .collect(Collectors.toMap(Doctor::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Set<UUID> userIds = doctorRows.stream()
                .map(Doctor::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        final Map<UUID, User> users = userRepository.findByTenantIdAndIdIn(tenantId, userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Map<UUID, Department> departments =
                departmentRepository.findByTenantIdAndIdIn(tenantId, departmentIds).stream()
                        .collect(Collectors.toMap(Department::getId, Function.identity(), (a, b) -> a, HashMap::new));

        return appointments.stream()
                .map(appointment -> toEvent(appointment, patients, doctors, users, departments))
                .toList();
    }

    private static CalendarEventResponse toEvent(
            final Appointment appointment,
            final Map<UUID, Patient> patients,
            final Map<UUID, Doctor> doctors,
            final Map<UUID, User> users,
            final Map<UUID, Department> departments
    ) {
        final Patient patient = patients.get(appointment.getPatientId());
        final Doctor doctor = doctors.get(appointment.getDoctorId());
        final User doctorUser = doctor == null ? null : users.get(doctor.getUserId());
        final Department department = departments.get(appointment.getDepartmentId());

        return new CalendarEventResponse(
                appointment.getId(),
                appointment.getAppointmentNumber(),
                appointment.getHospitalId(),
                appointment.getDepartmentId(),
                department == null ? null : department.getName(),
                appointment.getDoctorId(),
                displayName(
                        doctorUser == null ? null : doctorUser.getFirstName(),
                        doctorUser == null ? null : doctorUser.getLastName(),
                        doctor == null ? null : doctor.getEmployeeCode()
                ),
                doctor == null ? null : doctor.getEmployeeCode(),
                appointment.getPatientId(),
                displayName(
                        patient == null ? null : patient.getFirstName(),
                        patient == null ? null : patient.getLastName(),
                        patient == null ? null : patient.getMrn()
                ),
                patient == null ? null : patient.getMrn(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getDurationMinutes(),
                appointment.getStatus(),
                appointment.getAppointmentType(),
                appointment.getVisitType()
        );
    }

    private static String displayName(final String first, final String last, final String fallback) {
        final StringBuilder builder = new StringBuilder();
        if (first != null && !first.isBlank()) {
            builder.append(first.trim());
        }
        if (last != null && !last.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(last.trim());
        }
        if (!builder.isEmpty()) {
            return builder.toString();
        }
        return fallback;
    }
}
