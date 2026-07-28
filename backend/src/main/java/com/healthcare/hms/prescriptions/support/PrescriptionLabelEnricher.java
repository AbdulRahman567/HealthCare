package com.healthcare.hms.prescriptions.support;

import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionItemResponse;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionResponse;
import com.healthcare.hms.prescriptions.entity.Prescription;
import com.healthcare.hms.prescriptions.entity.PrescriptionItem;
import com.healthcare.hms.prescriptions.mapper.PrescriptionMapper;
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

@Component
public class PrescriptionLabelEnricher {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionMapper prescriptionMapper;

    public PrescriptionLabelEnricher(
            final PatientRepository patientRepository,
            final DoctorRepository doctorRepository,
            final DepartmentRepository departmentRepository,
            final UserRepository userRepository,
            final ConsultationRepository consultationRepository,
            final PrescriptionMapper prescriptionMapper
    ) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.consultationRepository = consultationRepository;
        this.prescriptionMapper = prescriptionMapper;
    }

    public PrescriptionResponse enrichOne(
            final UUID tenantId,
            final Prescription prescription,
            final List<PrescriptionItem> items
    ) {
        return enrich(tenantId, List.of(prescription), Map.of(prescription.getId(), items)).getFirst();
    }

    public List<PrescriptionResponse> enrich(
            final UUID tenantId,
            final Collection<Prescription> prescriptions,
            final Map<UUID, List<PrescriptionItem>> itemsByPrescriptionId
    ) {
        if (prescriptions.isEmpty()) {
            return List.of();
        }

        final Set<UUID> patientIds = prescriptions.stream().map(Prescription::getPatientId).collect(Collectors.toSet());
        final Set<UUID> doctorIds = prescriptions.stream().map(Prescription::getDoctorId).collect(Collectors.toSet());
        final Set<UUID> departmentIds = prescriptions.stream()
                .map(Prescription::getDepartmentId)
                .collect(Collectors.toSet());
        final Set<UUID> consultationIds = prescriptions.stream()
                .map(Prescription::getConsultationId)
                .collect(Collectors.toSet());

        final Map<UUID, Patient> patients = patientRepository.findByTenantIdAndIdIn(tenantId, patientIds).stream()
                .collect(Collectors.toMap(Patient::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Map<UUID, Doctor> doctors = doctorRepository.findByTenantIdAndIdIn(tenantId, doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Map<UUID, Department> departments = departmentRepository.findByTenantIdAndIdIn(tenantId, departmentIds)
                .stream()
                .collect(Collectors.toMap(Department::getId, Function.identity(), (a, b) -> a, HashMap::new));
        final Map<UUID, Consultation> consultations = consultationRepository.findAllById(consultationIds).stream()
                .filter(c -> tenantId.equals(c.getTenantId()))
                .collect(Collectors.toMap(Consultation::getId, Function.identity(), (a, b) -> a, HashMap::new));

        final Set<UUID> userIds = doctors.values().stream().map(Doctor::getUserId).collect(Collectors.toSet());
        final Map<UUID, User> users = userIds.isEmpty()
                ? Map.of()
                : userRepository.findByTenantIdAndIdIn(tenantId, userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, HashMap::new));

        return prescriptions.stream()
                .map(prescription -> {
                    final Patient patient = patients.get(prescription.getPatientId());
                    final Doctor doctor = doctors.get(prescription.getDoctorId());
                    final Department department = departments.get(prescription.getDepartmentId());
                    final Consultation consultation = consultations.get(prescription.getConsultationId());
                    final User doctorUser = doctor == null ? null : users.get(doctor.getUserId());
                    final List<PrescriptionItem> items = itemsByPrescriptionId.getOrDefault(
                            prescription.getId(), List.of());
                    final List<PrescriptionItemResponse> itemResponses = items.stream()
                            .map(prescriptionMapper::toItemResponse)
                            .toList();
                    return prescriptionMapper.toResponse(
                            prescription,
                            itemResponses,
                            consultation == null ? null : consultation.getConsultationNumber(),
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
