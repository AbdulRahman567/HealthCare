package com.healthcare.hms.patients.immunization.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.immunization.dto.request.UpsertImmunizationRequest;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationDueResponse;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationResponse;
import com.healthcare.hms.patients.immunization.entity.Immunization;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.enums.VaccineRoute;
import com.healthcare.hms.patients.immunization.mapper.ImmunizationMapper;
import com.healthcare.hms.patients.immunization.repository.ImmunizationRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImmunizationServiceImpl")
class ImmunizationServiceImplTest {

    private static final String IP = "127.0.0.1";
    private static final String UA = "junit";

    @Mock
    private ImmunizationRepository immunizationRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private ImmunizationMapper immunizationMapper;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ImmunizationServiceImpl service;

    private UUID tenantId;
    private UUID userId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        TenantContextHolder.set(new TenantContext(
                tenantId,
                "city-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        final AuthenticatedUser principal = new AuthenticatedUser(
                userId,
                tenantId,
                "nurse@city.test",
                Set.of("NURSE"),
                Set.of("PATIENT_READ", "PATIENT_UPDATE", "PATIENT_DELETE"),
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
    @DisplayName("create persists immunization, applies mapper, and audits")
    void create_success() {
        final UpsertImmunizationRequest request = sampleRequest();
        final Immunization saved = sampleEntity();

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(immunizationRepository.save(any(Immunization.class))).thenAnswer(invocation -> {
            final Immunization immunization = invocation.getArgument(0);
            immunization.setId(saved.getId());
            immunization.setTenantId(tenantId);
            return immunization;
        });
        when(immunizationMapper.toResponse(any(Immunization.class))).thenReturn(sampleResponse(saved));

        final ImmunizationResponse result = service.create(patientId, request, IP, UA);

        assertThat(result.vaccineName()).isEqualTo("Hepatitis B");
        assertThat(result.doseNumber()).isEqualTo(1);
        verify(immunizationMapper).apply(eq(request), any(Immunization.class));
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("IMMUNIZATION"),
                eq(saved.getId().toString()),
                eq(AuditAction.CREATE),
                isNull(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    @Test
    @DisplayName("create fails when patient missing")
    void create_patientNotFound() {
        when(patientRepository.findByIdAndTenantId(patientId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(patientId, sampleRequest(), IP, UA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient");
    }

    @Test
    @DisplayName("getDue returns administered records with nextDueDate on or before today")
    void getDue_success() {
        final Immunization due = sampleEntity();
        due.setNextDueDate(LocalDate.now().minusDays(1));

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(immunizationRepository
                .findByTenantIdAndPatientIdAndStatusAndNextDueDateLessThanEqualOrderByNextDueDateAscVaccineNameAsc(
                        eq(tenantId),
                        eq(patientId),
                        eq(ImmunizationStatus.ADMINISTERED),
                        any(LocalDate.class)
                ))
                .thenReturn(List.of(due));
        when(immunizationMapper.toResponse(due)).thenReturn(sampleResponse(due));

        final ImmunizationDueResponse result = service.getDue(patientId);

        assertThat(result.dueCount()).isEqualTo(1);
        assertThat(result.dueImmunizations()).hasSize(1);
        assertThat(result.patientId()).isEqualTo(patientId);
    }

    @Test
    @DisplayName("getById fails when immunization missing for patient")
    void getById_notFound() {
        final UUID immunizationId = UUID.randomUUID();
        when(immunizationRepository.findByIdAndTenantIdAndPatientId(immunizationId, tenantId, patientId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(patientId, immunizationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Immunization");
    }

    @Test
    @DisplayName("softDelete marks deleted and audits")
    void softDelete_success() {
        final Immunization existing = sampleEntity();
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(immunizationRepository.findByIdAndTenantIdAndPatientId(existing.getId(), tenantId, patientId))
                .thenReturn(Optional.of(existing));
        when(immunizationRepository.save(existing)).thenReturn(existing);

        service.softDelete(patientId, existing.getId(), IP, UA);

        assertThat(existing.isDeleted()).isTrue();
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("IMMUNIZATION"),
                eq(existing.getId().toString()),
                eq(AuditAction.DELETE),
                any(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    private UpsertImmunizationRequest sampleRequest() {
        return new UpsertImmunizationRequest(
                "Hepatitis B",
                "CVX-08",
                1,
                "GSK",
                "LOT-ABC-123",
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 7, 15),
                "Dr. Smith",
                VaccineRoute.INTRAMUSCULAR,
                ImmunizationStatus.ADMINISTERED,
                null
        );
    }

    private Immunization sampleEntity() {
        final Immunization immunization = new Immunization();
        immunization.setId(UUID.randomUUID());
        immunization.setTenantId(tenantId);
        immunization.setPatientId(patientId);
        immunization.setVaccineName("Hepatitis B");
        immunization.setVaccineCode("CVX-08");
        immunization.setDoseNumber(1);
        immunization.setManufacturer("GSK");
        immunization.setBatchNumber("LOT-ABC-123");
        immunization.setAdministrationDate(LocalDate.of(2026, 1, 15));
        immunization.setNextDueDate(LocalDate.of(2026, 7, 15));
        immunization.setHealthcareProvider("Dr. Smith");
        immunization.setRoute(VaccineRoute.INTRAMUSCULAR);
        immunization.setStatus(ImmunizationStatus.ADMINISTERED);
        immunization.setRecordedByUserId(userId);
        return immunization;
    }

    private ImmunizationResponse sampleResponse(final Immunization immunization) {
        return new ImmunizationResponse(
                immunization.getId(),
                immunization.getPatientId(),
                immunization.getVaccineName(),
                immunization.getVaccineCode(),
                immunization.getDoseNumber(),
                immunization.getManufacturer(),
                immunization.getBatchNumber(),
                immunization.getAdministrationDate(),
                immunization.getNextDueDate(),
                immunization.getHealthcareProvider(),
                immunization.getRoute(),
                immunization.getStatus(),
                immunization.getClinicalNotes(),
                immunization.isDueOnOrBefore(LocalDate.now()),
                immunization.getRecordedByUserId(),
                Instant.now(),
                Instant.now(),
                0L
        );
    }
}
