package com.healthcare.hms.clinical.support;

import com.healthcare.hms.clinical.dto.response.ConsultationResponse;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.mapper.ConsultationMapper;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Batch-enriches consultation responses with patient, doctor, and department labels.
 */
@Component
public class ConsultationLabelEnricher {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ConsultationMapper consultationMapper;

    public ConsultationLabelEnricher(
            final PatientRepository patientRepository,
            final DoctorRepository doctorRepository,
            final DepartmentRepository departmentRepository,
            final UserRepository userRepository,
            final ConsultationMapper consultationMapper
    ) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.consultationMapper = consultationMapper;
    }

    public ConsultationResponse enrichOne(final UUID tenantId, final Consultation consultation) {
        return enrich(tenantId, List.of(consultation)).getFirst();
    }

    public List<ConsultationResponse> enrich(final UUID tenantId, final Collection<Consultation> consultations) {
        if (consultations.isEmpty()) {
            return List.of();
        }

        final Set<UUID> patientIds = consultations.stream().map(Consultation::getPatientId).collect(Collectors.toSet());
        final Set<UUID> doctorIds = consultations.stream().map(Consultation::getDoctorId).collect(Collectors.toSet());
        final Set<UUID> departmentIds = consultations.stream()
                .map(Consultation::getDepartmentId)
                .collect(Collectors.toSet());

        final Map<UUID, Patient> patients = patientRepository.findByTenantIdAndIdIn(tenantId, patientIds).stream()
                .collect(Collectors.toMap(Patient::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Map<UUID, Doctor> doctors = doctorRepository.findByTenantIdAndIdIn(tenantId, doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Map<UUID, Department> departments = departmentRepository.findByTenantIdAndIdIn(tenantId, departmentIds)
                .stream()
                .collect(Collectors.toMap(Department::getId, Function.identity(), (a, b) -> a, HashMap::new));

        final Set<UUID> userIds = doctors.values().stream()
                .map(Doctor::getUserId)
                .collect(Collectors.toSet());
        final Map<UUID, User> users = userRepository.findByTenantIdAndIdIn(tenantId, userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, HashMap::new));

        return consultations.stream()
                .map(consultation -> {
                    final Patient patient = patients.get(consultation.getPatientId());
                    final Doctor doctor = doctors.get(consultation.getDoctorId());
                    final Department department = departments.get(consultation.getDepartmentId());
                    final User doctorUser = doctor == null ? null : users.get(doctor.getUserId());
                    return consultationMapper.toResponse(
                            consultation,
                            patient == null ? null : displayName(patient.getFirstName(), patient.getLastName(), patient.getMrn()),
                            patient == null ? null : patient.getMrn(),
                            doctorUser == null ? null : displayName(doctorUser.getFirstName(), doctorUser.getLastName(), null),
                            department == null ? null : department.getName()
                    );
                })
                .toList();
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
