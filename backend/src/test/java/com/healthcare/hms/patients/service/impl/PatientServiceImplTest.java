package com.healthcare.hms.patients.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.dto.request.EmergencyContactRequest;
import com.healthcare.hms.patients.dto.request.PatientSearchCriteria;
import com.healthcare.hms.patients.dto.request.RegisterPatientRequest;
import com.healthcare.hms.patients.dto.request.UpdatePatientRequest;
import com.healthcare.hms.patients.dto.response.EmergencyContactResponse;
import com.healthcare.hms.patients.dto.response.PatientResponse;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.BloodGroup;
import com.healthcare.hms.patients.enums.Gender;
import com.healthcare.hms.patients.enums.MaritalStatus;
import com.healthcare.hms.patients.enums.PatientStatus;
import com.healthcare.hms.patients.mapper.PatientMapper;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientServiceImpl")
class PatientServiceImplTest {

    private static final String IP = "127.0.0.1";
    private static final String UA = "junit";

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PatientMapper patientMapper;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PatientServiceImpl service;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();

        TenantContextHolder.set(new TenantContext(
                tenantId,
                "city-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        final AuthenticatedUser principal = new AuthenticatedUser(
                userId,
                tenantId,
                "admin@city.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("PATIENT_READ", "PATIENT_CREATE", "PATIENT_UPDATE", "PATIENT_DELETE"),
                1L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("register rejects duplicate MRN")
    void register_duplicateMrn_throwsConflict() {
        final RegisterPatientRequest request = registerRequest("mrn-001");
        when(patientRepository.existsByTenantIdAndMrnIgnoreCase(tenantId, "MRN-001")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request, IP, UA))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("MRN");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("register persists patient and audits")
    void register_success() {
        final RegisterPatientRequest request = registerRequest("mrn-001");
        final Patient saved = samplePatient(PatientStatus.ACTIVE);
        final PatientResponse response = sampleResponse(saved);

        when(patientRepository.existsByTenantIdAndMrnIgnoreCase(tenantId, "MRN-001")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            final Patient patient = invocation.getArgument(0);
            patient.setId(saved.getId());
            patient.setTenantId(tenantId);
            return patient;
        });
        when(patientMapper.toResponse(any(Patient.class))).thenReturn(response);

        final PatientResponse result = service.register(request, IP, UA);

        assertThat(result.mrn()).isEqualTo("MRN-0001");
        verify(patientMapper).applyRegister(eq(request), any(Patient.class));
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("PATIENT"),
                eq(saved.getId().toString()),
                eq(AuditAction.CREATE),
                isNull(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    @Test
    @DisplayName("getById throws when patient missing in tenant")
    void getById_missing_throwsNotFound() {
        final UUID id = UUID.randomUUID();
        when(patientRepository.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id, IP, UA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("update rejects duplicate MRN for another patient")
    void update_duplicateMrn_throwsConflict() {
        final Patient existing = samplePatient(PatientStatus.ACTIVE);
        final UpdatePatientRequest request = updateRequest("mrn-dup");
        when(patientRepository.findByIdAndTenantId(existing.getId(), tenantId))
                .thenReturn(Optional.of(existing));
        when(patientRepository.existsByTenantIdAndMrnIgnoreCaseAndIdNot(tenantId, "MRN-DUP", existing.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.update(existing.getId(), request, IP, UA))
                .isInstanceOf(ConflictException.class);
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("deactivate transitions ACTIVE to INACTIVE")
    void deactivate_success() {
        final Patient patient = samplePatient(PatientStatus.ACTIVE);
        when(patientRepository.findByIdAndTenantId(patient.getId(), tenantId))
                .thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(sampleResponse(patient));

        final PatientResponse result = service.deactivate(patient.getId(), IP, UA);

        assertThat(patient.getStatus()).isEqualTo(PatientStatus.INACTIVE);
        assertThat(result).isNotNull();
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("PATIENT"),
                eq(patient.getId().toString()),
                eq(AuditAction.UPDATE),
                any(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    @Test
    @DisplayName("deactivate rejects non-ACTIVE patient")
    void deactivate_inactive_throwsBusiness() {
        final Patient patient = samplePatient(PatientStatus.INACTIVE);
        when(patientRepository.findByIdAndTenantId(patient.getId(), tenantId))
                .thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> service.deactivate(patient.getId(), IP, UA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot deactivate");
    }

    @Test
    @DisplayName("reactivate transitions INACTIVE to ACTIVE")
    void reactivate_success() {
        final Patient patient = samplePatient(PatientStatus.INACTIVE);
        when(patientRepository.findByIdAndTenantId(patient.getId(), tenantId))
                .thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(sampleResponse(patient));

        service.reactivate(patient.getId(), IP, UA);

        assertThat(patient.getStatus()).isEqualTo(PatientStatus.ACTIVE);
    }

    @Test
    @DisplayName("reactivate rejects ACTIVE patient")
    void reactivate_active_throwsBusiness() {
        final Patient patient = samplePatient(PatientStatus.ACTIVE);
        when(patientRepository.findByIdAndTenantId(patient.getId(), tenantId))
                .thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> service.reactivate(patient.getId(), IP, UA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot reactivate");
    }

    private static RegisterPatientRequest registerRequest(final String mrn) {
        return new RegisterPatientRequest(
                mrn,
                "Sara",
                "Ahmed",
                LocalDate.of(1990, 5, 12),
                Gender.FEMALE,
                BloodGroup.O_POSITIVE,
                "42101-1234567-1",
                "+923001234567",
                "sara@example.com",
                "Karachi",
                new EmergencyContactRequest("Ali Ahmed", "+923009998877", "Spouse"),
                MaritalStatus.MARRIED
        );
    }

    private static UpdatePatientRequest updateRequest(final String mrn) {
        return new UpdatePatientRequest(
                mrn,
                "Sara",
                "Ahmed",
                LocalDate.of(1990, 5, 12),
                Gender.FEMALE,
                BloodGroup.O_POSITIVE,
                "42101-1234567-1",
                "+923001234567",
                "sara@example.com",
                "Karachi",
                new EmergencyContactRequest("Ali Ahmed", "+923009998877", "Spouse"),
                MaritalStatus.MARRIED
        );
    }

    @Test
    @DisplayName("search uses repository Specification pagination (no in-memory filter)")
    @SuppressWarnings("unchecked")
    void search_usesSpecifications() {
        final Patient patient = samplePatient(PatientStatus.ACTIVE);
        final Pageable pageable = PageRequest.of(0, 20);
        final Page<Patient> page = new PageImpl<>(List.of(patient), pageable, 1);

        when(patientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(patientMapper.toResponse(patient)).thenReturn(sampleResponse(patient));

        final PatientSearchCriteria criteria = new PatientSearchCriteria(
                "sara",
                null,
                null,
                null,
                null,
                null,
                null,
                PatientStatus.ACTIVE,
                null,
                Gender.FEMALE,
                null,
                null,
                null,
                20,
                40,
                null,
                null
        );

        final PageResponse<PatientResponse> result = service.search(criteria, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        verify(patientRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("search rejects inverted age range")
    void search_invalidAgeRange() {
        final PatientSearchCriteria criteria = new PatientSearchCriteria(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                50, 20, null, null
        );

        assertThatThrownBy(() -> service.search(criteria, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ageMin");
        verify(patientRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    private Patient samplePatient(final PatientStatus status) {
        final Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setTenantId(tenantId);
        patient.setMrn("MRN-0001");
        patient.setFirstName("Sara");
        patient.setLastName("Ahmed");
        patient.setDateOfBirth(LocalDate.of(1990, 5, 12));
        patient.setGender(Gender.FEMALE);
        patient.setBloodGroup(BloodGroup.O_POSITIVE);
        patient.setStatus(status);
        return patient;
    }

    private static PatientResponse sampleResponse(final Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getTenantId(),
                patient.getMrn(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getBloodGroup(),
                patient.getNationalId(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAddress(),
                new EmergencyContactResponse(null, null, null),
                patient.getMaritalStatus(),
                patient.getStatus(),
                patient.getPrimaryDepartmentId(),
                patient.getPrimaryDoctorId(),
                Instant.now(),
                Instant.now(),
                null,
                null,
                0L
        );
    }
}
